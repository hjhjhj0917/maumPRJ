package com.example.maum.service.impl;

import com.example.maum.dto.ChatBotDTO;
import com.example.maum.service.IChatBotService;
import jakarta.annotation.PostConstruct;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Flux;

@Service
@Slf4j
@RequiredArgsConstructor
public class ChatBotService implements IChatBotService {


    private WebClient webClient;

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

        return webClient.post()
                .uri("/api/rag-chat")
                .header("Accept", "text/plain") // 파이썬에서 순수 텍스트를 받기로 함
                .bodyValue(pDTO)
                .retrieve()
                .bodyToFlux(String.class)
                .doOnNext(data -> log.info("Python Raw Data: {}", data)) // 실시간 데이터 확인 로그
                .doOnComplete(() -> log.info("{}.streamChat Data Stream Completed!", this.getClass().getName()))
                .onErrorResume(e -> {
                    log.error("Python Communication Error: ", e);
                    return Flux.just("연결 중에 문제가 발생했어요. 잠시 후 다시 시도해주세요.");
                });
    }
}