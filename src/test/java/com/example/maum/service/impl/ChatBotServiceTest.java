package com.example.maum.service.impl;

import com.example.maum.chat.ChatMessageStatus;
import com.example.maum.dto.ChatBotDTO;
import com.example.maum.repository.ChatMessageRepository;
import com.example.maum.repository.ChatSessionRepository;
import com.example.maum.repository.UserRepository;
import com.example.maum.service.IChatWriteService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.http.HttpStatus;
import org.springframework.test.util.ReflectionTestUtils;
import org.springframework.web.reactive.function.client.ClientResponse;
import org.springframework.web.reactive.function.client.ExchangeFunction;
import org.springframework.web.reactive.function.client.ExchangeStrategies;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Mono;
import reactor.core.publisher.SignalType;

import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyBoolean;
import static org.mockito.ArgumentMatchers.anyList;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.ArgumentMatchers.isNull;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

// DB·FastAPI 없이 스트림 처리(텍스트/카드/오디오 표시 분리)와 끝난 뒤 답변 상태 결정을 확인함.
// FastAPI 는 가짜 ExchangeFunction 이 줄 단위 응답을 흘려보내 대신함(실제 응답도 줄 단위로 옴)
class ChatBotServiceTest {

    private static final long BOT_ID = 77L;

    IChatWriteService chatWriteService = mock(IChatWriteService.class);
    ChatBotService service;

    @BeforeEach
    void setUp() {
        service = new ChatBotService(chatWriteService, mock(ChatSessionRepository.class),
                mock(ChatMessageRepository.class), mock(UserRepository.class));
    }

    private ChatBotDTO request() {
        return ChatBotDTO.builder().userNo("u1").sessionPublicId("s1").message("안녕").build();
    }

    private void fakeAiServer(ExchangeFunction exchange) {
        ReflectionTestUtils.setField(service, "webClient", WebClient.builder().exchangeFunction(exchange).build());
    }

    private ExchangeFunction respondWith(String body) {
        return req -> Mono.just(ClientResponse.create(HttpStatus.OK, ExchangeStrategies.withDefaults())
                .header("Content-Type", "text/plain")
                .body(body)
                .build());
    }

    @Test
    void 텍스트와_카드를_나눠_저장하고_텍스트_완료_표시가_있으면_완료로_기록한다() {
        when(chatWriteService.beginTurn(anyString(), anyString(), anyString(), any()))
                .thenReturn(new IChatWriteService.Turn(BOT_ID, List.of(), null));
        fakeAiServer(respondWith("""
                안녕
                하세요<br>
                [[CARD]][{"type":"hospital","name":"마음센터"}][[/CARD]]
                [[TEXT_DONE]]
                """));

        List<String> received = service.streamChat(request()).collectList().block();

        // 화면에는 모든 조각이 그대로 전달됨
        assertEquals(4, received.size());
        verify(chatWriteService).finishTurn(eq(BOT_ID), eq(ChatMessageStatus.COMPLETE), eq("안녕하세요  \n"), eq(true),
                eq(List.of(Map.of("type", "hospital", "name", "마음센터"))));
    }

    @Test
    void AI_서버_오류면_안내_문구를_보내고_실패로_기록한다() {
        when(chatWriteService.beginTurn(anyString(), anyString(), anyString(), any()))
                .thenReturn(new IChatWriteService.Turn(BOT_ID, List.of(), null));
        fakeAiServer(req -> Mono.error(new IllegalStateException("down")));

        List<String> received = service.streamChat(request()).collectList().block();

        assertEquals(List.of("연결 중에 문제가 발생했어요. 잠시 후 다시 시도해주세요."), received);
        verify(chatWriteService).finishTurn(eq(BOT_ID), eq(ChatMessageStatus.FAILED), eq(""), eq(false), isNull());
    }

    @Test
    void 시작할_수_없으면_사유를_안내하고_답변_행은_만들지_않는다() {
        when(chatWriteService.beginTurn(anyString(), anyString(), anyString(), any()))
                .thenThrow(new IllegalStateException("앞선 답변을 만들고 있어요. 잠시 후 다시 보내주세요."));

        List<String> received = service.streamChat(request()).collectList().block();

        assertEquals(List.of("앞선 답변을 만들고 있어요. 잠시 후 다시 보내주세요."), received);
        verify(chatWriteService, never()).finishTurn(any(), any(), anyString(), anyBoolean(), anyList());
    }

    @Test
    void 같은_요청_재전송이면_저장된_답변을_다시_보낸다() {
        when(chatWriteService.beginTurn(anyString(), anyString(), anyString(), any()))
                .thenReturn(new IChatWriteService.Turn(BOT_ID, List.of(), "저장된 답변"));

        assertEquals(List.of("저장된 답변"), service.streamChat(request()).collectList().block());
        verify(chatWriteService, never()).finishTurn(any(), any(), anyString(), anyBoolean(), anyList());
    }

    @Test
    void 끝난_뒤_상태는_신호와_수신_내용으로_정한다() {
        assertEquals(ChatMessageStatus.COMPLETE, ChatBotService.resolveStatus(SignalType.ON_COMPLETE, false, false, "답"));
        // 클라이언트가 응답을 다 받고 먼저 끊은 경우
        assertEquals(ChatMessageStatus.COMPLETE, ChatBotService.resolveStatus(SignalType.CANCEL, false, true, "답"));
        // 중간에 끊긴 경우: 받은 만큼은 남김
        assertEquals(ChatMessageStatus.INTERRUPTED, ChatBotService.resolveStatus(SignalType.CANCEL, false, false, "답"));
        assertEquals(ChatMessageStatus.FAILED, ChatBotService.resolveStatus(SignalType.CANCEL, false, false, " "));
        assertEquals(ChatMessageStatus.FAILED, ChatBotService.resolveStatus(SignalType.ON_COMPLETE, true, true, "답"));
    }
}
