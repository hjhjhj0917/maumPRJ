package com.example.maum.service.impl;

import com.example.maum.diary.AnalysisStatus;
import com.example.maum.diary.EmotionCatalog;
import com.example.maum.diary.EmotionCatalog.RankedEmotion;
import com.example.maum.dto.AiAnalysisDTO;
import com.example.maum.dto.DiaryAnalysisInputDTO;
import com.example.maum.repository.DiaryAnalysisRepository;
import com.example.maum.repository.DiaryEmotionScoreRepository;
import com.example.maum.repository.DiaryMusicRepository;
import com.example.maum.repository.DiaryRepository;
import com.example.maum.repository.UserRepository;
import com.example.maum.repository.entity.DiaryAnalysisEntity;
import com.example.maum.repository.entity.DiaryEmotionScoreEntity;
import com.example.maum.repository.entity.DiaryEntity;
import com.example.maum.repository.entity.DiaryMusicEntity;
import com.example.maum.repository.entity.UserEntity;
import com.example.maum.service.IDiaryWriteService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.function.Consumer;

// DiaryAnalysisService 가 FastAPI 호출(수 초 걸리는 외부 호출)을 트랜잭션 밖에서 실행한 뒤, 그 결과를 DB에 쓰는 부분만
// 이 서비스로 분리함. 같은 클래스 안에서 this.로 호출하면 Spring AOP 프록시를 거치지 않아 @Transactional이
// 무시되는 문제(자기 호출)가 있어서, 별도 빈으로 두고 주입받아 호출하면 프록시를 거쳐 트랜잭션이 적용됨
@Service
@Slf4j
@RequiredArgsConstructor
public class DiaryWriteService implements IDiaryWriteService {

    private static final int TOP_EMOTION_COUNT = 10;

    private final DiaryRepository diaryRepository;
    private final DiaryAnalysisRepository diaryAnalysisRepository;
    private final DiaryEmotionScoreRepository diaryEmotionScoreRepository;
    private final DiaryMusicRepository diaryMusicRepository;
    private final UserRepository userRepository;
    private final EmotionCatalog emotionCatalog;

    @Override
    @Transactional(readOnly = true)
    public Optional<DiaryAnalysisInputDTO> loadAnalysisInput(Long diaryId) {

        Optional<DiaryEntity> oDiary = diaryRepository.findById(diaryId).filter(DiaryEntity::isPublished);
        Optional<DiaryAnalysisEntity> oAnalysis = diaryAnalysisRepository.findById(diaryId);

        if (oDiary.isEmpty() || oAnalysis.isEmpty()) {
            return Optional.empty();
        }

        DiaryEntity diary = oDiary.get();
        DiaryAnalysisEntity analysis = oAnalysis.get();

        Optional<UserEntity> oUser = userRepository.findById(diary.getUserId());

        if (oUser.isEmpty()) {
            return Optional.empty();
        }

        // 감정 점수가 이미 저장된 상태에서 뒤 단계만 이어서 할 때 쓰는 상위 감정 이름
        List<String> topEmotions = analysis.getAnalysisStatus().isFinished()
                ? diaryEmotionScoreRepository.findTop(diaryId, PageRequest.of(0, TOP_EMOTION_COUNT)).stream()
                .map(score -> emotionCatalog.findById(score.getEmotionId()))
                .flatMap(Optional::stream)
                .map(emotion -> emotion.getNameKo())
                .toList()
                : List.of();

        return Optional.of(DiaryAnalysisInputDTO.builder()
                .diaryId(diaryId)
                .diaryPublicId(diary.getPublicId().toString())
                .userPublicId(oUser.get().getPublicId().toString())
                .diaryDate(diary.getDiaryDate().toString())
                .title(diary.getTitle())
                .content(diary.getContent())
                .analysisStatus(analysis.getAnalysisStatus())
                .summaryStatus(analysis.getSummaryStatus())
                .embeddingStatus(analysis.getEmbeddingStatus())
                .summary(analysis.getSummary())
                .topEmotions(topEmotions)
                .build());
    }

    @Override
    @Transactional
    public boolean applyCore(Long diaryId, String title, String content, List<RankedEmotion> ranked,
                             AnalysisStatus status, Integer depLevel, BigDecimal depScore, String modelVersion) {

        if (!isCurrent(diaryId, title, content)) {
            return false;
        }

        RankedEmotion top = ranked.get(0);

        // 재시도로 다시 저장하는 경우를 위해 이전 점수를 먼저 지움
        diaryEmotionScoreRepository.deleteByDiaryId(diaryId);
        diaryEmotionScoreRepository.saveAll(ranked.stream()
                .map(r -> new DiaryEmotionScoreEntity(diaryId, r.emotion().getId(), r.score()))
                .toList());

        diaryAnalysisRepository.findById(diaryId).orElseThrow().applyCore(
                status, emotionCatalog.resolveMainGroup(top.emotion(), top.score()), depLevel, depScore, modelVersion);

        log.info("코어 분석 결과 저장 완료 (diaryId: {}, status: {}, 감정 {}개)", diaryId, status, ranked.size());

        return true;
    }

    @Override
    @Transactional
    public void failCore(Long diaryId, String title, String content) {
        markIfCurrent(diaryId, title, content, DiaryAnalysisEntity::failCore);
    }

    @Override
    @Transactional
    public boolean applySummary(Long diaryId, String title, String content, String summary,
                                List<AiAnalysisDTO.Track> tracks) {

        if (!isCurrent(diaryId, title, content)) {
            return false;
        }

        // 재분석이면 이전 추천곡을 지우고 새로 저장함 (일기 내용이 바뀌면 추천도 바뀌어야 하므로)
        diaryMusicRepository.deleteByDiaryId(diaryId);
        diaryMusicRepository.saveAll(toMusicEntities(diaryId, tracks));

        diaryAnalysisRepository.findById(diaryId).orElseThrow().applySummary(summary);

        log.info("요약·음악 저장 완료 (diaryId: {}, {}곡)", diaryId, tracks == null ? 0 : tracks.size());

        return true;
    }

    @Override
    @Transactional
    public void failSummary(Long diaryId, String title, String content) {
        markIfCurrent(diaryId, title, content, DiaryAnalysisEntity::failSummary);
    }

    @Override
    @Transactional
    public boolean applyEmbedding(Long diaryId, String title, String content, String embeddingVersion) {

        if (!isCurrent(diaryId, title, content)) {
            return false;
        }

        diaryAnalysisRepository.findById(diaryId).orElseThrow().applyEmbedding(embeddingVersion);

        log.info("임베딩 상태 반영 완료 (diaryId: {})", diaryId);

        return true;
    }

    @Override
    @Transactional
    public void failEmbedding(Long diaryId, String title, String content) {
        markIfCurrent(diaryId, title, content, DiaryAnalysisEntity::failEmbedding);
    }

    // 분석을 시작한 뒤 일기가 삭제되었거나 제목·본문이 바뀌었으면 이 결과는 버림
    private boolean isCurrent(Long diaryId, String title, String content) {
        boolean current = diaryRepository.findById(diaryId)
                .filter(DiaryEntity::isPublished)
                .filter(diary -> diary.hasSameText(title, content))
                .isPresent();

        if (!current) {
            log.info("분석하는 동안 일기가 바뀌었거나 삭제되어 결과를 저장하지 않음 (diaryId: {})", diaryId);
        }

        return current;
    }

    private void markIfCurrent(Long diaryId, String title, String content, Consumer<DiaryAnalysisEntity> mark) {
        if (isCurrent(diaryId, title, content)) {
            diaryAnalysisRepository.findById(diaryId).ifPresent(mark);
        }
    }

    private List<DiaryMusicEntity> toMusicEntities(Long diaryId, List<AiAnalysisDTO.Track> tracks) {
        List<DiaryMusicEntity> entities = new ArrayList<>();

        if (tracks == null) {
            return entities;
        }

        for (AiAnalysisDTO.Track track : tracks) {
            // 필수 값이 빠진 곡은 건너뜀 (DB NOT NULL)
            if (track.trackId() == null || track.trackName() == null || track.artistName() == null
                    || track.spotifyUrl() == null) {
                log.warn("필수 값이 없는 추천곡을 건너뜀 (diaryId: {})", diaryId);
                continue;
            }

            entities.add(DiaryMusicEntity.builder()
                    .diaryId(diaryId)
                    .trackOrder((short) entities.size())
                    .trackId(track.trackId())
                    .trackName(track.trackName())
                    .artistName(track.artistName())
                    .albumImageUrl(track.albumImageUrl())
                    .spotifyUrl(track.spotifyUrl())
                    .build());
        }

        return entities;
    }
}
