package com.example.maum.service;

import com.example.maum.dto.ChatBotDTO;
import com.example.maum.dto.ChatMessageDTO;
import com.example.maum.dto.ChatRoomDTO;
import reactor.core.publisher.Flux;

import java.util.List;

public interface IChatBotService {

    Flux<String> streamChat(ChatBotDTO pDTO) throws Exception;

    // ★ 즐겨찾기 이후 추가/수정
    ChatRoomDTO createRoom(ChatRoomDTO pDTO) throws Exception;

    // ★ 즐겨찾기 이후 추가/수정
    List<ChatRoomDTO> getRooms(ChatRoomDTO pDTO) throws Exception;

    // ★ 즐겨찾기 이후 추가/수정
    List<ChatMessageDTO> getRoomMessages(ChatRoomDTO pDTO) throws Exception;

    // ★ 즐겨찾기 이후 추가/수정
    void renameRoom(ChatRoomDTO pDTO) throws Exception;

    // ★ 즐겨찾기 이후 추가/수정
    void pinRoom(ChatRoomDTO pDTO) throws Exception;

    // ★ 즐겨찾기 이후 추가/수정
    void deleteRoom(ChatRoomDTO pDTO) throws Exception;

    // ★ 즐겨찾기 이후 추가/수정
    List<String> synthesizeMessageAudio(Long chatMsgNo, String userNo) throws Exception;
}
