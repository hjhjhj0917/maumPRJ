package com.example.maum.service.impl;

import com.example.maum.repository.DiaryMusicRepository;
import com.example.maum.repository.DiaryRepository;
import com.example.maum.repository.entity.DiaryMusicEntity;
import com.example.maum.service.IDiaryWriteService;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentMatchers;
import org.mockito.Mockito;
import org.springframework.aop.support.AopUtils;
import org.springframework.context.annotation.AnnotationConfigApplicationContext;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.transaction.TransactionDefinition;
import org.springframework.transaction.annotation.EnableTransactionManagement;
import org.springframework.transaction.support.AbstractPlatformTransactionManager;
import org.springframework.transaction.support.DefaultTransactionStatus;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

// DiaryWriteService의 @Transactional이 실제로 트랜잭션을 열고 닫는지(커밋/롤백) 확인하는 테스트.
// DB 없이 돌리기 위해 저장소는 Mockito 가짜로, 트랜잭션 매니저는 시작/커밋/롤백 시점을 기록하는 가짜로 대체하고,
// 스프링 AOP 프록시(@EnableTransactionManagement)는 실제 것을 사용함
class DiaryWriteServiceTest {

    // 트랜잭션 이벤트와 저장소 호출 순서를 한 줄로 이어서 기록 (순서 검증용)
    static final List<String> EVENTS = new ArrayList<>();
    static final List<Integer> SAVED_ORDERS = new ArrayList<>();
    static volatile boolean failOnSecondTrack = false;

    static class RecordingTransactionManager extends AbstractPlatformTransactionManager {
        @Override
        protected Object doGetTransaction() {
            return new Object();
        }

        @Override
        protected void doBegin(Object transaction, TransactionDefinition definition) {
            EVENTS.add("BEGIN");
        }

        @Override
        protected void doCommit(DefaultTransactionStatus status) {
            EVENTS.add("COMMIT");
        }

        @Override
        protected void doRollback(DefaultTransactionStatus status) {
            EVENTS.add("ROLLBACK");
        }
    }

    @Configuration
    @EnableTransactionManagement
    static class TestConfig {

        @Bean
        RecordingTransactionManager transactionManager() {
            return new RecordingTransactionManager();
        }

        @Bean
        DiaryRepository diaryRepository() {
            return fakeDiaryRepository();
        }

        @Bean
        DiaryMusicRepository diaryMusicRepository() {
            return fakeDiaryMusicRepository();
        }

        @Bean
        IDiaryWriteService diaryWriteService(DiaryRepository diaryRepository, DiaryMusicRepository diaryMusicRepository) {
            return new DiaryWriteService(diaryRepository, diaryMusicRepository);
        }
    }

    static DiaryRepository fakeDiaryRepository() {
        DiaryRepository repo = Mockito.mock(DiaryRepository.class);

        Mockito.when(repo.updateAnalysisResultDirectly(
                ArgumentMatchers.any(), ArgumentMatchers.any(), ArgumentMatchers.any(), ArgumentMatchers.any(),
                ArgumentMatchers.any(), ArgumentMatchers.any(), ArgumentMatchers.any()
        )).thenAnswer(inv -> {
            EVENTS.add("UPDATE_ANALYSIS");
            return 1;
        });

        Mockito.when(repo.updateDiaryDirectly(ArgumentMatchers.any(), ArgumentMatchers.any(), ArgumentMatchers.any()))
                .thenAnswer(inv -> {
                    EVENTS.add("UPDATE_CONTENT");
                    return 1;
                });

        return repo;
    }

    static DiaryMusicRepository fakeDiaryMusicRepository() {
        DiaryMusicRepository repo = Mockito.mock(DiaryMusicRepository.class);

        Mockito.doAnswer(inv -> {
            EVENTS.add("DELETE_MUSIC");
            return null;
        }).when(repo).deleteByDiaryNo(ArgumentMatchers.any());

        Mockito.when(repo.save(ArgumentMatchers.any(DiaryMusicEntity.class))).thenAnswer(inv -> {
            DiaryMusicEntity saved = inv.getArgument(0);
            if (failOnSecondTrack && saved.getTrackOrder() == 1) {
                throw new IllegalStateException("두 번째 곡 저장 실패(테스트용)");
            }
            EVENTS.add("INSERT_MUSIC");
            SAVED_ORDERS.add(saved.getTrackOrder());
            return saved;
        });

        return repo;
    }

    static AnnotationConfigApplicationContext context;
    static IDiaryWriteService service;

    @BeforeAll
    static void startContext() {
        context = new AnnotationConfigApplicationContext(TestConfig.class);
        service = context.getBean(IDiaryWriteService.class);
    }

    @AfterAll
    static void closeContext() {
        context.close();
    }

    @BeforeEach
    void reset() {
        EVENTS.clear();
        SAVED_ORDERS.clear();
        failOnSecondTrack = false;
    }

    private static List<Map<String, Object>> tracks(int count) {
        List<Map<String, Object>> list = new ArrayList<>();
        for (int i = 0; i < count; i++) {
            list.add(Map.of(
                    "trackId", "id" + i,
                    "trackName", "곡" + i,
                    "artistName", "아티스트" + i,
                    "albumImageUrl", "https://img/" + i,
                    "spotifyUrl", "https://spotify/" + i
            ));
        }
        return list;
    }

    @Test
    void 빈으로_주입받은_서비스는_AOP_프록시다() {
        assertTrue(AopUtils.isAopProxy(service), "@Transactional이 있는 서비스는 프록시로 등록되어야 함");
    }

    @Test
    void 분석결과와_음악저장이_하나의_트랜잭션으로_커밋된다() {
        service.applyAnalysisResult(15, "요약", "힘듦/지침", "#1E3A8A", 1, new BigDecimal("0.8734"), 1, tracks(3));

        assertEquals(
                List.of("BEGIN", "UPDATE_ANALYSIS", "DELETE_MUSIC", "INSERT_MUSIC", "INSERT_MUSIC", "INSERT_MUSIC", "COMMIT"),
                EVENTS
        );
        assertEquals(List.of(0, 1, 2), SAVED_ORDERS, "추천곡 순번은 0부터 차례로 붙어야 함");
    }

    @Test
    void 추천곡이_비어있으면_기존_추천곡만_지우고_커밋한다() {
        service.applyAnalysisResult(15, "요약", "행복", "#FFD700", 0, new BigDecimal("0.1"), 0, List.of());

        assertEquals(List.of("BEGIN", "UPDATE_ANALYSIS", "DELETE_MUSIC", "COMMIT"), EVENTS);
    }

    @Test
    void 추천곡이_null이어도_오류없이_커밋한다() {
        service.applyAnalysisResult(15, "요약", "행복", "#FFD700", 0, new BigDecimal("0.1"), 0, null);

        assertEquals(List.of("BEGIN", "UPDATE_ANALYSIS", "DELETE_MUSIC", "COMMIT"), EVENTS);
    }

    @Test
    void 음악저장_중_실패하면_롤백되고_커밋되지_않는다() {
        failOnSecondTrack = true;

        assertThrows(IllegalStateException.class, () ->
                service.applyAnalysisResult(15, "요약", "힘듦/지침", "#1E3A8A", 1, new BigDecimal("0.8"), 1, tracks(3)));

        assertTrue(EVENTS.contains("ROLLBACK"), "실패하면 롤백되어야 함: " + EVENTS);
        assertFalse(EVENTS.contains("COMMIT"), "실패했는데 커밋되면 안 됨: " + EVENTS);
        assertEquals("BEGIN", EVENTS.get(0));
        assertEquals("ROLLBACK", EVENTS.get(EVENTS.size() - 1));
    }

    @Test
    void 일기_내용_수정도_하나의_트랜잭션으로_실행된다() {
        service.updateDiaryContent(15, "제목", "내용");

        assertEquals(List.of("BEGIN", "UPDATE_CONTENT", "COMMIT"), EVENTS);
    }

    @Test
    void 프록시를_거치지_않고_직접_호출하면_트랜잭션이_시작되지_않는다() {
        // 같은 클래스 안에서 this.로 호출하는 것(자기 호출)과 같은 상황 — 분리한 이유를 보여주는 대조 테스트
        DiaryWriteService raw = new DiaryWriteService(fakeDiaryRepository(), fakeDiaryMusicRepository());

        raw.applyAnalysisResult(15, "요약", "행복", "#FFD700", 0, new BigDecimal("0.1"), 0, tracks(1));

        assertFalse(EVENTS.contains("BEGIN"), "프록시 없이 호출하면 BEGIN이 없어야 함: " + EVENTS);
        assertFalse(EVENTS.contains("COMMIT"));
        assertEquals(List.of("UPDATE_ANALYSIS", "DELETE_MUSIC", "INSERT_MUSIC"), EVENTS);
    }
}
