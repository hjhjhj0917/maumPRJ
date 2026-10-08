package com.example.maum.controller;

import com.example.maum.util.CmmUtil;
import com.example.maum.controller.response.CommonResponse;
import com.example.maum.service.ISttService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;
import java.util.Optional;

@RestController
@RequestMapping(value = "/api/v1")
@RequiredArgsConstructor
@Slf4j
// ★ 즐겨찾기 이후 추가/수정
public class SttController {

    private final ISttService sttService;

    @PostMapping(value = "/stt")
    public ResponseEntity<CommonResponse<String>> speechToText(@RequestParam("audio") MultipartFile audio,
                                                                @AuthenticationPrincipal Jwt jwt) throws Exception {

        log.info("{}.speechToText Start!", this.getClass().getName());

        log.info("userNo: {}, audioSize: {}, contentType: {}", CmmUtil.nvl(jwt.getSubject()), audio.getSize(), audio.getContentType());

        String text = Optional.ofNullable(sttService.transcribe(audio)).orElse("");

        log.info("{}.speechToText End!", this.getClass().getName());

        return ResponseEntity.ok(
                CommonResponse.of(HttpStatus.OK, HttpStatus.OK.series().name(), text)
        );
    }
}
