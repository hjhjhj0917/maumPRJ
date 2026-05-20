package com.example.maum.service.impl;

import com.example.maum.dto.UserInfoDTO;
import com.example.maum.service.IJwtTokenService;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.ResponseCookie;
import org.springframework.security.oauth2.jose.jws.MacAlgorithm;
import org.springframework.security.oauth2.jwt.*;
import org.springframework.stereotype.Service;

import java.time.Instant;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

@Service
@Slf4j
@RequiredArgsConstructor
public class JwtTokenService implements IJwtTokenService {

    private static final String CLAIM_USERNAME = "username";
    private static final String CLAIM_ROLES = "roles";
    private static final String CLAIM_TYPE = "type";
    private static final String TYPE_ACCESS = "access";
    private static final String TYPE_REFRESH = "refresh";
    private static final String CLAIM_USERID = "userId";

    private final JwtEncoder jwtEncoder;
    private final JwtDecoder jwtDecoder;
    private final RedisService redisService;

    @Value("${secure.jwt.token.creator}")
    private String issuer;

    @Value("${secure.jwt.token.access.valid.time}")
    private long accessTtlSec;

    @Value("${secure.jwt.token.refresh.valid.time}")
    private long refreshTtlSec;

    @Value("${secure.jwt.token.access.name}")
    private String accessCookie;

    @Value("${secure.jwt.token.refresh.name}")
    private String refreshCookie;

    private String encode(UserInfoDTO user, long ttlSec, String type) {
        Instant now = Instant.now();
        List<String> roles = splitRoles(user.roles());

        JwtClaimsSet claims = JwtClaimsSet.builder()
                .issuer(issuer)
                .issuedAt(now)
                .expiresAt(now.plusSeconds(ttlSec))
                .subject(user.userNo())
                .claim(CLAIM_USERNAME, user.userName())
                .claim(CLAIM_TYPE, type)
                .claim(CLAIM_ROLES, roles)
                .build();

        JwsHeader jwsHeader = JwsHeader.with(MacAlgorithm.HS256).build();

        return jwtEncoder.encode(JwtEncoderParameters.from(jwsHeader, claims)).getTokenValue();
    }


    private static List<String> splitRoles(String roles) {
        if (roles == null || roles.isBlank()) return List.of("USER");
        return Arrays.stream(roles.split(","))
                .map(String::trim)
                .filter(s -> !s.isEmpty())
                .collect(Collectors.toList());
    }


    /*
    Access Token 생성
    */
    @Override
    public String generateAccessToken(UserInfoDTO user) {
        return encode(user, accessTtlSec, TYPE_ACCESS);
    }


    /*
    Refresh Token 생성
    */
    @Override
    public String generateRefreshToken(UserInfoDTO user) {
        return encode(user, refreshTtlSec, TYPE_REFRESH);
    }


    @Override
    public int reissueTokens(String refreshToken, HttpServletResponse response) throws Exception {

        int res = 0;

        try {
            Jwt jwt = jwtDecoder.decode(refreshToken);
            String type = jwt.getClaimAsString(CLAIM_TYPE);

            if (TYPE_REFRESH.equals(type)) {
                String userNo = jwt.getSubject();
                String redisKey = "RT:" + userNo;
                String savedToken = redisService.getValues(redisKey);

                if (savedToken != null && savedToken.equals(refreshToken)) {

                    UserInfoDTO user = UserInfoDTO.builder()
                            .userNo(userNo)
                            .userId(jwt.getClaimAsString(CLAIM_USERID))
                            .userName(jwt.getClaimAsString(CLAIM_USERNAME))
                            .roles(String.join(",", jwt.getClaimAsStringList(CLAIM_ROLES)))
                            .build();

                    issueTokens(user, response);

                    res = 1;
                } else {
                    log.warn("Redis에 저장된 토큰과 일치하지 않거나 만료되었습니다.");
                }
            } else {
                log.warn("유효하지 않은 토큰 타입입니다.");
            }

        } catch (Exception e) {
            log.error("Token Reissue Error: {}", e.getMessage());
            res = 0;
        }

        return res;
    }


    /*
    Access/Refresh Token 각각 HttpOnly 쿠키로 저장
    */
    @Override
    public void writeTokenAsCookies(HttpServletResponse res, String accessToken, String refreshToken) {

        ResponseCookie at = ResponseCookie.from(accessCookie, accessToken)
                .httpOnly(true)
                .secure(false) // 개발중에만 false 이후에는 true로 설정 바꾸기
                .path("/")
                .sameSite("Lax")
                .maxAge(accessTtlSec)
                .build();

        ResponseCookie rt = ResponseCookie.from(refreshCookie, refreshToken)
                .httpOnly(true)
                .secure(false) // 개발중에만 false 이후에는 true로 설정 바꾸기
                .path("/")
                .sameSite("Lax")
                .maxAge(refreshTtlSec)
                .build();

        ResponseCookie loginFlag = ResponseCookie.from("isLoggedIn", "true")
                .httpOnly(false)
                .secure(false) // 개발중에만 false 이후에는 true로 설정 바꾸기
                .path("/")
                .sameSite("Lax")
                .maxAge(accessTtlSec)
                .build();

        res.addHeader("Set-Cookie", at.toString());
        res.addHeader("Set-Cookie", rt.toString());
        res.addHeader("Set-Cookie", loginFlag.toString());
    }

    @Override
    public void issueTokens(UserInfoDTO user, HttpServletResponse response) {

        String accessToken = generateAccessToken(user);
        String refreshToken = generateRefreshToken(user);

        long rtExpirationMillis = refreshTtlSec * 1000L;
        redisService.setValues("RT:" + user.userNo(), refreshToken, rtExpirationMillis);

        writeTokenAsCookies(response, accessToken, refreshToken);
    }
}
