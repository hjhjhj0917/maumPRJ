package com.example.maum.controller;

import com.example.maum.controller.response.CommonResponse;
import com.example.maum.dto.MsgDTO;
import jakarta.persistence.OptimisticLockException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

@RestControllerAdvice
public class GlobalExceptionHandler {

    // ★ 즐겨찾기 이후 추가/수정
    // ResponseEntity.ok()로 응답하면 body에 담긴 실제 상태 코드와 무관하게 HTTP 상태가 항상 200으로
    // 나가 프론트에서 실패 응답도 성공으로 처리하는 문제가 있었음 — 실제 상태 코드를 그대로 실어 보냄
    @ExceptionHandler(OptimisticLockException.class)
    public ResponseEntity<CommonResponse<MsgDTO>> handleOptimisticLockException(OptimisticLockException e) {

        MsgDTO dto = MsgDTO.builder()
                .result(0)
                .msg("다른 사용자가 먼저 변경했습니다. 다시 시도해주세요. error : " + e.getMessage())
                .build();

        return ResponseEntity.status(HttpStatus.CONFLICT).body(
                CommonResponse.of(HttpStatus.CONFLICT, HttpStatus.CONFLICT.series().name(), dto));
    }

    // ★ 즐겨찾기 이후 추가/수정
    @ExceptionHandler(IllegalArgumentException.class)
    public ResponseEntity<CommonResponse<String>> handleIllegalArgumentException(IllegalArgumentException e) {

        return ResponseEntity.badRequest().body(
                CommonResponse.of(HttpStatus.BAD_REQUEST, HttpStatus.BAD_REQUEST.series().name(), e.getMessage()));
    }
}
