package com.example.maum.service;

import com.example.maum.dto.ChatBotDTO;
import com.example.maum.dto.ChatMessageDTO;
import reactor.core.publisher.Flux;

import java.util.List;

public interface IChatBotService {

    Flux<String> streamChat(ChatBotDTO pDTO) throws Exception;

    List<ChatMessageDTO> getHistory(String userNo) throws Exception;
}
