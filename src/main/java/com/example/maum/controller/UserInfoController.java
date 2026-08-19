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

    @PostMapping(value = "userInfo")
    public ResponseEntity<CommonResponse<UserInfoDTO>> userInfo(@AuthenticationPrincipal Jwt jwt) throws Exception {

        log.info("{}.userInfo Start!", this.getClass().getName());

        final String userNo = CmmUtil.nvl(jwt.getSubject());

        UserInfoDTO pDTO = UserInfoDTO.builder().userNo(userNo).build();

        UserInfoDTO rDTO = Optional.ofNullable(userInfoService.getUserInfo(pDTO))
                .orElseGet(() -> UserInfoDTO.builder().build());

        log.info("{}.userInfo End!", this.getClass().getName());

        return ResponseEntity.ok(
                CommonResponse.of(HttpStatus.OK, HttpStatus.OK.series().name(), rDTO)
        );
    }


    @PostMapping(value = "getEmailExists")
    public ResponseEntity<CommonResponse<ExistsDTO>> getEmailExists(@RequestBody UserInfoDTO uDTO) throws Exception {

        log.info("{}.getEmailExists Start!", this.getClass().getName());

        String email = CmmUtil.nvl(uDTO.email());

        log.info("email: {}", email);

        UserInfoDTO pDTO = UserInfoDTO.builder()
                .email(EncryptUtil.encAES128BCBC(email))
                .build();

        ExistsDTO rDTO = Optional.ofNullable(userInfoService.getEmailExists(pDTO))
                .orElseGet(() -> ExistsDTO.builder().exists(false).authNumber(0).build());

        ExistsDTO resultDTO = ExistsDTO.builder()
                .exists(rDTO.exists())
                .authNumber(0)
                .build();

        log.info("{}.getEmailExists End!", this.getClass().getName());

        return ResponseEntity.ok(
                CommonResponse.of(HttpStatus.OK, HttpStatus.OK.series().name(), resultDTO)
        );
    }


    @PostMapping(value = "verifyEmailCode")
    public ResponseEntity<CommonResponse<MsgDTO>> verifyEmailCode(@RequestBody UserInfoDTO uDTO) throws Exception {

        log.info("{}.verifyEmailCode Start!", this.getClass().getName());

        MsgDTO rDTO = Optional.ofNullable(userInfoService.verifyEmailCode(uDTO))
                .orElseGet(() -> MsgDTO.builder().result(0).msg("인증 처리 중 오류가 발생했습니다.").build());

        log.info("{}.verifyEmailCode End!", this.getClass().getName());

        return ResponseEntity.ok(
                CommonResponse.of(HttpStatus.OK, HttpStatus.OK.series().name(), rDTO)
        );
    }


    @PostMapping(value = "logout")
    public ResponseEntity<CommonResponse<MsgDTO>> logout(@AuthenticationPrincipal Jwt jwt,
                                                         HttpServletRequest request,
                                                         HttpServletResponse response) throws Exception {

        log.info("{}.logout Start!", this.getClass().getName());

        String accessToken = bearerTokenResolver.resolve(request);
        final String userNo = CmmUtil.nvl(jwt.getSubject());

        long remainingMilliSeconds = 0;
        if (jwt.getExpiresAt() != null) {
            remainingMilliSeconds = jwt.getExpiresAt().toEpochMilli() - System.currentTimeMillis();
        }

        if (remainingMilliSeconds > 0) {
            List<ResponseCookie> cookies = userInfoService.logout(accessToken, userNo, remainingMilliSeconds);
            cookies.forEach(cookie -> response.addHeader("Set-Cookie", cookie.toString()));
        } else {
            List<ResponseCookie> cookies = userInfoService.logout(null, userNo, 0);
            cookies.forEach(cookie -> response.addHeader("Set-Cookie", cookie.toString()));
        }

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
    public ResponseEntity<CommonResponse<ExistsDTO>> findUserId(@RequestBody UserInfoDTO uDTO) throws Exception {

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

        ExistsDTO resultDTO = ExistsDTO.builder()
                .exists(rDTO.exists())
                .authNumber(0)
                .build();

        log.info("{}.findUserId End!", this.getClass().getName());

        return ResponseEntity.ok(
                CommonResponse.of(HttpStatus.OK, HttpStatus.OK.series().name(), resultDTO)
        );
    }


    @PostMapping(value = "getUserId")
    public ResponseEntity<CommonResponse<UserInfoDTO>> getUserId(@RequestBody UserInfoDTO uDTO) throws Exception {

        log.info("{}.getUserId Start!", this.getClass().getName());

        String email = CmmUtil.nvl(uDTO.email());
        String userName = CmmUtil.nvl(uDTO.userName());
        String code = CmmUtil.nvl(uDTO.code());

        log.info("email: {}, userName: {}, code: {}", email, userName, code);

        UserInfoDTO rDTO = Optional.ofNullable(userInfoService.getUserId(uDTO))
                .orElseGet(() -> UserInfoDTO.builder().build());

        log.info("{}.getUserId End!", this.getClass().getName());

        return ResponseEntity.ok(
                CommonResponse.of(HttpStatus.OK, HttpStatus.OK.series().name(), rDTO)
        );
    }


    @PostMapping(value = "findUserPw")
    public ResponseEntity<CommonResponse<ExistsDTO>> findUserPw(@RequestBody UserInfoDTO uDTO) throws Exception {

        log.info("{}.findUserPw Start!", this.getClass().getName());

        String email = CmmUtil.nvl(uDTO.email());
        String userId = CmmUtil.nvl(uDTO.userId());

        UserInfoDTO pDTO = UserInfoDTO.builder()
                .email(EncryptUtil.encAES128BCBC(email))
                .userId(userId)
                .build();

        ExistsDTO rDTO = Optional.ofNullable(userInfoService.findUserPw(pDTO))
                .orElseGet(() -> ExistsDTO.builder().exists(false).authNumber(0).build());

        ExistsDTO resultDTO = ExistsDTO.builder()
                .exists(rDTO.exists())
                .authNumber(0)
                .build();

        log.info("{}.findUserPw End!", this.getClass().getName());

        return ResponseEntity.ok(
                CommonResponse.of(HttpStatus.OK, HttpStatus.OK.series().name(), resultDTO)
        );
    }


    @PostMapping(value = "updateUserPw")
    public ResponseEntity<CommonResponse<MsgDTO>> updateUserPw(@RequestBody UserInfoDTO uDTO) throws Exception {

        log.info("{}.updateUserPw Start!", this.getClass().getName());

        int res = Optional.of(userInfoService.updatePassword(uDTO))
                .orElse(0);

        String msg = (res == 1) ? "비밀번호 수정이 완료되었습니다." : "인증번호가 일치하지 않거나 정보가 만료되었습니다.";

        MsgDTO rDTO = MsgDTO.builder()
                .result(res)
                .msg(msg)
                .build();

        log.info("{}.updateUserPw End!", this.getClass().getName());

        return ResponseEntity.ok(
                CommonResponse.of(HttpStatus.OK, HttpStatus.OK.series().name(), rDTO)
        );
    }


    @PostMapping(value = "updateProfileImg")
    public ResponseEntity<CommonResponse<MsgDTO>> updateProfileImg(@RequestBody UserInfoDTO uDTO,
                                                                   @AuthenticationPrincipal Jwt jwt) throws Exception {

        log.info("{}.updateProfileImg Start!", this.getClass().getName());

        final String userNo = CmmUtil.nvl(jwt.getSubject());
        String profileImage = CmmUtil.nvl(uDTO.profileImgUrl());

        log.info("프로필 변경 요청 - userNo: {}, profileImage: {}", userNo, profileImage);

        UserInfoDTO pDTO = UserInfoDTO.builder()
                .userNo(userNo)
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

        return ResponseEntity.ok(CommonResponse.of(HttpStatus.OK, HttpStatus.OK.series().name(), rDTO));
    }


    @PostMapping(value = "verifyCurrentPassword")
    public ResponseEntity<CommonResponse<MsgDTO>> verifyCurrentPassword(@RequestBody UserInfoDTO uDTO,
                                                                        @AuthenticationPrincipal Jwt jwt) throws Exception {

        log.info("{}.verifyCurrentPassword Start!", this.getClass().getName());

        final String userNo = CmmUtil.nvl(jwt.getSubject());
        String password = CmmUtil.nvl(uDTO.password());

        log.info("userNo: {}, password: {}", userNo, EncryptUtil.encHashSHA256(password));

        UserInfoDTO pDTO = UserInfoDTO.builder()
                .userNo(userNo)
                .password(password)
                .build();

        MsgDTO rDTO = userInfoService.verifyCurrentPassword(pDTO);

        log.info("{}.verifyCurrentPassword End!", this.getClass().getName());

        return ResponseEntity.ok(CommonResponse.of(HttpStatus.OK, HttpStatus.OK.series().name(), rDTO));
    }


    @PostMapping(value = "updateAccount")
    public ResponseEntity<CommonResponse<MsgDTO>> updateAccount(@RequestBody UserInfoDTO uDTO,
                                                                @AuthenticationPrincipal Jwt jwt) throws Exception {

        log.info("{}.updateAccount Start!", this.getClass().getName());

        final String userNo = CmmUtil.nvl(jwt.getSubject());
        String password = CmmUtil.nvl(uDTO.password());
        String email = CmmUtil.nvl(uDTO.email());
        String addr = CmmUtil.nvl(uDTO.addr());
        String detailAddr = CmmUtil.nvl(uDTO.detailAddr());

        log.info("userNo: {}, password: {}, email: {}, addr: {}, detailAddr: {}", userNo, EncryptUtil.encHashSHA256(password), email, addr, detailAddr);

        UserInfoDTO pDTO = UserInfoDTO.builder()
                .userNo(userNo)
                .password(password)
                .email(email.isEmpty() ? "" : EncryptUtil.encAES128BCBC(email))
                .addr(addr)
                .detailAddr(detailAddr)
                .build();

        int res = Optional.of(userInfoService.updateAccount(pDTO)).orElse(0);

        MsgDTO rDTO = MsgDTO.builder()
                .result(res)
                .msg(res == 1 ? "프로필 수정이 완료되었습니다." : "프로필 수정에 실패했습니다.")
                .build();

        log.info("{}.updateAccount End!", this.getClass().getName());

        return ResponseEntity.ok(CommonResponse.of(HttpStatus.OK, HttpStatus.OK.series().name(), rDTO));
    }


    @PostMapping(value = "sendWithdrawEmailCode")
    public ResponseEntity<CommonResponse<MsgDTO>> sendWithdrawEmailCode(@RequestBody UserInfoDTO uDTO,
                                                                        @AuthenticationPrincipal Jwt jwt) throws Exception {

        log.info("{}.sendWithdrawEmailCode Start!", this.getClass().getName());

        String email = CmmUtil.nvl(uDTO.email());

        UserInfoDTO pDTO = UserInfoDTO.builder()
                .email(EncryptUtil.encAES128BCBC(email))
                .build();

        MsgDTO rDTO = userInfoService.sendWithdrawEmailCode(pDTO);

        log.info("{}.sendWithdrawEmailCode End!", this.getClass().getName());

        return ResponseEntity.ok(CommonResponse.of(HttpStatus.OK, HttpStatus.OK.series().name(), rDTO));
    }


    @PostMapping(value = "deleteUser")
    public ResponseEntity<CommonResponse<MsgDTO>> deleteUser(@AuthenticationPrincipal Jwt jwt,
                                                             HttpServletRequest request,
                                                             HttpServletResponse response) throws Exception {

        log.info("{}.deleteUser Start!", this.getClass().getName());

        final String userNo = CmmUtil.nvl(jwt.getSubject());
        UserInfoDTO pDTO = UserInfoDTO.builder().userNo(userNo).build();

        int res = Optional.of(userInfoService.deleteUser(pDTO)).orElse(0);

        if (res == 1) {
            String accessToken = bearerTokenResolver.resolve(request);

            long remainingMilliSeconds = 0;
            if (jwt.getExpiresAt() != null) {
                remainingMilliSeconds = jwt.getExpiresAt().toEpochMilli() - System.currentTimeMillis();
            }

            List<ResponseCookie> cookies = userInfoService.logout(accessToken, userNo, remainingMilliSeconds);
            cookies.forEach(cookie -> response.addHeader("Set-Cookie", cookie.toString()));
        }

        MsgDTO rDTO = MsgDTO.builder()
                .result(res)
                .msg(res == 1 ? "회원 탈퇴가 완료되었습니다." : "회원 탈퇴 처리에 실패했습니다.")
                .build();

        log.info("{}.deleteUser End!", this.getClass().getName());

        return ResponseEntity.ok(CommonResponse.of(HttpStatus.OK, HttpStatus.OK.series().name(), rDTO));
    }
}