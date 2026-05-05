package com.example.maum.controller;

import com.example.maum.controller.response.CommonResponse;
import com.example.maum.dto.ExistsDTO;
import com.example.maum.dto.MsgDTO;
import jakarta.servlet.http.Cookie;
import com.example.maum.dto.UserInfoDTO;
import com.example.maum.service.IUserInfoService;
import com.example.maum.service.impl.RedisService;
import com.example.maum.util.CmmUtil;
import com.example.maum.util.EncryptUtil;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.security.oauth2.server.resource.web.BearerTokenResolver;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.Optional;

@RestController
@Slf4j
@RequestMapping(value = "/api/v1/account")
@RequiredArgsConstructor
public class UserInfoController {

    private final IUserInfoService userInfoService;
    private final RedisService redisService;
    private final BearerTokenResolver bearerTokenResolver;

    /* CREATE */

//    @PostMapping(value = "insertUserInfo")
//    public MsgDTO insertUserInfo(HttpServletRequest request) throws Exception {
//
//        log.info("{}.insertUserInfo Start!", this.getClass().getName());
//
//        String msg;
//
//        String userId = CmmUtil.nvl(request.getParameter("userId"));
//        String userName = CmmUtil.nvl(request.getParameter("userName"));
//        String password = CmmUtil.nvl(request.getParameter("password"));
//        String email = CmmUtil.nvl(request.getParameter("email"));
//        String birthDate = CmmUtil.nvl(request.getParameter("birthDate"));
//        String addr = CmmUtil.nvl(request.getParameter("addr"));
//        String detailAddr = CmmUtil.nvl(request.getParameter("detailAddr"));
//
//        log.info("userId: {}, userName: {}, email: {}, birthDate: {}, addr: {}, detailAddr: {}",
//                userId, userName, email, birthDate, addr, detailAddr);
//
//        UserInfoDTO pDTO = UserInfoDTO.builder()
//                .userId(userId)
//                .userName(userName)
//                .password(EncryptUtil.encHashSHA256(password))
//                .email(EncryptUtil.encAES128BCBC(email))
//                .birthDate(birthDate)
//                .addr(addr)
//                .detailAddr(detailAddr)
//                .build();
//
//        int res = userInfoService.insertUserInfo(pDTO);
//
//        log.info("회원가입 결과(res): {}", res);
//
//        if (res == 1) {
//            msg = "회원가입이 완료되었습니다.";
//            request.getSession().setAttribute("SS_USER_ID", userId);
//        } else if (res == 2) {
//            msg = "이미 가입된 아이디입니다.";
//        } else {
//            msg = "오류로 인해 회원가입이 실패하였습니다.";
//        }
//
//        MsgDTO dto = MsgDTO.builder()
//                .result(res)
//                .msg(msg)
//                .build();
//
//        log.info("{}.insertUserInfo End!", this.getClass().getName());
//
//        return dto;
//    }


    /* READ */

//    @PostMapping(value = "getUserIdExists")
//    public ExistsDTO getUserIdExists(HttpServletRequest request) throws Exception {
//
//        log.info("{}.getUserIdExists Start!", this.getClass().getName());
//
//        String userId = CmmUtil.nvl(request.getParameter("userId"));
//
//        log.info("userId: {}", userId);
//
//        UserInfoDTO pDTO = UserInfoDTO.builder()
//                .userId(userId)
//                .build();
//
//        ExistsDTO rDTO = Optional.ofNullable(userInfoService.getUserIdExists(pDTO))
//                .orElseGet(() -> ExistsDTO.builder().exists(false).build());
//
//        log.info("{}.getUserIdExists End!", this.getClass().getName());
//
//        return rDTO;
//    }



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

        final String userId = jwt.getSubject();

        UserInfoDTO pDTO = UserInfoDTO.builder().userId(userId).build();

        UserInfoDTO rDTO = Optional.ofNullable(userInfoService.getUserInfo(pDTO))
                .orElseGet(() -> UserInfoDTO.builder().build());

        log.info("{}.userInfo End!", this.getClass().getName());

        return ResponseEntity.ok(
                CommonResponse.of((HttpStatus.OK), HttpStatus.OK.series().name(), rDTO)
        );
    }


    @PostMapping(value = "getEmailExists")
    public ExistsDTO getEmailExists(HttpServletRequest request, HttpSession session) throws Exception {

        log.info("{}.getEmailExists Start!", this.getClass().getName());

        String email = CmmUtil.nvl(request.getParameter("email"));
        log.info("email : " + email);

        UserInfoDTO pDTO = UserInfoDTO.builder()
                .email(EncryptUtil.encAES128BCBC(email))
                .build();

        ExistsDTO rDTO = Optional.ofNullable(userInfoService.getEmailExists(pDTO))
                .orElseGet(() -> ExistsDTO.builder().exists(false).authNumber(0).build());

        if (!rDTO.exists() && rDTO.authNumber() != 0) {
            session.setAttribute("AUTH_" + email, rDTO.authNumber());
            session.setMaxInactiveInterval(3 * 60);
        }


        ExistsDTO dto = ExistsDTO.builder()
                .exists(rDTO.exists())
                .authNumber(0)
                .build();

        log.info("{}.getEmailExists End!", this.getClass().getName());

        return dto;
    }


    @PostMapping(value = "verifyEmailCode")
    public MsgDTO verifyEmailCode(HttpServletRequest request, HttpSession session) throws Exception {

        log.info("{}.verifyEmailCode Start!", this.getClass().getName());

        String email = CmmUtil.nvl(request.getParameter("email"));
        String code = CmmUtil.nvl(request.getParameter("code"));

        Object storedAuthCode = session.getAttribute("AUTH_" + email);

        MsgDTO rDTO;

        log.info("email: {}, code: {}, storedAuthCode: {}", email, code, storedAuthCode);

        if (storedAuthCode != null && storedAuthCode.toString().equals(code)) {
            rDTO = MsgDTO.builder()
                    .result(1)
                    .msg("인증에 성공하였습니다.")
                    .build();

            return rDTO;
        }

        rDTO = MsgDTO.builder()
                .result(0)
                .msg("인증번호가 일치하지 않거나 만료되었습니다.")
                .build();

        log.info("{}.verifyEmailCode End!", this.getClass().getName());

        return rDTO;
    }


//    @PostMapping(value = "loginProc")
//    public MsgDTO loginProc(HttpServletRequest request, HttpSession session) throws Exception {
//
//        log.info("{}.loginProc Start!", this.getClass().getName());
//
//        String msg;
//
//        String userId = CmmUtil.nvl(request.getParameter("userId"));
//        String password = CmmUtil.nvl(request.getParameter("password"));
//
//        log.info("userId: {}, password: {}", userId, password);
//
//        UserInfoDTO pDTO = UserInfoDTO.builder()
//                .userId(userId)
//                .password(EncryptUtil.encHashSHA256(password))
//                .build();
//
//        int res = userInfoService.getUserLogin(pDTO);
//
//        log.info("res: {}", res);
//
//        if (res == 1) {
//            msg = "로그인이 성공했습니다.";
//
//            UserInfoDTO rDTO = userInfoService.getUserInfo(pDTO);
//
//            session.setAttribute("SS_USER_ID", userId);
//            session.setAttribute("SS_USER_NO", rDTO.userNo());
//            session.setAttribute("SS_USER_NAME", rDTO.userName());
//            session.setAttribute("SS_USER_PROFILE_IMG", rDTO.profileImgUrl());
//        } else {
//            msg = "아이디와 비밀번호가 일치하지 않습니다.";
//        }
//
//        MsgDTO dto = MsgDTO.builder()
//                .result(res)
//                .msg(msg)
//                .build();
//
//        log.info("{}.loginProc End!", this.getClass().getName());
//
//        return dto;
//    }


    @GetMapping(value = "status")
    public UserInfoDTO getLoginStatus(HttpSession session) {

        log.info("{}.getLoginStatus Start!", this.getClass().getName());

        String userNo = (String) session.getAttribute("SS_USER_NO");

        UserInfoDTO rDTO;

        if (userNo == null) {
            rDTO = UserInfoDTO.builder().build();
            return rDTO;
        }

        String userId = CmmUtil.nvl((String) session.getAttribute("SS_USER_ID"));
        String userName = CmmUtil.nvl((String) session.getAttribute("SS_USER_NAME"));
        String profileImgUrl = CmmUtil.nvl((String) session.getAttribute("SS_USER_PROFILE_IMG"));

        log.info("{}.getLoginStatus Start!", this.getClass().getName());

        rDTO = UserInfoDTO.builder()
                .userNo(userNo)
                .userId(userId)
                .userName(userName)
                .profileImgUrl(profileImgUrl)
                .build();

        return rDTO;
    }


    @PostMapping(value = "logout")
    public ResponseEntity<CommonResponse<MsgDTO>> logout(HttpServletRequest request, HttpServletResponse response) {

        log.info("{}.logout Start!", this.getClass().getName());

        String accessToken = bearerTokenResolver.resolve(request);

        if (accessToken != null) {
            long atExpirationMillis = 15 * 60 * 1000L;
            redisService.setValues("AT:" + accessToken, "logout", atExpirationMillis);
            log.info("Access Token 블랙리스트 등록 완료");
        }

        Cookie accessCookie = new Cookie("accessCookieName", null); // application.yml에 적은 쿠키 이름
        accessCookie.setMaxAge(0);
        accessCookie.setPath("/");
        response.addCookie(accessCookie);

        Cookie refreshCookie = new Cookie("refreshCookieName", null);
        refreshCookie.setMaxAge(0);
        refreshCookie.setPath("/");
        response.addCookie(refreshCookie);

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
    public ExistsDTO findUserId(HttpServletRequest request, HttpSession session) throws Exception {

        log.info("{}.findUserId Start!", this.getClass().getName());

        String email = CmmUtil.nvl(request.getParameter("email"));
        String userName = CmmUtil.nvl(request.getParameter("userName"));

        log.info("email: {}, userName: {}", email, userName);

        ExistsDTO dto;

        UserInfoDTO pDTO = UserInfoDTO.builder()
                .email(EncryptUtil.encAES128BCBC(email))
                .userName(userName)
                .build();

        ExistsDTO rDTO = Optional.ofNullable(userInfoService.findUserId(pDTO))
                .orElseGet(() -> ExistsDTO.builder().exists(false).authNumber(0).build());

        if (rDTO.exists()) {
            session.setAttribute("AUTH_" + email, rDTO.authNumber());
            session.setMaxInactiveInterval(3 * 60);
        }

        log.info("{}.findUserId End!", this.getClass().getName());

        dto = ExistsDTO.builder()
                .exists(rDTO.exists())
                .authNumber(0)
                .build();

        return dto;
    }


    @PostMapping(value = "getUserId")
    public UserInfoDTO getUserId(HttpServletRequest request, HttpSession session) throws Exception {

        log.info("{}.getUserId Start!", this.getClass().getName());

        String email = CmmUtil.nvl(request.getParameter("email"));
        String userName = CmmUtil.nvl(request.getParameter("userName"));
        String code = CmmUtil.nvl(request.getParameter("code"));

        Object storedAuthCode = session.getAttribute("AUTH_" + email);

        log.info("email: {}, userName: {}, code: {}, storedAuthCode: {}", email, userName, code, storedAuthCode);

        UserInfoDTO rDTO;

        if (storedAuthCode != null && storedAuthCode.toString().equals(code)) {
            UserInfoDTO pDTO = UserInfoDTO.builder()
                    .email(EncryptUtil.encAES128BCBC(email))
                    .userName(userName)
                    .build();

            rDTO = Optional.ofNullable(userInfoService.getUserId(pDTO))
                    .orElseGet(() -> UserInfoDTO.builder().build());

            session.removeAttribute("AUTH_" + email);

            return rDTO;
        }

        log.info("{}.getUserId End!", this.getClass().getName());

        rDTO = UserInfoDTO.builder().build();

        return rDTO;
    }


    @PostMapping(value = "findUserPw")
    public ExistsDTO findUserPw(HttpServletRequest request, HttpSession session) throws Exception {

        log.info("{}.findUserPw Start!", this.getClass().getName());

        String email = CmmUtil.nvl(request.getParameter("email"));
        String userId = CmmUtil.nvl(request.getParameter("userId"));

        log.info("email: {}, userId: {}", email, userId);

        UserInfoDTO pDTO = UserInfoDTO.builder()
                .email(EncryptUtil.encAES128BCBC(email))
                .userId(userId)
                .build();

        ExistsDTO rDTO;

        rDTO = Optional.ofNullable(userInfoService.findUserPw(pDTO))
                .orElseGet(() -> ExistsDTO.builder().exists(false).authNumber(0).build());

        if (rDTO.exists()) {
            session.setAttribute("AUTH_" + email, rDTO.authNumber());
            session.setMaxInactiveInterval(3 * 60);
            session.setAttribute("UPDATE_PASSWORD_ID", userId);
        }

        log.info("{}.findUserPw End!", this.getClass().getName());

        rDTO = ExistsDTO.builder()
                .exists(rDTO.exists())
                .authNumber(0)
                .build();

        return rDTO;
    }


    /* UPDATE */

    @PostMapping(value = "updateUserPw")
    public MsgDTO updateUserPw(HttpServletRequest request, HttpSession session) throws Exception {

        log.info("{}.updateUserPw Start!", this.getClass().getName());

        String email = CmmUtil.nvl(request.getParameter("email"));
        String password = CmmUtil.nvl(request.getParameter("password"));
        String code = CmmUtil.nvl(request.getParameter("code"));

        Object storedAuthCode = session.getAttribute("AUTH_" + email);

        log.info("email: {}, code: {}, storedAuthCode: {}", email, code, storedAuthCode);

        int res = 0;
        String msg = "인증번호가 일치하지 않거나 만료되었습니다.";

        if (storedAuthCode != null && storedAuthCode.toString().equals(code)) {

            String userId = CmmUtil.nvl((String) session.getAttribute("UPDATE_PASSWORD_ID"));
            log.info("Updating password for userId: {}", userId);

            if (userId.isEmpty()) {
                msg = "인증 정보가 만료되었습니다. 다시 시도해주세요.";
            } else {
                UserInfoDTO pDTO = UserInfoDTO.builder()
                        .userId(userId)
                        .password(EncryptUtil.encHashSHA256(password))
                        .build();

                res = userInfoService.updatePassword(pDTO);

                if (res == 1) {
                    msg = "비밀번호 수정이 완료되었습니다.";
                    session.removeAttribute("AUTH_" + email);
                    session.removeAttribute("UPDATE_PASSWORD_ID");
                } else {
                    msg = "비밀번호 수정에 실패했습니다.";
                }
            }
        }

        MsgDTO dto = MsgDTO.builder()
                .result(res)
                .msg(msg)
                .build();

        log.info("{}.updateUserPw End!", this.getClass().getName());

        return dto;
    }


    // 이거 수정 고려 session 방식을 사용하고 있음
    @PostMapping(value = "updateProfileImg")
    public MsgDTO updateProfileImg(HttpServletRequest request, HttpSession session) throws Exception {

        log.info("{}.updateProfileImg Start!", this.getClass().getName());

        String msg;
        int res = 0;

        String userId = CmmUtil.nvl((String) session.getAttribute("SS_USER_ID"));
        String profileImage = CmmUtil.nvl(request.getParameter("profileImage"));

        UserInfoDTO pDTO = UserInfoDTO.builder()
                .userId(userId)
                .profileImgUrl(profileImage)
                .build();

        log.info("userId : {}, fileName : {}", userId, profileImage);

        if (userId.isEmpty()) {
            msg = "인증 정보가 만료되었습니다. 다시 시도해주세요.";
        } else {
            res = userInfoService.updateProfileImg(pDTO);

            if (res == 1) {
                msg = "프로필 설정이 완료되었습니다.";
                request.getSession().removeAttribute("SS_USER_ID");
            } else {
                msg = "프로필 변경에 실패했습니다.";
            }
        }

        MsgDTO dto = MsgDTO.builder()
                .result(res)
                .msg(msg)
                .build();

        log.info("{}.updateProfileImg End!", this.getClass().getName());

        return dto;
    }
}