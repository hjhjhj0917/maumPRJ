package com.example.maum.service;

import com.example.maum.dto.UserInfoDTO;
import jakarta.servlet.http.HttpServletResponse;

public interface IJwtTokenService {

    String generateAccessToken(UserInfoDTO user);

    String generateRefreshToken(UserInfoDTO user);

    void writeTokenAsCookies(HttpServletResponse res, String accessToken, String refreshToken);

    int reissueTokens(String refreshToken, HttpServletResponse response) throws Exception;

    default void issueTokens(UserInfoDTO user, HttpServletResponse res) {
        String at = generateAccessToken(user);
        String rt = generateRefreshToken(user);
        writeTokenAsCookies(res, at, rt);
    }
}
