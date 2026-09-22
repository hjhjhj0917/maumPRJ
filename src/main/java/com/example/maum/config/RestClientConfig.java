package com.example.maum.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.client.SimpleClientHttpRequestFactory;
import org.springframework.web.client.RestClient;

@Configuration
public class RestClientConfig {

    /*
    파이썬(FastAPI) AI 서버 호출용 RestClient - 모델 추론에 시간이 걸릴 수 있어
    읽기 타임아웃을 길게(120초) 잡음
    */
    // ★ 즐겨찾기 이후 추가/수정
    @Bean
    public RestClient pythonApiRestClient() {
        SimpleClientHttpRequestFactory factory = new SimpleClientHttpRequestFactory();
        factory.setConnectTimeout(10000);
        factory.setReadTimeout(120000);

        return RestClient.builder()
                .requestFactory(factory)
                .build();
    }
}
