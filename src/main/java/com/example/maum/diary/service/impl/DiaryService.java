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
import org.springframework.web.client.RestClient; // RestClient 임포트

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

    // RestTemplate 대신 최신 권장 방식인 RestClient 생성
    private final RestClient restClient = RestClient.create();

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
            String createdAt = CmmUtil.nvl(pDTO.createdAt()).trim();
            DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd");
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

                // RestClient를 활용한 체이닝(Fluent) 방식의 API 호출
                ResponseEntity<Map> response = restClient.post()
                        .uri(pythonApiUrl)
                        .body(requestMap)
                        .retrieve()
                        .toEntity(Map.class);

                if (response.getStatusCode().is2xxSuccessful() && response.getBody() != null) {
                    Map<String, Object> responseBody = response.getBody();
                    log.info("Analysis Result Received Successfully.");

                    try {
                        // 1. 파이썬에서 보낸 데이터 추출
                        String summary = (String) responseBody.get("analysis_summary");
                        String mainEmotion = (String) responseBody.get("main_emotion");
                        String emotionColor = (String) responseBody.get("main_color");

                        // 2. dep_res 내부 데이터 추출
                        Map<String, Object> depRes = (Map<String, Object>) responseBody.get("dep_res");

                        // Integer 변환을 더 안전하게 처리하도록 보완
                        Integer depLvl = Integer.parseInt(String.valueOf(depRes.get("final_level")));
                        BigDecimal depScore = new BigDecimal(String.valueOf(depRes.get("raw_score")));

                        Object isSymptomObj = depRes.get("is_symptom");
                        Integer symptomYn = (isSymptomObj instanceof Boolean && (Boolean) isSymptomObj) ? 1 : 0;

                        // 3. 엔티티 업데이트
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

    /*
    월별 일기 목록 조회
    */
    @Transactional(readOnly = true)
    @Cacheable(value = "diaryCache", key = "#pDTO.userNo() + '_' + #pDTO.createdAt()")
    @Override
    public List<DiaryDTO> getMonthlyDiaryList(DiaryDTO pDTO) throws Exception {

        log.info("{}.getMonthlyDiaryList Start!", this.getClass().getName());

        String dateStr = CmmUtil.nvl(pDTO.createdAt());

        if (dateStr.isEmpty() || !dateStr.contains("-")) {
            log.warn("조회 날짜가 비어있거나 형식이 잘못되었습니다.");
            return new ArrayList<>();
        }

        YearMonth yearMonth = YearMonth.of(
                Integer.parseInt(dateStr.split("-")[0]),
                Integer.parseInt(dateStr.split("-")[1])
        );
        LocalDate start = yearMonth.atDay(1);
        LocalDate end = yearMonth.atEndOfMonth();

        List<DiaryEntity> entities = diaryRepository.findAllByUserNoAndCreatedAtBetween(
                pDTO.userNo(), start, end);

        List<DiaryDTO> rList = entities.stream()
                .map(e -> DiaryDTO.builder()
                        .diaryNo(e.getDiaryNo())
                        .userNo(e.getUserNo())
                        .title(e.getTitle())
                        .emotionColor(e.getEmotionColor())
                        .createdAt(e.getCreatedAt().toString())
                        .build())
                .collect(Collectors.toList());

        log.info("{}.getMonthlyDiaryList End!", this.getClass().getName());

        return rList;
    }

    /*
    일기 상세 조회
    */
    @Transactional(readOnly = true)
    @Override
    public DiaryDTO getDiaryDetail(DiaryDTO pDTO) throws Exception {
        log.info("{}.getDiaryDetail Start!", this.getClass().getName());

        DiaryEntity rEntity = diaryRepository.findById(pDTO.diaryNo())
                .orElseThrow(() -> new Exception("해당 일기를 찾을 수 없습니다."));

        if (!rEntity.getUserNo().equals(pDTO.userNo())) {
            throw new Exception("해당 일기에 대한 접근 권한이 없습니다.");
        }

        DiaryDTO rDTO = DiaryDTO.builder()
                .diaryNo(rEntity.getDiaryNo())
                .userNo(rEntity.getUserNo())
                .title(rEntity.getTitle())
                .content(rEntity.getContent())
                .emotionColor(rEntity.getEmotionColor())
                .mainEmotion(rEntity.getMainEmotion())
                .summary(rEntity.getSummary())
                .depLvl(rEntity.getDepLvl())
                .depScore(rEntity.getDepScore())
                .symptomYn(rEntity.getSymptomYn())
                .createdAt(rEntity.getCreatedAt().toString())
                .build();

        log.info("{}.getDiaryDetail End!", this.getClass().getName());

        return rDTO;
    }
}