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

@RestController
@RequestMapping("/api/v1/chat")
@RequiredArgsConstructor
@Slf4j
public class ChatBotController {

    private final IChatBotService chatBotService;

    // ★ 즐겨찾기 이후 추가/수정
    @PostMapping(value = "/stream", produces = MediaType.TEXT_EVENT_STREAM_VALUE) /* 응답을 한 번에 주지 않고 스트리밍으로 조각조각 전달 */
    public Flux<String> chatStream(@RequestBody ChatBotDTO cDTO, @AuthenticationPrincipal Jwt jwt) throws Exception {

        log.info("{}.chatStream Start!", this.getClass().getName());

        String userNo = CmmUtil.nvl(jwt.getSubject());
        String message = CmmUtil.nvl(cDTO.message());
        Integer chatRoomNo = cDTO.chatRoomNo();

        log.info("userNo: {}, chatRoomNo: {}, message: {}", userNo, chatRoomNo, message);

        ChatBotDTO pDTO = ChatBotDTO.builder()
                .userNo(userNo)
                .message(message)
                .chatRoomNo(chatRoomNo)
                .build();

        Flux<String> res = Optional.ofNullable(chatBotService.streamChat(pDTO))
                .orElseGet(Flux::empty);

        log.info("{}.chatStream End!", this.getClass().getName());

        return res;
    }

    // ★ 즐겨찾기 이후 추가/수정
    @PostMapping("/rooms")
    public ResponseEntity<CommonResponse<ChatRoomDTO>> createRoom(@AuthenticationPrincipal Jwt jwt) throws Exception {

        log.info("{}.createRoom Start!", this.getClass().getName());

        String userNo = CmmUtil.nvl(jwt.getSubject());

        ChatRoomDTO pDTO = ChatRoomDTO.builder().userNo(userNo).build();

        ChatRoomDTO rDTO = chatBotService.createRoom(pDTO);

        log.info("{}.createRoom End!", this.getClass().getName());

        return ResponseEntity.ok(
                CommonResponse.of(HttpStatus.OK, HttpStatus.OK.series().name(), rDTO)
        );
    }

    // ★ 즐겨찾기 이후 추가/수정
    @GetMapping("/rooms")
    public ResponseEntity<CommonResponse<List<ChatRoomDTO>>> getRooms(@AuthenticationPrincipal Jwt jwt) throws Exception {

        log.info("{}.getRooms Start!", this.getClass().getName());

        String userNo = CmmUtil.nvl(jwt.getSubject());

        ChatRoomDTO pDTO = ChatRoomDTO.builder().userNo(userNo).build();

        List<ChatRoomDTO> rList = Optional.ofNullable(chatBotService.getRooms(pDTO))
                .orElseGet(ArrayList::new);

        log.info("{}.getRooms End!", this.getClass().getName());

        return ResponseEntity.ok(
                CommonResponse.of(HttpStatus.OK, HttpStatus.OK.series().name(), rList)
        );
    }

    // ★ 즐겨찾기 이후 추가/수정
    @PostMapping("/rooms/{chatRoomNo}/title")
    public ResponseEntity<CommonResponse<Integer>> renameRoom(
            @PathVariable Integer chatRoomNo, @RequestBody ChatRoomDTO dDTO, @AuthenticationPrincipal Jwt jwt) throws Exception {

        log.info("{}.renameRoom Start!", this.getClass().getName());

        String userNo = CmmUtil.nvl(jwt.getSubject());

        ChatRoomDTO pDTO = ChatRoomDTO.builder()
                .chatRoomNo(chatRoomNo)
                .userNo(userNo)
                .roomTitle(CmmUtil.nvl(dDTO.roomTitle()))
                .build();

        chatBotService.renameRoom(pDTO);

        log.info("{}.renameRoom End!", this.getClass().getName());

        return ResponseEntity.ok(
                CommonResponse.of(HttpStatus.OK, "이름이 변경되었습니다.", chatRoomNo)
        );
    }

    // ★ 즐겨찾기 이후 추가/수정
    @PostMapping("/rooms/{chatRoomNo}/pin")
    public ResponseEntity<CommonResponse<Integer>> pinRoom(
            @PathVariable Integer chatRoomNo, @RequestBody ChatRoomDTO dDTO, @AuthenticationPrincipal Jwt jwt) throws Exception {

        log.info("{}.pinRoom Start!", this.getClass().getName());

        String userNo = CmmUtil.nvl(jwt.getSubject());
        Integer isPinned = dDTO.isPinned();

        ChatRoomDTO pDTO = ChatRoomDTO.builder()
                .chatRoomNo(chatRoomNo)
                .userNo(userNo)
                .isPinned(isPinned)
                .build();

        chatBotService.pinRoom(pDTO);

        String msg = (isPinned != null && isPinned == 1) ? "상단에 고정되었습니다." : "고정이 해제되었습니다.";

        log.info("{}.pinRoom End!", this.getClass().getName());

        return ResponseEntity.ok(
                CommonResponse.of(HttpStatus.OK, msg, chatRoomNo)
        );
    }

    // ★ 즐겨찾기 이후 추가/수정
    @DeleteMapping("/rooms/{chatRoomNo}")
    public ResponseEntity<CommonResponse<Integer>> deleteRoom(
            @PathVariable Integer chatRoomNo, @AuthenticationPrincipal Jwt jwt) throws Exception {

        log.info("{}.deleteRoom Start!", this.getClass().getName());

        String userNo = CmmUtil.nvl(jwt.getSubject());

        ChatRoomDTO pDTO = ChatRoomDTO.builder()
                .chatRoomNo(chatRoomNo)
                .userNo(userNo)
                .build();

        chatBotService.deleteRoom(pDTO);

        log.info("{}.deleteRoom End!", this.getClass().getName());

        return ResponseEntity.ok(
                CommonResponse.of(HttpStatus.OK, "채팅방이 삭제되었습니다.", chatRoomNo)
        );
    }

    // ★ 즐겨찾기 이후 추가/수정
    @GetMapping("/rooms/{chatRoomNo}/messages")
    public ResponseEntity<CommonResponse<List<ChatMessageDTO>>> getRoomMessages(
            @PathVariable Integer chatRoomNo, @AuthenticationPrincipal Jwt jwt) throws Exception {

        log.info("{}.getRoomMessages Start!", this.getClass().getName());

        String userNo = CmmUtil.nvl(jwt.getSubject());

        ChatRoomDTO pDTO = ChatRoomDTO.builder()
                .chatRoomNo(chatRoomNo)
                .userNo(userNo)
                .build();

        List<ChatMessageDTO> rList = Optional.ofNullable(chatBotService.getRoomMessages(pDTO))
                .orElseGet(ArrayList::new);

        log.info("{}.getRoomMessages End!", this.getClass().getName());

        return ResponseEntity.ok(
                CommonResponse.of(HttpStatus.OK, HttpStatus.OK.series().name(), rList)
        );
    }

    // ★ 즐겨찾기 이후 추가/수정
    @PostMapping("/messages/{chatMsgNo}/tts")
    public ResponseEntity<CommonResponse<List<String>>> synthesizeMessageAudio(
            @PathVariable Long chatMsgNo, @AuthenticationPrincipal Jwt jwt) throws Exception {

        log.info("{}.synthesizeMessageAudio Start!", this.getClass().getName());

        String userNo = CmmUtil.nvl(jwt.getSubject());

        List<String> audioChunks = Optional.ofNullable(chatBotService.synthesizeMessageAudio(chatMsgNo, userNo))
                .orElseGet(ArrayList::new);

        log.info("{}.synthesizeMessageAudio End!", this.getClass().getName());

        return ResponseEntity.ok(
                CommonResponse.of(HttpStatus.OK, HttpStatus.OK.series().name(), audioChunks)
        );
    }
}
