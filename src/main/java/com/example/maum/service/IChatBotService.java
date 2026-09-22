package com.example.maum.service;

import com.example.maum.dto.ChatBotDTO;
import com.example.maum.dto.ChatMessageDTO;
import com.example.maum.dto.ChatRoomDTO;
import reactor.core.publisher.Flux;

import java.util.List;

public interface IChatBotService {

    Flux<String> streamChat(ChatBotDTO pDTO) throws Exception;

    ChatRoomDTO createRoom(ChatRoomDTO pDTO) throws Exception;

    List<ChatRoomDTO> getRooms(ChatRoomDTO pDTO) throws Exception;

    List<ChatMessageDTO> getRoomMessages(ChatRoomDTO pDTO) throws Exception;

    void renameRoom(ChatRoomDTO pDTO) throws Exception;

    void pinRoom(ChatRoomDTO pDTO) throws Exception;

    void deleteRoom(ChatRoomDTO pDTO) throws Exception;

    List<String> synthesizeMessageAudio(Long chatMsgNo, String userNo) throws Exception;
}
