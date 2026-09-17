package com.example.maum.service.impl;

import com.example.maum.dto.DepressionTrendDTO;
import com.example.maum.dto.DiaryDTO;
import com.example.maum.dto.DiaryImageDTO;
import com.example.maum.dto.DiaryMusicDTO;
import com.example.maum.dto.DiaryStatsDTO;
import com.example.maum.dto.EmotionStatDTO;
import com.example.maum.dto.MsgDTO;
import com.example.maum.dto.ReportCardDTO;
import com.example.maum.dto.TopMusicDTO;
import com.example.maum.repository.DiaryImageRepository;
import com.example.maum.repository.DiaryLogRepository;
import com.example.maum.repository.DiaryMusicRepository;
import com.example.maum.repository.DiaryRepository;
import com.example.maum.repository.entity.DiaryEntity;
import com.example.maum.repository.entity.DiaryImageEntity;
import com.example.maum.repository.entity.DiaryLogDocument;
import com.example.maum.repository.entity.DiaryMusicEntity;
import com.example.maum.repository.projection.DepressionTrendProjection;
import com.example.maum.repository.projection.TopMusicProjection;
import com.example.maum.service.IDiaryService;
import com.example.maum.service.IGcsService;
import com.example.maum.util.CmmUtil;
import com.example.maum.util.DateUtil;
import com.example.maum.util.EmotionColorMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.data.mongodb.core.query.Query;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.client.RestClient;
import org.springframework.web.multipart.MultipartFile;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;
import java.time.YearMonth;
import java.util.*;
import java.util.stream.Collectors;

@Service
@Slf4j
@RequiredArgsConstructor
public class DiaryService implements IDiaryService {

    private static final int MAX_DIARY_IMAGE_COUNT = 3;

    private final DiaryRepository diaryRepository;
    private final DiaryLogRepository diaryLogRepository;
    private final DiaryImageRepository diaryImageRepository;
    private final DiaryMusicRepository diaryMusicRepository;
    private final IGcsService gcsService;
    private final MongoTemplate mongoTemplate;

    private final RestClient pythonApiRestClient;

    /*
    파이썬 AI 서버로 감정 분석 요청 - 응답에 포함된 감정 기반 음악 추천 결과(tracks)도 함께 저장함
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

            ResponseEntity<Map> response = pythonApiRestClient.post()
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

                    saveMusicTracks(entity.getDiaryNo(), (List<Map<String, Object>>) responseBody.get("tracks"));

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

    /*
    감정분석 응답에 포함된 감정 기반 음악 추천 결과를 DIARY_MUSIC에 저장함.
    재분석(수정)일 경우 기존 추천곡은 지우고 새로 저장함 (일기 내용이 바뀌면 추천도 바뀌어야 하므로)
    */
    private void saveMusicTracks(Integer diaryNo, List<Map<String, Object>> tracks) {

        diaryMusicRepository.deleteByDiaryNo(diaryNo);

        if (tracks == null || tracks.isEmpty()) {
            log.warn("음악 추천 결과 없음 (diaryNo: {})", diaryNo);
            return;
        }

        int order = 0;
        for (Map<String, Object> track : tracks) {
            DiaryMusicEntity musicEntity = DiaryMusicEntity.builder()
                    .diaryNo(diaryNo)
                    .trackId((String) track.get("trackId"))
                    .trackName((String) track.get("trackName"))
                    .artistName((String) track.get("artistName"))
                    .albumImageUrl((String) track.get("albumImageUrl"))
                    .spotifyUrl((String) track.get("spotifyUrl"))
                    .trackOrder(order++)
                    .build();

            diaryMusicRepository.save(musicEntity);
        }

        log.info("음악 추천 저장 완료 (diaryNo: {}, {}곡)", diaryNo, tracks.size());
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
    일기 임시저장 - AI 분석/음악 추천 호출 없이 제목/내용만 저장함.
    diaryNo가 없으면 새로 생성하고, 있으면 그 자리에 덮어씀 (자동저장이 반복 호출되므로)
    */
    @Transactional
    @Override
    public int draftSave(DiaryDTO pDTO) throws Exception {

        log.info("{}.draftSave Start!", this.getClass().getName());

        int res = 0;

        try {
            String title = CmmUtil.nvl(pDTO.title());
            String content = CmmUtil.nvl(pDTO.content());

            if (pDTO.diaryNo() != null) {
                Optional<DiaryEntity> rEntity = diaryRepository.findById(pDTO.diaryNo());

                if (rEntity.isPresent() && rEntity.get().getUserNo().equals(pDTO.userNo())) {
                    diaryRepository.updateDiaryDirectly(Long.valueOf(pDTO.diaryNo()), title, content);
                    res = pDTO.diaryNo();
                }
            } else {
                String createdAt = CmmUtil.nvl(pDTO.createdAt()).trim();
                LocalDate parsedDate = DateUtil.parseLocalDate(createdAt, "yyyy-MM-dd");

                DiaryEntity pEntity = DiaryEntity.builder()
                        .userNo(pDTO.userNo())
                        .title(title)
                        .content(content)
                        .createdAt(parsedDate)
                        .build();

                pEntity = diaryRepository.save(pEntity);
                res = pEntity.getDiaryNo();
            }
        } catch (Exception e) {
            res = 0;
            log.error("Diary Draft Save Error : {}", e.getMessage());
        }

        log.info("{}.draftSave End!", this.getClass().getName());

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

                // DB 행은 FK cascade로 같이 지워지지만, GCS에 올라간 실제 파일은 직접 지워야 함
                List<DiaryImageEntity> images = diaryImageRepository.findByDiaryNoOrderByImageOrderAsc(diaryNo);
                for (DiaryImageEntity image : images) {
                    gcsService.deleteImage(image.getImageUrl());
                }

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

            List<DiaryImageDTO> images = diaryImageRepository.findByDiaryNoOrderByImageOrderAsc(rEntity.getDiaryNo())
                    .stream()
                    .map(img -> DiaryImageDTO.builder()
                            .imageNo(img.getImageNo())
                            .diaryNo(img.getDiaryNo())
                            .imageUrl(img.getImageUrl())
                            .imageOrder(img.getImageOrder())
                            .build())
                    .collect(Collectors.toList());

            List<DiaryMusicDTO> musics = diaryMusicRepository.findByDiaryNoOrderByTrackOrderAsc(rEntity.getDiaryNo())
                    .stream()
                    .map(m -> DiaryMusicDTO.builder()
                            .musicNo(m.getMusicNo())
                            .diaryNo(m.getDiaryNo())
                            .trackId(m.getTrackId())
                            .trackName(m.getTrackName())
                            .artistName(m.getArtistName())
                            .albumImageUrl(m.getAlbumImageUrl())
                            .spotifyUrl(m.getSpotifyUrl())
                            .trackOrder(m.getTrackOrder())
                            .build())
                    .collect(Collectors.toList());

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
                    .images(images)
                    .musics(musics)
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

        List<DiaryEntity> entities = diaryRepository.findTop20ByUserNoOrderByIsPinnedDescCreatedAtDesc(userNo);

        List<DiaryDTO> rList = new ArrayList<>();

        for (DiaryEntity e : entities) {
            DiaryDTO dto = DiaryDTO.builder()
                    .diaryNo(e.getDiaryNo())
                    .title(e.getTitle())
                    .emotionColor(e.getEmotionColor())
                    .isPinned(e.getIsPinned())
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
    public List<EmotionStatDTO> getEmotionStats(DiaryDTO pDTO) throws Exception {

        log.info("{}.getEmotionStats Start!", this.getClass().getName());

        Integer userNo = Integer.parseInt(CmmUtil.nvl(pDTO.userNo()));
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
                    EmotionColorMapper.getColor(entry.getKey())
            );
            rList.add(dto);
        }

        log.info("{}.getEmotionStats End!", this.getClass().getName());

        return rList;
    }

    /*
    마이페이지 - 총 작성 수 / 연속 작성일 통계 조회
    */
    @Transactional(readOnly = true)
    @Override
    public DiaryStatsDTO getDiaryStats(DiaryDTO pDTO) throws Exception {

        log.info("{}.getDiaryStats Start!", this.getClass().getName());

        String sUserNo = CmmUtil.nvl(pDTO.userNo());

        long totalCount = diaryRepository.countByUserNo(sUserNo);

        List<LocalDate> dates = Optional.ofNullable(
                diaryRepository.findAllCreatedAtByUserNoOrderByCreatedAtDesc(sUserNo)
        ).orElseGet(ArrayList::new);

        int currentStreak = calculateCurrentStreak(dates);
        int longestStreak = calculateLongestStreak(dates);

        DiaryStatsDTO rDTO = DiaryStatsDTO.builder()
                .totalCount(totalCount)
                .currentStreak(currentStreak)
                .longestStreak(longestStreak)
                .build();

        log.info("{}.getDiaryStats End!", this.getClass().getName());

        return rDTO;
    }

    /*
    최신순으로 정렬된 날짜 목록에서 오늘 또는 어제부터 이어지는 연속 작성일 계산
    */
    private int calculateCurrentStreak(List<LocalDate> descDates) {

        if (descDates.isEmpty()) {
            return 0;
        }

        LocalDate today = LocalDate.now();
        LocalDate mostRecent = descDates.get(0);

        if (!mostRecent.equals(today) && !mostRecent.equals(today.minusDays(1))) {
            return 0;
        }

        int streak = 1;
        LocalDate cursor = mostRecent;

        for (int i = 1; i < descDates.size(); i++) {
            LocalDate expected = cursor.minusDays(1);

            if (descDates.get(i).equals(expected)) {
                streak++;
                cursor = expected;
            } else {
                break;
            }
        }

        return streak;
    }

    /*
    최신순으로 정렬된 날짜 목록 전체를 훑어 역대 최장 연속 작성일 계산
    */
    private int calculateLongestStreak(List<LocalDate> descDates) {

        if (descDates.isEmpty()) {
            return 0;
        }

        int longest = 1;
        int running = 1;

        for (int i = 1; i < descDates.size(); i++) {
            if (descDates.get(i).equals(descDates.get(i - 1).minusDays(1))) {
                running++;
            } else {
                running = 1;
            }
            longest = Math.max(longest, running);
        }

        return longest;
    }

    /*
    마이페이지 - 최근 6개월 월별 우울 지수 추이 조회
    */
    @Transactional(readOnly = true)
    @Override
    public List<DepressionTrendDTO> getDepressionTrend(DiaryDTO pDTO) throws Exception {

        log.info("{}.getDepressionTrend Start!", this.getClass().getName());

        String sUserNo = CmmUtil.nvl(pDTO.userNo());
        LocalDate fromDate = LocalDate.now().minusMonths(5).withDayOfMonth(1);

        List<DepressionTrendProjection> projections = Optional.ofNullable(
                diaryRepository.findDepressionTrendByUserNo(sUserNo, fromDate)
        ).orElseGet(ArrayList::new);

        List<DepressionTrendDTO> rList = projections.stream()
                .map(p -> DepressionTrendDTO.builder()
                        .month(p.getMonth())
                        .avgDepScore(p.getAvgDepScore())
                        .diaryCount(p.getDiaryCount())
                        .build())
                .collect(Collectors.toList());

        log.info("{}.getDepressionTrend End!", this.getClass().getName());

        return rList;
    }

    private static final int TOP_MUSIC_LIMIT = 5;

    /*
    마이페이지 - 가장 많이 추천된 음악 Top N 조회
    */
    @Transactional(readOnly = true)
    @Override
    public List<TopMusicDTO> getTopRecommendedMusic(DiaryDTO pDTO) throws Exception {

        log.info("{}.getTopRecommendedMusic Start!", this.getClass().getName());

        String sUserNo = CmmUtil.nvl(pDTO.userNo());

        List<TopMusicProjection> projections = Optional.ofNullable(
                diaryMusicRepository.findTopRecommendedMusicByUserNo(sUserNo, PageRequest.of(0, TOP_MUSIC_LIMIT))
        ).orElseGet(ArrayList::new);

        List<TopMusicDTO> rList = projections.stream()
                .map(p -> TopMusicDTO.builder()
                        .trackName(p.getTrackName())
                        .artistName(p.getArtistName())
                        .albumImageUrl(p.getAlbumImageUrl())
                        .spotifyUrl(p.getSpotifyUrl())
                        .recommendCount(p.getRecommendCount())
                        .build())
                .collect(Collectors.toList());

        log.info("{}.getTopRecommendedMusic End!", this.getClass().getName());

        return rList;
    }

    private static final int WEEKLY_REPORT_PERIOD_DAYS = 7;

    /*
    마이페이지 - 최근 일주일 일기를 바탕으로 한 주간 리포트 카드(Gemini 코멘트) 조회
    Gemini 호출 비용 때문에 하루 단위(weeklyReportCache TTL 24시간)로 결과를 캐싱함
    */
    @Cacheable(value = "weeklyReportCache", key = "#pDTO.userNo()")
    @Override
    public ReportCardDTO getWeeklyReport(DiaryDTO pDTO) throws Exception {

        log.info("{}.getWeeklyReport Start!", this.getClass().getName());

        String sUserNo = CmmUtil.nvl(pDTO.userNo());

        LocalDate today = LocalDate.now();
        LocalDate weekAgo = today.minusDays(WEEKLY_REPORT_PERIOD_DAYS - 1);

        List<DiaryEntity> entities = Optional.ofNullable(
                diaryRepository.findAllByUserNoAndCreatedAtBetween(sUserNo, weekAgo, today)
        ).orElseGet(ArrayList::new);

        List<Map<String, Object>> entryMaps = entities.stream()
                .filter(e -> e.getSummary() != null)
                .map(e -> {
                    Map<String, Object> entry = new HashMap<>();
                    entry.put("title", CmmUtil.nvl(e.getTitle()));
                    entry.put("summary", CmmUtil.nvl(e.getSummary()));
                    entry.put("main_emotion", CmmUtil.nvl(e.getMainEmotion()));
                    return entry;
                })
                .collect(Collectors.toList());

        String comment;
        try {
            Map<String, Object> requestMap = new HashMap<>();
            requestMap.put("user_no", Integer.parseInt(sUserNo));
            requestMap.put("entries", entryMaps);

            ResponseEntity<Map> response = pythonApiRestClient.post()
                    .uri(pythonApiUrl + "/api/weekly-report")
                    .body(requestMap)
                    .retrieve()
                    .toEntity(Map.class);

            comment = (response.getStatusCode().is2xxSuccessful() && response.getBody() != null)
                    ? CmmUtil.nvl((String) response.getBody().get("comment"))
                    : "이번 한 주도 스스로를 잘 돌보고 계시네요. 다음 주도 응원할게요.";

        } catch (Exception e) {
            log.error("주간 리포트 생성 요청 실패: {}", e.getMessage());
            comment = "이번 한 주도 스스로를 잘 돌보고 계시네요. 다음 주도 응원할게요.";
        }

        List<BigDecimal> depScores = entities.stream()
                .map(DiaryEntity::getDepScore)
                .filter(Objects::nonNull)
                .collect(Collectors.toList());

        BigDecimal avgDepScore = depScores.isEmpty()
                ? null
                : depScores.stream()
                        .reduce(BigDecimal.ZERO, BigDecimal::add)
                        .divide(BigDecimal.valueOf(depScores.size()), 1, RoundingMode.HALF_UP);

        ReportCardDTO rDTO = ReportCardDTO.builder()
                .comment(comment)
                .periodStart(DateUtil.formatLocalDate(weekAgo, "yyyy-MM-dd"))
                .periodEnd(DateUtil.formatLocalDate(today, "yyyy-MM-dd"))
                .diaryCount(entities.size())
                .avgDepScore(avgDepScore)
                .build();

        log.info("{}.getWeeklyReport End!", this.getClass().getName());

        return rDTO;
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

    /*
    제목만 수정 (사이드바 인라인 이름변경용)
    */
    @Transactional
    @CacheEvict(value = "diaryCache", allEntries = true)
    @Override
    public int updateTitle(DiaryDTO pDTO) throws Exception {

        log.info("{}.updateTitle Start!", this.getClass().getName());

        String userNo = CmmUtil.nvl(pDTO.userNo());
        Integer diaryNo = pDTO.diaryNo();
        String title = CmmUtil.nvl(pDTO.title());

        int res = diaryRepository.updateTitleDirectly(diaryNo, userNo, title);

        log.info("{}.updateTitle End!", this.getClass().getName());

        return res;
    }

    /*
    사이드바 상단 고정
    */
    @Transactional
    @CacheEvict(value = "diaryCache", allEntries = true)
    @Override
    public int updatePinned(DiaryDTO pDTO) throws Exception {

        log.info("{}.updatePinned Start!", this.getClass().getName());

        String userNo = CmmUtil.nvl(pDTO.userNo());
        Integer diaryNo = pDTO.diaryNo();
        Integer isPinned = pDTO.isPinned();

        int res = diaryRepository.updatePinnedDirectly(diaryNo, userNo, isPinned);

        log.info("{}.updatePinned End!", this.getClass().getName());

        return res;
    }

    /*
    일기 이미지 업로드 (GCS) - 일기당 최대 MAX_DIARY_IMAGE_COUNT장까지 허용
    */
    @Transactional
    @Override
    public List<DiaryImageDTO> uploadDiaryImages(Integer diaryNo, String userNo, List<MultipartFile> images) throws Exception {

        log.info("{}.uploadDiaryImages Start!", this.getClass().getName());

        DiaryEntity entity = diaryRepository.findById(diaryNo)
                .orElseThrow(() -> new IllegalArgumentException("존재하지 않는 일기입니다."));

        if (!entity.getUserNo().equals(userNo)) {
            throw new IllegalArgumentException("본인의 일기에만 이미지를 추가할 수 있습니다.");
        }

        int existingCount = diaryImageRepository.countByDiaryNo(diaryNo);

        if (existingCount + images.size() > MAX_DIARY_IMAGE_COUNT) {
            throw new IllegalArgumentException("이미지는 최대 " + MAX_DIARY_IMAGE_COUNT + "장까지 등록할 수 있습니다.");
        }

        List<DiaryImageDTO> rList = new ArrayList<>();
        int order = existingCount;

        for (MultipartFile image : images) {
            String imageUrl = gcsService.uploadImage(image, diaryNo);

            DiaryImageEntity savedEntity = diaryImageRepository.save(
                    DiaryImageEntity.builder()
                            .diaryNo(diaryNo)
                            .imageUrl(imageUrl)
                            .imageOrder(order++)
                            .build()
            );

            rList.add(DiaryImageDTO.builder()
                    .imageNo(savedEntity.getImageNo())
                    .diaryNo(diaryNo)
                    .imageUrl(savedEntity.getImageUrl())
                    .imageOrder(savedEntity.getImageOrder())
                    .build());
        }

        log.info("{}.uploadDiaryImages End!", this.getClass().getName());

        return rList;
    }

    /*
    일기 이미지 삭제
    */
    @Transactional
    @Override
    public MsgDTO deleteDiaryImage(DiaryImageDTO pDTO) throws Exception {

        log.info("{}.deleteDiaryImage Start!", this.getClass().getName());

        Integer imageNo = pDTO.imageNo();
        String userNo = CmmUtil.nvl(pDTO.userNo());

        Optional<DiaryImageEntity> oImage = diaryImageRepository.findById(imageNo);

        if (oImage.isEmpty()) {
            return MsgDTO.builder().result(0).msg("존재하지 않는 이미지입니다.").build();
        }

        DiaryImageEntity image = oImage.get();
        Optional<DiaryEntity> oEntity = diaryRepository.findById(image.getDiaryNo());

        if (oEntity.isEmpty() || !oEntity.get().getUserNo().equals(userNo)) {
            return MsgDTO.builder().result(0).msg("본인의 일기 이미지만 삭제할 수 있습니다.").build();
        }

        diaryImageRepository.delete(image);
        gcsService.deleteImage(image.getImageUrl());

        log.info("{}.deleteDiaryImage End!", this.getClass().getName());

        return MsgDTO.builder().result(1).msg("이미지가 삭제되었습니다.").build();
    }
}