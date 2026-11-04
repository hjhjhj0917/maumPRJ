package com.example.maum.service.impl;

import com.example.maum.auth.UserStatus;
import com.example.maum.diary.DiaryAnalysisRequestedEvent;
import com.example.maum.diary.DiaryStatus;
import com.example.maum.diary.EmotionCatalog;
import com.example.maum.dto.DiaryDTO;
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
import com.example.maum.service.IGcsService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.test.util.ReflectionTestUtils;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

// DB·FastAPI 없이 DiaryService 의 일기 생명주기 판단(임시저장/발행/수정 시 재분석 여부/삭제/소유자 확인)만 확인함.
// 저장소는 Mockito 가짜로 대체하고, 쿼리 자체는 DiaryDbIntegrationTest 가 실제 DB로 확인함
class DiaryServiceTest {

    private static final long USER_ID = 1L;
    private static final long DIARY_ID = 10L;

    DiaryRepository diaryRepository = mock(DiaryRepository.class);
    DiaryAnalysisRepository diaryAnalysisRepository = mock(DiaryAnalysisRepository.class);
    DiaryEmotionScoreRepository diaryEmotionScoreRepository = mock(DiaryEmotionScoreRepository.class);
    DiaryMusicRepository diaryMusicRepository = mock(DiaryMusicRepository.class);
    DiaryImageRepository diaryImageRepository = mock(DiaryImageRepository.class);
    SyncOutboxRepository syncOutboxRepository = mock(SyncOutboxRepository.class);
    UserRepository userRepository = mock(UserRepository.class);
    ApplicationEventPublisher eventPublisher = mock(ApplicationEventPublisher.class);

    UUID userPublicId = UUID.randomUUID();
    DiaryService service;

    @BeforeEach
    void setUp() {
        service = new DiaryService(diaryRepository, diaryAnalysisRepository, diaryEmotionScoreRepository,
                diaryMusicRepository, diaryImageRepository, syncOutboxRepository, userRepository,
                mock(IGcsService.class), mock(EmotionCatalog.class), eventPublisher);

        UserEntity user = UserEntity.builder().id(USER_ID).publicId(userPublicId).email("u@example.com")
                .role("USER").status(UserStatus.ACTIVE).build();
        when(userRepository.findByPublicId(userPublicId)).thenReturn(Optional.of(user));

        // 새 행 저장 시 DB 가 PK 를 채워 주는 동작을 흉내 냄
        when(diaryRepository.save(any(DiaryEntity.class))).thenAnswer(inv -> {
            DiaryEntity saved = inv.getArgument(0);
            if (saved.getId() == null) {
                ReflectionTestUtils.setField(saved, "id", DIARY_ID);
            }
            return saved;
        });
    }

    private DiaryEntity storedDiary(DiaryStatus status) {
        DiaryEntity diary = DiaryEntity.builder().id(DIARY_ID).publicId(UUID.randomUUID()).userId(USER_ID)
                .diaryDate(LocalDate.parse("2026-11-01")).title("제목").content("본문").status(DiaryStatus.DRAFT).build();

        if (status == DiaryStatus.PUBLISHED) {
            diary.publish(java.time.Instant.now());
        }

        when(diaryRepository.findByPublicIdAndUserId(diary.getPublicId(), USER_ID)).thenReturn(Optional.of(diary));

        return diary;
    }

    private DiaryDTO.DiaryDTOBuilder request() {
        return DiaryDTO.builder().userPublicId(userPublicId.toString());
    }

    @Test
    void 새_일기를_발행하면_분석_대기_행을_만들고_분석_이벤트를_보낸다() throws Exception {
        String publicId = service.diaryInsert(request().title("제목").content("본문").diaryDate("2026-11-01").build());

        ArgumentCaptor<DiaryEntity> diaryCaptor = ArgumentCaptor.forClass(DiaryEntity.class);
        verify(diaryRepository).save(diaryCaptor.capture());
        assertEquals(DiaryStatus.PUBLISHED, diaryCaptor.getValue().getStatus());
        assertEquals(publicId, diaryCaptor.getValue().getPublicId().toString());

        ArgumentCaptor<DiaryAnalysisEntity> analysisCaptor = ArgumentCaptor.forClass(DiaryAnalysisEntity.class);
        verify(diaryAnalysisRepository).save(analysisCaptor.capture());
        assertEquals(DIARY_ID, analysisCaptor.getValue().getDiaryId());

        verify(eventPublisher).publishEvent(new DiaryAnalysisRequestedEvent(DIARY_ID));
    }

    @Test
    void 임시저장본을_발행하면_같은_일기가_발행_상태가_된다() throws Exception {
        DiaryEntity draft = storedDiary(DiaryStatus.DRAFT);

        service.diaryInsert(request().publicId(draft.getPublicId().toString())
                .title("완성된 제목").content("완성된 본문").diaryDate("2026-11-02").build());

        assertEquals(DiaryStatus.PUBLISHED, draft.getStatus());
        assertEquals("완성된 제목", draft.getTitle());
        assertEquals(LocalDate.parse("2026-11-02"), draft.getDiaryDate());
        verify(eventPublisher).publishEvent(new DiaryAnalysisRequestedEvent(DIARY_ID));
    }

    @Test
    void 제목이나_본문이_비어_있으면_발행하지_않는다() {
        assertThrows(IllegalArgumentException.class,
                () -> service.diaryInsert(request().title(" ").content("본문").diaryDate("2026-11-01").build()));
        assertThrows(IllegalArgumentException.class,
                () -> service.diaryInsert(request().title("제목").content("").diaryDate("2026-11-01").build()));

        verify(diaryRepository, never()).save(any());
    }

    @Test
    void 이미_발행된_일기를_다시_발행하거나_임시저장할_수_없다() {
        DiaryEntity published = storedDiary(DiaryStatus.PUBLISHED);
        String publicId = published.getPublicId().toString();

        assertThrows(IllegalArgumentException.class, () -> service.diaryInsert(
                request().publicId(publicId).title("제목").content("본문").diaryDate("2026-11-01").build()));
        assertThrows(IllegalArgumentException.class, () -> service.draftSave(
                request().publicId(publicId).title("제목").content("본문").diaryDate("2026-11-01").build()));
    }

    @Test
    void 임시저장은_분석을_만들지_않고_빈_본문도_허용한다() throws Exception {
        service.draftSave(request().title("").content("").diaryDate("2026-11-01").build());

        ArgumentCaptor<DiaryEntity> captor = ArgumentCaptor.forClass(DiaryEntity.class);
        verify(diaryRepository).save(captor.capture());
        assertEquals(DiaryStatus.DRAFT, captor.getValue().getStatus());
        verify(diaryAnalysisRepository, never()).save(any());
        verify(eventPublisher, never()).publishEvent(any(Object.class));
    }

    @Test
    void 제목과_본문이_그대로면_재분석하지_않는다() throws Exception {
        DiaryEntity published = storedDiary(DiaryStatus.PUBLISHED);

        MsgDTO res = service.diaryUpdate(request().publicId(published.getPublicId().toString())
                .title("제목").content("본문").build());

        assertEquals(1, res.result());
        verify(diaryEmotionScoreRepository, never()).deleteByDiaryId(any());
        verify(eventPublisher, never()).publishEvent(any(Object.class));
    }

    @Test
    void 본문이_바뀌면_이전_결과를_지우고_다시_분석한다() throws Exception {
        DiaryEntity published = storedDiary(DiaryStatus.PUBLISHED);
        DiaryAnalysisEntity analysis = DiaryAnalysisEntity.pending(DIARY_ID, true);
        analysis.applySummary("옛 요약");
        when(diaryAnalysisRepository.findById(DIARY_ID)).thenReturn(Optional.of(analysis));

        service.diaryUpdate(request().publicId(published.getPublicId().toString())
                .title("제목").content("바뀐 본문").build());

        assertEquals("바뀐 본문", published.getContent());
        verify(diaryEmotionScoreRepository).deleteByDiaryId(DIARY_ID);
        verify(diaryMusicRepository).deleteByDiaryId(DIARY_ID);
        assertEquals(null, analysis.getSummary());
        verify(eventPublisher).publishEvent(new DiaryAnalysisRequestedEvent(DIARY_ID));
    }

    @Test
    void 남의_일기나_없는_일기는_같은_오류로_거절한다() {
        UUID someoneElsesDiary = UUID.randomUUID();
        when(diaryRepository.findByPublicIdAndUserId(someoneElsesDiary, USER_ID)).thenReturn(Optional.empty());

        IllegalArgumentException notFound = assertThrows(IllegalArgumentException.class, () -> service.diaryUpdate(
                request().publicId(someoneElsesDiary.toString()).title("제목").content("본문").build()));
        IllegalArgumentException malformed = assertThrows(IllegalArgumentException.class, () -> service.diaryDelete(
                request().publicId("not-a-uuid").build()));

        assertEquals("존재하지 않는 일기입니다.", notFound.getMessage());
        assertTrue(malformed.getMessage().contains("올바르지"));
    }

    @Test
    void 일기를_지우면_벡터와_이미지_삭제를_outbox_에_예약한다() throws Exception {
        DiaryEntity published = storedDiary(DiaryStatus.PUBLISHED);
        when(diaryImageRepository.findByDiaryIdOrderBySortOrderAsc(DIARY_ID)).thenReturn(List.of(
                DiaryImageEntity.builder().diaryId(DIARY_ID).sortOrder((short) 0).storageKey("diary/x/a.png").build()));

        service.diaryDelete(request().publicId(published.getPublicId().toString()).build());

        @SuppressWarnings("unchecked")
        ArgumentCaptor<Iterable<SyncOutboxEntity>> captor = ArgumentCaptor.forClass(Iterable.class);
        verify(syncOutboxRepository).saveAll(captor.capture());

        List<String> rows = new java.util.ArrayList<>();
        captor.getValue().forEach(row -> rows.add(row.getTarget() + ":" + row.getRefKey()));
        assertEquals(List.of("MONGO:" + published.getPublicId(), "STORAGE:diary/x/a.png"), rows);
        verify(diaryRepository).delete(published);
    }

    @Test
    void 날씨_값이_허용_범위를_벗어나면_거절한다() {
        assertThrows(IllegalArgumentException.class, () -> service.draftSave(
                request().title("t").content("c").diaryDate("2026-11-01").weatherCondition("HAIL").build()));
        assertThrows(IllegalArgumentException.class, () -> service.draftSave(
                request().title("t").content("c").diaryDate("2026-11-01")
                        .temperatureC(java.math.BigDecimal.valueOf(99)).build()));
    }
}
