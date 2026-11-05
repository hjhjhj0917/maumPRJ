package com.example.maum.service.impl;

import com.example.maum.chat.ChatMessageStatus;
import com.example.maum.dto.ChatBotDTO;
import com.example.maum.dto.ChatMessageDTO;
import com.example.maum.dto.ChatRoomDTO;
import com.example.maum.dto.TtsRequestDTO;
import com.example.maum.dto.TtsResponseDTO;
import com.example.maum.repository.ChatMessageRepository;
import com.example.maum.repository.ChatSessionRepository;
import com.example.maum.repository.UserRepository;
import com.example.maum.repository.entity.ChatMessageEntity;
import com.example.maum.repository.entity.ChatSessionEntity;
import com.example.maum.service.IChatBotService;
import com.example.maum.service.IChatWriteService;
import jakarta.annotation.PostConstruct;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.reactive.function.client.ExchangeStrategies;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Flux;
import reactor.core.publisher.SignalType;
import tools.jackson.core.type.TypeReference;
import tools.jackson.databind.json.JsonMapper;

import java.time.Duration;
import java.time.Instant;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.atomic.AtomicBoolean;

@Service
@Slf4j
@RequiredArgsConstructor
public class ChatBotService implements IChatBotService {

    private static final DateTimeFormatter DATE_FORMAT =
            DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss").withZone(ZoneId.of("Asia/Seoul"));
    private static final int MAX_TITLE_LENGTH = 100;
    private static final String CARD_OPEN = "[[CARD]]";
    private static final String CARD_CLOSE = "[[/CARD]]";
    private static final JsonMapper JSON = JsonMapper.builder().build();

    private WebClient webClient;

    private final IChatWriteService chatWriteService;

    private final ChatSessionRepository chatSessionRepository;

    private final ChatMessageRepository chatMessageRepository;

    private final UserRepository userRepository;

    @Value("${secure.python.api.url}")
    private String pythonApiUrl;

    @PostConstruct // pythonApiUrl이 @Value로 주입된 뒤에 WebClient를 생성해야 하므로 생성자 대신 여기서 초기화
    public void init() {
        this.webClient = WebClient.builder()
                .baseUrl(pythonApiUrl)
                // /api/tts 응답(base64 오디오)이 WebClient 기본 버퍼 한도(256KB)를 넘어서
                // DataBufferLimitException이 나는 것을 확인해서, 메시지 하나 분량(최대 10MB)까지 허용하도록 늘림
                .exchangeStrategies(ExchangeStrategies.builder()
                        .codecs(configurer -> configurer.defaultCodecs().maxInMemorySize(10 * 1024 * 1024))
                        .build())
                .build();
    }

    // DB 쓰기는 ChatWriteService 의 짧은 트랜잭션이 맡고, 여기서는 스트리밍 중 DB 연결을 잡지 않음
    @Override
    public Flux<String> streamChat(ChatBotDTO pDTO) {

        log.info("{}.streamChat Start!", this.getClass().getName());

        IChatWriteService.Turn turn;
        try {
            turn = chatWriteService.beginTurn(pDTO.userNo(), pDTO.sessionPublicId(), pDTO.message(),
                    parseClientMsgId(pDTO.clientMsgId()));
        } catch (IllegalArgumentException | IllegalStateException e) {
            // 권한 없음, 중복/동시 요청 등은 오류 대신 안내 문구로 응답함
            log.warn("streamChat 시작 불가: {}", e.getMessage());
            return Flux.just(e.getMessage());
        }

        if (turn.replay() != null) {
            return Flux.just(turn.replay());
        }

        ChatBotDTO requestDTO = ChatBotDTO.builder()
                .userNo(pDTO.userNo())
                .message(pDTO.message())
                .history(turn.history())
                .build();

        StringBuilder botResponse = new StringBuilder();
        List<Map<String, Object>> cards = new ArrayList<>();
        AtomicBoolean hasAudio = new AtomicBoolean(false);
        AtomicBoolean textDone = new AtomicBoolean(false);
        AtomicBoolean failed = new AtomicBoolean(false);

        return webClient.post()
                .uri("/api/rag-chat")
                .header("Accept", "text/plain") // 파이썬 쪽과 순수 텍스트로 주고받기로 합의됨
                .bodyValue(requestDTO)
                .retrieve()
                .bodyToFlux(String.class)
                .doOnNext(data -> {
                    // 응답 본문/음성 데이터는 로그에 남기지 않고 종류와 길이만 기록
                    log.info("Python Data Received - type: {}, length: {}", resolveChunkType(data), data.length());
                    // TTS 음성 데이터 자체는 대화 기록에 노이즈만 되므로 저장하지 않고,
                    // 나중에 다시 들을 때는 저장된 텍스트로 TTS를 재생성함(synthesizeMessageAudio) — 그때 쓸 표시만 남김
                    // [[TEXT_DONE]]는 텍스트 전송이 끝나고 음성 합성이 시작됐다는 표시라서, 라이브 합성이 실패하거나
                    // 오디오가 도착하기 전에 연결이 끊겨도 재진입 시 다시 합성해 들을 수 있도록 이 시점에도 표시를 남김
                    if (data.startsWith("[[AUDIO]]") || data.startsWith("[[TEXT_DONE]]")) {
                        hasAudio.set(true);
                        textDone.set(true);
                    } else if (data.startsWith(CARD_OPEN)) {
                        cards.addAll(parseCards(data));
                    } else {
                        botResponse.append(data);
                    }
                })
                .onErrorResume(e -> {
                    log.error("Python Communication Error: ", e);
                    failed.set(true);
                    return Flux.just("연결 중에 문제가 발생했어요. 잠시 후 다시 시도해주세요.");
                })
                // doOnComplete는 클라이언트가 응답을 다 받자마자 구독을 취소해버리는 경우
                // 호출이 안 될 수 있어서(그러면 봇 답변이 저장 안 됨), 완료/에러/취소
                // 어떤 경우에도 반드시 실행되는 doFinally로 저장 로직을 옮김
                .doFinally(signalType -> {
                    log.info("{}.streamChat Data Stream Finished! signal: {}", this.getClass().getName(), signalType);

                    String cleanBotResponse = botResponse.toString()
                            .replace("<br>", "  \n")
                            .replace("<sp>", " ");

                    chatWriteService.finishTurn(turn.botMessageId(),
                            resolveStatus(signalType, failed.get(), textDone.get(), cleanBotResponse),
                            cleanBotResponse, hasAudio.get(), cards.isEmpty() ? null : cards);
                });
    }

    @Override
    @Transactional
    public ChatRoomDTO createRoom(ChatRoomDTO pDTO) {
        log.info("{}.createRoom Start!", this.getClass().getName());

        ChatSessionEntity saved = chatSessionRepository.saveAndFlush(ChatSessionEntity.builder()
                .publicId(UUID.randomUUID())
                .userId(resolveUserId(pDTO.userPublicId()))
                .build());

        log.info("{}.createRoom End!", this.getClass().getName());

        // created_at, updated_at 은 DB 가 채우므로 방금 만든 방은 현재 시각으로 응답함
        String now = format(Instant.now());

        return ChatRoomDTO.builder()
                .publicId(saved.getPublicId().toString())
                .userPublicId(pDTO.userPublicId())
                .isPinned(false)
                .createdAt(now)
                .updatedAt(now)
                .build();
    }

    @Transactional(readOnly = true)
    @Override
    public List<ChatRoomDTO> getRooms(ChatRoomDTO pDTO) {
        log.info("{}.getRooms Start!", this.getClass().getName());

        return chatSessionRepository.findByUserIdOrderByPinnedDescUpdatedAtDesc(resolveUserId(pDTO.userPublicId()))
                .stream()
                .map(s -> toRoomDTO(s, pDTO.userPublicId()))
                .toList();
    }

    @Transactional(readOnly = true)
    @Override
    public List<ChatMessageDTO> getRoomMessages(ChatRoomDTO pDTO) {
        log.info("{}.getRoomMessages Start!", this.getClass().getName());

        ChatSessionEntity session = findOwnedSession(pDTO.publicId(), pDTO.userPublicId());

        return chatMessageRepository.findBySessionIdOrderBySeqAsc(session.getId()).stream()
                // 아무 내용 없이 실패한 답변은 화면에 보이지 않게 함
                .filter(m -> !(m.getStatus() == ChatMessageStatus.FAILED && m.getContent().isBlank()))
                .map(m -> ChatMessageDTO.builder()
                        .chatMsgNo(m.getId())
                        .role(ChatWriteService.toWireRole(m.getRole()))
                        .content(m.getContent())
                        .hasAudio(m.isHasAudio())
                        .status(m.getStatus().name())
                        .cards(m.getCards())
                        .build())
                .toList();
    }

    @Override
    public List<String> synthesizeMessageAudio(Long chatMsgNo, String userPublicId) throws Exception {
        log.info("{}.synthesizeMessageAudio Start!", this.getClass().getName());

        ChatMessageEntity message = chatMessageRepository.findById(chatMsgNo)
                .orElseThrow(() -> new IllegalArgumentException("존재하지 않는 메시지입니다."));

        // 채팅방 소유자 검증 — 다른 사용자의 메시지를 chatMsgNo만으로 재생하지 못하게 함
        Long userId = resolveUserId(userPublicId);
        chatSessionRepository.findById(message.getSessionId())
                .filter(s -> s.getUserId().equals(userId))
                .orElseThrow(() -> new IllegalArgumentException("존재하지 않거나 권한이 없는 채팅방입니다."));

        TtsRequestDTO requestDTO = TtsRequestDTO.builder()
                .text(message.getContent())
                .build();

        TtsResponseDTO response = webClient.post()
                .uri("/api/tts")
                .bodyValue(requestDTO)
                .retrieve()
                .bodyToMono(TtsResponseDTO.class)
                // 재합성이 끝없이 대기하지 않도록 상한을 둠(Python은 청크마다 최대 30초 x 2회까지 시도)
                .block(Duration.ofSeconds(120));

        log.info("{}.synthesizeMessageAudio End!", this.getClass().getName());

        return response != null ? response.audioChunks() : Collections.emptyList();
    }

    @Override
    @Transactional
    public void renameRoom(ChatRoomDTO pDTO) {
        log.info("{}.renameRoom Start!", this.getClass().getName());

        String title = pDTO.roomTitle() == null ? "" : pDTO.roomTitle().trim();
        if (title.isEmpty() || title.length() > MAX_TITLE_LENGTH) {
            throw new IllegalArgumentException("채팅방 이름은 1~" + MAX_TITLE_LENGTH + "자로 입력해주세요.");
        }

        findOwnedSession(pDTO.publicId(), pDTO.userPublicId()).changeTitle(title);
    }

    @Override
    @Transactional
    public void pinRoom(ChatRoomDTO pDTO) {
        log.info("{}.pinRoom Start!", this.getClass().getName());

        findOwnedSession(pDTO.publicId(), pDTO.userPublicId()).changePinned(Boolean.TRUE.equals(pDTO.isPinned()));
    }

    @Override
    @Transactional
    public void deleteRoom(ChatRoomDTO pDTO) {
        log.info("{}.deleteRoom Start!", this.getClass().getName());

        // 메시지는 DB 의 ON DELETE CASCADE 가 함께 지움
        chatSessionRepository.delete(findOwnedSession(pDTO.publicId(), pDTO.userPublicId()));
    }

    // 정상 완료는 COMPLETE. 클라이언트가 응답을 다 받고 먼저 끊은 경우(TEXT_DONE 수신 후 취소)도 완료로 봄
    static ChatMessageStatus resolveStatus(SignalType signal, boolean failed, boolean textDone, String content) {
        if (failed) {
            return ChatMessageStatus.FAILED;
        }
        if (signal == SignalType.ON_COMPLETE || (signal == SignalType.CANCEL && textDone)) {
            return ChatMessageStatus.COMPLETE;
        }
        return content.isBlank() ? ChatMessageStatus.FAILED : ChatMessageStatus.INTERRUPTED;
    }

    private List<Map<String, Object>> parseCards(String data) {
        int start = data.indexOf(CARD_OPEN) + CARD_OPEN.length();
        int end = data.indexOf(CARD_CLOSE);
        if (end < start) {
            return List.of();
        }
        try {
            return JSON.readValue(data.substring(start, end), new TypeReference<List<Map<String, Object>>>() {
            });
        } catch (Exception e) {
            log.warn("카드 데이터를 해석하지 못했습니다: {}", e.getMessage());
            return List.of();
        }
    }

    // 스트림으로 받은 조각이 어떤 종류인지 접두어로 구분 (로그 표기용)
    private String resolveChunkType(String data) {
        if (data.startsWith("[[AUDIO]]")) return "AUDIO";
        if (data.startsWith("[[TEXT_DONE]]")) return "TEXT_DONE";
        if (data.startsWith(CARD_OPEN)) return "CARD";
        return "TEXT";
    }

    private UUID parseClientMsgId(String value) {
        if (value == null || value.isBlank()) {
            return null;
        }
        try {
            return UUID.fromString(value);
        } catch (IllegalArgumentException e) {
            throw new IllegalArgumentException("요청 정보가 올바르지 않습니다.");
        }
    }

    private Long resolveUserId(String userPublicId) {
        try {
            return userRepository.findByPublicId(UUID.fromString(userPublicId))
                    .orElseThrow(() -> new IllegalArgumentException("사용자 정보를 찾을 수 없습니다."))
                    .getId();
        } catch (IllegalArgumentException e) {
            throw new IllegalArgumentException("사용자 정보를 찾을 수 없습니다.");
        }
    }

    private ChatSessionEntity findOwnedSession(String sessionPublicId, String userPublicId) {
        Long userId = resolveUserId(userPublicId);
        try {
            return chatSessionRepository.findByPublicIdAndUserId(UUID.fromString(sessionPublicId), userId)
                    .orElseThrow(() -> new IllegalArgumentException("존재하지 않거나 권한이 없는 채팅방입니다."));
        } catch (IllegalArgumentException e) {
            throw new IllegalArgumentException("존재하지 않거나 권한이 없는 채팅방입니다.");
        }
    }

    private ChatRoomDTO toRoomDTO(ChatSessionEntity entity, String userPublicId) {
        return ChatRoomDTO.builder()
                .publicId(entity.getPublicId().toString())
                .userPublicId(userPublicId)
                .roomTitle(entity.getTitle())
                .isPinned(entity.isPinned())
                .createdAt(format(entity.getCreatedAt()))
                .updatedAt(format(entity.getUpdatedAt()))
                .build();
    }

    private String format(Instant instant) {
        return instant == null ? null : DATE_FORMAT.format(instant);
    }
}
