package com.example.maum.scheduler;

import com.example.maum.service.IMentalInstService;
import jakarta.annotation.PostConstruct;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.function.client.WebClient;

import java.util.concurrent.CompletableFuture;

@Slf4j
@Component
@RequiredArgsConstructor
public class DataUpdateScheduler {

    private final IMentalInstService mentalInstService;
    private WebClient webClient;

    @Value("${secure.python.api.url}")
    private String pythonApiUrl;

    @PostConstruct
    public void init() {
        this.webClient = WebClient.builder()
                .baseUrl(pythonApiUrl)
                .build();
    }

    /**
     * 작업 1: 데이터 업데이트 지시 (매월 1일 새벽 3시 0분)
     * 역할: 파이썬 서버에 무거운 작업을 던져두고 바로 빠집니다.
     */
    @Scheduled(cron = "0 0 3 1 * ?")
    public void triggerPythonDataUpdate() {
        log.info("{}.triggerPythonDataUpdate Start! 파이썬 서버로 데이터 업데이트 지시", this.getClass().getName());

        webClient.post()
                .uri("/batch/update-data")
                .retrieve()
                .bodyToMono(String.class)
                .subscribe(
                        response -> log.info("월간 배치 작업 지시 성공: {}", response),
                        error -> log.error("배치 작업 지시 중 에러 발생: {}", error.getMessage())
                );
    }

    /**
     * 작업 2: 캐시 초기화 및 리워밍 (매월 1일 새벽 3시 30분)
     * 역할: 파이썬 배치가 완전히 끝났을 시간(30분 뒤)에 기존 지도를 지우고 최신 지도를 적재합니다.
     */
    @Scheduled(cron = "0 30 3 1 * ?")
    public void refreshCacheAfterBatch() {
        log.info("새벽 3시 30분: 파이썬 배치가 완료되었을 것으로 간주하고 캐시 리워밍을 시작합니다.");

        try {
            boolean isCacheCleared = mentalInstService.clearInstitutionsCache();

            if (isCacheCleared) {
                log.info("캐시 무효화 완료. 비동기로 지도 데이터를 다시 적재합니다.");

                CompletableFuture.runAsync(() -> {
                    try {
                        mentalInstService.getAllInstitutions();
                        log.info("지도 데이터 최신화 및 캐시 리워밍 완벽 종료!");
                    } catch (Exception e) {
                        log.error("캐시 리워밍 중 치명적 에러: {}", e.getMessage());
                    }
                });
            }
        } catch (Exception e) {
            log.error("캐시 초기화 중 오류 발생 (리워밍 중단): {}", e.getMessage());
        }
    }
}