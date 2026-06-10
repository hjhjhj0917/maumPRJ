package com.example.maum.controller;

import com.example.maum.auth.AuthInfo;
import com.example.maum.controller.response.CommonResponse;
import com.example.maum.dto.MsgDTO;
import com.example.maum.dto.UserInfoDTO;
import com.example.maum.service.IJwtTokenService;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@Slf4j
@RequestMapping(value =  "api/v1/login")
@RequiredArgsConstructor
@RestController
public class LoginController {

    private final AuthenticationManager authenticationManager;
    private final IJwtTokenService jwtTokenService;

    @PostMapping(value = "loginProc")
    public ResponseEntity<CommonResponse<MsgDTO>> loginProc(@RequestBody UserInfoDTO pDTO, HttpServletResponse response) {

        log.info("{}.loginProc Start!", this.getClass().getName());

        MsgDTO dto;

        try {
            /*
            * authenticationManager는 미인증 토큰(인증서)를
            * DaoAuthenticationProvider전달 이게 몰래 loadUserByUsername 호출
            * 해당 아이디 유저의 아이디와 암호화된 비밀번호와 현재 아이디와 평문 비번을 자동 암호화 해서 비교
            */
            Authentication auth = authenticationManager.authenticate(
                    new UsernamePasswordAuthenticationToken(pDTO.userId(), pDTO.password()) /* 인증전 단계에서 미인증 토큰 생성 */
            );

            AuthInfo principal = (AuthInfo) auth.getPrincipal();
            UserInfoDTO u = principal.userInfoDTO();

            jwtTokenService.issueTokens(u, response);

            dto = MsgDTO.builder()
                    .result(1)
                    .msg("로그인 성공")
                    .build();

            log.info("로그인 성공: {}", pDTO.userId());

        } catch (Exception e) {
            log.warn("로그인 실패 (userId: {}): {}", pDTO.userId(), e.getMessage());

            dto = MsgDTO.builder()
                    .result(0)
                    .msg("아이디 또는 비밀번호가 일치하지 않습니다.")
                    .build();
        }

        log.info("{}.loginProc End!", this.getClass().getName());

        return ResponseEntity.ok(
                CommonResponse.of(HttpStatus.OK, HttpStatus.OK.series().name(), dto)
        );
    }

    @PostMapping(value = "loginInfo")
    public ResponseEntity<CommonResponse<UserInfoDTO>> loginInfo(@AuthenticationPrincipal Jwt jwt) {

        log.info("{}.loginInfo Start!", this.getClass().getName());

        UserInfoDTO dto;

        if (jwt == null) {

            dto = UserInfoDTO.builder()
                    .userId("")
                    .userName("")
                    .roles("")
                    .build();
        } else {

            String userId = jwt.getSubject();
            String userName = jwt.getClaim("username");
            List<String> roles = jwt.getClaim("roles");
            String rolesCsv = (roles == null) ? "" : String.join(",", roles);

            dto = UserInfoDTO.builder()
                    .userId(userId)
                    .userName(userName)
                    .roles(rolesCsv)
                    .build();
        }

        log.info("{}.loginInfo End!", this.getClass().getName());

        return ResponseEntity.ok(
                CommonResponse.of(HttpStatus.OK, HttpStatus.OK.series().name(), dto)
        );
    }

    @PostMapping(value = "/refresh")
    public ResponseEntity<CommonResponse<MsgDTO>> refreshToken(
            @CookieValue(value = "${secure.jwt.token.refresh.name}", required = false) String refreshToken,
            HttpServletResponse response) {

        log.info("{}.refreshToken Start!", this.getClass().getName());

        if (refreshToken == null || refreshToken.isEmpty()) {
            log.info("{}.refreshToken End!", this.getClass().getName());
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                    .body(CommonResponse.of(HttpStatus.UNAUTHORIZED, HttpStatus.UNAUTHORIZED.series().name(), MsgDTO.builder().result(0).msg("세션이 만료되었습니다.").build()));
        }

        try {
            int res = jwtTokenService.reissueTokens(refreshToken, response);

            if (res == 1) {
                log.info("{}.refreshToken End!", this.getClass().getName());

                return ResponseEntity.ok(
                        CommonResponse.of(HttpStatus.OK, HttpStatus.OK.series().name(), MsgDTO.builder().result(1).msg("세션이 연장되었습니다.").build())
                );
            } else {
                log.info("{}.refreshToken End!", this.getClass().getName());

                return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                        .body(CommonResponse.of(HttpStatus.UNAUTHORIZED, HttpStatus.UNAUTHORIZED.series().name(), MsgDTO.builder().result(0).msg("유효하지 않은 세션입니다.").build()));
            }
        } catch (Exception e) {
            log.error("Refresh Controller Error : {}", e.getMessage());
            log.info("{}.refreshToken End!", this.getClass().getName());

            return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                    .body(CommonResponse.of(HttpStatus.UNAUTHORIZED, HttpStatus.UNAUTHORIZED.series().name(), MsgDTO.builder().result(0).msg("인증 처리 중 오류가 발생했습니다.").build()));
        }
    }
}