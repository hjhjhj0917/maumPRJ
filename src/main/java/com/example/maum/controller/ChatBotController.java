package com.example.maum.controller;

import com.example.maum.controller.response.CommonResponse;
import com.example.maum.dto.ChatBotDTO;
import com.example.maum.dto.ChatMessageDTO;
import com.example.maum.service.IChatBotService;
import com.example.maum.util.CmmUtil;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.web.bind.annotation.*;
import reactor.core.publisher.Flux;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

@RestController
@RequestMapping("/api/v1/chat")
@RequiredArgsConstructor
@Slf4j
public class ChatBotController {

    private final IChatBotService chatBotService;

    @PostMapping(value = "/stream", produces = MediaType.TEXT_EVENT_STREAM_VALUE) /* 한 번에 처리하는게 아니라 조각조각 처리 */
    public Flux<String> chatStream(@RequestBody ChatBotDTO cDTO, @AuthenticationPrincipal Jwt jwt) throws Exception { /* 조가조각 처리가 가능한 객체 */

        log.info("{}.chatStream Start!", this.getClass().getName());

        String userNo = CmmUtil.nvl(jwt.getSubject());
        String message = CmmUtil.nvl(cDTO.message());

        log.info("userNo: {}, message: {}", userNo, message);

        ChatBotDTO pDTO = ChatBotDTO.builder()
                .userNo(userNo)
                .message(message)
                .build();

        Flux<String> res = Optional.ofNullable(chatBotService.streamChat(pDTO))
                .orElseGet(Flux::empty);

        log.info("{}.chatStream End!", this.getClass().getName());

        return res;
    }


    @GetMapping("/history")
    public ResponseEntity<CommonResponse<List<ChatMessageDTO>>> getHistory(@AuthenticationPrincipal Jwt jwt) throws Exception {

        log.info("{}.getHistory Start!", this.getClass().getName());

        String userNo = CmmUtil.nvl(jwt.getSubject());

        List<ChatMessageDTO> rList = Optional.ofNullable(chatBotService.getHistory(userNo))
                .orElseGet(ArrayList::new);

        log.info("{}.getHistory End!", this.getClass().getName());

        return ResponseEntity.ok(
                CommonResponse.of(HttpStatus.OK, HttpStatus.OK.series().name(), rList)
        );
    }
}