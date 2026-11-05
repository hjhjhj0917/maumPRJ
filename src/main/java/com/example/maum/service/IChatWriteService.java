package com.example.maum.service;

import com.example.maum.chat.ChatMessageStatus;
import com.example.maum.dto.ChatMessageDTO;

import java.util.List;
import java.util.Map;
import java.util.UUID;

// 챗봇 대화의 DB 쓰기만 짧은 트랜잭션으로 모아 둠(스트리밍 중에는 DB 연결을 잡고 있지 않기 위함)
public interface IChatWriteService {

    // botMessageId: 스트리밍 결과를 채울 답변 행, replay: 같은 clientMsgId 로 이미 끝난 요청이면 저장된 답변
    record Turn(Long botMessageId, List<ChatMessageDTO> history, String replay) {
    }

    // 사용자 메시지 저장 + 빈 답변 행(STREAMING) 생성. 세션 소유자 확인과 중복/동시 요청 차단 포함
    Turn beginTurn(String userPublicId, String sessionPublicId, String message, UUID clientMsgId);

    void finishTurn(Long botMessageId, ChatMessageStatus status, String content, boolean hasAudio,
                    List<Map<String, Object>> cards);
}
