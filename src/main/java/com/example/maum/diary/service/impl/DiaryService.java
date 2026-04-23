package com.example.maum.diary.service.impl;

import com.example.maum.diary.dto.DiaryDTO;
import com.example.maum.diary.repository.DiaryRepository;
import com.example.maum.diary.repository.entity.DiaryEntity;
import com.example.maum.diary.service.IDiaryService;
import com.example.maum.global.util.CmmUtil;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.client.RestTemplate;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.YearMonth;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

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

    @Transactional(readOnly = true)
    @Cacheable(value = "diaryCache", key = "#pDTO.userNo() + '_' + #pDTO.createdAt()")
    @Override
    public List<DiaryDTO> getMonthlyDiaryList(DiaryDTO pDTO) throws Exception {

        log.info("{}.getMonthlyDiaryList Start!", this.getClass().getName());

        // 1. 날짜 파싱 안전하게 처리 (CmmUtil 활용)
        String dateStr = CmmUtil.nvl(pDTO.createdAt()); // "2026-04"

        if (dateStr.isEmpty() || !dateStr.contains("-")) {
            log.warn("조회 날짜가 비어있거나 형식이 잘못되었습니다.");
            return new ArrayList<>();
        }

        // 2. 해당 월의 시작일(1일)과 종료일(말일) 계산
        YearMonth yearMonth = YearMonth.of(
                Integer.parseInt(dateStr.split("-")[0]),
                Integer.parseInt(dateStr.split("-")[1])
        );
        LocalDate start = yearMonth.atDay(1);
        LocalDate end = yearMonth.atEndOfMonth();

        // 3. DB 조회 (JPA Between 사용)
        List<DiaryEntity> entities = diaryRepository.findAllByUserNoAndCreatedAtBetween(
                pDTO.userNo(), start, end);

        // 4. Entity -> DTO 변환 (Stream API 활용)
        List<DiaryDTO> rList = entities.stream()
                .map(e -> DiaryDTO.builder()
                        .diaryNo(e.getDiaryNo())
                        .userNo(e.getUserNo())
                        .title(e.getTitle())
                        .emotionColor(e.getEmotionColor())
                        .createdAt(e.getCreatedAt().toString()) // "2026-04-23"
                        .build())
                .collect(Collectors.toList());

        log.info("{}.getMonthlyDiaryList End!", this.getClass().getName());

        return rList;
    }
}