package com.example.maum.controller;

import com.example.maum.controller.response.CommonResponse;
import com.example.maum.dto.DiaryDTO;
import com.example.maum.dto.MsgDTO;
import com.example.maum.service.impl.DiaryService;
import com.example.maum.util.CmmUtil;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpSession;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.web.bind.annotation.*;

import java.util.ArrayList;
import java.util.List;

@RestController
@Slf4j
@RequestMapping(value = "/api/v1/diary")
@RequiredArgsConstructor
public class DiaryController {

    private final DiaryService diaryService;

    /*
    일기 저장
    */
    @PostMapping(value = "diaryInsert")
    public MsgDTO diaryInsert(HttpServletRequest request, HttpSession session) throws Exception {

        log.info("{}.diaryInsert Start!", this.getClass().getName());

        String msg = "";

        Object sessionUserNo = session.getAttribute("SS_USER_NO");

        MsgDTO rDTO;

        if (sessionUserNo == null) {
            log.warn("세션 만료 또는 비로그인 사용자의 접근입니다.");

            rDTO = MsgDTO.builder()
                    .result(0)
                    .msg("로그인이 필요한 서비스입니다.")
                    .build();

            return rDTO;
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

        rDTO = MsgDTO.builder()
                .result(res)
                .msg(msg)
                .build();

        log.info("{}.diaryInsert End!", this.getClass().getName());

        return rDTO;
    }

    /*
    월별 일기 목록 불러오기
    */
    @GetMapping("/monthly")
    public ResponseEntity<CommonResponse<List<DiaryDTO>>> getMonthlyDiaryList(
            DiaryDTO pDTO,
            @AuthenticationPrincipal Jwt jwt) {

        log.info("{}.getMonthlyDiaryList Start!", this.getClass().getName());

        if (jwt == null) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                    .body(CommonResponse.<List<DiaryDTO>>builder()
                            .httpStatus(HttpStatus.UNAUTHORIZED)
                            .message("로그인이 필요합니다.")
                            .build());
        }

        try {
            Integer userNo = Integer.parseInt(jwt.getSubject());

            DiaryDTO sDTO = DiaryDTO.builder()
                    .userNo(userNo)
                    .createdAt(CmmUtil.nvl(pDTO.createdAt()))
                    .build();

            List<DiaryDTO> rList = diaryService.getMonthlyDiaryList(sDTO);
            if (rList == null) rList = new ArrayList<>();

            return ResponseEntity.ok(
                    CommonResponse.<List<DiaryDTO>>builder()
                            .httpStatus(HttpStatus.OK)
                            .message("조회 성공")
                            .data(rList)
                            .build()
            );

        } catch (Exception e) {
            log.error("조회 중 에러 발생: ", e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(CommonResponse.<List<DiaryDTO>>builder()
                            .httpStatus(HttpStatus.INTERNAL_SERVER_ERROR)
                            .message("서버 조회 오류")
                            .build());
        }
    }

    /*
    일기 상세 보기
    */
    @GetMapping("/detail")
    public ResponseEntity<CommonResponse<DiaryDTO>> getDiaryDetail(@RequestParam(value = "diaryNo") Integer diaryNo, HttpSession session) {

        log.info("{}.getDiaryDetail Start!", this.getClass().getName());

        ResponseEntity<CommonResponse<DiaryDTO>> response;

        Integer userNo = (Integer) session.getAttribute("SS_USER_NO");

        if (userNo == null) {
            response = ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                    .body(CommonResponse.<DiaryDTO>builder()
                            .httpStatus(HttpStatus.UNAUTHORIZED)
                            .message("로그인이 필요한 서비스입니다.")
                            .build());
            return response;
        }

        try {
            DiaryDTO pDTO = DiaryDTO.builder()
                    .diaryNo(diaryNo)
                    .userNo(userNo)
                    .build();

            DiaryDTO rDTO = diaryService.getDiaryDetail(pDTO);

            response = ResponseEntity.ok(
                    CommonResponse.<DiaryDTO>builder()
                            .httpStatus(HttpStatus.OK)
                            .message("일기 조회 성공")
                            .data(rDTO)
                            .build()
            );

        } catch (Exception e) {
            log.error("일기 상세 조회 중 오류 발생: ", e);
            response = ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(CommonResponse.<DiaryDTO>builder()
                            .httpStatus(HttpStatus.INTERNAL_SERVER_ERROR)
                            .message(e.getMessage())
                            .build());
        }

        return response;
    }
}