package com.example.maum.service;

import com.example.maum.dto.ChatBotDTO;
import reactor.core.publisher.Flux;

import java.util.List;

public interface IChatBotService {

    Flux<String> streamChat(ChatBotDTO pDTO) throws Exception;

    List<Object> getHistory(String userNo) throws Exception;
}
