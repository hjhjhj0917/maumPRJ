package com.example.maum.service;

import com.example.maum.dto.ChatBotDTO;
import com.example.maum.dto.ChatMessageDTO;
import com.example.maum.dto.ChatRoomDTO;
import reactor.core.publisher.Flux;

import java.util.List;

public interface IChatBotService {

    Flux<String> streamChat(ChatBotDTO pDTO) throws Exception;

    ChatRoomDTO createRoom(String userNo) throws Exception;

    List<ChatRoomDTO> getRooms(String userNo) throws Exception;

    List<ChatMessageDTO> getRoomMessages(String userNo, Integer chatRoomNo) throws Exception;

    void renameRoom(String userNo, Integer chatRoomNo, String roomTitle) throws Exception;

    void pinRoom(String userNo, Integer chatRoomNo, Integer isPinned) throws Exception;

    void deleteRoom(String userNo, Integer chatRoomNo) throws Exception;
}
