package com.example.maum.service.impl;

import com.example.maum.dto.SttResultDTO;
import com.example.maum.service.ISttService;
import jakarta.annotation.PostConstruct;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.ByteArrayResource;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Service;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.reactive.function.BodyInserters;
import org.springframework.web.reactive.function.client.WebClient;

@Service
@Slf4j
// ★ 즐겨찾기 이후 추가/수정
public class SttService implements ISttService {

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
    public String transcribe(MultipartFile audio) throws Exception {
        log.info("{}.transcribe Start!", this.getClass().getName());

        MultiValueMap<String, Object> body = new LinkedMultiValueMap<>();
        body.add("audio", new ByteArrayResource(audio.getBytes()) {
            @Override
            public String getFilename() {
                return audio.getOriginalFilename() != null ? audio.getOriginalFilename() : "audio.webm";
            }
        });

        SttResultDTO rDTO = webClient.post()
                .uri("/api/stt")
                .contentType(MediaType.MULTIPART_FORM_DATA)
                .body(BodyInserters.fromMultipartData(body))
                .retrieve()
                .bodyToMono(SttResultDTO.class)
                .block();

        log.info("{}.transcribe End!", this.getClass().getName());

        return rDTO != null ? rDTO.text() : "";
    }
}
