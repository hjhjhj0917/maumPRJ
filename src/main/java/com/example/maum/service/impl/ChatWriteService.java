package com.example.maum.service.impl;

import com.example.maum.chat.ChatMessageStatus;
import com.example.maum.chat.ChatRole;
import com.example.maum.dto.ChatMessageDTO;
import com.example.maum.repository.ChatMessageRepository;
import com.example.maum.repository.ChatSessionRepository;
import com.example.maum.repository.UserRepository;
import com.example.maum.repository.entity.ChatMessageEntity;
import com.example.maum.repository.entity.ChatSessionEntity;
import com.example.maum.service.IChatWriteService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Duration;
import java.time.Instant;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.UUID;

@Slf4j
@Service
@RequiredArgsConstructor
public class ChatWriteService implements IChatWriteService {

    static final int MAX_MESSAGE_LENGTH = 2000;
    static final int MAX_TITLE_LENGTH = 30;
    // 이 시간이 지나도 STREAMING 이면 서버가 중간에 죽은 것으로 보고 끊긴 답변으로 처리함
    private static final Duration STALE_STREAMING = Duration.ofMinutes(5);
    private static final List<ChatMessageStatus> HISTORY_STATUSES =
            List.of(ChatMessageStatus.COMPLETE, ChatMessageStatus.INTERRUPTED);

    private final ChatSessionRepository chatSessionRepository;
    private final ChatMessageRepository chatMessageRepository;
    private final UserRepository userRepository;

    @Override
    @Transactional
    public Turn beginTurn(String userPublicId, String sessionPublicId, String message, UUID clientMsgId) {

        log.info("{}.beginTurn Start!", this.getClass().getName());

        if (message == null || message.isBlank()) {
            throw new IllegalArgumentException("메시지를 입력해주세요.");
        }
        if (message.length() > MAX_MESSAGE_LENGTH) {
            throw new IllegalArgumentException("메시지는 " + MAX_MESSAGE_LENGTH + "자까지 보낼 수 있어요.");
        }

        Long userId = userRepository.findByPublicId(parseUuid(userPublicId))
                .orElseThrow(() -> new IllegalArgumentException("사용자 정보를 찾을 수 없습니다."))
                .getId();
        ChatSessionEntity session = chatSessionRepository.findForUpdate(parseUuid(sessionPublicId), userId)
                .orElseThrow(() -> new IllegalArgumentException("채팅방을 찾을 수 없어요. 새로고침 후 다시 시도해주세요."));

        UUID msgId = clientMsgId != null ? clientMsgId : UUID.randomUUID();

        // 같은 요청이 다시 오면(네트워크 재시도) 새로 만들지 않고 저장된 답변을 돌려줌
        if (chatMessageRepository.findBySessionIdAndClientMsgIdAndRole(session.getId(), msgId, ChatRole.USER).isPresent()) {
            return replayOf(session.getId(), msgId);
        }

        chatMessageRepository.interruptStale(session.getId(), Instant.now().minus(STALE_STREAMING));
        if (chatMessageRepository.existsBySessionIdAndStatus(session.getId(), ChatMessageStatus.STREAMING)) {
            throw new IllegalStateException("앞선 답변을 만들고 있어요. 잠시 후 다시 보내주세요.");
        }

        // 최근 4턴을 시간순으로(AI 서버에 맥락으로 전달). 새 메시지를 저장하기 전에 조회해야 중복되지 않음
        List<ChatMessageEntity> recent = new ArrayList<>(chatMessageRepository
                .findTop8BySessionIdAndStatusInOrderBySeqDesc(session.getId(), HISTORY_STATUSES));
        Collections.reverse(recent);
        List<ChatMessageDTO> history = recent.stream()
                .map(m -> ChatMessageDTO.builder().role(toWireRole(m.getRole())).content(m.getContent()).build())
                .toList();

        chatMessageRepository.save(ChatMessageEntity.builder()
                .sessionId(session.getId()).seq(session.nextSeq()).role(ChatRole.USER)
                .content(message).status(ChatMessageStatus.COMPLETE).clientMsgId(msgId).build());
        ChatMessageEntity bot = chatMessageRepository.save(ChatMessageEntity.builder()
                .sessionId(session.getId()).seq(session.nextSeq()).role(ChatRole.BOT)
                .content("").status(ChatMessageStatus.STREAMING).clientMsgId(msgId).build());

        // 제목이 비어 있으면(첫 메시지) 사용자 메시지 앞부분으로 자동 설정
        if (session.getTitle() == null || session.getTitle().isBlank()) {
            session.changeTitle(message.length() > MAX_TITLE_LENGTH ? message.substring(0, MAX_TITLE_LENGTH) + "..." : message);
        }

        log.info("{}.beginTurn End!", this.getClass().getName());

        return new Turn(bot.getId(), history, null);
    }

    @Override
    @Transactional
    public void finishTurn(Long botMessageId, ChatMessageStatus status, String content, boolean hasAudio,
                           List<Map<String, Object>> cards) {

        log.info("{}.finishTurn Start! status: {}", this.getClass().getName(), status);

        chatMessageRepository.findById(botMessageId)
                .ifPresent(m -> m.finish(status, content, hasAudio, cards));

        log.info("{}.finishTurn End!", this.getClass().getName());
    }

    private Turn replayOf(Long sessionId, UUID msgId) {
        ChatMessageEntity bot = chatMessageRepository
                .findBySessionIdAndClientMsgIdAndRole(sessionId, msgId, ChatRole.BOT)
                .orElseThrow(() -> new IllegalStateException("이전 요청을 처리하고 있어요. 잠시 후 확인해주세요."));

        if (bot.getStatus() == ChatMessageStatus.STREAMING || bot.getContent().isBlank()) {
            throw new IllegalStateException("이전 요청을 처리하고 있어요. 잠시 후 확인해주세요.");
        }
        return new Turn(bot.getId(), List.of(), bot.getContent());
    }

    static String toWireRole(ChatRole role) {
        return role == ChatRole.BOT ? "bot" : "user";
    }

    private UUID parseUuid(String value) {
        try {
            return UUID.fromString(value);
        } catch (Exception e) {
            throw new IllegalArgumentException("요청 정보가 올바르지 않습니다.");
        }
    }
}
