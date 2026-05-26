package com.example.maum.controller;

import com.example.maum.dto.ChatBotDTO;
import com.example.maum.service.IChatBotService;
import com.example.maum.util.CmmUtil;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.MediaType;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import reactor.core.publisher.Flux;

@RestController
@RequestMapping("/api/v1/chat")
@RequiredArgsConstructor
@Slf4j
public class ChatBotController {

    private final IChatBotService chatBotService;

    @PostMapping(value = "/stream", produces = MediaType.TEXT_EVENT_STREAM_VALUE)
    public Flux<String> chatStream(@RequestBody ChatBotDTO cDTO, @AuthenticationPrincipal Jwt jwt) throws Exception {

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
}