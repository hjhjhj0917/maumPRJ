package com.example.maum.diary.controller;

import com.example.maum.diary.dto.DiaryDTO;
import com.example.maum.diary.service.impl.DiaryService;
import com.example.maum.global.dto.MsgDTO;
import com.example.maum.global.util.CmmUtil;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpSession;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseBody;

@Controller
@Slf4j
@RequestMapping(value = "/api/diary")
@RequiredArgsConstructor
public class DiaryController {

    private final DiaryService diaryService;

    /*
    일기 저장
    */
    @ResponseBody
    @PostMapping(value = "diaryInsert")
    public MsgDTO diaryInsert(HttpServletRequest request, HttpSession session) throws Exception {

        log.info("{}.diaryInsert Start!", this.getClass().getName());

        String msg = "";

        Object sessionUserNo = session.getAttribute("SS_USER_NO");

        if (sessionUserNo == null) {
            log.warn("세션 만료 또는 비로그인 사용자의 접근입니다.");
            return MsgDTO.builder()
                    .result(0)
                    .msg("로그인이 필요한 서비스입니다.")
                    .build();
        }

        Integer userNo = (Integer) sessionUserNo;
        String title = CmmUtil.nvl(request.getParameter("title"));
        String content = CmmUtil.nvl(request.getParameter("content"));
        String createdAt = CmmUtil.nvl(request.getParameter("createdAt"));

        log.info("userNo: {}, title: {}, content: {}, createdAt: {}", userNo, title, content, createdAt);

        DiaryDTO pDTO = DiaryDTO.builder()
                .userNo(userNo)
                .title(title)
                .content(content)
                .createdAt(createdAt)
                .build();

        int res = diaryService.diaryInsert(pDTO);

        log.info("일기 저장 결과(res): {}", res);

        if (res == 1) {
            msg = "저장이 완료되었습니다.";
        } else {
            msg = "오류로 인해 저장이 실패하였습니다.";
        }

        MsgDTO dto = MsgDTO.builder()
                .result(res)
                .msg(msg)
                .build();

        log.info("{}.diaryInsert End!", this.getClass().getName());

        return dto;
    }
}
