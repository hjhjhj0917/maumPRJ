package com.example.maum.account.controller;

import com.example.maum.account.dto.ExistsDTO;
import com.example.maum.account.dto.UserInfoDTO;
import com.example.maum.account.service.IUserInfoService;
import com.example.maum.global.dto.MsgDTO;
import com.example.maum.global.util.CmmUtil;
import com.example.maum.global.util.EncryptUtil;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpSession;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseBody;

import javax.swing.*;
import java.util.Optional;

@Controller
@Slf4j
@RequestMapping(value = "/api/account")
@RequiredArgsConstructor
public class UserInfoController {

    private final IUserInfoService userInfoService;

    /*
    아이디 중복 확인
    */
    @ResponseBody
    @PostMapping(value = "getUserIdExists")
    public ExistsDTO getUserExists(HttpServletRequest request) throws Exception {

        log.info("{}.getUserExists Start!", this.getClass().getName());

        String userId = CmmUtil.nvl(request.getParameter("userId"));

        log.info("userId: {}", userId);

        UserInfoDTO pDTO = UserInfoDTO.builder()
                .userId(userId)
                .build();

        ExistsDTO rDTO = Optional.ofNullable(userInfoService.getUserIdExists(pDTO))
                .orElseGet(() -> ExistsDTO.builder().exists(false).build());

        log.info("{}.getUserExists End!", this.getClass().getName());

        return rDTO;
    }

    /*
    이메일 중복 확인
    */
    @ResponseBody
    @PostMapping(value = "getEmailExists")
    public ExistsDTO getEmailExists(HttpServletRequest request, HttpSession session) throws Exception {

        log.info(this.getClass().getName() + ".getEmailExists Start!");

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

        log.info(this.getClass().getName() + ".getEmailExists End!");

        return ExistsDTO.builder()
                .exists(rDTO.exists())
                .authNumber(0)
                .build();
    }

    /*
    이메일 인증번호 확인
    */
    @ResponseBody
    @PostMapping(value = "verifyEmailCode")
    public MsgDTO verifyEmailCode(HttpServletRequest request, HttpSession session) throws Exception {
        log.info(this.getClass().getName() + ".verifyEmailCode Start!");

        String email = CmmUtil.nvl(request.getParameter("email"));
        String code = CmmUtil.nvl(request.getParameter("code"));

        Object storedAuthCode = session.getAttribute("AUTH_" + email);

        log.info("email: {}, code: {}, storedAuthCode: {}", email, code, storedAuthCode);

        if (storedAuthCode != null && storedAuthCode.toString().equals(code)) {
            return MsgDTO.builder().result(1).msg("인증에 성공하였습니다.").build();
        }

        return MsgDTO.builder().result(0).msg("인증번호가 일치하지 않거나 만료되었습니다.").build();
    }

    /*
    회원가입
    */
    @ResponseBody
    @PostMapping(value = "insertUserInfo")
    public MsgDTO insertUserInfo(HttpServletRequest request) throws Exception {

        log.info("{}.insertUserInfo Start!", this.getClass().getName());

        String msg;

        String userId = CmmUtil.nvl(request.getParameter("userId"));
        String userName = CmmUtil.nvl(request.getParameter("userName"));
        String password = CmmUtil.nvl(request.getParameter("password"));
        String email = CmmUtil.nvl(request.getParameter("email"));
        String birthDate = CmmUtil.nvl(request.getParameter("birthDate"));
        String addr = CmmUtil.nvl(request.getParameter("addr"));
        String detailAddr = CmmUtil.nvl(request.getParameter("detailAddr"));

        log.info("userId: {}, userName: {}, email: {}, birthDate: {}, addr: {}, detailAddr: {}",
                userId, userName, email, birthDate, addr, detailAddr);

        UserInfoDTO pDTO = UserInfoDTO.builder()
                .userId(userId)
                .userName(userName)
                .password(EncryptUtil.encHashSHA256(password))
                .email(EncryptUtil.encAES128BCBC(email))
                .birthDate(birthDate)
                .addr(addr)
                .detailAddr(detailAddr)
                .build();

        int res = userInfoService.insertUserInfo(pDTO);

        log.info("회원가입 결과(res): {}", res);

        if (res == 1) {
            msg = "회원가입이 완료되었습니다.";
            request.getSession().setAttribute("SS_USER_ID", userId);
        } else if (res == 2) {
            msg = "이미 가입된 아이디입니다.";
        } else {
            msg = "오류로 인해 회원가입이 실패하였습니다.";
        }

        MsgDTO dto = MsgDTO.builder()
                .result(res)
                .msg(msg)
                .build();

        log.info("{}.insertUserInfo End!", this.getClass().getName());

        return dto;
    }

    /*
    프로필 이미지 수정
    */
    @ResponseBody
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

    /*
    로그인
    */
    @ResponseBody
    @PostMapping(value = "loginProc")
    public MsgDTO loginProc(HttpServletRequest request, HttpSession session) throws Exception {

        log.info("{}.loginProc Start!", this.getClass().getName());

        String msg;

        String userId = CmmUtil.nvl(request.getParameter("userId"));
        String password = CmmUtil.nvl(request.getParameter("password"));

        log.info("userId: {}, password: {}", userId, password);

        UserInfoDTO pDTO = UserInfoDTO.builder()
                .userId(userId)
                .password(EncryptUtil.encHashSHA256(password))
                .build();

        int res = userInfoService.getUserLogin(pDTO);

        log.info("res: {}", res);

        if (res == 1) {
            msg = "로그인이 성공했습니다.";

            UserInfoDTO rDTO = userInfoService.getUserInfo(pDTO);

            session.setAttribute("SS_USER_ID", userId);
            session.setAttribute("SS_USER_NO", rDTO.userNo());
            session.setAttribute("SS_USER_NAME", rDTO.userName());
            session.setAttribute("SS_USER_PROFILE_IMG", rDTO.profileImgUrl());
        } else {
            msg = "아이디와 비밀번호가 일치하지 않습니다.";
        }

        MsgDTO dto = MsgDTO.builder()
                .result(res)
                .msg(msg)
                .build();

        log.info("{}.loginProc End!", this.getClass().getName());

        return dto;
    }

    /*
    로그인 상태 확인
    */
    @ResponseBody
    @GetMapping(value = "status")
    public UserInfoDTO getLoginStatus(HttpSession session) {

        log.info("{}.getLoginStatus Start!", this.getClass().getName());

        Integer userNo = (Integer) session.getAttribute("SS_USER_NO");

        if (userNo == null) return UserInfoDTO.builder().build();

        String userId = CmmUtil.nvl((String) session.getAttribute("SS_USER_ID"));
        String userName = CmmUtil.nvl((String) session.getAttribute("SS_USER_NAME"));
        String profileImgUrl = CmmUtil.nvl((String) session.getAttribute("SS_USER_PROFILE_IMG"));

        log.info("{}.getLoginStatus Start!", this.getClass().getName());

        return UserInfoDTO.builder()
                .userNo(userNo)
                .userId(userId)
                .userName(userName)
                .profileImgUrl(profileImgUrl)
                .build();
    }

    /*
    로그아웃
    */
    @ResponseBody
    @PostMapping(value = "logout")
    public MsgDTO logout(HttpSession session) {

        log.info("{}.logout Start!", this.getClass().getName());

        session.invalidate();

        MsgDTO dto = MsgDTO.builder()
                .result(1)
                .msg("로그아웃하였습니다.")
                .build();

        log.info("{}.logout End!", this.getClass().getName());

        return dto;
    }

    /*
    아이디 찾기
    */
    @ResponseBody
    @PostMapping(value = "findUserId")
    public ExistsDTO findUserId(HttpServletRequest request, HttpSession session) throws Exception {

        log.info("{}.findUserId Start!", this.getClass().getName());

        String email = CmmUtil.nvl(request.getParameter("email"));
        String userName = CmmUtil.nvl(request.getParameter("userName"));

        log.info("email: {}, userName: {}", email, userName);

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

        return ExistsDTO.builder()
                .exists(rDTO.exists())
                .authNumber(0)
                .build();
    }

    /*
    메일과 이름으로 아이디 조회
    */
    @ResponseBody
    @PostMapping(value = "getUserId")
    public UserInfoDTO getUserId(HttpServletRequest request, HttpSession session) throws Exception {

        log.info("{}.getUserId Start!", this.getClass().getName());

        String email = CmmUtil.nvl(request.getParameter("email"));
        String userName = CmmUtil.nvl(request.getParameter("userName"));
        String code = CmmUtil.nvl(request.getParameter("code"));

        Object storedAuthCode = session.getAttribute("AUTH_" + email);

        log.info("email: {}, userName: {}, code: {}, storedAuthCode: {}", email, userName, code, storedAuthCode);

        if (storedAuthCode != null && storedAuthCode.toString().equals(code)) {
            UserInfoDTO pDTO = UserInfoDTO.builder()
                    .email(EncryptUtil.encAES128BCBC(email))
                    .userName(userName)
                    .build();

            UserInfoDTO rDTO = Optional.ofNullable(userInfoService.getUserId(pDTO))
                    .orElseGet(() -> UserInfoDTO.builder().build());

            session.removeAttribute("AUTH_" + email);

            return rDTO;
        }

        log.info("{}.getUserId End!", this.getClass().getName());

        return UserInfoDTO.builder().build();
    }

    /*
    비밀번호 찾기
    */
    @ResponseBody
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

        ExistsDTO rDTO = Optional.ofNullable(userInfoService.findUserPw(pDTO))
                .orElseGet(() -> ExistsDTO.builder().exists(false).authNumber(0).build());

        if (rDTO.exists()) {
            session.setAttribute("AUTH_" + email, rDTO.authNumber());
            session.setMaxInactiveInterval(3 * 60);
            session.setAttribute("UPDATE_PASSWORD_ID", userId);
        }

        log.info("{}.findUserPw End!", this.getClass().getName());

        return ExistsDTO.builder()
                .exists(rDTO.exists())
                .authNumber(0)
                .build();
    }

    /*
    비밀번호 수정
    */
    @ResponseBody
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
}