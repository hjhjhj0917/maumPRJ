package com.example.maum;

import com.example.maum.service.IMentalInstService;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cache.annotation.EnableCaching;
import org.springframework.context.annotation.Bean;
import org.springframework.data.jpa.repository.config.EnableJpaRepositories;
import org.springframework.scheduling.annotation.EnableScheduling;

import java.util.TimeZone;

@EnableJpaRepositories
@EnableCaching
@EnableScheduling
@SpringBootApplication
public class MaumApplication {

    public static void main(String[] args) {
        // 배포 컨테이너의 JVM 기본 시간대는 UTC라 LocalDate.now()가 한국 날짜보다 하루 늦을 수 있음 —
        // (예: 오전 9시 이전에 쓴 일기가 "이번 주 리포트"에서 빠짐) 한국 시간으로 고정함
        TimeZone.setDefault(TimeZone.getTimeZone("Asia/Seoul"));
        SpringApplication.run(MaumApplication.class, args);
    }

    @Bean
    public CommandLineRunner warmUp(IMentalInstService mentalInstService) {
        return args -> {
            System.out.println("서버 구동 완료: 지도 데이터 캐시 워밍을 시작합니다...");

            mentalInstService.getAllInstitutions();

            System.out.println("지도 데이터캐시 워밍 완료");
        };
    }

}
