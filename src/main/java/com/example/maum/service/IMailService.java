package com.example.maum.service;

import com.example.maum.dto.MailDTO;

public interface IMailService {

    int doSendMail(MailDTO pDTO);
}
