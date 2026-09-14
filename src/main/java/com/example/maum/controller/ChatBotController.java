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

    @PostMapping(value = "/stream", produces = MediaType.TEXT_EVENT_STREAM_VALUE) /* 한 번에 처리하는게 아니라 조각조각 처리 */
    public Flux<String> chatStream(@RequestBody ChatBotDTO cDTO, @AuthenticationPrincipal Jwt jwt) throws Exception { /* 조가조각 처리가 가능한 객체 */

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

    @PostMapping("/rooms")
    public ResponseEntity<CommonResponse<ChatRoomDTO>> createRoom(@AuthenticationPrincipal Jwt jwt) throws Exception {

        log.info("{}.createRoom Start!", this.getClass().getName());

        String userNo = CmmUtil.nvl(jwt.getSubject());

        ChatRoomDTO rDTO = chatBotService.createRoom(userNo);

        log.info("{}.createRoom End!", this.getClass().getName());

        return ResponseEntity.ok(
                CommonResponse.of(HttpStatus.OK, HttpStatus.OK.series().name(), rDTO)
        );
    }

    @GetMapping("/rooms")
    public ResponseEntity<CommonResponse<List<ChatRoomDTO>>> getRooms(@AuthenticationPrincipal Jwt jwt) throws Exception {

        log.info("{}.getRooms Start!", this.getClass().getName());

        String userNo = CmmUtil.nvl(jwt.getSubject());

        List<ChatRoomDTO> rList = Optional.ofNullable(chatBotService.getRooms(userNo))
                .orElseGet(ArrayList::new);

        log.info("{}.getRooms End!", this.getClass().getName());

        return ResponseEntity.ok(
                CommonResponse.of(HttpStatus.OK, HttpStatus.OK.series().name(), rList)
        );
    }

    @PostMapping("/rooms/{chatRoomNo}/title")
    public ResponseEntity<CommonResponse<Integer>> renameRoom(
            @PathVariable Integer chatRoomNo, @RequestBody ChatRoomDTO dDTO, @AuthenticationPrincipal Jwt jwt) throws Exception {

        log.info("{}.renameRoom Start!", this.getClass().getName());

        String userNo = CmmUtil.nvl(jwt.getSubject());

        chatBotService.renameRoom(userNo, chatRoomNo, CmmUtil.nvl(dDTO.roomTitle()));

        log.info("{}.renameRoom End!", this.getClass().getName());

        return ResponseEntity.ok(
                CommonResponse.of(HttpStatus.OK, "이름이 변경되었습니다.", chatRoomNo)
        );
    }

    @PostMapping("/rooms/{chatRoomNo}/pin")
    public ResponseEntity<CommonResponse<Integer>> pinRoom(
            @PathVariable Integer chatRoomNo, @RequestBody ChatRoomDTO dDTO, @AuthenticationPrincipal Jwt jwt) throws Exception {

        log.info("{}.pinRoom Start!", this.getClass().getName());

        String userNo = CmmUtil.nvl(jwt.getSubject());
        Integer isPinned = dDTO.isPinned();

        chatBotService.pinRoom(userNo, chatRoomNo, isPinned);

        String msg = (isPinned != null && isPinned == 1) ? "상단에 고정되었습니다." : "고정이 해제되었습니다.";

        log.info("{}.pinRoom End!", this.getClass().getName());

        return ResponseEntity.ok(
                CommonResponse.of(HttpStatus.OK, msg, chatRoomNo)
        );
    }

    @DeleteMapping("/rooms/{chatRoomNo}")
    public ResponseEntity<CommonResponse<Integer>> deleteRoom(
            @PathVariable Integer chatRoomNo, @AuthenticationPrincipal Jwt jwt) throws Exception {

        log.info("{}.deleteRoom Start!", this.getClass().getName());

        String userNo = CmmUtil.nvl(jwt.getSubject());

        chatBotService.deleteRoom(userNo, chatRoomNo);

        log.info("{}.deleteRoom End!", this.getClass().getName());

        return ResponseEntity.ok(
                CommonResponse.of(HttpStatus.OK, "채팅방이 삭제되었습니다.", chatRoomNo)
        );
    }

    @GetMapping("/rooms/{chatRoomNo}/messages")
    public ResponseEntity<CommonResponse<List<ChatMessageDTO>>> getRoomMessages(
            @PathVariable Integer chatRoomNo, @AuthenticationPrincipal Jwt jwt) throws Exception {

        log.info("{}.getRoomMessages Start!", this.getClass().getName());

        String userNo = CmmUtil.nvl(jwt.getSubject());

        List<ChatMessageDTO> rList = Optional.ofNullable(chatBotService.getRoomMessages(userNo, chatRoomNo))
                .orElseGet(ArrayList::new);

        log.info("{}.getRoomMessages End!", this.getClass().getName());

        return ResponseEntity.ok(
                CommonResponse.of(HttpStatus.OK, HttpStatus.OK.series().name(), rList)
        );
    }
}
