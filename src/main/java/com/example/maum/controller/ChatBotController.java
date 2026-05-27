package com.example.maum.controller;

import com.example.maum.dto.ChatBotDTO;
import com.example.maum.service.IChatBotService;
import com.example.maum.util.CmmUtil;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.MediaType;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.web.bind.annotation.*;
import reactor.core.publisher.Flux;

import java.util.List;

@RestController
@RequestMapping("/api/v1/chat")
@RequiredArgsConstructor
@Slf4j
public class ChatBotController {

    private final IChatBotService chatBotService;

    @PostMapping(value = "/stream", produces = MediaType.TEXT_EVENT_STREAM_VALUE) // SSE 방식 요청 후 응답생성되는 즉시 계속 전달 GPT 같은 느낌
    public Flux<String> chatStream(@RequestBody ChatBotDTO cDTO, @AuthenticationPrincipal Jwt jwt) throws Exception { // Flux는 비동기 스트림 객체

        log.info("{}.streamChat Start!", this.getClass().getName());

        String userNo = CmmUtil.nvl(jwt.getSubject());
        String message = CmmUtil.nvl(cDTO.message());

        log.info("userNo: {}, message: {}", userNo, message);

        ChatBotDTO pDTO = ChatBotDTO.builder()
                .userNo(userNo)
                .message(message)
                .build();

        return chatBotService.streamChat(pDTO);
    }

    @GetMapping("/history")
    public List<Object> getHistory(@AuthenticationPrincipal Jwt jwt) throws Exception {

        log.info("{}.getHistory Start!", this.getClass().getName());

        String userNo = CmmUtil.nvl(jwt.getSubject());

        return chatBotService.getHistory(userNo);
    }
}