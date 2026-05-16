package com.example.maum.service;

import com.example.maum.dto.ChatBotDTO;
import reactor.core.publisher.Flux;

public interface IChatBotService {

    Flux<String> streamChat(ChatBotDTO pDTO) throws Exception;
}
