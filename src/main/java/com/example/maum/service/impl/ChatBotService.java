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

    @PostConstruct
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
            String userMsgJson = objectMapper.writeValueAsString(
                    ChatMessageDTO.builder().role("user").content(pDTO.message()).build());
            redisService.pushMessage(redisKey, userMsgJson);
        } catch (Exception e) {
            log.error("사용자 메시지 JSON 변환 에러: ", e);
        }

        StringBuilder botResponse = new StringBuilder();

        return webClient.post()
                .uri("/api/rag-chat")
                .header("Accept", "text/plain") // 파이썬에서 순수 텍스트를 받기로 함
                .bodyValue(pDTO)
                .retrieve()
                .bodyToFlux(String.class)
                .doOnNext(data -> {
                    log.info("Python Raw Data: {}", data);
                    botResponse.append(data); // 데이터 축적
                })
                .doOnComplete(() -> {
                    log.info("{}.streamChat Data Stream Completed!", this.getClass().getName());
                    // 챗봇 응답 저장
                    try {
                        // <br>과 <sp> 태그를 변환하여 저장
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
                .onErrorResume(e -> {
                    log.error("Python Communication Error: ", e);
                    return Flux.just("연결 중에 문제가 발생했어요. 잠시 후 다시 시도해주세요.");
                });
    }

    @Override
    public List<Object> getHistory(String userNo) throws Exception {
        log.info("{}.getHistory Start!", this.getClass().getName());
        List<Object> rawHistory = redisService.getList("chat:" + userNo);
        List<Object> history = new ArrayList<>();

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