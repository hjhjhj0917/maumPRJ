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

        Flux<String> rFlux = webClient.post()
                .uri("/api/rag-chat")
                .bodyValue(pDTO)
                .retrieve()
                .bodyToFlux(String.class)
                .doOnComplete(() -> log.info("{}.streamChat Data Stream Completed!", this.getClass().getName()))
                .onErrorReturn("잠시 연결이 고르지 않아요. 조금 이따가 다시 말해줄래요?");

        log.info("{}.streamChat End!", this.getClass().getName());

        return rFlux;
    }
}