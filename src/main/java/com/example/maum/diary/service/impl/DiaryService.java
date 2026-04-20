package com.example.maum.diary.service.impl;

import com.example.maum.diary.dto.DiaryDTO;
import com.example.maum.diary.repository.DiaryRepository;
import com.example.maum.diary.repository.entity.DiaryEntity;
import com.example.maum.diary.service.IDiaryService;
import com.example.maum.global.util.CmmUtil;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.client.RestTemplate;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.HashMap;
import java.util.Map;

@Service
@Slf4j
@RequiredArgsConstructor
public class DiaryService implements IDiaryService {

    private final DiaryRepository diaryRepository;
    private final RestTemplate restTemplate = new RestTemplate();

    @Value("${secure.python.api.url}")
    private String pythonApiUrl;

    /*
    일기 작성
    */
    @Transactional
    @Override
    public int diaryInsert(DiaryDTO pDTO) throws Exception {

        log.info("{}.diaryInsert Start!", this.getClass().getName());

        int res = 0;

        try {
            String createdAt = CmmUtil.nvl(pDTO.createdAt());
            DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy년 MM월 dd일");
            LocalDate parsedDate = LocalDate.parse(createdAt, formatter);

            DiaryEntity pEntity = DiaryEntity.builder()
                    .userNo(pDTO.userNo())
                    .title(CmmUtil.nvl(pDTO.title()))
                    .content(CmmUtil.nvl(pDTO.content()))
                    .createdAt(parsedDate)
                    .build();

            diaryRepository.save(pEntity);

            res = 1;

            try {
                Map<String, String> requestMap = new HashMap<>();
                requestMap.put("content", pDTO.content());
                requestMap.put("disease_type", "depression");

                ResponseEntity<Map> response = restTemplate.postForEntity(pythonApiUrl, requestMap, Map.class);

                if (response.getStatusCode().is2xxSuccessful() && response.getBody() != null) {
                    Map<String, Object> responseBody = response.getBody();
                    log.info("Analysis Result Received Successfully.");

                    try {
                        // 1. 파이썬에서 보낸 데이터 추출
                        String summary = (String) responseBody.get("analysis_summary");
                        String mainEmotion = (String) responseBody.get("main_emotion");

                        // 파이썬 코드에서 'main_color'로 보냈다면 아래와 같이 받습니다.
                        // 만약 파이썬에서 'emotion_color'로 키를 바꿨다면 해당 키값을 입력하세요.
                        String emotionColor = (String) responseBody.get("main_color");

                        // 2. dep_res 내부 데이터 추출
                        Map<String, Object> depRes = (Map<String, Object>) responseBody.get("dep_res");
                        Integer depLvl = (Integer) depRes.get("final_level"); // 파이썬 결과 키에 맞춰 수정

                        BigDecimal depScore = new BigDecimal(String.valueOf(depRes.get("raw_score")));

                        Boolean isSymptom = (Boolean) depRes.get("is_symptom");
                        Integer symptomYn = (isSymptom != null && isSymptom) ? 1 : 0;

                        // 3. 엔티티 업데이트 (수정된 메서드 호출)
                        pEntity.updateAnalysisResult(summary, mainEmotion, emotionColor, depLvl, depScore, symptomYn);

                        diaryRepository.save(pEntity);
                        log.info("Analysis Result (including Color: {}) Successfully Saved to DB.", emotionColor);

                    } catch (Exception parseEx) {
                        log.error("Failed to parse and save analysis result to DB: {}", parseEx.getMessage());
                        parseEx.printStackTrace(); // 상세 에러 확인용
                    }

                } else {
                    log.error("Analysis Request Failed. Status Code: {}", response.getStatusCode());
                }

            } catch (Exception e) {
                log.error("Python Server Communication Error: {}", e.getMessage());
            }

        } catch (Exception e) {
            res = 2;
            log.error("Diary Insert Error : {}", e.getMessage());
        }

        return res;
    }
}