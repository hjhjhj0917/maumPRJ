package com.example.maum.global.controller;

import com.example.maum.global.controller.response.CommonResponse;
import com.example.maum.global.dto.MsgDTO;
import jakarta.persistence.OptimisticLockException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestController;

@RestController
@ControllerAdvice
public class GlobalExceptionHandler {

    @ExceptionHandler(OptimisticLockException.class)
    public ResponseEntity<CommonResponse<MsgDTO>> handleOptimisticLockException(OptimisticLockException e) {

        MsgDTO dto = MsgDTO.builder()
                .result(0)
                .msg("다른 사용자가 먼저 변경했습니다. 다시 시도해주세요. error : " + e.getMessage())
                .build();

        return ResponseEntity.ok(
                CommonResponse.of(HttpStatus.CONFLICT, HttpStatus.CONFLICT.series().name(), dto));
    }

    @ExceptionHandler(IllegalArgumentException.class)
    public ResponseEntity<CommonResponse<String>> handleIllegalArgumentException(IllegalArgumentException e) {

        return ResponseEntity.ok(
                CommonResponse.of(HttpStatus.BAD_REQUEST, HttpStatus.BAD_REQUEST.series().name(), e.getMessage()));
    }
}
