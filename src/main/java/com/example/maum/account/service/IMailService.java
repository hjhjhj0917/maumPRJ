package com.example.maum.account.service;

import com.example.maum.account.dto.MailDTO;

public interface IMailService {

    int doSendMail(MailDTO pDTO);
}
