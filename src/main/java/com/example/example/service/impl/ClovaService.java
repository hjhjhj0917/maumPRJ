package com.example.example.service.impl;

import com.example.example.dto.ClovaDTO;
import com.example.example.service.IClovaServiece;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.*;
import org.springframework.http.HttpEntity;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import java.util.List;
import java.util.Map;

@Service
@Slf4j
@RequiredArgsConstructor
public class ClovaService implements IClovaServiece {

    @Value("${secure.clova.studio.url}")
    private String clovaUrl;

    @Value("${secure.clova.studio.api-key}")
    private String apiKey;

    // RestTemplate은 Bean으로 등록하여 주입받는 것이 좋으나, 빠른 테스트를 위해 필드 생성 방식을 유지합니다.
    private final RestTemplate restTemplate = new RestTemplate();

    @Override
    public Map<String, Object> analyzeWithThinking(String diaryContent) {

        log.info("{}.analyzeWithThinking Start!", this.getClass().getName());

        // 1. 헤더 설정 (Request ID 제외)
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        headers.set("X-NCP-CLOVASTUDIO-API-KEY", apiKey);

        // 2. 메시지 구성 (HCX-007 Thinking 모델 최적화)
        List<ClovaDTO.Message> messages = List.of(
                new ClovaDTO.Message("system",
                        "너는 심리 분석 전문가야. 사용자의 일기를 깊이 있게 추론(Thinking)한 뒤, " +
                                "최종 결과는 반드시 다음 JSON 형식으로만 응답해: " +
                                "{\"emotion_summary\": \"...\", \"stress_level\": 0~100, \"advice\": \"...\"}"),
                new ClovaDTO.Message("user", diaryContent)
        );

        ClovaDTO requestBody = new ClovaDTO();
        requestBody.setMessages(messages);
        requestBody.setIncludeThinking(true); // HCX-007의 추론 과정 포함

        HttpEntity<ClovaDTO> entity = new HttpEntity<>(requestBody, headers);

        try {
            log.info("{}.api 호출 시작 Start!", this.getClass().getName());

            // 3. API 호출
            ResponseEntity<Map> response = restTemplate.postForEntity(clovaUrl, entity, Map.class);

            // 4. 결과 추출 로직
            Map<String, Object> body = response.getBody();
            log.info("{}.api 응답 수신 성공 Start!", this.getClass().getName());

            if (body != null && "0000".equals(String.valueOf(body.get("status")))) { // 가이드상 성공 코드가 0000인 경우 확인
                Map<String, Object> result = (Map<String, Object>) body.get("result");
                Map<String, Object> message = (Map<String, Object>) result.get("message");

                String content = (String) message.get("content");
                Object thinking = result.get("thinking");

                log.info("분석 내용(content): {}", content);
                log.debug("추론 과정(thinking): {}", thinking);

                log.info("분석 완료 및 성공 반환");
                return Map.of(
                        "status", "success",
                        "analysis", content,
                        "thinking", thinking != null ? thinking : "No thinking data"
                );
            }
            log.warn("API 응답 코드 미일치(실패): status={}", body != null ? body.get("status") : "null");
            return Map.of("status", "error", "message", "API 응답이 실패했습니다. body를 확인하세요.");

        } catch (Exception e) {
            log.error("API 호출 중 예외 발생!", e);
            return Map.of(
                    "status", "error",
                    "message", "분석 중 오류 발생: " + e.getMessage()
            );
        } finally {
            log.info("{}.analyzeWithThinking End!", this.getClass().getName());
        }
    }
}
