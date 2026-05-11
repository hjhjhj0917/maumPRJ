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

    /* [Diary Analyze] */

    private void requestAnalysisAndUpdate(DiaryEntity entity, String content) {
        try {
            Map<String, Object> requestMap = new HashMap<>();

            requestMap.put("diary_no", entity.getDiaryNo());
            requestMap.put("user_no", entity.getUserNo());
            requestMap.put("title", entity.getTitle());
            requestMap.put("content", content);
            requestMap.put("disease_type", "depression");

            ResponseEntity<Map> response = restClient.post()
                    .uri(pythonApiUrl)
                    .body(requestMap)
                    .retrieve()
                    .toEntity(Map.class);

            if (response.getStatusCode().is2xxSuccessful() && response.getBody() != null) {
                Map<String, Object> responseBody = response.getBody();
                log.info("파이썬 분석 결과 수신 성공.");

                try {
                    String summary = (String) responseBody.get("analysis_summary");
                    String mainEmotion = (String) responseBody.get("main_emotion");
                    String emotionColor = (String) responseBody.get("main_color");

                    Map<String, Object> depRes = (Map<String, Object>) responseBody.get("dep_res");
                    Integer depLvl = Integer.parseInt(String.valueOf(depRes.get("final_level")));
                    BigDecimal depScore = new BigDecimal(String.valueOf(depRes.get("raw_score")));
                    Object isSymptomObj = depRes.get("is_symptom");
                    Integer symptomYn = (isSymptomObj instanceof Boolean && (Boolean) isSymptomObj) ? 1 : 0;

                    entity.updateAnalysisResult(summary, mainEmotion, emotionColor, depLvl, depScore, symptomYn);

                    diaryRepository.save(entity);
                    log.info("분석 결과 DB 반영 완료 (Color: {})", emotionColor);

                } catch (Exception parseEx) {
                    log.error("분석 결과 파싱 실패: {}", parseEx.getMessage());
                }
            } else {
                log.error("파이썬 분석 요청 실패. Status: {}", response.getStatusCode());
            }
        } catch (Exception e) {
            log.error("파이썬 서버 통신 에러: {}", e.getMessage());
        }
    }


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

            pEntity = diaryRepository.save(pEntity);
            res = 1;

            requestAnalysisAndUpdate(pEntity, pDTO.content());

        } catch (Exception e) {
            res = 2;
            log.error("Diary Insert Error : {}", e.getMessage());
        }

        log.info("{}.diaryInsert End!", this.getClass().getName());

        return res;
    }


    @Transactional
    @Override
    public MsgDTO diaryUpdate(DiaryDTO pDTO) throws Exception {

        log.info("{}.diaryUpdate Start!", this.getClass().getName());

        int res = 0;
        String msg = "일기 수정에 실패하였습니다.";

        Integer userNo = pDTO.userNo();
        Integer diaryNo = pDTO.diaryNo();
        String title = CmmUtil.nvl(pDTO.title());
        String content = CmmUtil.nvl(pDTO.content());

        Optional<DiaryEntity> rEntity = diaryRepository.findById(diaryNo);

        if (rEntity.isPresent()) {
            DiaryEntity entity = rEntity.get();

            if (entity.getUserNo().equals(userNo)) {

                entity.updateDiary(title, content);
                diaryRepository.save(entity);

                requestAnalysisAndUpdate(entity, content);

                res = 1;
                msg = "일기가 성공적으로 수정 및 재분석되었습니다.";
            } else {
                msg = "본인의 일기만 수정할 수 있습니다.";
            }
        } else {
            msg = "존재하지 않는 일기입니다.";
        }

        MsgDTO rDTO = MsgDTO.builder()
                .result(res)
                .msg(msg)
                .build();

        log.info("{}.diaryUpdate End!", this.getClass().getName());

        return rDTO;
    }


    @Transactional
    @Override
    public MsgDTO diaryDelete(DiaryDTO pDTO) throws Exception {

        log.info("{}.diaryDelete Start!", this.getClass().getName());

        int res = 0;
        String msg = "일기 삭제에 실패하였습니다.";

        Integer userNo = pDTO.userNo();
        Integer diaryNo = pDTO.diaryNo();

        log.info("삭제 시도 - diaryNo: {}, userNo: {}", diaryNo, userNo);

        Optional<DiaryEntity> rEntity = diaryRepository.findById(diaryNo);

        if (rEntity.isPresent()) {
            DiaryEntity entity = rEntity.get();

            if (entity.getUserNo().equals(userNo)) {

                diaryRepository.delete(entity);

                res = 1;
                msg = "일기가 성공적으로 삭제되었습니다.";
                log.info("일기 삭제 성공 - diaryNo: {}", diaryNo);

            } else {
                msg = "본인의 일기만 삭제할 수 있습니다.";
                log.warn("권한 없는 삭제 시도 감지 - 요청자: {}, 실제작성자: {}", userNo, entity.getUserNo());
            }
        } else {
            msg = "이미 삭제되었거나 존재하지 않는 일기입니다.";
        }

        MsgDTO rDTO = MsgDTO.builder()
                .result(res)
                .msg(msg)
                .build();

        log.info("{}.diaryDelete End!", this.getClass().getName());

        return rDTO;
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


    @Transactional(readOnly = true)
    @Override
    public List<DiaryDTO> searchDiaryList(DiaryDTO pDTO) throws Exception {

        log.info("{}.searchDiaryList Start!", this.getClass().getName());

        List<DiaryEntity> entities = diaryRepository.findByUserNoAndTitleContainingOrderByCreatedAtDesc(
                pDTO.userNo(), pDTO.title());

        List<DiaryDTO> rList = entities.stream()
                .map(e -> DiaryDTO.builder()
                        .diaryNo(e.getDiaryNo())
                        .title(e.getTitle())
                        .emotionColor(e.getEmotionColor())
                        .createdAt(DateUtil.formatLocalDate(e.getCreatedAt(), "yyyy-MM-dd"))
                        .build())
                .collect(Collectors.toList());

        log.info("{}.searchDiaryList End!", this.getClass().getName());

        return rList;
    }

    @Transactional(readOnly = true)
    @Override
    public List<DiaryDTO> getDiaryListByColors(Integer userNo, List<String> colors) throws Exception {

        log.info("{}.getDiaryListByColors Start!", this.getClass().getName());

        List<DiaryDTO> rList = diaryRepository.findByUserNoAndEmotionColorInOrderByCreatedAtDesc(userNo, colors).stream()
                .map(e -> DiaryDTO.builder()
                        .diaryNo(e.getDiaryNo())
                        .title(e.getTitle())
                        .emotionColor(e.getEmotionColor())
                        .createdAt(DateUtil.formatLocalDate(e.getCreatedAt(), "yyyy-MM-dd"))
                        .build())
                .collect(Collectors.toList());

        log.info("{}.getDiaryListByColors End!", this.getClass().getName());

        return rList;
    }
}