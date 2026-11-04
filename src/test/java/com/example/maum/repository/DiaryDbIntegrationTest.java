package com.example.maum.repository;

import com.example.maum.auth.AuthProvider;
import com.example.maum.auth.UserStatus;
import com.example.maum.diary.AnalysisStatus;
import com.example.maum.diary.DiaryStatus;
import com.example.maum.diary.EmotionCatalog;
import com.example.maum.dto.AiAnalysisDTO;
import com.example.maum.dto.DiaryAnalysisInputDTO;
import com.example.maum.repository.entity.DiaryAnalysisEntity;
import com.example.maum.repository.entity.DiaryEntity;
import com.example.maum.repository.entity.UserAuthProviderEntity;
import com.example.maum.repository.entity.UserEntity;
import com.example.maum.repository.projection.DiaryListProjection;
import com.example.maum.service.IDiaryWriteService;
import com.example.maum.service.impl.DiaryWriteService;
import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import org.flywaydb.core.Flyway;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.condition.EnabledIf;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Import;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.jpa.repository.config.EnableJpaRepositories;
import org.springframework.jdbc.datasource.DriverManagerDataSource;
import org.springframework.orm.jpa.JpaTransactionManager;
import org.springframework.orm.jpa.LocalContainerEntityManagerFactoryBean;
import org.springframework.orm.jpa.vendor.HibernateJpaVendorAdapter;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import org.springframework.transaction.PlatformTransactionManager;
import org.springframework.transaction.annotation.EnableTransactionManagement;
import org.springframework.transaction.annotation.Transactional;

import javax.sql.DataSource;
import java.math.BigDecimal;
import java.sql.Connection;
import java.sql.DriverManager;
import java.time.Instant;
import java.time.LocalDate;
import java.time.temporal.ChronoUnit;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

// 실제 PostgreSQL(docker-compose.dev.yml 의 maum_test DB)에 Flyway 로 스키마를 만들고 JPQL·제약·저장 로직을 확인함.
// DB 가 떠 있지 않으면 건너뜀. 각 테스트는 트랜잭션 안에서 실행되고 끝나면 롤백됨
//   준비: docker compose -f docker-compose.dev.yml up -d  →  CREATE DATABASE maum_test
@EnabledIf("dbAvailable")
@ExtendWith(SpringExtension.class)
@ContextConfiguration(classes = DiaryDbIntegrationTest.Config.class)
@Transactional
class DiaryDbIntegrationTest {

    private static final String URL = "jdbc:postgresql://localhost:5432/maum_test";
    private static final String USER = "maum";
    private static final String PASSWORD = "maum_local_only";

    static boolean dbAvailable() {
        try (Connection ignored = DriverManager.getConnection(URL, USER, PASSWORD)) {
            return true;
        } catch (Exception e) {
            return false;
        }
    }

    @Configuration
    @EnableTransactionManagement
    @EnableJpaRepositories(basePackages = "com.example.maum.repository")
    @Import({DiaryWriteService.class, EmotionCatalog.class})
    static class Config {

        @Bean
        DataSource dataSource() {
            DriverManagerDataSource dataSource = new DriverManagerDataSource(URL, USER, PASSWORD);
            dataSource.setDriverClassName("org.postgresql.Driver");

            // 테스트 DB 는 매번 비우고 V1, V2 를 다시 적용함 (maum_test 전용)
            Flyway flyway = Flyway.configure()
                    .dataSource(dataSource)
                    .locations("classpath:db/migration")
                    .cleanDisabled(false)
                    .load();
            flyway.clean();
            flyway.migrate();

            return dataSource;
        }

        @Bean
        LocalContainerEntityManagerFactoryBean entityManagerFactory(DataSource dataSource) {
            LocalContainerEntityManagerFactoryBean factory = new LocalContainerEntityManagerFactoryBean();
            factory.setDataSource(dataSource);
            factory.setPackagesToScan("com.example.maum.repository.entity");
            factory.setJpaVendorAdapter(new HibernateJpaVendorAdapter());

            Map<String, Object> properties = new HashMap<>();
            properties.put("hibernate.physical_naming_strategy",
                    "org.hibernate.boot.model.naming.PhysicalNamingStrategyStandardImpl");
            factory.setJpaPropertyMap(properties);

            return factory;
        }

        @Bean
        PlatformTransactionManager transactionManager(jakarta.persistence.EntityManagerFactory entityManagerFactory) {
            return new JpaTransactionManager(entityManagerFactory);
        }
    }

    @PersistenceContext
    EntityManager em;
    @Autowired
    UserRepository userRepository;
    @Autowired
    UserAuthProviderRepository userAuthProviderRepository;
    @Autowired
    DiaryRepository diaryRepository;
    @Autowired
    DiaryAnalysisRepository diaryAnalysisRepository;
    @Autowired
    DiaryEmotionScoreRepository diaryEmotionScoreRepository;
    @Autowired
    DiaryMusicRepository diaryMusicRepository;
    @Autowired
    EmotionCatalog emotionCatalog;
    @Autowired
    IDiaryWriteService diaryWriteService;

    private UserEntity newUser(String email, UserStatus status) {
        return userRepository.save(UserEntity.builder()
                .publicId(UUID.randomUUID())
                .email(email)
                .nickname("마음이")
                .birthYear(1999)
                .emailVerifiedAt(Instant.now())
                .role("USER")
                .status(status)
                .build());
    }

    private DiaryEntity newDiary(UserEntity user, String date, String title, DiaryStatus status) {
        DiaryEntity diary = DiaryEntity.builder()
                .publicId(UUID.randomUUID())
                .userId(user.getId())
                .diaryDate(LocalDate.parse(date))
                .title(title)
                .content("오늘은 평범한 하루였다.")
                .status(DiaryStatus.DRAFT)
                .build();

        if (status == DiaryStatus.PUBLISHED) {
            diary.publish(Instant.now());
        }

        return diaryRepository.save(diary);
    }

    private Map<String, Float> allScores(String topLabel, float topScore) {
        Map<String, Float> scores = new HashMap<>();

        emotionRepositoryNames().forEach(name -> scores.put(name, 0.01f));
        scores.put(topLabel, topScore);

        return scores;
    }

    private List<String> emotionRepositoryNames() {
        return em.createQuery("SELECT e.nameKo FROM EmotionEntity e", String.class).getResultList();
    }

    private void flushAndClear() {
        em.flush();
        em.clear();
    }

    @Test
    void 시드_감정은_44개이고_대분류_색상을_읽는다() {
        assertEquals(44, emotionCatalog.size());
        assertEquals("#FFD700", emotionCatalog.colorOf("JOY"));
        assertEquals("슬픔", emotionCatalog.findByName("슬픔").orElseThrow().getNameKo());
    }

    @Test
    void 사용자_쿼리_3개가_실제_DB에서_동작한다() {
        UserEntity user = newUser("a@example.com", UserStatus.ACTIVE);
        userAuthProviderRepository.save(UserAuthProviderEntity.builder()
                .userId(user.getId()).provider(AuthProvider.LOCAL)
                .providerUserId(user.getPublicId().toString()).passwordHash("HASH").build());
        UserEntity expired = newUser("b@example.com", UserStatus.ACTIVE);
        expired.withdraw(Instant.now().minus(15, ChronoUnit.DAYS), Instant.now().minus(1, ChronoUnit.DAYS));
        flushAndClear();

        assertEquals("HASH", userAuthProviderRepository
                .findByPublicIdAndProvider(user.getPublicId(), AuthProvider.LOCAL).orElseThrow().getPasswordHash());

        assertEquals(1, userAuthProviderRepository.updateLastLoginAt(user.getPublicId(), AuthProvider.LOCAL, Instant.now()));
        assertTrue(userAuthProviderRepository.findByUserIdAndProvider(user.getId(), AuthProvider.LOCAL)
                .orElseThrow().getLastLoginAt() != null);

        assertEquals(1, userRepository.deleteWithdrawnUsersPurgedUntil(Instant.now()));
        assertFalse(userRepository.existsByEmail("b@example.com"));
        assertTrue(userRepository.existsByEmail("a@example.com"));
    }

    @Test
    void 목록_쿼리는_본인의_발행본만_조건에_맞게_돌려준다() {
        UserEntity me = newUser("me@example.com", UserStatus.ACTIVE);
        UserEntity other = newUser("other@example.com", UserStatus.ACTIVE);

        DiaryEntity d1 = newDiary(me, "2026-11-01", "첫 일기", DiaryStatus.PUBLISHED);
        DiaryEntity d2 = newDiary(me, "2026-11-03", "100% 만족", DiaryStatus.PUBLISHED);
        newDiary(me, "2026-10-31", "지난달", DiaryStatus.PUBLISHED);
        newDiary(me, "2026-11-02", "쓰는 중", DiaryStatus.DRAFT);
        newDiary(other, "2026-11-01", "남의 일기", DiaryStatus.PUBLISHED);

        diaryAnalysisRepository.save(DiaryAnalysisEntity.pending(d1.getId(), true));
        flushAndClear();

        // 달력: 해당 월의 발행본만, 날짜 순
        List<DiaryListProjection> monthly = diaryRepository.findMonthly(me.getId(),
                LocalDate.parse("2026-11-01"), LocalDate.parse("2026-11-30"));
        assertEquals(List.of("첫 일기", "100% 만족"), monthly.stream().map(DiaryListProjection::getTitle).toList());

        // 검색: % 를 일반 글자로 찾고, 남의 일기와 임시 저장본은 나오지 않음
        assertEquals(1, diaryRepository.searchByTitle(me.getId(), "100\\%").size());
        assertEquals(0, diaryRepository.searchByTitle(me.getId(), "남의").size());
        assertEquals(1, diaryRepository.searchByTitle(me.getId(), "일기").size());

        // 임시 저장 목록
        assertEquals(List.of("쓰는 중"), diaryRepository.findDrafts(me.getId()).stream()
                .map(DiaryListProjection::getTitle).toList());

        // 최근 목록: 고정 우선 → 날짜 내림차순
        d1 = diaryRepository.findById(d1.getId()).orElseThrow();
        d1.changePinned(true);
        d2 = diaryRepository.findById(d2.getId()).orElseThrow();
        d2.changeFavorite(true);
        flushAndClear();

        List<DiaryListProjection> recent = diaryRepository.findRecent(me.getId(), PageRequest.of(0, 20));
        assertEquals("첫 일기", recent.get(0).getTitle());
        assertEquals(List.of("100% 만족"), diaryRepository.findFavorites(me.getId()).stream()
                .map(DiaryListProjection::getTitle).toList());
    }

    @Test
    void 코어_분석_결과를_저장하면_점수_44개와_대분류가_채워지고_본문이_바뀌면_저장하지_않는다() {
        UserEntity me = newUser("core@example.com", UserStatus.ACTIVE);
        DiaryEntity diary = newDiary(me, "2026-11-01", "슬픈 날", DiaryStatus.PUBLISHED);
        diaryAnalysisRepository.save(DiaryAnalysisEntity.pending(diary.getId(), true));
        flushAndClear();

        var ranked = emotionCatalog.rank(allScores("슬픔", 0.9f));
        assertEquals(44, ranked.size());

        assertTrue(diaryWriteService.applyCore(diary.getId(), diary.getTitle(), diary.getContent(), ranked,
                AnalysisStatus.DONE, 1, new BigDecimal("0.7312"), "test-v1"));
        flushAndClear();

        DiaryAnalysisEntity analysis = diaryAnalysisRepository.findById(diary.getId()).orElseThrow();
        assertEquals(AnalysisStatus.DONE, analysis.getAnalysisStatus());
        assertEquals("SADNESS", analysis.getMainGroupCode());
        assertEquals(1, analysis.getDepLevel());
        assertEquals(44, diaryEmotionScoreRepository.count());

        // 대표 감정은 점수 1위, 상위 10개 조회
        var top = diaryEmotionScoreRepository.findTop(diary.getId(), PageRequest.of(0, 10));
        assertEquals(10, top.size());
        assertEquals("슬픔", emotionCatalog.findById(top.get(0).getEmotionId()).orElseThrow().getNameKo());

        // 재시도로 같은 결과를 다시 저장해도 중복 키 오류 없이 44개 유지
        assertTrue(diaryWriteService.applyCore(diary.getId(), diary.getTitle(), diary.getContent(), ranked,
                AnalysisStatus.DONE, 1, new BigDecimal("0.7312"), "test-v1"));
        flushAndClear();
        assertEquals(44, diaryEmotionScoreRepository.count());

        // 분석하는 동안 본문이 바뀐 경우(오래된 결과)는 버림
        assertFalse(diaryWriteService.applyCore(diary.getId(), diary.getTitle(), "다른 본문", ranked,
                AnalysisStatus.DONE, 0, new BigDecimal("0.1"), "test-v1"));
    }

    @Test
    void 우울_모델_생략_상태와_뚜렷한_감정이_없는_경우의_제약을_통과한다() {
        UserEntity me = newUser("skip@example.com", UserStatus.ACTIVE);
        DiaryEntity diary = newDiary(me, "2026-11-01", "평범한 날", DiaryStatus.PUBLISHED);
        diaryAnalysisRepository.save(DiaryAnalysisEntity.pending(diary.getId(), true));
        flushAndClear();

        // 1위 점수가 0.15 미만이면 무감정으로 분류
        var ranked = emotionCatalog.rank(allScores("행복", 0.1f));
        assertTrue(diaryWriteService.applyCore(diary.getId(), diary.getTitle(), diary.getContent(), ranked,
                AnalysisStatus.DEP_SKIPPED, 0, null, "test-v1"));
        flushAndClear();

        DiaryAnalysisEntity analysis = diaryAnalysisRepository.findById(diary.getId()).orElseThrow();
        assertEquals("NEUTRAL", analysis.getMainGroupCode());
        assertEquals(AnalysisStatus.DEP_SKIPPED, analysis.getAnalysisStatus());
        assertEquals(null, analysis.getDepScore());
    }

    @Test
    void 요약과_추천곡_임베딩_상태를_단계별로_저장하고_재시도_대상을_찾는다() {
        UserEntity me = newUser("stage@example.com", UserStatus.ACTIVE);
        DiaryEntity diary = newDiary(me, "2026-11-01", "단계 확인", DiaryStatus.PUBLISHED);
        diaryAnalysisRepository.save(DiaryAnalysisEntity.pending(diary.getId(), true));
        flushAndClear();

        DiaryAnalysisInputDTO input = diaryWriteService.loadAnalysisInput(diary.getId()).orElseThrow();
        assertEquals(AnalysisStatus.PENDING, input.analysisStatus());
        assertTrue(input.topEmotions().isEmpty());

        // 방금 시작한 건은 5분 이후 구간에 해당하지 않아 재시도 대상이 아님
        Instant now = Instant.now();
        assertTrue(diaryAnalysisRepository.findRetryTargets(now.minus(24, ChronoUnit.HOURS), now.minus(5, ChronoUnit.MINUTES),
                PageRequest.of(0, 20)).isEmpty());
        assertEquals(List.of(diary.getId()), diaryAnalysisRepository.findRetryTargets(
                now.minus(24, ChronoUnit.HOURS), now.plus(1, ChronoUnit.MINUTES), PageRequest.of(0, 20)));

        var tracks = List.of(
                new AiAnalysisDTO.Track("t1", "곡1", "가수1", "http://img/1", "http://spotify/1"),
                new AiAnalysisDTO.Track("t2", "곡2", "가수2", null, "http://spotify/2"),
                new AiAnalysisDTO.Track(null, "필수값 없음", "가수3", null, "http://spotify/3"));

        assertTrue(diaryWriteService.applySummary(diary.getId(), diary.getTitle(), diary.getContent(), "요약문", tracks));
        flushAndClear();

        assertEquals(2, diaryMusicRepository.findByDiaryIdOrderByTrackOrderAsc(diary.getId()).size());
        assertEquals("요약문", diaryAnalysisRepository.findById(diary.getId()).orElseThrow().getSummary());

        assertTrue(diaryWriteService.applyEmbedding(diary.getId(), diary.getTitle(), diary.getContent(), "emb-v1"));
        flushAndClear();

        assertEquals("emb-v1", diaryAnalysisRepository.findById(diary.getId()).orElseThrow().getEmbeddingVersion());

        // 재분석: 이전 추천곡·점수를 한 번에 지움
        assertEquals(2, diaryMusicRepository.deleteByDiaryId(diary.getId()));
    }

    @Test
    void 일기를_지우면_분석_점수_음악이_함께_사라진다() {
        UserEntity me = newUser("del@example.com", UserStatus.ACTIVE);
        DiaryEntity diary = newDiary(me, "2026-11-01", "지울 일기", DiaryStatus.PUBLISHED);
        diaryAnalysisRepository.save(DiaryAnalysisEntity.pending(diary.getId(), true));
        flushAndClear();

        diaryWriteService.applyCore(diary.getId(), diary.getTitle(), diary.getContent(),
                emotionCatalog.rank(allScores("기쁨", 0.8f)), AnalysisStatus.DONE, 0, new BigDecimal("0.1"), "v");
        flushAndClear();

        diaryRepository.delete(diaryRepository.findById(diary.getId()).orElseThrow());
        flushAndClear();

        assertEquals(0, diaryEmotionScoreRepository.count());
        assertEquals(0, diaryAnalysisRepository.count());
    }
}
