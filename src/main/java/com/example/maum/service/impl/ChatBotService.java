package com.example.maum.service.impl;

import com.example.maum.dto.ChatBotDTO;
import com.example.maum.dto.ChatMessageDTO;
import com.example.maum.dto.ChatRoomDTO;
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
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Flux;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Collections;
import java.util.List;

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

    @PostConstruct /* 통신 설정 안에 같이 다 들어와야 실행한다 */
    public void init() {
        this.webClient = WebClient.builder()
                .baseUrl(pythonApiUrl)
                .build();
    }

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

        // 사용자 메시지 저장
        saveMessage(chatRoomNo, "user", pDTO.message());

        // 방 제목이 비어있으면(첫 메시지) 사용자 메시지 앞부분으로 자동 설정, 아니면 최근 활동 시각만 갱신
        LocalDateTime now = LocalDateTime.now();
        if (room.getRoomTitle() == null || room.getRoomTitle().isBlank()) {
            String autoTitle = pDTO.message().length() > 30 ? pDTO.message().substring(0, 30) + "..." : pDTO.message();
            chatRoomRepository.updateTitleAndTouch(chatRoomNo, autoTitle, now);
        } else {
            chatRoomRepository.touchUpdatedAt(chatRoomNo, now);
        }

        StringBuilder botResponse = new StringBuilder();

        return webClient.post()
                .uri("/api/rag-chat")
                .header("Accept", "text/plain") /* 파이썬에서 순수 텍스트를 받기로 함 */
                .bodyValue(requestDTO)
                .retrieve() /* 응답 상태 준비 */
                .bodyToFlux(String.class) /* 응답을 여러 조각으로 받음 */
                .doOnNext(data -> { /* 실시간 데이터 처리 */
                    log.info("Python Raw Data: {}", data);
                    // TTS 음성 데이터, 카드 JSON, 텍스트 완료 마커는 대화 기록에 노이즈만 되므로 저장하지 않음
                    if (!data.startsWith("[[AUDIO]]") && !data.startsWith("[[CARD]]") && !data.startsWith("[[TEXT_DONE]]")) {
                        botResponse.append(data);
                    }
                })
                .onErrorResume(e -> { /* 예외처리 회로 차단 */
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

                    /* <br>과 <sp> 태그를 변환하여 저장 */
                    String cleanBotResponse = botResponse.toString()
                            .replace("<br>", "  \n")
                            .replace("<sp>", " ");

                    saveMessage(chatRoomNo, "bot", cleanBotResponse);
                });
    }

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

    @Override
    public List<ChatRoomDTO> getRooms(ChatRoomDTO pDTO) {
        log.info("{}.getRooms Start!", this.getClass().getName());

        return chatRoomRepository.findByUserNoOrderByIsPinnedDescUpdatedAtDesc(pDTO.userNo()).stream()
                .map(this::toRoomDTO)
                .toList();
    }

    @Override
    public List<ChatMessageDTO> getRoomMessages(ChatRoomDTO pDTO) throws Exception {
        log.info("{}.getRoomMessages Start!", this.getClass().getName());

        ChatRoomEntity room = chatRoomRepository.findByChatRoomNoAndUserNo(pDTO.chatRoomNo(), pDTO.userNo())
                .orElseThrow(() -> new Exception("존재하지 않거나 권한이 없는 채팅방입니다."));

        return chatMessageRepository.findByChatRoomNoOrderByCreatedAtAsc(room.getChatRoomNo()).stream()
                .map(m -> ChatMessageDTO.builder().role(m.getRole()).content(m.getContent()).build())
                .toList();
    }

    @Override
    @Transactional
    public void renameRoom(ChatRoomDTO pDTO) throws Exception {
        log.info("{}.renameRoom Start!", this.getClass().getName());

        int res = chatRoomRepository.updateTitleDirectly(pDTO.chatRoomNo(), pDTO.userNo(), pDTO.roomTitle());
        if (res == 0) {
            throw new Exception("존재하지 않거나 권한이 없는 채팅방입니다.");
        }
    }

    @Override
    @Transactional
    public void pinRoom(ChatRoomDTO pDTO) throws Exception {
        log.info("{}.pinRoom Start!", this.getClass().getName());

        int res = chatRoomRepository.updatePinnedDirectly(pDTO.chatRoomNo(), pDTO.userNo(), pDTO.isPinned());
        if (res == 0) {
            throw new Exception("존재하지 않거나 권한이 없는 채팅방입니다.");
        }
    }

    @Override
    @Transactional
    public void deleteRoom(ChatRoomDTO pDTO) throws Exception {
        log.info("{}.deleteRoom Start!", this.getClass().getName());

        long res = chatRoomRepository.deleteByChatRoomNoAndUserNo(pDTO.chatRoomNo(), pDTO.userNo());
        if (res == 0) {
            throw new Exception("존재하지 않거나 권한이 없는 채팅방입니다.");
        }
    }

    private void saveMessage(Integer chatRoomNo, String role, String content) {
        ChatMessageEntity message = ChatMessageEntity.builder()
                .chatRoomNo(chatRoomNo)
                .role(role)
                .content(content)
                .createdAt(LocalDateTime.now())
                .build();
        chatMessageRepository.save(message);
    }

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
