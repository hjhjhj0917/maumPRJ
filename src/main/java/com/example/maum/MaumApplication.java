package com.example.maum;

import com.example.maum.service.IMentalInstService;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cache.annotation.EnableCaching;
import org.springframework.context.annotation.Bean;
import org.springframework.data.jpa.repository.config.EnableJpaRepositories;

@EnableJpaRepositories
@EnableCaching
@SpringBootApplication
public class MaumApplication {

    public static void main(String[] args) {
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
