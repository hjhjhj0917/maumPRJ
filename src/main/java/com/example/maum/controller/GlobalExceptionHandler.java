package com.example.maum.controller;

import com.example.maum.controller.response.CommonResponse;
import com.example.maum.dto.MsgDTO;
import jakarta.persistence.OptimisticLockException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

@RestControllerAdvice /* 컨트롤러 계층으로 들어온 요청이 예외를 던지는 순간, 그 예외가 사용자에게 전달되지 않도록 중간에서 낚아챔 */
public class GlobalExceptionHandler {

    @ExceptionHandler(OptimisticLockException.class) /* 데이터베이스의 동시성 문제 해결을 위해 발생함 */
    public ResponseEntity<CommonResponse<MsgDTO>> handleOptimisticLockException(OptimisticLockException e) {

        MsgDTO dto = MsgDTO.builder()
                .result(0)
                .msg("다른 사용자가 먼저 변경했습니다. 다시 시도해주세요. error : " + e.getMessage())
                .build();

        return ResponseEntity.ok(
                CommonResponse.of(HttpStatus.CONFLICT, HttpStatus.CONFLICT.series().name(), dto));
    }

    @ExceptionHandler(IllegalArgumentException.class) /* 입력값에 문제가 있는 경우 발생함 */
    public ResponseEntity<CommonResponse<String>> handleIllegalArgumentException(IllegalArgumentException e) {

        return ResponseEntity.ok(
                CommonResponse.of(HttpStatus.BAD_REQUEST, HttpStatus.BAD_REQUEST.series().name(), e.getMessage()));
    }
}
