package com.example.maum.controller;

import com.example.maum.auth.UserRole;
import com.example.maum.controller.response.CommonResponse;
import com.example.maum.dto.MsgDTO;
import com.example.maum.dto.UserInfoDTO;
import com.example.maum.service.IUserInfoService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@Slf4j
@RequestMapping(value = "/api/v1/reg")
@RequiredArgsConstructor
@RestController
public class UserRegController {

    private final IUserInfoService userInfoService;
    private final PasswordEncoder bCryptPasswordEncoder;


    @PostMapping(value = "getUserIdExists")
    public ResponseEntity<CommonResponse<UserInfoDTO>> getUserIdExists(@RequestBody UserInfoDTO pDTO) throws Exception {

        log.info("{}.getUserIdExists Start!", this.getClass().getName());

        UserInfoDTO rDTO = userInfoService.getUserIdExists(pDTO);

        log.info("{}.getUserIdExists End!", this.getClass().getName());

        return ResponseEntity.ok(
                CommonResponse.of(HttpStatus.OK, HttpStatus.OK.series().name(), rDTO)
        );
    }


    @PostMapping(value = "insertUserInfo")
    public ResponseEntity<?> insertUserInfo(@Valid @RequestBody UserInfoDTO pDTO, BindingResult bindingResult) {

        log.info("{}.insertUserInfo Start!", this.getClass().getName());

        if (bindingResult.hasErrors()) {
            log.info("error: {}", bindingResult);
            return CommonResponse.getErrors(bindingResult);
        }

        int res = 0;
        String msg = "";
        MsgDTO dto;

        log.info("pDTO: {}", pDTO);

        try {

            UserInfoDTO nDTO = UserInfoDTO.createUser(
                    pDTO,
                    bCryptPasswordEncoder.encode(pDTO.password()),
                    UserRole.USER.getValue()
            );

            res = userInfoService.insertUserInfo(nDTO);

            log.info("회원가입 결과(res): {}", res);

            if (res == 1) {
                msg = "회원가입되었습니다.";
            } else if (res == 2) {
                msg = "이미 가입된 아이디입니다.";
            } else {
                msg = "오류로 인해 회원가입이 실패하였습니다.";
            }
        } catch (Exception e) {

            msg = "실패하였습니다. : " + e;
            res = 2;
            log.error("회원가입 중 예외 발생", e);
        } finally {

            dto = MsgDTO.builder().result(res).msg(msg).build();
            log.info("{}.insertUserInfo End!", this.getClass().getName());
        }

        return ResponseEntity.ok(
                CommonResponse.of(HttpStatus.OK, HttpStatus.OK.series().name(), dto)
        );
    }
}
