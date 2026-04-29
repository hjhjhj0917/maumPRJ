package com.example.maum.diary.controller;

import com.example.maum.diary.dto.DiaryDTO;
import com.example.maum.diary.service.impl.DiaryService;
import com.example.maum.global.controller.response.CommonResponse;
import com.example.maum.global.dto.MsgDTO;
import com.example.maum.global.util.CmmUtil;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpSession;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.ArrayList;
import java.util.List;

@RestController
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

    @GetMapping("/monthly")
    public ResponseEntity<CommonResponse<List<DiaryDTO>>> getMonthlyDiaryList(DiaryDTO pDTO, HttpSession session) {

        log.info("{}.getMonthlyDiaryList Start!", this.getClass().getName());

        Integer userNo = (Integer) session.getAttribute("SS_USER_NO");

        if (userNo == null) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                    .body(CommonResponse.<List<DiaryDTO>>builder()
                            .httpStatus(HttpStatus.UNAUTHORIZED)
                            .message("로그인이 필요합니다.")
                            .build());
        }

        try {
            // Record는 불변이므로 필요한 정보(userNo)를 합쳐 새 DTO 객체 생성
            DiaryDTO searchDTO = DiaryDTO.builder()
                    .userNo(userNo)
                    .createdAt(CmmUtil.nvl(pDTO.createdAt()))
                    .build();

            List<DiaryDTO> rList = diaryService.getMonthlyDiaryList(searchDTO);
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

    @GetMapping("/detail")
    public ResponseEntity<CommonResponse<DiaryDTO>> getDiaryDetail(@RequestParam(value = "diaryNo") Integer diaryNo, HttpSession session) {

        log.info("{}.getDiaryDetail Start!", this.getClass().getName());

        // 1. 세션에서 로그인 사용자 번호 확인
        Integer userNo = (Integer) session.getAttribute("SS_USER_NO");

        if (userNo == null) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                    .body(CommonResponse.<DiaryDTO>builder()
                            .httpStatus(HttpStatus.UNAUTHORIZED)
                            .message("로그인이 필요한 서비스입니다.")
                            .build());
        }

        try {
            // 2. 조회용 DTO 구성 (diaryNo와 userNo 포함)
            DiaryDTO pDTO = DiaryDTO.builder()
                    .diaryNo(diaryNo)
                    .userNo(userNo)
                    .build();

            // 3. 서비스 호출
            DiaryDTO rDTO = diaryService.getDiaryDetail(pDTO);

            // 4. 성공 응답 반환
            return ResponseEntity.ok(
                    CommonResponse.<DiaryDTO>builder()
                            .httpStatus(HttpStatus.OK)
                            .message("일기 조회 성공")
                            .data(rDTO)
                            .build()
            );

        } catch (Exception e) {
            log.error("일기 상세 조회 중 오류 발생: ", e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(CommonResponse.<DiaryDTO>builder()
                            .httpStatus(HttpStatus.INTERNAL_SERVER_ERROR)
                            .message(e.getMessage())
                            .build());
        }
    }
}
