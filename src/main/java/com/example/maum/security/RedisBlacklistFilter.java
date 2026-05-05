package com.example.maum.security;

import com.example.maum.service.impl.RedisService;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.oauth2.server.resource.web.BearerTokenResolver;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;

@Slf4j
@Component
@RequiredArgsConstructor
public class RedisBlacklistFilter extends OncePerRequestFilter {

    private final RedisService redisService;
    private final BearerTokenResolver bearerTokenResolver;

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain) throws ServletException, IOException {

        String accessToken = bearerTokenResolver.resolve(request);

        if (accessToken != null && redisService.hasKey("AT:" + accessToken)) {
            log.warn("블랙리스트에 등록된 로그아웃 토큰 접근: {}", accessToken);
            response.sendError(HttpServletResponse.SC_UNAUTHORIZED, "로그아웃 처리된 사용자입니다.");
            return;
        }

        filterChain.doFilter(request, response);
    }
}
