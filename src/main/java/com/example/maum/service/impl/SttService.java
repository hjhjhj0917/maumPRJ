package com.example.maum.service.impl;

import com.example.maum.dto.SttResultDTO;
import com.example.maum.service.ISttService;
import jakarta.annotation.PostConstruct;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.ByteArrayResource;
import org.springframework.http.MediaType;
import org.springframework.http.client.reactive.ReactorClientHttpConnector;
import org.springframework.stereotype.Service;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.reactive.function.BodyInserters;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.netty.http.client.HttpClient;

import java.time.Duration;

@Service
@Slf4j
// ★ 즐겨찾기 이후 추가/수정
public class SttService implements ISttService {

    // 마이크를 켜고 말 없이 바로 닫는 등 Python(Google STT)쪽 응답이 지연/누락되는 경우,
    // 타임아웃이 하나도 없으면 .block()이 끝없이 대기해 프론트가 무한 로딩에 빠지는 문제가 있었음
    private static final Duration RESPONSE_TIMEOUT = Duration.ofSeconds(20);

    private WebClient webClient;

    @Value("${secure.python.api.url}")
    private String pythonApiUrl;

    @PostConstruct
    public void init() {
        HttpClient httpClient = HttpClient.create()
                .responseTimeout(RESPONSE_TIMEOUT);

        this.webClient = WebClient.builder()
                .baseUrl(pythonApiUrl)
                .clientConnector(new ReactorClientHttpConnector(httpClient))
                .build();
    }

    @Override
    public String transcribe(MultipartFile audio) throws Exception {
        log.info("{}.transcribe Start!", this.getClass().getName());

        MultiValueMap<String, Object> body = new LinkedMultiValueMap<>();
        body.add("audio", new ByteArrayResource(audio.getBytes()) {
            @Override
            public String getFilename() {
                return audio.getOriginalFilename() != null ? audio.getOriginalFilename() : "audio.webm";
            }
        });

        SttResultDTO rDTO;
        try {
            rDTO = webClient.post()
                    .uri("/api/stt")
                    .contentType(MediaType.MULTIPART_FORM_DATA)
                    .body(BodyInserters.fromMultipartData(body))
                    .retrieve()
                    .bodyToMono(SttResultDTO.class)
                    // HttpClient의 responseTimeout과 별개로, block() 자체도 타임아웃을 줘서
                    // 응답 자체가 아예 안 오는 경우(연결은 됐지만 스트림이 멈추는 경우 등)까지 방어함
                    .block(RESPONSE_TIMEOUT);
        } catch (Exception e) {
            log.error("STT 서버 통신 실패/타임아웃: {}", e.getMessage());
            rDTO = null;
        }

        log.info("{}.transcribe End!", this.getClass().getName());

        return rDTO != null ? rDTO.text() : "";
    }
}
