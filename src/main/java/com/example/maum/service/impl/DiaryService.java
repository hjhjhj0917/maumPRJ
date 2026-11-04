package com.example.maum.service.impl;

import com.example.maum.diary.DiaryAnalysisRequestedEvent;
import com.example.maum.diary.DiaryStatus;
import com.example.maum.diary.EmotionCatalog;
import com.example.maum.dto.DiaryDTO;
import com.example.maum.dto.DiaryImageDTO;
import com.example.maum.dto.DiaryMusicDTO;
import com.example.maum.dto.EmotionScoreDTO;
import com.example.maum.dto.MsgDTO;
import com.example.maum.repository.DiaryAnalysisRepository;
import com.example.maum.repository.DiaryEmotionScoreRepository;
import com.example.maum.repository.DiaryImageRepository;
import com.example.maum.repository.DiaryMusicRepository;
import com.example.maum.repository.DiaryRepository;
import com.example.maum.repository.SyncOutboxRepository;
import com.example.maum.repository.UserRepository;
import com.example.maum.repository.entity.DiaryAnalysisEntity;
import com.example.maum.repository.entity.DiaryEntity;
import com.example.maum.repository.entity.DiaryImageEntity;
import com.example.maum.repository.entity.SyncOutboxEntity;
import com.example.maum.repository.entity.UserEntity;
import com.example.maum.repository.projection.DiaryListProjection;
import com.example.maum.service.IDiaryService;
import com.example.maum.service.IGcsService;
import com.example.maum.util.CmmUtil;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.math.BigDecimal;
import java.time.Instant;
import java.time.LocalDate;
import java.time.YearMonth;
import java.time.format.DateTimeParseException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
@Slf4j
@RequiredArgsConstructor
public class DiaryService implements IDiaryService {

    private static final int MAX_DIARY_IMAGE_COUNT = 3;
    private static final int TOP_EMOTION_COUNT = 10;
    private static final int RECENT_DIARY_LIMIT = 20;

    // DB CHECK(ck_diary_content_len, diary.title VARCHAR(200), diary_temperature, ck_diary_weather)와 같은 기준
    private static final int MAX_TITLE_LENGTH = 200;
    private static final int MAX_CONTENT_LENGTH = 10000;
    private static final int MAX_WEATHER_REGION_LENGTH = 50;
    private static final BigDecimal MIN_TEMPERATURE = BigDecimal.valueOf(-60);
    private static final BigDecimal MAX_TEMPERATURE = BigDecimal.valueOf(60);
    private static final Set<String> WEATHER_CONDITIONS = Set.of(
            "CLEAR", "PARTLY_CLOUDY", "CLOUDY", "RAIN", "SNOW", "SLEET", "FOG", "THUNDERSTORM", "OTHER");

    private final DiaryRepository diaryRepository;
    private final DiaryAnalysisRepository diaryAnalysisRepository;
    private final DiaryEmotionScoreRepository diaryEmotionScoreRepository;
    private final DiaryMusicRepository diaryMusicRepository;
    private final DiaryImageRepository diaryImageRepository;
    private final SyncOutboxRepository syncOutboxRepository;
    private final UserRepository userRepository;
    private final IGcsService gcsService;
    private final EmotionCatalog emotionCatalog;
    private final ApplicationEventPublisher eventPublisher;

    // 이 서비스의 쓰기 메서드는 외부 호출(GCS 업로드 제외) 없이 DB 만 다루므로 메서드 전체를 하나의 트랜잭션으로 묶음.
    // AI 분석은 커밋된 뒤에 이벤트로 시작되어(DiaryAnalysisService) 사용자의 응답을 기다리게 하지 않음

    @Transactional
    @Override
    public String draftSave(DiaryDTO pDTO) throws Exception {

        log.info("{}.draftSave Start!", this.getClass().getName());

        Long userId = resolveUserId(pDTO.userPublicId());
        String title = CmmUtil.nvl(pDTO.title());
        String content = CmmUtil.nvl(pDTO.content());

        validateLength(title, content);

        LocalDate diaryDate = parseDiaryDate(pDTO.diaryDate());
        DiaryEntity diary;

        if (hasText(pDTO.publicId())) {
            diary = findOwnedDiary(pDTO.publicId(), userId);

            if (diary.isPublished()) {
                throw new IllegalArgumentException("이미 발행된 일기는 임시 저장할 수 없습니다.");
            }

            diary.changeText(title, content);
            diary.changeDate(diaryDate);
        } else {
            diary = newDraft(userId, diaryDate, title, content);
        }

        applyWeather(diary, pDTO);
        diary = diaryRepository.save(diary);

        log.info("{}.draftSave End!", this.getClass().getName());

        return diary.getPublicId().toString();
    }

    @CacheEvict(value = "diaryCache", key = "#pDTO.userPublicId()")
    @Transactional
    @Override
    public String diaryInsert(DiaryDTO pDTO) throws Exception {

        log.info("{}.diaryInsert Start!", this.getClass().getName());

        Long userId = resolveUserId(pDTO.userPublicId());
        String title = CmmUtil.nvl(pDTO.title());
        String content = CmmUtil.nvl(pDTO.content());

        validateForPublish(title, content);

        LocalDate diaryDate = parseDiaryDate(pDTO.diaryDate());
        DiaryEntity diary;

        if (hasText(pDTO.publicId())) {
            diary = findOwnedDiary(pDTO.publicId(), userId);

            if (diary.isPublished()) {
                throw new IllegalArgumentException("이미 발행된 일기입니다.");
            }

            diary.changeText(title, content);
            diary.changeDate(diaryDate);
        } else {
            diary = newDraft(userId, diaryDate, title, content);
        }

        applyWeather(diary, pDTO);
        diary.publish(Instant.now());
        diary = diaryRepository.save(diary);

        diaryAnalysisRepository.save(DiaryAnalysisEntity.pending(diary.getId(), isAiEnabled(userId)));
        eventPublisher.publishEvent(new DiaryAnalysisRequestedEvent(diary.getId()));

        log.info("일기 발행 완료 (publicId: {})", diary.getPublicId());
        log.info("{}.diaryInsert End!", this.getClass().getName());

        return diary.getPublicId().toString();
    }

    @CacheEvict(value = "diaryCache", key = "#pDTO.userPublicId()")
    @Transactional
    @Override
    public MsgDTO diaryUpdate(DiaryDTO pDTO) throws Exception {

        log.info("{}.diaryUpdate Start!", this.getClass().getName());

        Long userId = resolveUserId(pDTO.userPublicId());
        DiaryEntity diary = findOwnedDiary(pDTO.publicId(), userId);

        if (!diary.isPublished()) {
            throw new IllegalArgumentException("임시 저장된 일기는 발행한 뒤에 수정할 수 있습니다.");
        }

        String title = CmmUtil.nvl(pDTO.title());
        String content = CmmUtil.nvl(pDTO.content());

        validateForPublish(title, content);

        MsgDTO rDTO;

        // 제목·본문이 같으면 분석 결과가 그대로 유효하므로 재분석하지 않음
        if (diary.hasSameText(title, content)) {
            rDTO = MsgDTO.builder().result(1).msg("변경된 내용이 없습니다.").build();
        } else {
            diary.changeText(title, content);
            resetAnalysis(diary.getId(), userId);
            eventPublisher.publishEvent(new DiaryAnalysisRequestedEvent(diary.getId()));

            rDTO = MsgDTO.builder().result(1).msg("일기가 수정되었습니다. 변경된 내용으로 다시 분석합니다.").build();
        }

        log.info("{}.diaryUpdate End!", this.getClass().getName());

        return rDTO;
    }

    // 외부 저장소(Mongo 벡터, GCS 이미지) 삭제는 DB 삭제와 같은 트랜잭션에서 sync_outbox 에 기록만 해 두고,
    // 실제 삭제는 워커(4단계)가 재시도하며 처리함 — DB 삭제가 롤백되면 기록도 함께 사라져 파일만 지워지는 일이 없음
    @CacheEvict(value = "diaryCache", key = "#pDTO.userPublicId()")
    @Transactional
    @Override
    public MsgDTO diaryDelete(DiaryDTO pDTO) throws Exception {

        log.info("{}.diaryDelete Start!", this.getClass().getName());

        Long userId = resolveUserId(pDTO.userPublicId());
        DiaryEntity diary = findOwnedDiary(pDTO.publicId(), userId);

        List<SyncOutboxEntity> outbox = new ArrayList<>();

        if (diary.isPublished()) {
            outbox.add(outboxOf(SyncOutboxEntity.TARGET_MONGO, diary.getPublicId().toString()));
        }

        diaryImageRepository.findByDiaryIdOrderBySortOrderAsc(diary.getId())
                .forEach(image -> outbox.add(outboxOf(SyncOutboxEntity.TARGET_STORAGE, image.getStorageKey())));

        syncOutboxRepository.saveAll(outbox);
        diaryRepository.delete(diary);

        log.info("일기 삭제 완료 (publicId: {}, 외부 삭제 예약 {}건)", diary.getPublicId(), outbox.size());
        log.info("{}.diaryDelete End!", this.getClass().getName());

        return MsgDTO.builder().result(1).msg("일기가 성공적으로 삭제되었습니다.").build();
    }

    @Transactional(readOnly = true)
    @Override
    public List<DiaryDTO> getMonthlyDiaryList(DiaryDTO pDTO) throws Exception {

        log.info("{}.getMonthlyDiaryList Start!", this.getClass().getName());

        String dateStr = CmmUtil.nvl(pDTO.diaryDate());

        YearMonth yearMonth;

        try {
            yearMonth = YearMonth.parse(dateStr.length() > 7 ? dateStr.substring(0, 7) : dateStr);
        } catch (DateTimeParseException e) {
            log.warn("조회 월 형식이 잘못되었습니다: {}", dateStr);
            return new ArrayList<>();
        }

        Long userId = resolveUserId(pDTO.userPublicId());

        List<DiaryDTO> rList = toListDTOs(diaryRepository.findMonthly(userId, yearMonth.atDay(1), yearMonth.atEndOfMonth()));

        log.info("{}.getMonthlyDiaryList End! ({}건)", this.getClass().getName(), rList.size());

        return rList;
    }

    @Transactional(readOnly = true)
    @Override
    public DiaryDTO getDiaryDetail(DiaryDTO pDTO) throws Exception {

        log.info("{}.getDiaryDetail Start!", this.getClass().getName());

        Long userId = resolveUserId(pDTO.userPublicId());
        DiaryEntity diary = findOwnedDiary(pDTO.publicId(), userId);

        List<DiaryImageDTO> images = diaryImageRepository.findByDiaryIdOrderBySortOrderAsc(diary.getId()).stream()
                .map(this::toImageDTO)
                .collect(Collectors.toList());

        DiaryDTO.DiaryDTOBuilder builder = DiaryDTO.builder()
                .publicId(diary.getPublicId().toString())
                .title(diary.getTitle())
                .content(diary.getContent())
                .diaryDate(diary.getDiaryDate().toString())
                .status(diary.getStatus().name())
                .isFavorite(diary.isFavorite())
                .isPinned(diary.isPinned())
                .weatherCondition(diary.getWeatherCondition())
                .temperatureC(diary.getTemperatureC())
                .weatherRegion(diary.getWeatherRegion())
                .images(images);

        // 임시 저장본에는 분석이 없음. 발행본이면 단계별 상태와 이미 끝난 단계의 결과를 함께 내려줌
        if (diary.isPublished()) {
            diaryAnalysisRepository.findById(diary.getId()).ifPresent(analysis -> {
                // 화면의 대표 감정은 44개 중 점수 1위, 이어서 상위 10개. 대분류는 색상에만 사용
                List<EmotionScoreDTO> topEmotions = diaryEmotionScoreRepository
                        .findTop(diary.getId(), PageRequest.of(0, TOP_EMOTION_COUNT)).stream()
                        .map(score -> emotionCatalog.toScoreDTO(score.getEmotionId(), score.getScore()))
                        .flatMap(Optional::stream)
                        .collect(Collectors.toList());

                builder.analysisStatus(analysis.getAnalysisStatus().name())
                        .summaryStatus(analysis.getSummaryStatus().name())
                        .summary(analysis.getSummary())
                        .emotionColor(emotionCatalog.colorOf(analysis.getMainGroupCode()))
                        .mainEmotion(topEmotions.isEmpty() ? null : topEmotions.get(0).name())
                        .topEmotions(topEmotions)
                        .depLevel(analysis.getDepLevel())
                        .depScore(analysis.getDepScore())
                        .musics(diaryMusicRepository.findByDiaryIdOrderByTrackOrderAsc(diary.getId()).stream()
                                .map(music -> DiaryMusicDTO.builder()
                                        .trackId(music.getTrackId())
                                        .trackName(music.getTrackName())
                                        .artistName(music.getArtistName())
                                        .albumImageUrl(music.getAlbumImageUrl())
                                        .spotifyUrl(music.getSpotifyUrl())
                                        .trackOrder(music.getTrackOrder().intValue())
                                        .build())
                                .collect(Collectors.toList()));
            });
        }

        log.info("{}.getDiaryDetail End!", this.getClass().getName());

        return builder.build();
    }

    @Transactional(readOnly = true)
    @Override
    public List<DiaryDTO> searchDiaryList(DiaryDTO pDTO) throws Exception {

        log.info("{}.searchDiaryList Start!", this.getClass().getName());

        String keyword = CmmUtil.nvl(pDTO.title()).trim();

        if (keyword.isEmpty()) {
            return new ArrayList<>();
        }

        Long userId = resolveUserId(pDTO.userPublicId());

        List<DiaryDTO> rList = toListDTOs(diaryRepository.searchByTitle(userId, escapeLike(keyword)));

        log.info("{}.searchDiaryList End!", this.getClass().getName());

        return rList;
    }

    @Transactional(readOnly = true)
    @Override
    public List<DiaryDTO> getDiaryListByGroups(String userPublicId, List<String> groupCodes) throws Exception {

        log.info("{}.getDiaryListByGroups Start!", this.getClass().getName());

        if (groupCodes == null || groupCodes.isEmpty()) {
            return new ArrayList<>();
        }

        Long userId = resolveUserId(userPublicId);

        List<DiaryDTO> rList = toListDTOs(diaryRepository.findByGroupCodes(userId, groupCodes));

        log.info("{}.getDiaryListByGroups End!", this.getClass().getName());

        return rList;
    }

    // 분석이 끝나 색상이 바뀌면 DiaryAnalysisService 가 이 캐시를 사용자 단위로 비움
    @Transactional(readOnly = true)
    @Cacheable(value = "diaryCache", key = "#pDTO.userPublicId()", condition = "#pDTO.userPublicId() != null")
    @Override
    public List<DiaryDTO> getRecentDiaryList(DiaryDTO pDTO) throws Exception {

        log.info("{}.getRecentDiaryList Start!", this.getClass().getName());

        Long userId = resolveUserId(pDTO.userPublicId());

        List<DiaryDTO> rList = toListDTOs(diaryRepository.findRecent(userId, PageRequest.of(0, RECENT_DIARY_LIMIT)));

        log.info("{}.getRecentDiaryList End!", this.getClass().getName());

        return rList;
    }

    @Transactional(readOnly = true)
    @Override
    public List<DiaryDTO> getFavoriteDiaryList(DiaryDTO pDTO) throws Exception {

        log.info("{}.getFavoriteDiaryList Start!", this.getClass().getName());

        List<DiaryDTO> rList = toListDTOs(diaryRepository.findFavorites(resolveUserId(pDTO.userPublicId())));

        log.info("{}.getFavoriteDiaryList End!", this.getClass().getName());

        return rList;
    }

    @Transactional(readOnly = true)
    @Override
    public List<DiaryDTO> getDraftDiaryList(DiaryDTO pDTO) throws Exception {

        log.info("{}.getDraftDiaryList Start!", this.getClass().getName());

        List<DiaryDTO> rList = toListDTOs(diaryRepository.findDrafts(resolveUserId(pDTO.userPublicId())));

        log.info("{}.getDraftDiaryList End!", this.getClass().getName());

        return rList;
    }

    @Transactional
    @Override
    public int updateFavorite(DiaryDTO pDTO) throws Exception {

        log.info("{}.updateFavorite Start!", this.getClass().getName());

        if (pDTO.isFavorite() == null) {
            throw new IllegalArgumentException("즐겨찾기 값이 필요합니다.");
        }

        findOwnedDiary(pDTO.publicId(), resolveUserId(pDTO.userPublicId())).changeFavorite(pDTO.isFavorite());

        log.info("{}.updateFavorite End!", this.getClass().getName());

        return 1;
    }

    // 사이드바 이름 변경용. 이름만 바꾸는 가벼운 수정이라 재분석하지 않음 (본문이 같으면 분석 결과도 같으므로)
    @CacheEvict(value = "diaryCache", key = "#pDTO.userPublicId()")
    @Transactional
    @Override
    public int updateTitle(DiaryDTO pDTO) throws Exception {

        log.info("{}.updateTitle Start!", this.getClass().getName());

        String title = CmmUtil.nvl(pDTO.title());

        if (title.isBlank() || title.length() > MAX_TITLE_LENGTH) {
            throw new IllegalArgumentException("제목은 1자 이상 " + MAX_TITLE_LENGTH + "자 이하로 입력해주세요.");
        }

        findOwnedDiary(pDTO.publicId(), resolveUserId(pDTO.userPublicId())).changeTitle(title);

        log.info("{}.updateTitle End!", this.getClass().getName());

        return 1;
    }

    @CacheEvict(value = "diaryCache", key = "#pDTO.userPublicId()")
    @Transactional
    @Override
    public int updatePinned(DiaryDTO pDTO) throws Exception {

        log.info("{}.updatePinned Start!", this.getClass().getName());

        if (pDTO.isPinned() == null) {
            throw new IllegalArgumentException("고정 값이 필요합니다.");
        }

        findOwnedDiary(pDTO.publicId(), resolveUserId(pDTO.userPublicId())).changePinned(pDTO.isPinned());

        log.info("{}.updatePinned End!", this.getClass().getName());

        return 1;
    }

    // GCS 업로드(외부 네트워크 호출)가 반복되므로 @Transactional 을 붙이지 않음 (붙이면 업로드가 끝날 때까지 DB 커넥션을 붙잡음).
    // "GCS 업로드 전부 → DB 저장 한 번" 순서로 처리해서, 중간에 하나라도 실패하면 이미 올라간 파일을 지우고(보상 처리)
    // DB에는 아무것도 남기지 않음 — saveAll()은 Spring Data JPA가 자체 트랜잭션으로 묶어주므로 전부 성공 또는 전부 실패함
    @Override
    public List<DiaryImageDTO> uploadDiaryImages(String diaryPublicId, String userPublicId,
                                                 List<MultipartFile> images) throws Exception {

        log.info("{}.uploadDiaryImages Start!", this.getClass().getName());

        DiaryEntity diary = findOwnedDiary(diaryPublicId, resolveUserId(userPublicId));

        List<DiaryImageEntity> existing = diaryImageRepository.findByDiaryIdOrderBySortOrderAsc(diary.getId());

        if (existing.size() + images.size() > MAX_DIARY_IMAGE_COUNT) {
            throw new IllegalArgumentException("이미지는 최대 " + MAX_DIARY_IMAGE_COUNT + "장까지 등록할 수 있습니다.");
        }

        // 중간 이미지를 지운 뒤에도 순서가 겹치지 않도록 마지막 순서의 다음 번호부터 부여함
        int nextOrder = existing.isEmpty() ? 0 : existing.get(existing.size() - 1).getSortOrder() + 1;
        List<String> uploadedKeys = new ArrayList<>();

        try {
            for (MultipartFile image : images) {
                uploadedKeys.add(gcsService.uploadImage(image, diary.getPublicId().toString()));
            }

            List<DiaryImageEntity> toSave = new ArrayList<>();

            for (String storageKey : uploadedKeys) {
                toSave.add(DiaryImageEntity.builder()
                        .diaryId(diary.getId())
                        .sortOrder((short) nextOrder++)
                        .storageKey(storageKey)
                        .build());
            }

            List<DiaryImageDTO> rList = diaryImageRepository.saveAll(toSave).stream()
                    .map(this::toImageDTO)
                    .collect(Collectors.toList());

            log.info("{}.uploadDiaryImages End!", this.getClass().getName());

            return rList;

        } catch (Exception e) {
            // 보상 처리: 중간에 실패했으면 이미 GCS에 올라간 파일을 지워 고아 파일이 남지 않게 함
            log.error("이미지 업로드 실패, 이미 업로드된 {}개 파일을 삭제합니다: {}", uploadedKeys.size(), e.getMessage());

            uploadedKeys.forEach(gcsService::deleteImage);

            throw e;
        }
    }

    @Transactional
    @Override
    public MsgDTO deleteDiaryImage(DiaryImageDTO pDTO) throws Exception {

        log.info("{}.deleteDiaryImage Start!", this.getClass().getName());

        Long userId = resolveUserId(pDTO.userPublicId());

        DiaryImageEntity image = Optional.ofNullable(pDTO.imageNo())
                .flatMap(diaryImageRepository::findById)
                .orElse(null);

        // 없는 이미지와 남의 이미지를 같은 응답으로 처리해 존재 여부가 드러나지 않게 함
        boolean owned = image != null && diaryRepository.findById(image.getDiaryId())
                .map(diary -> diary.getUserId().equals(userId))
                .orElse(false);

        if (!owned) {
            return MsgDTO.builder().result(0).msg("존재하지 않는 이미지입니다.").build();
        }

        diaryImageRepository.delete(image);
        syncOutboxRepository.save(outboxOf(SyncOutboxEntity.TARGET_STORAGE, image.getStorageKey()));

        log.info("{}.deleteDiaryImage End!", this.getClass().getName());

        return MsgDTO.builder().result(1).msg("이미지가 삭제되었습니다.").build();
    }

    // 분석을 처음부터 다시 하기 위해 이전 결과(감정 점수, 추천곡, 요약 등)를 모두 비움
    private void resetAnalysis(Long diaryId, Long userId) {
        boolean aiEnabled = isAiEnabled(userId);

        diaryEmotionScoreRepository.deleteByDiaryId(diaryId);
        diaryMusicRepository.deleteByDiaryId(diaryId);

        diaryAnalysisRepository.findById(diaryId).ifPresentOrElse(
                analysis -> analysis.reset(aiEnabled),
                () -> diaryAnalysisRepository.save(DiaryAnalysisEntity.pending(diaryId, aiEnabled)));
    }

    // shortcut: 프라이버시 모드(AI_PROCESSING 동의) 도입 전이라 항상 허용함. 2b 단계에서 동의 이력으로 판단하도록 바꾼다
    private boolean isAiEnabled(Long userId) {
        return true;
    }

    private Long resolveUserId(String userPublicId) {
        return userRepository.findByPublicId(parseUuid(userPublicId, "사용자 정보가 올바르지 않습니다."))
                .map(UserEntity::getId)
                .orElseThrow(() -> new IllegalArgumentException("사용자 정보를 찾을 수 없습니다."));
    }

    // 없는 일기와 남의 일기를 같은 메시지로 처리해 존재 여부가 드러나지 않게 함
    private DiaryEntity findOwnedDiary(String diaryPublicId, Long userId) {
        return diaryRepository.findByPublicIdAndUserId(parseUuid(diaryPublicId, "일기 번호가 올바르지 않습니다."), userId)
                .orElseThrow(() -> new IllegalArgumentException("존재하지 않는 일기입니다."));
    }

    private UUID parseUuid(String value, String errorMessage) {
        try {
            return UUID.fromString(CmmUtil.nvl(value));
        } catch (IllegalArgumentException e) {
            throw new IllegalArgumentException(errorMessage);
        }
    }

    private LocalDate parseDiaryDate(String value) {
        try {
            return LocalDate.parse(CmmUtil.nvl(value).trim());
        } catch (DateTimeParseException e) {
            throw new IllegalArgumentException("일기 날짜가 올바르지 않습니다. (yyyy-MM-dd)");
        }
    }

    private DiaryEntity newDraft(Long userId, LocalDate diaryDate, String title, String content) {
        return DiaryEntity.builder()
                .publicId(UUID.randomUUID())
                .userId(userId)
                .diaryDate(diaryDate)
                .title(title)
                .content(content)
                .status(DiaryStatus.DRAFT)
                .build();
    }

    private void validateLength(String title, String content) {
        if (title.length() > MAX_TITLE_LENGTH) {
            throw new IllegalArgumentException("제목은 " + MAX_TITLE_LENGTH + "자 이하로 입력해주세요.");
        }

        if (content.length() > MAX_CONTENT_LENGTH) {
            throw new IllegalArgumentException("본문은 " + MAX_CONTENT_LENGTH + "자 이하로 입력해주세요.");
        }
    }

    private void validateForPublish(String title, String content) {
        if (title.isBlank() || content.isBlank()) {
            throw new IllegalArgumentException("제목과 내용을 모두 입력해주세요.");
        }

        validateLength(title, content);
    }

    // 날씨 값이 하나라도 오면 세 값을 함께 교체함 (없는 값은 NULL). 날씨를 못 가져온 경우에는 값을 보내지 않으면 기존 값을 유지함
    private void applyWeather(DiaryEntity diary, DiaryDTO pDTO) {
        if (pDTO.weatherCondition() == null && pDTO.temperatureC() == null && pDTO.weatherRegion() == null) {
            return;
        }

        String condition = pDTO.weatherCondition();
        BigDecimal temperature = pDTO.temperatureC();
        String region = pDTO.weatherRegion();

        if (condition != null && !WEATHER_CONDITIONS.contains(condition)) {
            throw new IllegalArgumentException("날씨 값이 올바르지 않습니다.");
        }

        if (temperature != null && (temperature.compareTo(MIN_TEMPERATURE) < 0 || temperature.compareTo(MAX_TEMPERATURE) > 0)) {
            throw new IllegalArgumentException("기온 값이 올바르지 않습니다.");
        }

        if (region != null && region.length() > MAX_WEATHER_REGION_LENGTH) {
            throw new IllegalArgumentException("지역명이 너무 깁니다.");
        }

        diary.changeWeather(condition, temperature, region);
    }

    private boolean hasText(String value) {
        return value != null && !value.isBlank();
    }

    private SyncOutboxEntity outboxOf(String target, String refKey) {
        return SyncOutboxEntity.builder()
                .target(target)
                .action(SyncOutboxEntity.ACTION_DELETE)
                .refKey(refKey)
                .build();
    }

    // LIKE 의 특수문자(% _ \)를 일반 글자로 검색하도록 이스케이프 (쿼리의 ESCAPE '\' 와 짝)
    private String escapeLike(String keyword) {
        return keyword.replace("\\", "\\\\").replace("%", "\\%").replace("_", "\\_");
    }

    private List<DiaryDTO> toListDTOs(Collection<DiaryListProjection> rows) {
        // 캐시(Redis JSON)에 저장되는 목록도 있어 불변 리스트가 아닌 ArrayList 로 수집
        return rows.stream()
                .map(row -> DiaryDTO.builder()
                        .publicId(row.getPublicId().toString())
                        .title(row.getTitle())
                        .diaryDate(row.getDiaryDate().toString())
                        .isFavorite(row.getFavorite())
                        .isPinned(row.getPinned())
                        .emotionColor(emotionCatalog.colorOf(row.getMainGroupCode()))
                        .build())
                .collect(Collectors.toList());
    }

    private DiaryImageDTO toImageDTO(DiaryImageEntity image) {
        return DiaryImageDTO.builder()
                .imageNo(image.getId())
                .imageUrl(gcsService.toUrl(image.getStorageKey()))
                .imageOrder(image.getSortOrder().intValue())
                .build();
    }
}
