package com.example.maum.controller;

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

@RestController
@RequestMapping("/api/v1/stt")
@RequiredArgsConstructor
@Slf4j
public class SttController {

    private final ISttService sttService;

    @PostMapping
    public ResponseEntity<CommonResponse<String>> speechToText(@RequestParam("audio") MultipartFile audio,
                                                                @AuthenticationPrincipal Jwt jwt) throws Exception {

        log.info("{}.speechToText Start!", this.getClass().getName());

        String text = sttService.transcribe(audio);

        log.info("{}.speechToText End!", this.getClass().getName());

        return ResponseEntity.ok(
                CommonResponse.of(HttpStatus.OK, HttpStatus.OK.series().name(), text)
        );
    }
}
