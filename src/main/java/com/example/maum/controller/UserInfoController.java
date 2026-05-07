package com.example.maum.controller;

import com.example.maum.controller.response.CommonResponse;
import com.example.maum.dto.ExistsDTO;
import com.example.maum.dto.MsgDTO;
import com.example.maum.dto.UserInfoDTO;
import com.example.maum.service.IUserInfoService;
import com.example.maum.util.CmmUtil;
import com.example.maum.util.EncryptUtil;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseCookie;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.security.oauth2.server.resource.web.BearerTokenResolver;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.Optional;

@RestController
@Slf4j
@RequestMapping(value = "/api/v1/account")
@RequiredArgsConstructor
public class UserInfoController {

    private final IUserInfoService userInfoService;
    private final BearerTokenResolver bearerTokenResolver;

    /* CREATE */


    /* READ */

    @PostMapping(value = "userInfo")
    public ResponseEntity<CommonResponse<UserInfoDTO>> userInfo(@AuthenticationPrincipal Jwt jwt) throws Exception {

        log.info("{}.userInfo Start!", this.getClass().getName());

        if (jwt == null) {

            log.warn("JWT principal is null - unauthorized access to /api/v1/account/userInfo");

            return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                    .body(CommonResponse.of(
                            HttpStatus.UNAUTHORIZED,
                            HttpStatus.UNAUTHORIZED.series().name(),
                            UserInfoDTO.builder().build()
                    ));
        }

        final String userNo = jwt.getSubject();
        String userId = jwt.getClaimAsString("userId");

        UserInfoDTO pDTO = UserInfoDTO.builder().userNo(userNo).userId(userId).build();

        UserInfoDTO rDTO = Optional.ofNullable(userInfoService.getUserInfo(pDTO))
                .orElseGet(() -> UserInfoDTO.builder().build());

        log.info("{}.userInfo End!", this.getClass().getName());

        return ResponseEntity.ok(
                CommonResponse.of((HttpStatus.OK), HttpStatus.OK.series().name(), rDTO)
        );
    }


    @PostMapping(value = "getEmailExists")
    public ExistsDTO getEmailExists(@RequestBody UserInfoDTO uDTO) throws Exception {

        log.info("{}.getEmailExists Start!", this.getClass().getName());

        String email = CmmUtil.nvl(uDTO.email());

        log.info("email: {}", email);

        UserInfoDTO pDTO = UserInfoDTO.builder()
                .email(EncryptUtil.encAES128BCBC(email))
                .build();

        ExistsDTO rDTO = Optional.ofNullable(userInfoService.getEmailExists(pDTO))
                .orElseGet(() -> ExistsDTO.builder().exists(false).authNumber(0).build());

        log.info("{}.getEmailExists End!", this.getClass().getName());

        return ExistsDTO.builder()
                .exists(rDTO.exists())
                .authNumber(0)
                .build();
    }


    @PostMapping(value = "verifyEmailCode")
    public MsgDTO verifyEmailCode(@RequestBody UserInfoDTO uDTO) throws Exception {

        log.info("{}.verifyEmailCode Start!", this.getClass().getName());

        MsgDTO rDTO = Optional.ofNullable(userInfoService.verifyEmailCode(uDTO))
                .orElseGet(() -> MsgDTO.builder().result(0).msg("인증 처리 중 오류가 발생했습니다.").build());

        log.info("{}.verifyEmailCode End!", this.getClass().getName());

        return rDTO;
    }


    @PostMapping(value = "logout")
    public ResponseEntity<CommonResponse<MsgDTO>> logout(@AuthenticationPrincipal Jwt jwt,
                                                         HttpServletRequest request,
                                                         HttpServletResponse response) throws Exception {

        log.info("{}.logout Start!", this.getClass().getName());

        String accessToken = bearerTokenResolver.resolve(request);
        String userNo = (jwt != null) ? jwt.getSubject() : null;

        List<ResponseCookie> cookies = userInfoService.logout(accessToken, userNo);

        cookies.forEach(cookie -> response.addHeader("Set-Cookie", cookie.toString()));

        MsgDTO dto = MsgDTO.builder()
                .result(1)
                .msg("로그아웃 되었습니다.")
                .build();

        log.info("{}.logout End!", this.getClass().getName());

        return ResponseEntity.ok(
                CommonResponse.of(HttpStatus.OK, HttpStatus.OK.series().name(), dto)
        );
    }


    @PostMapping(value = "findUserId")
    public ExistsDTO findUserId(@RequestBody UserInfoDTO uDTO) throws Exception {

        log.info("{}.findUserId Start!", this.getClass().getName());

        String email = CmmUtil.nvl(uDTO.email());
        String userName = CmmUtil.nvl(uDTO.userName());

        log.info("email: {}, userName: {}", email, userName);

        UserInfoDTO pDTO = UserInfoDTO.builder()
                .email(EncryptUtil.encAES128BCBC(email))
                .userName(userName)
                .build();

        ExistsDTO rDTO = Optional.ofNullable(userInfoService.findUserId(pDTO))
                .orElseGet(() -> ExistsDTO.builder().exists(false).authNumber(0).build());

        log.info("{}.findUserId End!", this.getClass().getName());

        return ExistsDTO.builder()
                .exists(rDTO.exists())
                .authNumber(0)
                .build();
    }


    @PostMapping(value = "getUserId")
    public UserInfoDTO getUserId(@RequestBody UserInfoDTO uDTO) throws Exception {

        log.info("{}.getUserId Start!", this.getClass().getName());

        String email = CmmUtil.nvl(uDTO.email());
        String userName = CmmUtil.nvl(uDTO.userName());
        String code = CmmUtil.nvl(uDTO.code());

        log.info("email: {}, userName: {}, code: {}", email, userName, code);

        UserInfoDTO rDTO = Optional.ofNullable(userInfoService.getUserId(uDTO))
                .orElseGet(() -> UserInfoDTO.builder().build());

        log.info("{}.getUserId End!", this.getClass().getName());

        return rDTO;
    }


    @PostMapping(value = "findUserPw")
    public ExistsDTO findUserPw(@RequestBody UserInfoDTO uDTO) throws Exception {

        log.info("{}.findUserPw Start!", this.getClass().getName());

        String email = CmmUtil.nvl(uDTO.email());
        String userId = CmmUtil.nvl(uDTO.userId());

        UserInfoDTO pDTO = UserInfoDTO.builder()
                .email(EncryptUtil.encAES128BCBC(email))
                .userId(userId)
                .build();

        ExistsDTO rDTO = Optional.ofNullable(userInfoService.findUserPw(pDTO))
                .orElseGet(() -> ExistsDTO.builder().exists(false).authNumber(0).build());

        log.info("{}.findUserPw End!", this.getClass().getName());

        return ExistsDTO.builder()
                .exists(rDTO.exists())
                .authNumber(0)
                .build();
    }


    @PostMapping(value = "updateUserPw")
    public MsgDTO updateUserPw(@RequestBody UserInfoDTO uDTO) throws Exception {

        log.info("{}.updateUserPw Start!", this.getClass().getName());

        int res = Optional.of(userInfoService.updatePassword(uDTO))
                .orElse(0);

        String msg = (res == 1) ? "비밀번호 수정이 완료되었습니다." : "인증번호가 일치하지 않거나 정보가 만료되었습니다.";

        MsgDTO rDTO = MsgDTO.builder()
                .result(res)
                .msg(msg)
                .build();

        log.info("{}.updateUserPw End!", this.getClass().getName());

        return rDTO;
    }


    @PostMapping(value = "updateProfileImg")
    public MsgDTO updateProfileImg(@RequestBody UserInfoDTO uDTO, @AuthenticationPrincipal Jwt jwt) throws Exception {

        log.info("{}.updateProfileImg Start!", this.getClass().getName());

        if (jwt == null) {
            log.warn("인증 정보(JWT)가 누락된 접근입니다.");
            return MsgDTO.builder().result(0).msg("인증 정보가 없습니다.").build();
        }

        String userId = jwt.getClaimAsString("userId");
        String profileImage = CmmUtil.nvl(uDTO.profileImgUrl());

        log.info("프로필 변경 요청 - userId: {}, profileImage: {}", userId, profileImage);

        UserInfoDTO pDTO = UserInfoDTO.builder()
                .userId(userId)
                .profileImgUrl(profileImage)
                .build();

        int res = Optional.of(userInfoService.updateProfileImg(pDTO))
                .orElse(0);

        String msg = (res == 1) ? "프로필 설정이 완료되었습니다." : "프로필 변경에 실패했습니다.";

        MsgDTO rDTO = MsgDTO.builder()
                .result(res)
                .msg(msg)
                .build();

        log.info("{}.updateProfileImg End!", this.getClass().getName());

        return rDTO;
    }
}