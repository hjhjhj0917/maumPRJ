package com.example.example.analysis.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.*;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.client.HttpClientErrorException;

import java.util.HashMap;
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

    private final RestTemplate restTemplate = new RestTemplate();

    @Override
    public Map<String, Object> analyzeWithThinking(String diaryContent) {

        log.info("{}.analyzeWithThinking Start!", this.getClass().getName());

        // 1. 헤더 설정 (v3 규격 맞춤)
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        // v3는 기본적으로 스트림 응답을 선호하므로 Accept를 명시적으로 설정합니다.
        headers.set("Accept", "application/json");
        headers.set("Authorization", "Bearer " + apiKey);

        // 2. 메시지 구성 (v3 HCX-007 규격)
        // role과 content 구조를 명확히 합니다.
        List<Map<String, String>> messages = List.of(
                Map.of("role", "system", "content",
                        "너는 심리 분석 전문가야. 사용자의 일기를 깊이 있게 추론(Thinking)한 뒤, " +
                                "최종 결과는 반드시 다음 JSON 형식으로만 응답해: " +
                                "{\"emotion_summary\": \"...\", \"stress_level\": 0~100, \"advice\": \"...\"}"),
                Map.of("role", "user", "content", diaryContent)
        );

        // 3. 요청 바디 구성 (Thinking 객체 포함)
        Map<String, Object> requestBody = new HashMap<>();
        requestBody.put("messages", messages);

        // HCX-007 전용: includeThinking 대신 객체 형태의 thinking 설정을 사용합니다.
        requestBody.put("thinking", Map.of("effort", "low"));

        // 추가 파라미터 (가이드 권장값)
        requestBody.put("maxCompletionTokens", 5120);
        requestBody.put("temperature", 0.5);
        requestBody.put("topP", 0.8);

        HttpEntity<Map<String, Object>> entity = new HttpEntity<>(requestBody, headers);

        try {
            log.info("{}.api 호출 시작 Start!", this.getClass().getName());

            // API 호출
            ResponseEntity<Map> response = restTemplate.postForEntity(clovaUrl, entity, Map.class);
            Map<String, Object> body = response.getBody();
            log.info("{}.api 응답 수신 성공 Start!", this.getClass().getName());

            if (body != null && body.get("result") != null) {
                Map<String, Object> result = (Map<String, Object>) body.get("result");
                Map<String, Object> message = (Map<String, Object>) result.get("message");

                if (message != null) {
                    String content = (String) message.get("content");
                    // 로그를 보니 키 이름이 'thinkingContent'입니다.
                    Object thinking = result.get("thinkingContent");

                    log.info("분석 내용(content): {}", content);
                    log.debug("추론 과정(thinking): {}", thinking);

                    log.info("분석 완료 및 성공 반환");
                    return Map.of(
                            "status", "success",
                            "analysis", content,
                            "thinking", thinking != null ? thinking : "No thinking data"
                    );
                }
            }

            log.warn("API 응답 성공했으나 데이터 구조가 예상과 다름: body={}", body);
            return Map.of("status", "error", "message", "응답 구조 분석 실패: " + body);

        } catch (HttpClientErrorException.Unauthorized e) {
            log.error("인증 오류 401 키 값 또는 권한을 확인하세요.");
            log.error("네이버 응답 메시지: {}", e.getResponseBodyAsString());
            return Map.of("status", "error", "message", "인증 오류가 발생했습니다.");

        } catch (HttpClientErrorException e) {
            log.error("HTTP 에러 상태코드: {}, 상세내용: {} ####", e.getStatusCode(), e.getResponseBodyAsString());
            return Map.of("status", "error", "message", "API 통신 오류: " + e.getResponseBodyAsString());

        } catch (Exception e) {
            log.error("API 호출 중 예외 발생!", e);
            return Map.of("status", "error", "message", "분석 중 오류 발생: " + e.getMessage());
        } finally {
            log.info("{}.analyzeWithThinking End!", this.getClass().getName());
        }
    }
}