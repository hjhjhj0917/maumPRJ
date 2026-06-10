package com.example.maum.controller;

import com.example.maum.controller.response.CommonResponse;
import com.example.maum.dto.DiaryDTO;
import com.example.maum.dto.EmotionStatDTO;
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

    @PostMapping(value = "diaryInsert")
    public ResponseEntity<CommonResponse<Integer>> diaryInsert(@RequestBody DiaryDTO dDTO, @AuthenticationPrincipal Jwt jwt) throws Exception {

        log.info("{}.diaryInsert Start!", this.getClass().getName());

        final String userNo = CmmUtil.nvl(jwt.getSubject());

        DiaryDTO pDTO = DiaryDTO.builder()
                .userNo(userNo)
                .title(CmmUtil.nvl(dDTO.title()))
                .content(CmmUtil.nvl(dDTO.content()))
                .createdAt(CmmUtil.nvl(dDTO.createdAt()))
                .build();

        int generatedDiaryNo = diaryService.diaryInsert(pDTO);

        log.info("일기 저장 결과(generatedDiaryNo): {}", generatedDiaryNo);
        log.info("{}.diaryInsert End!", this.getClass().getName());

        if (generatedDiaryNo > 0) {
            return ResponseEntity.ok(
                    CommonResponse.of(HttpStatus.OK, "저장이 완료되었습니다.", generatedDiaryNo)
            );
        } else {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(CommonResponse.of(HttpStatus.INTERNAL_SERVER_ERROR, "오류로 인해 저장이 실패하였습니다.", null));
        }
    }

    @PostMapping(value = "diaryUpdate")
    public ResponseEntity<CommonResponse<Integer>> diaryUpdate(@RequestBody DiaryDTO dDTO, @AuthenticationPrincipal Jwt jwt) throws Exception {

        log.info("{}.diaryUpdate Start!", this.getClass().getName());

        String userNo = CmmUtil.nvl(jwt.getSubject());
        Integer diaryNo = dDTO.diaryNo();

        DiaryDTO pDTO = DiaryDTO.builder()
                .userNo(userNo)
                .diaryNo(diaryNo)
                .title(CmmUtil.nvl(dDTO.title()))
                .content(CmmUtil.nvl(dDTO.content()))
                .build();

        MsgDTO rDTO = Optional.ofNullable(diaryService.diaryUpdate(pDTO))
                .orElseGet(() -> MsgDTO.builder().result(0).msg("일기 수정에 실패하였습니다.").build());

        log.info("{}.diaryUpdate End!", this.getClass().getName());

        if (rDTO.result() == 1) {
            return ResponseEntity.ok(
                    CommonResponse.of(HttpStatus.OK, rDTO.msg(), diaryNo)
            );
        } else {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(CommonResponse.of(HttpStatus.INTERNAL_SERVER_ERROR, rDTO.msg(), null));
        }
    }

    @PostMapping(value = "diaryDelete")
    public ResponseEntity<CommonResponse<Integer>> diaryDelete(@RequestBody DiaryDTO dDTO, @AuthenticationPrincipal Jwt jwt) throws Exception {

        log.info("{}.diaryDelete Start!", this.getClass().getName());

        String userNo = CmmUtil.nvl(jwt.getSubject());
        Integer diaryNo = dDTO.diaryNo();

        DiaryDTO pDTO = DiaryDTO.builder().userNo(userNo).diaryNo(diaryNo).build();

        MsgDTO rDTO = Optional.ofNullable(diaryService.diaryDelete(pDTO))
                .orElseGet(() -> MsgDTO.builder().result(0).msg("일기 삭제에 실패하였습니다.").build());

        log.info("{}.diaryDelete End!", this.getClass().getName());

        if (rDTO.result() == 1) {
            return ResponseEntity.ok(
                    CommonResponse.of(HttpStatus.OK, rDTO.msg(), diaryNo)
            );
        } else {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(CommonResponse.of(HttpStatus.INTERNAL_SERVER_ERROR, rDTO.msg(), null));
        }
    }

    @GetMapping("/monthly")
    public ResponseEntity<CommonResponse<List<DiaryDTO>>> getMonthlyDiaryList(DiaryDTO pDTO, @AuthenticationPrincipal Jwt jwt) {

        log.info("{}.getMonthlyDiaryList Start!", this.getClass().getName());

        try {
            String userNo = CmmUtil.nvl(jwt.getSubject());

            DiaryDTO sDTO = DiaryDTO.builder()
                    .userNo(userNo)
                    .createdAt(CmmUtil.nvl(pDTO.createdAt()))
                    .build();

            List<DiaryDTO> rList = diaryService.getMonthlyDiaryList(sDTO);
            if (rList == null) rList = new ArrayList<>();

            log.info("{}.getMonthlyDiaryList End!", this.getClass().getName());

            return ResponseEntity.ok(
                    CommonResponse.of(HttpStatus.OK, "조회 성공", rList)
            );

        } catch (Exception e) {
            log.error("조회 중 에러 발생: ", e);
            log.info("{}.getMonthlyDiaryList End!", this.getClass().getName());

            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(CommonResponse.of(HttpStatus.INTERNAL_SERVER_ERROR, "서버 조회 오류", null));
        }
    }

    @GetMapping("/detail")
    public ResponseEntity<CommonResponse<DiaryDTO>> getDiaryDetail(@RequestParam(value = "diaryNo") Integer diaryNo, @AuthenticationPrincipal Jwt jwt) {

        log.info("{}.getDiaryDetail Start!", this.getClass().getName());

        String userNo = CmmUtil.nvl(jwt.getSubject());

        try {
            DiaryDTO pDTO = DiaryDTO.builder()
                    .diaryNo(diaryNo)
                    .userNo(userNo)
                    .build();

            DiaryDTO rDTO = diaryService.getDiaryDetail(pDTO);

            log.info("{}.getDiaryDetail End!", this.getClass().getName());

            return ResponseEntity.ok(
                    CommonResponse.of(HttpStatus.OK, "일기 조회 성공", rDTO)
            );

        } catch (Exception e) {
            log.error("일기 상세 조회 중 오류 발생: ", e);
            log.info("{}.getDiaryDetail End!", this.getClass().getName());

            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(CommonResponse.of(HttpStatus.INTERNAL_SERVER_ERROR, e.getMessage(), null));
        }
    }

    @GetMapping("/search")
    public ResponseEntity<CommonResponse<List<DiaryDTO>>> searchDiaryList(@RequestParam(value = "keyword") String keyword, @AuthenticationPrincipal Jwt jwt) {

        log.info("{}.searchDiaryList Start!", this.getClass().getName());

        try {
            String userNo = CmmUtil.nvl(jwt.getSubject());

            DiaryDTO pDTO = DiaryDTO.builder()
                    .userNo(userNo)
                    .title(CmmUtil.nvl(keyword))
                    .build();

            List<DiaryDTO> rList = diaryService.searchDiaryList(pDTO);

            log.info("{}.searchDiaryList End!", this.getClass().getName());

            return ResponseEntity.ok(
                    CommonResponse.of(HttpStatus.OK, "검색 결과 조회 성공", rList)
            );
        } catch (Exception e) {
            log.error("검색 중 에러 발생: ", e);
            log.info("{}.searchDiaryList End!", this.getClass().getName());

            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(CommonResponse.of(HttpStatus.INTERNAL_SERVER_ERROR, "서버 조회 오류", null));
        }
    }

    @GetMapping("/filter")
    public ResponseEntity<CommonResponse<List<DiaryDTO>>> filterDiaryList(@RequestParam(value = "colors") List<String> colors, @AuthenticationPrincipal Jwt jwt) {

        log.info("{}.filterDiaryList Start!", this.getClass().getName());

        try {
            String userNo = CmmUtil.nvl(jwt.getSubject());

            List<DiaryDTO> rList = diaryService.getDiaryListByColors(userNo, colors);

            log.info("{}.filterDiaryList End!", this.getClass().getName());

            return ResponseEntity.ok(
                    CommonResponse.of(HttpStatus.OK, "필터 결과 조회 성공", rList)
            );
        } catch (Exception e) {
            log.error("필터 조회 중 에러: ", e);
            log.info("{}.filterDiaryList End!", this.getClass().getName());

            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(CommonResponse.of(HttpStatus.INTERNAL_SERVER_ERROR, "서버 조회 오류", null));
        }
    }

    @GetMapping("/recent")
    public ResponseEntity<CommonResponse<List<DiaryDTO>>> getRecentDiaryList(@AuthenticationPrincipal Jwt jwt) throws Exception {

        log.info("{}.getRecentDiaryList Start!", this.getClass().getName());

        String userNo = CmmUtil.nvl(jwt.getSubject());

        log.info("userNo: {}", userNo);

        DiaryDTO pDTO = DiaryDTO.builder()
                .userNo(userNo)
                .build();

        List<DiaryDTO> rList = Optional.ofNullable(diaryService.getRecentDiaryList(pDTO))
                .orElseGet(ArrayList::new);

        log.info("{}.getRecentDiaryList End!", this.getClass().getName());

        return ResponseEntity.ok(
                CommonResponse.of(HttpStatus.OK, "최근 일기 조회 성공", rList)
        );
    }

    @GetMapping("/emotions/stats")
    public ResponseEntity<CommonResponse<List<EmotionStatDTO>>> getEmotionStats(@AuthenticationPrincipal Jwt jwt) {
        log.info("{}.getEmotionStats Start!", this.getClass().getName());
        try {
            String userNo = CmmUtil.nvl(jwt.getSubject());
            List<EmotionStatDTO> rList = diaryService.getEmotionStats(userNo);

            log.info("{}.getEmotionStats End!", this.getClass().getName());
            return ResponseEntity.ok(
                    CommonResponse.of(HttpStatus.OK, "감정 통계 조회 성공", rList)
            );
        } catch (Exception e) {
            log.error("통계 조회 중 에러: ", e);
            log.info("{}.getEmotionStats End!", this.getClass().getName());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(CommonResponse.of(HttpStatus.INTERNAL_SERVER_ERROR, "서버 조회 오류", null));
        }
    }
}