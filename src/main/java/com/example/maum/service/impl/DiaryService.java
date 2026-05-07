package com.example.maum.service.impl;

import com.example.maum.dto.DiaryDTO;
import com.example.maum.dto.MsgDTO;
import com.example.maum.repository.DiaryRepository;
import com.example.maum.repository.entity.DiaryEntity;
import com.example.maum.service.IDiaryService;
import com.example.maum.util.CmmUtil;
import com.example.maum.util.DateUtil;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.client.RestClient;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.YearMonth;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
@Slf4j
@RequiredArgsConstructor
public class DiaryService implements IDiaryService {

    private final DiaryRepository diaryRepository;
    private final RestClient restClient = RestClient.create();

    @Value("${secure.python.api.url}")
    private String pythonApiUrl;

    /* [Diary Management] */

    @Transactional
    @Override
    public int diaryInsert(DiaryDTO pDTO) throws Exception {

        log.info("{}.diaryInsert Start!", this.getClass().getName());

        int res = 0;

        try {
            String createdAt = CmmUtil.nvl(pDTO.createdAt()).trim();

            LocalDate parsedDate = DateUtil.parseLocalDate(createdAt, "yyyy-MM-dd");

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

                ResponseEntity<Map> response = restClient.post()
                        .uri(pythonApiUrl)
                        .body(requestMap)
                        .retrieve()
                        .toEntity(Map.class);

                if (response.getStatusCode().is2xxSuccessful() && response.getBody() != null) {
                    Map<String, Object> responseBody = response.getBody();
                    log.info("Analysis Result Received Successfully.");

                    try {
                        String summary = (String) responseBody.get("analysis_summary");
                        String mainEmotion = (String) responseBody.get("main_emotion");
                        String emotionColor = (String) responseBody.get("main_color");

                        Map<String, Object> depRes = (Map<String, Object>) responseBody.get("dep_res");

                        Integer depLvl = Integer.parseInt(String.valueOf(depRes.get("final_level")));
                        BigDecimal depScore = new BigDecimal(String.valueOf(depRes.get("raw_score")));

                        Object isSymptomObj = depRes.get("is_symptom");
                        Integer symptomYn = (isSymptomObj instanceof Boolean && (Boolean) isSymptomObj) ? 1 : 0;

                        pEntity.updateAnalysisResult(summary, mainEmotion, emotionColor, depLvl, depScore, symptomYn);

                        diaryRepository.save(pEntity);
                        log.info("Analysis Result (including Color: {}) Successfully Saved to DB.", emotionColor);

                    } catch (Exception parseEx) {
                        log.error("Failed to parse and save analysis result to DB: {}", parseEx.getMessage());
                        parseEx.printStackTrace();
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

        log.info("{}.diaryInsert End!", this.getClass().getName());

        return res;
    }


    @Override
    public MsgDTO diaryUpdate(DiaryDTO pDTO) throws Exception {

        log.info("{}.diaryUpdate Start!", this.getClass().getName());

        Integer userNo = pDTO.userNo();
        Integer diaryNo = pDTO.diaryNo();
        String title = pDTO.title();
        String content = pDTO.content();

        log.info("{}.diaryUpdate End!", this.getClass().getName());

        return null;
    }


    /* [Diary Retrieval] */

    @Transactional(readOnly = true)
    @Cacheable(value = "diaryCache", key = "#pDTO.userNo() + '_' + #pDTO.createdAt()") // 중복된 요청을 빠르게 처리하기 위함 (캐시를 redis에 저장하게 수정)
    @Override
    public List<DiaryDTO> getMonthlyDiaryList(DiaryDTO pDTO) throws Exception {

        log.info("{}.getMonthlyDiaryList Start!", this.getClass().getName());

        String dateStr = CmmUtil.nvl(pDTO.createdAt());

        if (dateStr.isEmpty() || !dateStr.contains("-")) {
            log.warn("조회 날짜가 비어있거나 형식이 잘못되었습니다.");
            return new ArrayList<>();
        }

        YearMonth yearMonth;
        try {
            yearMonth = YearMonth.parse(dateStr.length() > 7 ? dateStr.substring(0, 7) : dateStr);
        } catch (Exception e) {
            log.warn("YearMonth 파싱 실패: {}", dateStr);
            return new ArrayList<>();
        }

        LocalDate start = yearMonth.atDay(1);
        LocalDate end = yearMonth.atEndOfMonth();

        List<DiaryEntity> entities = diaryRepository.findAllByUserNoAndCreatedAtBetween(pDTO.userNo(), start, end);

        List<DiaryDTO> rList = entities.stream()
                .map(e -> DiaryDTO.builder()
                        .diaryNo(e.getDiaryNo())
                        .userNo(e.getUserNo())
                        .title(e.getTitle())
                        .emotionColor(e.getEmotionColor())
                        .createdAt(DateUtil.formatLocalDate(e.getCreatedAt(), "yyyy-MM-dd"))
                        .build())
                .collect(Collectors.toList());

        log.info("{}.getMonthlyDiaryList End!", this.getClass().getName());

        return rList;
    }


    @Transactional(readOnly = true)
    @Override
    public DiaryDTO getDiaryDetail(DiaryDTO pDTO) throws Exception {
        log.info("{}.getDiaryDetail Start!", this.getClass().getName());

        Optional<DiaryEntity> oEntity = diaryRepository.findById(pDTO.diaryNo());

        DiaryDTO rDTO;

        if (oEntity.isPresent()) {
            DiaryEntity rEntity = oEntity.get();

            if (!rEntity.getUserNo().equals(pDTO.userNo())) {
                throw new Exception("해당 일기에 대한 접근 권한이 없습니다.");
            }

            rDTO = DiaryDTO.builder()
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
                    .createdAt(DateUtil.formatLocalDate(rEntity.getCreatedAt(), "yyyy-MM-dd"))
                    .build();
        } else {
            throw new Exception("해당 일기를 찾을 수 없습니다.");
        }

        log.info("{}.getDiaryDetail End!", this.getClass().getName());

        return rDTO;
    }
}