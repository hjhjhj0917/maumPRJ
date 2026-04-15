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
                        String summary = (String) responseBody.get("analysis_summary");
                        String mainEmotion = (String) responseBody.get("main_emotion");

                        Map<String, Object> depRes = (Map<String, Object>) responseBody.get("dep_res");
                        Integer depLvl = (Integer) depRes.get("DEP_LVL");

                        BigDecimal depScore = new BigDecimal(String.valueOf(depRes.get("DEP_SCORE")));

                        Boolean isSymptom = (Boolean) depRes.get("IS_SYMPTOM");
                        Integer symptomYn = (isSymptom != null && isSymptom) ? 1 : 0;

                        pEntity.updateAnalysisResult(summary, mainEmotion, depLvl, depScore, symptomYn);

                        diaryRepository.save(pEntity);
                        log.info("Analysis Result Successfully Saved to DB.");

                    } catch (Exception parseEx) {
                        log.error("Failed to parse and save analysis result to DB: {}", parseEx.getMessage());
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