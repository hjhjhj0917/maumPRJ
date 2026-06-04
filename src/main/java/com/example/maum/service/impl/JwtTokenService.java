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
    private static final String CLAIM_ABS_EXP = "absExp";

    private final JwtEncoder jwtEncoder;
    private final JwtDecoder jwtDecoder;
    private final RedisService redisService;

    @Value("${secure.jwt.token.creator}")
    private String issuer;

    @Value("${secure.jwt.token.access.valid.time}")
    private long accessTtlSec;

    @Value("${secure.jwt.token.refresh.valid.time}")
    private long refreshTtlSec;

    @Value("${secure.jwt.token.absolute.valid.time}")
    private long absoluteTtlSec;

    @Value("${secure.jwt.token.access.name}")
    private String accessCookie;

    @Value("${secure.jwt.token.refresh.name}")
    private String refreshCookie;

    private String encode(UserInfoDTO user, long ttlSec, String type, Instant absExp) {
        Instant now = Instant.now();
        List<String> roles = splitRoles(user.roles());

        JwtClaimsSet.Builder claimsBuilder = JwtClaimsSet.builder()
                .issuer(issuer)
                .issuedAt(now)
                .expiresAt(now.plusSeconds(ttlSec))
                .subject(user.userNo())
                .claim(CLAIM_USERID, user.userId())
                .claim(CLAIM_USERNAME, user.userName())
                .claim(CLAIM_TYPE, type)
                .claim(CLAIM_ROLES, roles);

        if (absExp != null) {
            claimsBuilder.claim(CLAIM_ABS_EXP, absExp.getEpochSecond());
        }

        JwsHeader jwsHeader = JwsHeader.with(MacAlgorithm.HS256).build();

        return jwtEncoder.encode(JwtEncoderParameters.from(jwsHeader, claimsBuilder.build())).getTokenValue();
    }

    private static List<String> splitRoles(String roles) {
        if (roles == null || roles.isBlank()) return List.of("USER");
        return Arrays.stream(roles.split(","))
                .map(String::trim)
                .filter(s -> !s.isEmpty())
                .collect(Collectors.toList());
    }

    @Override
    public String generateAccessToken(UserInfoDTO user) {
        return encode(user, accessTtlSec, TYPE_ACCESS, null);
    }

    @Override
    public String generateRefreshToken(UserInfoDTO user) {
        Instant initialAbsExp = Instant.now().plusSeconds(absoluteTtlSec);
        return encode(user, refreshTtlSec, TYPE_REFRESH, initialAbsExp);
    }

    private String generateRefreshTokenWithAbsExp(UserInfoDTO user, Instant absExp) {
        return encode(user, refreshTtlSec, TYPE_REFRESH, absExp);
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

                    Long absExpEpoch = jwt.getClaim(CLAIM_ABS_EXP);
                    Instant absExp = null;

                    if (absExpEpoch != null) {
                        absExp = Instant.ofEpochSecond(absExpEpoch);
                        if (Instant.now().isAfter(absExp)) {
                            log.warn("절대 만료 시간이 지났습니다. 강제 로그아웃 처리됩니다. (userNo: {})", userNo);
                            redisService.deleteValues(redisKey);
                            return 0;
                        }
                    } else {
                        absExp = Instant.now().plusSeconds(absoluteTtlSec);
                    }

                    UserInfoDTO user = UserInfoDTO.builder()
                            .userNo(userNo)
                            .userId(jwt.getClaimAsString(CLAIM_USERID))
                            .userName(jwt.getClaimAsString(CLAIM_USERNAME))
                            .roles(String.join(",", jwt.getClaimAsStringList(CLAIM_ROLES)))
                            .build();

                    issueTokensWithAbsExp(user, response, absExp);

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

    @Override
    public void writeTokenAsCookies(HttpServletResponse res, String accessToken, String refreshToken) {

        ResponseCookie at = ResponseCookie.from(accessCookie, accessToken)
                .httpOnly(true)
                .secure(true)
                .path("/")
                .sameSite("Lax")
                .maxAge(accessTtlSec)
                .build();

        ResponseCookie rt = ResponseCookie.from(refreshCookie, refreshToken)
                .httpOnly(true)
                .secure(true)
                .path("/")
                .sameSite("Lax")
                .maxAge(refreshTtlSec)
                .build();

        ResponseCookie loginFlag = ResponseCookie.from("isLoggedIn", "true")
                .httpOnly(false)
                .secure(true)
                .path("/")
                .sameSite("Lax")
                .maxAge(refreshTtlSec)
                .build();

        res.addHeader("Set-Cookie", at.toString());
        res.addHeader("Set-Cookie", rt.toString());
        res.addHeader("Set-Cookie", loginFlag.toString());
    }

    @Override
    public void issueTokens(UserInfoDTO user, HttpServletResponse response) {
        Instant initialAbsExp = Instant.now().plusSeconds(absoluteTtlSec);
        issueTokensWithAbsExp(user, response, initialAbsExp);
    }

    private void issueTokensWithAbsExp(UserInfoDTO user, HttpServletResponse response, Instant absExp) {
        String accessToken = generateAccessToken(user);
        String refreshToken = generateRefreshTokenWithAbsExp(user, absExp); // 발급된 절대 시간을 계속 이어감

        long rtExpirationMillis = refreshTtlSec * 1000L;
        redisService.setValues("RT:" + user.userNo(), refreshToken, rtExpirationMillis);

        writeTokenAsCookies(response, accessToken, refreshToken);
    }
}