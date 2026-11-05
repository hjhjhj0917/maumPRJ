package com.example.maum.controller;

import com.example.maum.controller.response.CommonResponse;
import com.example.maum.dto.ChatBotDTO;
import com.example.maum.dto.ChatMessageDTO;
import com.example.maum.dto.ChatRoomDTO;
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

// 사용자는 항상 JWT subject(사용자 publicId)로 정하고, 요청 본문에 담긴 사용자 값은 쓰지 않음
@RestController
@RequestMapping(value = "/api/v1/chat")
@RequiredArgsConstructor
@Slf4j
public class ChatBotController {

    private final IChatBotService chatBotService;

    @PostMapping(value = "/stream", produces = MediaType.TEXT_EVENT_STREAM_VALUE) /* 응답을 한 번에 주지 않고 스트리밍으로 조각조각 전달 */
    public Flux<String> chatStream(@RequestBody ChatBotDTO cDTO, @AuthenticationPrincipal Jwt jwt) throws Exception {

        log.info("{}.chatStream Start!", this.getClass().getName());

        String userPublicId = CmmUtil.nvl(jwt.getSubject());
        String message = CmmUtil.nvl(cDTO.message());

        log.info("userPublicId: {}, sessionPublicId: {}, messageLength: {}", userPublicId, cDTO.sessionPublicId(), message.length());

        ChatBotDTO pDTO = ChatBotDTO.builder()
                .userNo(userPublicId)
                .message(message)
                .sessionPublicId(CmmUtil.nvl(cDTO.sessionPublicId()))
                .clientMsgId(cDTO.clientMsgId())
                .build();

        Flux<String> res = Optional.ofNullable(chatBotService.streamChat(pDTO))
                .orElseGet(Flux::empty);

        log.info("{}.chatStream End!", this.getClass().getName());

        return res;
    }

    @PostMapping(value = "/rooms")
    public ResponseEntity<CommonResponse<ChatRoomDTO>> createRoom(@AuthenticationPrincipal Jwt jwt) throws Exception {

        log.info("{}.createRoom Start!", this.getClass().getName());

        String userPublicId = CmmUtil.nvl(jwt.getSubject());

        ChatRoomDTO rDTO = Optional.ofNullable(chatBotService.createRoom(ChatRoomDTO.builder().userPublicId(userPublicId).build()))
                .orElseThrow(() -> new RuntimeException("채팅방 생성에 실패하였습니다."));

        log.info("{}.createRoom End!", this.getClass().getName());

        return ResponseEntity.ok(
                CommonResponse.of(HttpStatus.OK, HttpStatus.OK.series().name(), rDTO)
        );
    }

    @GetMapping(value = "/rooms")
    public ResponseEntity<CommonResponse<List<ChatRoomDTO>>> getRooms(@AuthenticationPrincipal Jwt jwt) throws Exception {

        log.info("{}.getRooms Start!", this.getClass().getName());

        String userPublicId = CmmUtil.nvl(jwt.getSubject());

        List<ChatRoomDTO> rList = Optional.ofNullable(chatBotService.getRooms(ChatRoomDTO.builder().userPublicId(userPublicId).build()))
                .orElseGet(ArrayList::new);

        log.info("{}.getRooms End!", this.getClass().getName());

        return ResponseEntity.ok(
                CommonResponse.of(HttpStatus.OK, HttpStatus.OK.series().name(), rList)
        );
    }

    @PostMapping(value = "/rooms/{publicId}/title")
    public ResponseEntity<CommonResponse<String>> renameRoom(
            @PathVariable String publicId, @RequestBody ChatRoomDTO dDTO, @AuthenticationPrincipal Jwt jwt) throws Exception {

        log.info("{}.renameRoom Start!", this.getClass().getName());

        String userPublicId = CmmUtil.nvl(jwt.getSubject());

        log.info("userPublicId: {}, publicId: {}, titleLength: {}", userPublicId, publicId, CmmUtil.nvl(dDTO.roomTitle()).length());

        chatBotService.renameRoom(ChatRoomDTO.builder()
                .publicId(publicId)
                .userPublicId(userPublicId)
                .roomTitle(CmmUtil.nvl(dDTO.roomTitle()))
                .build());

        log.info("{}.renameRoom End!", this.getClass().getName());

        return ResponseEntity.ok(
                CommonResponse.of(HttpStatus.OK, "이름이 변경되었습니다.", publicId)
        );
    }

    @PostMapping(value = "/rooms/{publicId}/pin")
    public ResponseEntity<CommonResponse<String>> pinRoom(
            @PathVariable String publicId, @RequestBody ChatRoomDTO dDTO, @AuthenticationPrincipal Jwt jwt) throws Exception {

        log.info("{}.pinRoom Start!", this.getClass().getName());

        String userPublicId = CmmUtil.nvl(jwt.getSubject());
        boolean isPinned = Boolean.TRUE.equals(dDTO.isPinned());

        log.info("userPublicId: {}, publicId: {}, isPinned: {}", userPublicId, publicId, isPinned);

        chatBotService.pinRoom(ChatRoomDTO.builder()
                .publicId(publicId)
                .userPublicId(userPublicId)
                .isPinned(isPinned)
                .build());

        log.info("{}.pinRoom End!", this.getClass().getName());

        return ResponseEntity.ok(
                CommonResponse.of(HttpStatus.OK, isPinned ? "상단에 고정되었습니다." : "고정이 해제되었습니다.", publicId)
        );
    }

    @DeleteMapping(value = "/rooms/{publicId}")
    public ResponseEntity<CommonResponse<String>> deleteRoom(
            @PathVariable String publicId, @AuthenticationPrincipal Jwt jwt) throws Exception {

        log.info("{}.deleteRoom Start!", this.getClass().getName());

        String userPublicId = CmmUtil.nvl(jwt.getSubject());

        log.info("userPublicId: {}, publicId: {}", userPublicId, publicId);

        chatBotService.deleteRoom(ChatRoomDTO.builder().publicId(publicId).userPublicId(userPublicId).build());

        log.info("{}.deleteRoom End!", this.getClass().getName());

        return ResponseEntity.ok(
                CommonResponse.of(HttpStatus.OK, "채팅방이 삭제되었습니다.", publicId)
        );
    }

    @GetMapping(value = "/rooms/{publicId}/messages")
    public ResponseEntity<CommonResponse<List<ChatMessageDTO>>> getRoomMessages(
            @PathVariable String publicId, @AuthenticationPrincipal Jwt jwt) throws Exception {

        log.info("{}.getRoomMessages Start!", this.getClass().getName());

        String userPublicId = CmmUtil.nvl(jwt.getSubject());

        log.info("userPublicId: {}, publicId: {}", userPublicId, publicId);

        List<ChatMessageDTO> rList = Optional.ofNullable(chatBotService.getRoomMessages(
                        ChatRoomDTO.builder().publicId(publicId).userPublicId(userPublicId).build()))
                .orElseGet(ArrayList::new);

        log.info("{}.getRoomMessages End!", this.getClass().getName());

        return ResponseEntity.ok(
                CommonResponse.of(HttpStatus.OK, HttpStatus.OK.series().name(), rList)
        );
    }

    @PostMapping(value = "/messages/{chatMsgNo}/tts")
    public ResponseEntity<CommonResponse<List<String>>> synthesizeMessageAudio(
            @PathVariable Long chatMsgNo, @AuthenticationPrincipal Jwt jwt) throws Exception {

        log.info("{}.synthesizeMessageAudio Start!", this.getClass().getName());

        String userPublicId = CmmUtil.nvl(jwt.getSubject());

        log.info("userPublicId: {}, chatMsgNo: {}", userPublicId, chatMsgNo);

        List<String> audioChunks = Optional.ofNullable(chatBotService.synthesizeMessageAudio(chatMsgNo, userPublicId))
                .orElseGet(ArrayList::new);

        log.info("{}.synthesizeMessageAudio End!", this.getClass().getName());

        return ResponseEntity.ok(
                CommonResponse.of(HttpStatus.OK, HttpStatus.OK.series().name(), audioChunks)
        );
    }
}
