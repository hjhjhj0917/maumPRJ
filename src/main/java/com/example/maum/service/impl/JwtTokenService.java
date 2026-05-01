package com.example.maum.service.impl;

import com.example.maum.dto.UserInfoDTO;
import com.example.maum.service.IJwtTokenService;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.ResponseCookie;
import org.springframework.security.oauth2.jose.jws.MacAlgorithm;
import org.springframework.security.oauth2.jwt.JwsHeader;
import org.springframework.security.oauth2.jwt.JwtClaimsSet;
import org.springframework.security.oauth2.jwt.JwtEncoder;
import org.springframework.security.oauth2.jwt.JwtEncoderParameters;
import org.springframework.stereotype.Service;

import java.time.Instant;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class JwtTokenService implements IJwtTokenService {

    private static final String CLAIM_USERNAME = "username";
    private static final String CLAIM_ROLES = "roles";
    private static final String CLAIM_TYPE = "type";
    private static final String TYPE_ACCESS = "access";
    private static final String TYPE_REFRESH = "refresh";

    private final JwtEncoder jwtEncoder;

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
                .subject(user.userId())
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

    /*
    Access/Refresh Token 각각 HttpOnly 쿠키로 저장
     */
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

        res.addHeader("Set-Cookie", at.toString());
        res.addHeader("Set-Cookie", rt.toString());
    }
}
