package com.example.maum.account.service.impl;

import com.example.maum.account.dto.MailDTO;
import com.example.maum.account.service.IMailService;
import com.example.maum.global.util.CmmUtil;
import jakarta.mail.internet.MimeMessage;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.stereotype.Service;

@Service
@Slf4j
@RequiredArgsConstructor
public class MailService implements IMailService {

    private final JavaMailSender mailSender;

    @Value("${secure.mail.username}")
    private String fromMail;

    @Override
    public int doSendMail(MailDTO pDTO) {

        log.info(this.getClass().getName() + ".doSendMail start!");

        int res = 1;

        if (pDTO == null) {
            pDTO = new MailDTO();
        }

        String toMail = CmmUtil.nvl(pDTO.getReceiver()); // 받는사람
        String title = CmmUtil.nvl(pDTO.getTitle()); // 메일제목
        String contents = CmmUtil.nvl(pDTO.getContent()); // 메일제목

        log.info("toMail : " + toMail);
        log.info("title : " + title);
        log.info("contents : " + contents);

        MimeMessage message = mailSender.createMimeMessage();

        MimeMessageHelper messageHelper = new MimeMessageHelper(message, "UTF-8");

        try {

            messageHelper.setTo(toMail); // 받는 사람
            messageHelper.setFrom(fromMail); // 보내는 사람
            messageHelper.setSubject(title); // 메일 제목
            messageHelper.setText(contents); // 메일 내용

            mailSender.send(message);

        } catch (Exception e) {
            res = 0;
            log.info("[ERROR] " + this.getClass().getName() + ".doSendMail : " + e);
        }

        log.info(this.getClass().getName() + ".doSendMail end!");
        return res;
    }
}
