package com.example.maum.controller;

import com.example.maum.controller.response.CommonResponse;
import com.example.maum.dto.DiaryDTO;
import com.example.maum.dto.MsgDTO;
import com.example.maum.service.impl.DiaryService;
import com.example.maum.util.CmmUtil;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.web.bind.annotation.*;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

@RestController
@Slf4j
@RequestMapping(value = "/api/v1/diary")
@RequiredArgsConstructor
public class DiaryController {

    private final DiaryService diaryService;

    /* [Diary Management] */

    @PostMapping(value = "diaryInsert")
    public MsgDTO diaryInsert(@RequestBody DiaryDTO dDTO, @AuthenticationPrincipal Jwt jwt) throws Exception {

        log.info("{}.diaryInsert Start!", this.getClass().getName());

        String msg = "";

        MsgDTO rDTO;

        if (jwt == null) {
            log.warn("인증 정보가 없습니다.");
            return MsgDTO.builder()
                    .result(0)
                    .msg("로그인이 필요한 서비스입니다.")
                    .build();
        }

        Integer userNo = Integer.parseInt(jwt.getSubject());
        String title = CmmUtil.nvl(dDTO.title());
        String content = CmmUtil.nvl(dDTO.content());
        String createdAt = CmmUtil.nvl(dDTO.createdAt());

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


    @PostMapping(value = "diaryUpdate")
    public MsgDTO diaryUpdate(@RequestBody DiaryDTO dDTO, @AuthenticationPrincipal Jwt jwt) throws Exception {

        log.info("{}.diaryUpdate Start!", this.getClass().getName());

        if (jwt == null) { // 이거 그냥 로그인 정보 없으면 아예 페이지 조차 접근 못 하게 처리하는 방법 생각해 보기
            log.warn("인증 정보가 없습니다.");
            return MsgDTO.builder()
                    .result(0)
                    .msg("로그인이 필요한 서비스입니다.")
                    .build();
        }

        Integer userNo = Integer.parseInt(jwt.getSubject());
        Integer diaryNo = dDTO.diaryNo();
        String title = CmmUtil.nvl(dDTO.title());
        String content = CmmUtil.nvl(dDTO.content());

        log.info("userNo: {}, diaryNo: {}, title: {}, content: {}", userNo, diaryNo, title, content);

        DiaryDTO pDTO = DiaryDTO.builder()
                .userNo(userNo)
                .diaryNo(diaryNo)
                .title(title)
                .content(content)
                .build();

        MsgDTO rDTO = Optional.ofNullable(diaryService.diaryUpdate(pDTO))
                .orElseGet(() -> MsgDTO.builder().result(0).msg("일기 수정에 실패하였습니다.").build());

        log.info("{}.diaryUpdate End!", this.getClass().getName());

        return rDTO;
    }


    @PostMapping(value = "diaryDelete")
    public MsgDTO diaryDelete(@RequestBody DiaryDTO dDTO, @AuthenticationPrincipal Jwt jwt) throws Exception {

        log.info("{}.diaryDelete Start!", this.getClass().getName());

        if (jwt == null) {
            log.warn("인증 정보가 없습니다.");
            return MsgDTO.builder()
                    .result(0)
                    .msg("로그인이 필요한 서비스입니다.")
                    .build();
        }


        Integer userNo = Integer.parseInt(jwt.getSubject());
        Integer diaryNo = dDTO.diaryNo();

        log.info("userNo: {}, diaryNo: {}", userNo, diaryNo);

        DiaryDTO pDTO = DiaryDTO.builder()
                .userNo(userNo)
                .diaryNo(diaryNo)
                .build();

        MsgDTO rDTO = Optional.ofNullable(diaryService.diaryDelete(pDTO))
                .orElseGet(() -> MsgDTO.builder().result(0).msg("일기 삭제에 실패하였습니다.").build());

        log.info("{}.diaryDelete End!", this.getClass().getName());

        return rDTO;
    }


    /* [Diary Retrieval] */

    @GetMapping("/monthly")
    public ResponseEntity<CommonResponse<List<DiaryDTO>>> getMonthlyDiaryList(DiaryDTO pDTO, @AuthenticationPrincipal Jwt jwt) {

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


    @GetMapping("/detail")
    public ResponseEntity<CommonResponse<DiaryDTO>> getDiaryDetail(@RequestParam(value = "diaryNo") Integer diaryNo, @AuthenticationPrincipal Jwt jwt) {

        log.info("{}.getDiaryDetail Start!", this.getClass().getName());

        ResponseEntity<CommonResponse<DiaryDTO>> res;

        if (jwt == null) {
            res = ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                    .body(CommonResponse.<DiaryDTO>builder()
                            .httpStatus(HttpStatus.UNAUTHORIZED)
                            .message("로그인이 필요한 서비스입니다.")
                            .build());
            return res;
        }

        Integer userNo = Integer.parseInt(jwt.getSubject());

        try {
            DiaryDTO pDTO = DiaryDTO.builder()
                    .diaryNo(diaryNo)
                    .userNo(userNo)
                    .build();

            DiaryDTO rDTO = diaryService.getDiaryDetail(pDTO);

            res = ResponseEntity.ok(
                    CommonResponse.<DiaryDTO>builder()
                            .httpStatus(HttpStatus.OK)
                            .message("일기 조회 성공")
                            .data(rDTO)
                            .build()
            );

        } catch (Exception e) {
            log.error("일기 상세 조회 중 오류 발생: ", e);
            res = ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(CommonResponse.<DiaryDTO>builder()
                            .httpStatus(HttpStatus.INTERNAL_SERVER_ERROR)
                            .message(e.getMessage())
                            .build());
        }

        return res;
    }


    @GetMapping("/search")
    public ResponseEntity<CommonResponse<List<DiaryDTO>>> searchDiaryList(@RequestParam(value = "keyword") String keyword, @AuthenticationPrincipal Jwt jwt) {

        log.info("{}.searchDiaryList Start!", this.getClass().getName());

        if (jwt == null) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
        }

        try {
            Integer userNo = Integer.parseInt(jwt.getSubject());
            DiaryDTO pDTO = DiaryDTO.builder()
                    .userNo(userNo)
                    .title(CmmUtil.nvl(keyword))
                    .build();

            List<DiaryDTO> rList = diaryService.searchDiaryList(pDTO);

            log.info("{}.searchDiaryList End!", this.getClass().getName());

            return ResponseEntity.ok(
                    CommonResponse.<List<DiaryDTO>>builder()
                            .httpStatus(HttpStatus.OK)
                            .message("검색 결과 조회 성공")
                            .data(rList)
                            .build()
            );
        } catch (Exception e) {
            log.error("검색 중 에러 발생: ", e);
            log.info("{}.searchDiaryList End!", this.getClass().getName());

            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }


    @GetMapping("/filter")
    public ResponseEntity<CommonResponse<List<DiaryDTO>>> filterDiaryList(@RequestParam(value = "colors") List<String> colors,
            @AuthenticationPrincipal Jwt jwt) {

        log.info("{}.filterDiaryList Start!", this.getClass().getName());

        if (jwt == null) return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();

        try {
            Integer userNo = Integer.parseInt(jwt.getSubject());
            // Service에 colors 리스트를 전달하여 조회
            List<DiaryDTO> rList = diaryService.getDiaryListByColors(userNo, colors);

            log.info("{}.filterDiaryList End!", this.getClass().getName());

            return ResponseEntity.ok(
                    CommonResponse.<List<DiaryDTO>>builder()
                            .httpStatus(HttpStatus.OK)
                            .message("필터 결과 조회 성공")
                            .data(rList)
                            .build()
            );
        } catch (Exception e) {
            log.error("필터 조회 중 에러: ", e);
            log.info("{}.filterDiaryList End!", this.getClass().getName());

            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }
}