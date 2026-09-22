package com.example.maum.service.impl;

import com.example.maum.dto.ChatBotDTO;
import com.example.maum.dto.ChatMessageDTO;
import com.example.maum.dto.ChatRoomDTO;
import com.example.maum.dto.TtsRequestDTO;
import com.example.maum.dto.TtsResponseDTO;
import com.example.maum.repository.ChatMessageRepository;
import com.example.maum.repository.ChatRoomRepository;
import com.example.maum.repository.entity.ChatMessageEntity;
import com.example.maum.repository.entity.ChatRoomEntity;
import com.example.maum.service.IChatBotService;
import jakarta.annotation.PostConstruct;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.reactive.function.client.ExchangeStrategies;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Flux;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.atomic.AtomicBoolean;

@Service
@Slf4j
@RequiredArgsConstructor
public class ChatBotService implements IChatBotService {

    private static final DateTimeFormatter DATE_FORMAT = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");

    private WebClient webClient;

    private final ChatRoomRepository chatRoomRepository;

    private final ChatMessageRepository chatMessageRepository;

    @Value("${secure.python.api.url}")
    private String pythonApiUrl;

    // ★ 즐겨찾기 이후 추가/수정
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

    // ★ 즐겨찾기 이후 추가/수정
    @Override
    @Transactional // updateTitleAndTouch/touchUpdatedAt이 @Modifying 커스텀 쿼리라 트랜잭션이 명시적으로 필요함
    // (여기서 여는 트랜잭션은 메서드 안의 동기적인 DB 조회/수정 구간까지만 걸리고,
    //  Flux를 리턴한 뒤 비동기로 진행되는 스트리밍/오디오 처리에는 영향 없음)
    public Flux<String> streamChat(ChatBotDTO pDTO) {

        log.info("{}.streamChat Start!", this.getClass().getName());

        String userNo = pDTO.userNo();
        Integer chatRoomNo = pDTO.chatRoomNo();

        // 이 채팅방이 실제로 이 유저 소유인지 확인 (다른 유저 방에 메시지가 끼어들지 않게)
        ChatRoomEntity room = chatRoomRepository.findByChatRoomNoAndUserNo(chatRoomNo, userNo)
                .orElse(null);

        if (room == null) {
            log.error("존재하지 않거나 권한이 없는 채팅방입니다. chatRoomNo: {}, userNo: {}", chatRoomNo, userNo);
            return Flux.just("채팅방을 찾을 수 없어요. 새로고침 후 다시 시도해주세요.");
        }

        // 최근 8개(4턴)를 최신순으로 가져와서 시간순으로 다시 뒤집음 — Gemini에 넘길 맥락용
        List<ChatMessageEntity> recentMessages = chatMessageRepository
                .findTop8ByChatRoomNoOrderByCreatedAtDesc(chatRoomNo);
        Collections.reverse(recentMessages);

        List<ChatMessageDTO> recentHistory = recentMessages.stream()
                .map(m -> ChatMessageDTO.builder().role(m.getRole()).content(m.getContent()).build())
                .toList();

        ChatBotDTO requestDTO = ChatBotDTO.builder()
                .userNo(userNo)
                .message(pDTO.message())
                .history(recentHistory)
                .build();

        saveMessage(chatRoomNo, "user", pDTO.message(), false);

        // 방 제목이 비어있으면(첫 메시지) 사용자 메시지 앞부분으로 자동 설정, 아니면 최근 활동 시각만 갱신
        LocalDateTime now = LocalDateTime.now();
        if (room.getRoomTitle() == null || room.getRoomTitle().isBlank()) {
            String autoTitle = pDTO.message().length() > 30 ? pDTO.message().substring(0, 30) + "..." : pDTO.message();
            chatRoomRepository.updateTitleAndTouch(chatRoomNo, autoTitle, now);
        } else {
            chatRoomRepository.touchUpdatedAt(chatRoomNo, now);
        }

        StringBuilder botResponse = new StringBuilder();
        AtomicBoolean hasAudio = new AtomicBoolean(false);

        return webClient.post()
                .uri("/api/rag-chat")
                .header("Accept", "text/plain") // 파이썬 쪽과 순수 텍스트로 주고받기로 합의됨
                .bodyValue(requestDTO)
                .retrieve()
                .bodyToFlux(String.class)
                .doOnNext(data -> {
                    log.info("Python Raw Data: {}", data);
                    // TTS 음성 데이터 자체는 대화 기록에 노이즈만 되므로 저장하지 않고,
                    // 나중에 다시 들을 때는 저장된 텍스트로 TTS를 재생성함(synthesizeMessageAudio) — 그때 쓸 표시만 남김
                    if (data.startsWith("[[AUDIO]]")) {
                        hasAudio.set(true);
                    } else if (!data.startsWith("[[CARD]]") && !data.startsWith("[[TEXT_DONE]]")) {
                        botResponse.append(data);
                    }
                })
                .onErrorResume(e -> {
                    log.error("Python Communication Error: ", e);
                    return Flux.just("연결 중에 문제가 발생했어요. 잠시 후 다시 시도해주세요.");
                })
                // doOnComplete는 클라이언트가 응답을 다 받자마자 구독을 취소해버리는 경우
                // 호출이 안 될 수 있어서(그러면 봇 답변이 저장 안 됨), 완료/에러/취소
                // 어떤 경우에도 반드시 실행되는 doFinally로 저장 로직을 옮김
                .doFinally(signalType -> {
                    log.info("{}.streamChat Data Stream Finished! signal: {}", this.getClass().getName(), signalType);

                    if (botResponse.isEmpty()) {
                        return;
                    }

                    String cleanBotResponse = botResponse.toString()
                            .replace("<br>", "  \n")
                            .replace("<sp>", " ");

                    saveMessage(chatRoomNo, "bot", cleanBotResponse, hasAudio.get());
                });
    }

    // ★ 즐겨찾기 이후 추가/수정
    @Override
    public ChatRoomDTO createRoom(ChatRoomDTO pDTO) {
        log.info("{}.createRoom Start!", this.getClass().getName());

        LocalDateTime now = LocalDateTime.now();
        ChatRoomEntity room = ChatRoomEntity.builder()
                .userNo(pDTO.userNo())
                .isPinned(0)
                .createdAt(now)
                .updatedAt(now)
                .build();

        ChatRoomEntity saved = chatRoomRepository.save(room);

        return toRoomDTO(saved);
    }

    // ★ 즐겨찾기 이후 추가/수정
    @Override
    public List<ChatRoomDTO> getRooms(ChatRoomDTO pDTO) {
        log.info("{}.getRooms Start!", this.getClass().getName());

        return chatRoomRepository.findByUserNoOrderByIsPinnedDescUpdatedAtDesc(pDTO.userNo()).stream()
                .map(this::toRoomDTO)
                .toList();
    }

    // ★ 즐겨찾기 이후 추가/수정
    @Override
    public List<ChatMessageDTO> getRoomMessages(ChatRoomDTO pDTO) throws Exception {
        log.info("{}.getRoomMessages Start!", this.getClass().getName());

        ChatRoomEntity room = chatRoomRepository.findByChatRoomNoAndUserNo(pDTO.chatRoomNo(), pDTO.userNo())
                .orElseThrow(() -> new Exception("존재하지 않거나 권한이 없는 채팅방입니다."));

        return chatMessageRepository.findByChatRoomNoOrderByCreatedAtAsc(room.getChatRoomNo()).stream()
                .map(m -> ChatMessageDTO.builder()
                        .chatMsgNo(m.getChatMsgNo())
                        .role(m.getRole())
                        .content(m.getContent())
                        .hasAudio(m.getHasAudio())
                        .build())
                .toList();
    }

    // ★ 즐겨찾기 이후 추가/수정
    @Override
    public List<String> synthesizeMessageAudio(Long chatMsgNo, String userNo) throws Exception {
        log.info("{}.synthesizeMessageAudio Start!", this.getClass().getName());

        ChatMessageEntity message = chatMessageRepository.findById(chatMsgNo)
                .orElseThrow(() -> new IllegalArgumentException("존재하지 않는 메시지입니다."));

        // 채팅방 소유자 검증 — 다른 사용자의 메시지를 chatMsgNo만으로 재생하지 못하게 함
        chatRoomRepository.findByChatRoomNoAndUserNo(message.getChatRoomNo(), userNo)
                .orElseThrow(() -> new IllegalArgumentException("존재하지 않거나 권한이 없는 채팅방입니다."));

        TtsRequestDTO requestDTO = TtsRequestDTO.builder()
                .text(message.getContent())
                .build();

        TtsResponseDTO response = webClient.post()
                .uri("/api/tts")
                .bodyValue(requestDTO)
                .retrieve()
                .bodyToMono(TtsResponseDTO.class)
                .block();

        log.info("{}.synthesizeMessageAudio End!", this.getClass().getName());

        return response != null ? response.audioChunks() : Collections.emptyList();
    }

    // ★ 즐겨찾기 이후 추가/수정
    @Override
    @Transactional
    public void renameRoom(ChatRoomDTO pDTO) throws Exception {
        log.info("{}.renameRoom Start!", this.getClass().getName());

        int res = chatRoomRepository.updateTitleDirectly(pDTO.chatRoomNo(), pDTO.userNo(), pDTO.roomTitle());
        if (res == 0) {
            throw new Exception("존재하지 않거나 권한이 없는 채팅방입니다.");
        }
    }

    // ★ 즐겨찾기 이후 추가/수정
    @Override
    @Transactional
    public void pinRoom(ChatRoomDTO pDTO) throws Exception {
        log.info("{}.pinRoom Start!", this.getClass().getName());

        int res = chatRoomRepository.updatePinnedDirectly(pDTO.chatRoomNo(), pDTO.userNo(), pDTO.isPinned());
        if (res == 0) {
            throw new Exception("존재하지 않거나 권한이 없는 채팅방입니다.");
        }
    }

    // ★ 즐겨찾기 이후 추가/수정
    @Override
    @Transactional
    public void deleteRoom(ChatRoomDTO pDTO) throws Exception {
        log.info("{}.deleteRoom Start!", this.getClass().getName());

        long res = chatRoomRepository.deleteByChatRoomNoAndUserNo(pDTO.chatRoomNo(), pDTO.userNo());
        if (res == 0) {
            throw new Exception("존재하지 않거나 권한이 없는 채팅방입니다.");
        }
    }

    // ★ 즐겨찾기 이후 추가/수정
    private void saveMessage(Integer chatRoomNo, String role, String content, boolean hasAudio) {
        ChatMessageEntity message = ChatMessageEntity.builder()
                .chatRoomNo(chatRoomNo)
                .role(role)
                .content(content)
                .hasAudio(hasAudio)
                .createdAt(LocalDateTime.now())
                .build();
        chatMessageRepository.save(message);
    }

    // ★ 즐겨찾기 이후 추가/수정
    private ChatRoomDTO toRoomDTO(ChatRoomEntity entity) {
        return ChatRoomDTO.builder()
                .chatRoomNo(entity.getChatRoomNo())
                .roomTitle(entity.getRoomTitle())
                .isPinned(entity.getIsPinned())
                .createdAt(entity.getCreatedAt().format(DATE_FORMAT))
                .updatedAt(entity.getUpdatedAt().format(DATE_FORMAT))
                .build();
    }
}
