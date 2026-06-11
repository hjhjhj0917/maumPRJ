package com.example.maum.service.impl;

import com.example.maum.dto.ChatBotDTO;
import com.example.maum.dto.ChatMessageDTO;
import com.example.maum.service.IChatBotService;
import com.example.maum.service.IRedisService;
import jakarta.annotation.PostConstruct;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Flux;
import tools.jackson.databind.ObjectMapper;

import java.util.ArrayList;
import java.util.List;

@Service
@Slf4j
@RequiredArgsConstructor
public class ChatBotService implements IChatBotService {


    private WebClient webClient;

    private final IRedisService redisService;

    private final ObjectMapper objectMapper;

    @Value("${secure.python.api.url}")
    private String pythonApiUrl;

        @PostConstruct /* 통신 설정 안에 같이 다 들어와야 실행한다 */
        public void init() {
            this.webClient = WebClient.builder()
                    .baseUrl(pythonApiUrl)
                    .build();
        }

    @Override
    public Flux<String> streamChat(ChatBotDTO pDTO) {

        log.info("{}.streamChat Start!", this.getClass().getName());

        String userNo = pDTO.userNo();
        String redisKey = "chat:" + userNo;

        // 사용자 메시지 저장
        try {
            String userMsgJson = objectMapper.writeValueAsString( /* JSON 직렬화를 위한 타입변경 */
                    ChatMessageDTO.builder().role("user").content(pDTO.message()).build());
            redisService.pushMessage(redisKey, userMsgJson);
        } catch (Exception e) {
            log.error("사용자 메시지 JSON 변환 에러: ", e);
        }

        StringBuilder botResponse = new StringBuilder();

        return webClient.post()
                .uri("/api/rag-chat")
                .header("Accept", "text/plain") /* 파이썬에서 순수 텍스트를 받기로 함 */
                .bodyValue(pDTO)
                .retrieve() /* 응답 상태 준비 */
                .bodyToFlux(String.class) /* 응답을 여러 조각으로 받음 */
                .doOnNext(data -> { /* 실시간 데이터 처리 */
                    log.info("Python Raw Data: {}", data);
                    botResponse.append(data); // 데이터 축적
                })
                .doOnComplete(() -> { /* 스트림 종료후 저장 */
                    log.info("{}.streamChat Data Stream Completed!", this.getClass().getName());

                    try {
                        /* <br>과 <sp> 태그를 변환하여 저장 */
                        String cleanBotResponse = botResponse.toString()
                                .replace("<br>", "  \n")
                                .replace("<sp>", " ");

                        String botMsgJson = objectMapper.writeValueAsString(
                                ChatMessageDTO.builder().role("bot").content(cleanBotResponse).build());
                        redisService.pushMessage(redisKey, botMsgJson);
                    } catch (Exception e) {
                        log.error("챗봇 응답 JSON 변환 에러: ", e);
                    }
                })
                .onErrorResume(e -> { /* 예외처리 회로 차단 */
                    log.error("Python Communication Error: ", e);
                    return Flux.just("연결 중에 문제가 발생했어요. 잠시 후 다시 시도해주세요.");
                });
    }

    @Override
    public List<ChatMessageDTO> getHistory(String userNo) throws Exception {
        log.info("{}.getHistory Start!", this.getClass().getName());

        List<Object> rawHistory = redisService.getList("chat:" + userNo);

        List<ChatMessageDTO> history = new ArrayList<>();

        for (Object item : rawHistory) {
            try {
                history.add(objectMapper.readValue(item.toString(), ChatMessageDTO.class));
            } catch (Exception e) {
                log.error("채팅 내역 JSON 파싱 에러: ", e);
            }
        }

        return history;
    }
}