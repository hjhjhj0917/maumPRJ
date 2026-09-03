package com.example.maum.service.impl;

import com.example.maum.dto.DiaryDTO;
import com.example.maum.dto.EmotionStatDTO;
import com.example.maum.dto.MsgDTO;
import com.example.maum.repository.DiaryLogRepository;
import com.example.maum.repository.DiaryRepository;
import com.example.maum.repository.entity.DiaryEntity;
import com.example.maum.repository.entity.DiaryLogDocument;
import com.example.maum.service.IDiaryService;
import com.example.maum.util.CmmUtil;
import com.example.maum.util.DateUtil;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.data.mongodb.core.query.Query;
import org.springframework.http.ResponseEntity;
import org.springframework.http.client.SimpleClientHttpRequestFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.client.RestClient;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.YearMonth;
import java.util.*;

@Service
@Slf4j
@RequiredArgsConstructor
public class DiaryService implements IDiaryService {

    private final DiaryRepository diaryRepository;
    private final DiaryLogRepository diaryLogRepository;
    private final MongoTemplate mongoTemplate;

    private final RestClient restClient = createRestClientWithTimeout();

    /*
    RestClient 타임아웃 설정
    */
    private RestClient createRestClientWithTimeout() {
        SimpleClientHttpRequestFactory factory = new SimpleClientHttpRequestFactory();
        factory.setConnectTimeout(10000);
        factory.setReadTimeout(120000);

        return RestClient.builder()
                .requestFactory(factory)
                .build();
    }

    /*
    감정에 따른 색상 코드 반환
    */
    private String getEmotionColor(String emotion) {
        List<String> yellow = Arrays.asList("즐거움/신남", "행복", "기쁨", "뿌듯함", "흐뭇함(귀여움/예쁨)", "감동/감탄", "고마움", "환영/호의");
        List<String> mint = Arrays.asList("안심/신뢰", "존경", "아껴주는", "편안/쾌적");
        List<String> purple = Arrays.asList("공포/무서움", "불안/걱정", "부담/안_내킴", "의심/불신");
        List<String> blue = Arrays.asList("놀람", "신기함/관심", "어이없음", "경악", "당황/난처");
        List<String> darkBlue = Arrays.asList("슬픔", "절망", "서러움", "불쌍함/연민", "안타까움/실망", "패배/자기혐오", "힘듦/지침");
        List<String> olive = Arrays.asList("역겨움/징그러움", "증오/혐오", "지긋지긋", "한심함");
        List<String> red = Arrays.asList("화남/분노", "짜증", "불평/불만");
        List<String> orange = Arrays.asList("기대감", "비장함", "깨달음");

        if (yellow.contains(emotion)) return "#FFF0A8";
        if (mint.contains(emotion)) return "#A8E6CF";
        if (purple.contains(emotion)) return "#DDBDF1";
        if (blue.contains(emotion)) return "#A2D2FF";
        if (darkBlue.contains(emotion)) return "#8EA4D2";
        if (olive.contains(emotion)) return "#C5D8A4";
        if (red.contains(emotion)) return "#FFB3B3";
        if (orange.contains(emotion)) return "#FFDFBA";
        return "#D9D9D9";
    }

    /*
    파이썬 AI 서버로 감정 분석 요청
    */
    private void requestAnalysisAndUpdate(DiaryEntity entity, String newTitle, String newContent) {

        try {
            Map<String, Object> requestMap = new HashMap<>();

            requestMap.put("diary_no", entity.getDiaryNo());
            requestMap.put("user_no", entity.getUserNo());
            requestMap.put("title", newTitle);
            requestMap.put("created_at", entity.getCreatedAt());
            requestMap.put("content", newContent);
            requestMap.put("disease_type", "depression");

            ResponseEntity<Map> response = restClient.post()
                    .uri(pythonApiUrl + "/api/analyze")
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

                    diaryRepository.updateAnalysisResultDirectly(
                            Long.valueOf(entity.getDiaryNo()),
                            summary,
                            mainEmotion,
                            emotionColor,
                            depLvl,
                            depScore,
                            symptomYn
                    );

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

    @Value("${secure.python.api.url}")
    private String pythonApiUrl;

    /*
    일기 등록
    */
    @Transactional
    @CacheEvict(value = "diaryCache", allEntries = true)
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

            res = pEntity.getDiaryNo();

            requestAnalysisAndUpdate(pEntity, pEntity.getTitle(), pEntity.getContent());

        } catch (Exception e) {
            res = 0;
            log.error("Diary Insert Error : {}", e.getMessage());
        }

        log.info("{}.diaryInsert End!", this.getClass().getName());

        return res;
    }

    /*
    일기 수정
    */
    @Transactional
    @CacheEvict(value = "diaryCache", allEntries = true)
    @Override
    public MsgDTO diaryUpdate(DiaryDTO pDTO) throws Exception {

        log.info("{}.diaryUpdate Start!", this.getClass().getName());

        int res = 0;
        String msg = "일기 수정에 실패하였습니다.";

        String userNo = CmmUtil.nvl(pDTO.userNo());
        Integer diaryNo = pDTO.diaryNo();
        String title = CmmUtil.nvl(pDTO.title());
        String content = CmmUtil.nvl(pDTO.content());

        Optional<DiaryEntity> rEntity = diaryRepository.findById(diaryNo);

        if (rEntity.isPresent()) {
            DiaryEntity entity = rEntity.get();

            if (entity.getUserNo().equals(userNo)) {
                diaryRepository.updateDiaryDirectly(Long.valueOf(diaryNo), title, content);

                requestAnalysisAndUpdate(entity, title, content);

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

    /*
    일기 삭제
    */
    @Transactional
    @CacheEvict(value = "diaryCache", allEntries = true)
    @Override
    public MsgDTO diaryDelete(DiaryDTO pDTO) throws Exception {

        log.info("{}.diaryDelete Start!", this.getClass().getName());

        int res = 0;
        String msg = "일기 삭제에 실패하였습니다.";

        String userNo = CmmUtil.nvl(pDTO.userNo());
        Integer diaryNo = pDTO.diaryNo();

        log.info("삭제 시도 - diaryNo: {}, userNo: {}", diaryNo, userNo);

        Optional<DiaryEntity> rEntity = diaryRepository.findById(diaryNo);

        if (rEntity.isPresent()) {
            DiaryEntity entity = rEntity.get();

            if (entity.getUserNo().equals(userNo)) {

                diaryRepository.delete(entity);

                try {
                    Query query = new Query(Criteria.where("DIARY_NO").is(diaryNo));
                    mongoTemplate.remove(query, "DIARY_LOGS");
                } catch (Exception e) {
                    log.error(e.getMessage());
                }

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

    /*
    월별 일기 목록 조회
    */
    @Transactional(readOnly = true)
    @Override
    public List<DiaryDTO> getMonthlyDiaryList(DiaryDTO pDTO) throws Exception {

        log.info("{}.getMonthlyDiaryList Start!", this.getClass().getName());

        String userNo = pDTO.userNo();
        String dateStr = CmmUtil.nvl(pDTO.createdAt());

        log.info("Request Monthly Diary List - userNo: {}, createdAt: {}", userNo, dateStr);

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

        log.info("Query Date Range: {} ~ {}", start, end);

        List<DiaryEntity> entities = diaryRepository.findAllByUserNoAndCreatedAtBetween(userNo, start, end);

        log.info("Found {} diary entities.", entities.size());

        List<DiaryDTO> rList = new ArrayList<>();

        for (DiaryEntity e : entities) {
            DiaryDTO dto = DiaryDTO.builder()
                    .diaryNo(e.getDiaryNo())
                    .userNo(e.getUserNo())
                    .title(e.getTitle())
                    .emotionColor(e.getEmotionColor())
                    .isFavorite(e.getIsFavorite())
                    .createdAt(DateUtil.formatLocalDate(e.getCreatedAt(), "yyyy-MM-dd"))
                    .build();
            rList.add(dto);
        }

        log.info("{}.getMonthlyDiaryList End!", this.getClass().getName());

        return rList;
    }

    /*
    일기 상세 보기
    */
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
                    .isFavorite(rEntity.getIsFavorite())
                    .createdAt(DateUtil.formatLocalDate(rEntity.getCreatedAt(), "yyyy-MM-dd"))
                    .build();
        } else {
            throw new Exception("해당 일기를 찾을 수 없습니다.");
        }

        log.info("{}.getDiaryDetail End!", this.getClass().getName());

        return rDTO;
    }

    /*
    일기 제목 검색
    */
    @Transactional(readOnly = true)
    @Override
    public List<DiaryDTO> searchDiaryList(DiaryDTO pDTO) throws Exception {

        log.info("{}.searchDiaryList Start!", this.getClass().getName());

        List<DiaryEntity> entities = diaryRepository.findByUserNoAndTitleContainingOrderByCreatedAtDesc(
                pDTO.userNo(), pDTO.title());

        List<DiaryDTO> rList = new ArrayList<>();

        for (DiaryEntity e : entities) {
            DiaryDTO dto = DiaryDTO.builder()
                    .diaryNo(e.getDiaryNo())
                    .title(e.getTitle())
                    .emotionColor(e.getEmotionColor())
                    .isFavorite(e.getIsFavorite())
                    .createdAt(DateUtil.formatLocalDate(e.getCreatedAt(), "yyyy-MM-dd"))
                    .build();
            rList.add(dto);
        }

        log.info("{}.searchDiaryList End!", this.getClass().getName());

        return rList;
    }

    /*
    감정 필터 검색
    */
    @Transactional(readOnly = true)
    @Override
    public List<DiaryDTO> getDiaryListByColors(String userNo, List<String> colors) throws Exception {

        log.info("{}.getDiaryListByColors Start!", this.getClass().getName());

        List<DiaryEntity> entities = diaryRepository.findByUserNoAndEmotionColorInOrderByCreatedAtDesc(userNo, colors);

        List<DiaryDTO> rList = new ArrayList<>();

        for (DiaryEntity e : entities) {
            DiaryDTO dto = DiaryDTO.builder()
                    .diaryNo(e.getDiaryNo())
                    .title(e.getTitle())
                    .emotionColor(e.getEmotionColor())
                    .isFavorite(e.getIsFavorite())
                    .createdAt(DateUtil.formatLocalDate(e.getCreatedAt(), "yyyy-MM-dd"))
                    .build();
            rList.add(dto);
        }

        log.info("{}.getDiaryListByColors End!", this.getClass().getName());

        return rList;
    }

    /*
    최근 일기 목록 조회
    */
    @Cacheable(value = "diaryCache", key = "#pDTO.userNo()", condition = "#pDTO.userNo() != null")
    @Override
    public List<DiaryDTO> getRecentDiaryList(DiaryDTO pDTO) throws Exception {

        log.info("{}.getRecentDiaryList Start!", this.getClass().getName());

        String userNo = CmmUtil.nvl(pDTO.userNo());

        log.info("userNo: {}", userNo);

        List<DiaryEntity> entities = diaryRepository.findTop20ByUserNoOrderByCreatedAtDesc(userNo);

        List<DiaryDTO> rList = new ArrayList<>();

        for (DiaryEntity e : entities) {
            DiaryDTO dto = DiaryDTO.builder()
                    .diaryNo(e.getDiaryNo())
                    .title(e.getTitle())
                    .emotionColor(e.getEmotionColor())
                    .createdAt(DateUtil.formatLocalDate(e.getCreatedAt(), "yyyy-MM-dd"))
                    .build();
            rList.add(dto);
        }

        log.info("{}.getRecentDiaryList End!", this.getClass().getName());

        return rList;
    }

    /*
    즐겨찾기 일기 목록 조회
    */
    @Transactional(readOnly = true)
    @Override
    public List<DiaryDTO> getFavoriteDiaryList(DiaryDTO pDTO) throws Exception {

        log.info("{}.getFavoriteDiaryList Start!", this.getClass().getName());

        String userNo = CmmUtil.nvl(pDTO.userNo());

        List<DiaryEntity> entities = Optional.ofNullable(
                diaryRepository.findByUserNoAndIsFavoriteOrderByCreatedAtDesc(userNo, 1)
        ).orElseGet(ArrayList::new);

        log.info("Found {} favorite diary entities.", entities.size());

        List<DiaryDTO> rList = new ArrayList<>();

        for (DiaryEntity e : entities) {
            DiaryDTO dto = DiaryDTO.builder()
                    .diaryNo(e.getDiaryNo())
                    .title(e.getTitle())
                    .emotionColor(e.getEmotionColor())
                    .isFavorite(e.getIsFavorite())
                    .createdAt(DateUtil.formatLocalDate(e.getCreatedAt(), "yyyy-MM-dd"))
                    .build();
            rList.add(dto);
        }

        log.info("{}.getFavoriteDiaryList End!", this.getClass().getName());

        return rList;
    }

    /*
    마이페이지 감정 통계 조회
    */
    @Override
    public List<EmotionStatDTO> getEmotionStats(String userNoStr) throws Exception {

        log.info("{}.getEmotionStats Start!", this.getClass().getName());

        Integer userNo = Integer.parseInt(userNoStr);
        List<DiaryLogDocument> logs = diaryLogRepository.findByUserNo(userNo);
        Map<String, Integer> countMap = new HashMap<>();

        for (DiaryLogDocument logDoc : logs) {
            if (logDoc.getEmoRes() != null) {
                for (Map.Entry<String, Double> entry : logDoc.getEmoRes().entrySet()) {
                    if (entry.getValue() != null && entry.getValue() >= 0.6) {
                        countMap.put(entry.getKey(), countMap.getOrDefault(entry.getKey(), 0) + 1);
                    }
                }
            }
        }

        List<Map.Entry<String, Integer>> entryList = new ArrayList<>(countMap.entrySet());

        entryList.sort((e1, e2) -> e2.getValue().compareTo(e1.getValue()));

        List<EmotionStatDTO> rList = new ArrayList<>();

        for (Map.Entry<String, Integer> entry : entryList) {
            EmotionStatDTO dto = new EmotionStatDTO(
                    entry.getKey(),
                    entry.getValue(),
                    getEmotionColor(entry.getKey())
            );
            rList.add(dto);
        }

        log.info("{}.getEmotionStats End!", this.getClass().getName());

        return rList;
    }

    /*
    즐겨찾기
    */
    @Transactional
//    @CacheEvict(value = "diaryCache", allEntries = true) 마이페이지 리스트에도 캐시를 적용할 건가 확인이 필요
    @Override
    public int updateFavorite(DiaryDTO pDTO) throws Exception {

        log.info("{}.updateFavorite Start!", this.getClass().getName());

        String userNo = CmmUtil.nvl(pDTO.userNo());
        Integer diaryNo = pDTO.diaryNo();
        Integer isFavorite = pDTO.isFavorite();

        log.info("userNo: {}, diaryNo: {}, isFavorite: {}", userNo, diaryNo, isFavorite);

        int res = diaryRepository.updateFavorite(diaryNo, userNo, isFavorite);

        log.info("{}.updateFavorite End!", this.getClass().getName());

        return res;
    }
}