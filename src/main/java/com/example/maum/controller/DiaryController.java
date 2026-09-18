package com.example.maum.controller;

import com.example.maum.controller.response.CommonResponse;
import com.example.maum.dto.DepressionTrendDTO;
import com.example.maum.dto.DiaryDTO;
import com.example.maum.dto.DiaryImageDTO;
import com.example.maum.dto.DiaryStatsDTO;
import com.example.maum.dto.EmotionStatDTO;
import com.example.maum.dto.MsgDTO;
import com.example.maum.dto.ReportCardDTO;
import com.example.maum.dto.TopMusicDTO;
import com.example.maum.service.impl.DiaryService;
import com.example.maum.util.CmmUtil;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

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
    public ResponseEntity<CommonResponse<Integer>> diaryInsert(@RequestBody DiaryDTO dDTO,
                                                               @AuthenticationPrincipal Jwt jwt) throws Exception {

        log.info("{}.diaryInsert Start!", this.getClass().getName());

        String userNo = CmmUtil.nvl(jwt.getSubject());

        DiaryDTO pDTO = DiaryDTO.builder()
                .userNo(userNo)
                .title(CmmUtil.nvl(dDTO.title()))
                .content(CmmUtil.nvl(dDTO.content()))
                .createdAt(CmmUtil.nvl(dDTO.createdAt()))
                .build();

        int generatedDiaryNo = diaryService.diaryInsert(pDTO);

        if (generatedDiaryNo <= 0) {
            throw new RuntimeException("오류로 인해 저장이 실패하였습니다.");
        }

        log.info("일기 저장 결과(generatedDiaryNo): {}", generatedDiaryNo);
        log.info("{}.diaryInsert End!", this.getClass().getName());

        return ResponseEntity.ok(
                CommonResponse.of(HttpStatus.OK, "저장이 완료되었습니다.", generatedDiaryNo)
        );
    }

    // AI 분석/음악 추천 없이 제목/내용만 저장 (자동저장용)
    @PostMapping(value = "draftSave")
    public ResponseEntity<CommonResponse<Integer>> diaryDraftSave(@RequestBody DiaryDTO dDTO,
                                                                   @AuthenticationPrincipal Jwt jwt) throws Exception {

        log.info("{}.diaryDraftSave Start!", this.getClass().getName());

        String userNo = CmmUtil.nvl(jwt.getSubject());

        DiaryDTO pDTO = DiaryDTO.builder()
                .diaryNo(dDTO.diaryNo())
                .userNo(userNo)
                .title(CmmUtil.nvl(dDTO.title()))
                .content(CmmUtil.nvl(dDTO.content()))
                .createdAt(CmmUtil.nvl(dDTO.createdAt()))
                .build();

        int diaryNo = diaryService.draftSave(pDTO);

        if (diaryNo <= 0) {
            throw new RuntimeException("임시저장에 실패하였습니다.");
        }

        log.info("{}.diaryDraftSave End!", this.getClass().getName());

        return ResponseEntity.ok(
                CommonResponse.of(HttpStatus.OK, "임시저장되었습니다.", diaryNo)
        );
    }

    @PostMapping(value = "diaryUpdate")
    public ResponseEntity<CommonResponse<Integer>> diaryUpdate(@RequestBody DiaryDTO dDTO,
                                                               @AuthenticationPrincipal Jwt jwt) throws Exception {

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
                .orElseThrow(() -> new RuntimeException("일기 수정에 실패하였습니다."));

        if (rDTO.result() != 1) {
            throw new RuntimeException(rDTO.msg());
        }

        log.info("{}.diaryUpdate End!", this.getClass().getName());

        return ResponseEntity.ok(
                CommonResponse.of(HttpStatus.OK, rDTO.msg(), diaryNo)
        );
    }

    @PostMapping(value = "diaryDelete")
    public ResponseEntity<CommonResponse<Integer>> diaryDelete(@RequestBody DiaryDTO dDTO,
                                                               @AuthenticationPrincipal Jwt jwt) throws Exception {

        log.info("{}.diaryDelete Start!", this.getClass().getName());

        String userNo = CmmUtil.nvl(jwt.getSubject());
        Integer diaryNo = dDTO.diaryNo();

        DiaryDTO pDTO = DiaryDTO.builder().userNo(userNo).diaryNo(diaryNo).build();

        MsgDTO rDTO = Optional.ofNullable(diaryService.diaryDelete(pDTO))
                .orElseThrow(() -> new RuntimeException("일기 삭제에 실패하였습니다."));

        if (rDTO.result() != 1) {
            throw new RuntimeException(rDTO.msg());
        }

        log.info("{}.diaryDelete End!", this.getClass().getName());

        return ResponseEntity.ok(
                CommonResponse.of(HttpStatus.OK, rDTO.msg(), diaryNo)
        );
    }

    @GetMapping("/monthly")
    public ResponseEntity<CommonResponse<List<DiaryDTO>>> getMonthlyDiaryList(DiaryDTO pDTO,
                                                                              @AuthenticationPrincipal Jwt jwt) throws Exception {

        log.info("{}.getMonthlyDiaryList Start!", this.getClass().getName());

        String userNo = CmmUtil.nvl(jwt.getSubject());

        DiaryDTO sDTO = DiaryDTO.builder()
                .userNo(userNo)
                .createdAt(CmmUtil.nvl(pDTO.createdAt()))
                .build();

        List<DiaryDTO> rList = Optional.ofNullable(diaryService.getMonthlyDiaryList(sDTO))
                .orElseGet(ArrayList::new);

        log.info("{}.getMonthlyDiaryList End!", this.getClass().getName());

        return ResponseEntity.ok(
                CommonResponse.of(HttpStatus.OK, "조회 성공", rList)
        );
    }

    @GetMapping("/{diaryNo}")
    public ResponseEntity<CommonResponse<DiaryDTO>> getDiaryDetail(@PathVariable Integer diaryNo,
                                                                   @AuthenticationPrincipal Jwt jwt) throws Exception {

        log.info("{}.getDiaryDetail Start!", this.getClass().getName());

        String userNo = CmmUtil.nvl(jwt.getSubject());

        DiaryDTO pDTO = DiaryDTO.builder()
                .diaryNo(diaryNo)
                .userNo(userNo)
                .build();

        DiaryDTO rDTO = Optional.ofNullable(diaryService.getDiaryDetail(pDTO))
                .orElseThrow(() -> new IllegalArgumentException("존재하지 않거나 조회할 수 없는 일기입니다."));

        log.info("{}.getDiaryDetail End!", this.getClass().getName());

        return ResponseEntity.ok(
                CommonResponse.of(HttpStatus.OK, "일기 조회 성공", rDTO)
        );
    }

    @GetMapping("/search")
    public ResponseEntity<CommonResponse<List<DiaryDTO>>> searchDiaryList(@RequestParam(value = "keyword") String keyword,
                                                                          @AuthenticationPrincipal Jwt jwt) throws Exception {

        log.info("{}.searchDiaryList Start!", this.getClass().getName());

        String userNo = CmmUtil.nvl(jwt.getSubject());

        DiaryDTO pDTO = DiaryDTO.builder()
                .userNo(userNo)
                .title(CmmUtil.nvl(keyword))
                .build();

        List<DiaryDTO> rList = Optional.ofNullable(diaryService.searchDiaryList(pDTO))
                .orElseGet(ArrayList::new);

        log.info("{}.searchDiaryList End!", this.getClass().getName());

        return ResponseEntity.ok(
                CommonResponse.of(HttpStatus.OK, "검색 결과 조회 성공", rList)
        );
    }

    @GetMapping("/filter")
    public ResponseEntity<CommonResponse<List<DiaryDTO>>> filterDiaryList(@RequestParam(value = "colors") List<String> colors,
                                                                          @AuthenticationPrincipal Jwt jwt) throws Exception {

        log.info("{}.filterDiaryList Start!", this.getClass().getName());

        String userNo = CmmUtil.nvl(jwt.getSubject());

        List<DiaryDTO> rList = Optional.ofNullable(diaryService.getDiaryListByColors(userNo, colors))
                .orElseGet(ArrayList::new);

        log.info("{}.filterDiaryList End!", this.getClass().getName());

        return ResponseEntity.ok(
                CommonResponse.of(HttpStatus.OK, "필터 결과 조회 성공", rList)
        );
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

    @GetMapping("/favorites")
    public ResponseEntity<CommonResponse<List<DiaryDTO>>> getFavoriteDiaryList(@AuthenticationPrincipal Jwt jwt) throws Exception {

        log.info("{}.getFavoriteDiaryList Start!", this.getClass().getName());

        String userNo = CmmUtil.nvl(jwt.getSubject());

        DiaryDTO pDTO = DiaryDTO.builder()
                .userNo(userNo)
                .build();

        List<DiaryDTO> rList = Optional.ofNullable(diaryService.getFavoriteDiaryList(pDTO))
                .orElseGet(ArrayList::new);

        log.info("{}.getFavoriteDiaryList End!", this.getClass().getName());

        return ResponseEntity.ok(
                CommonResponse.of(HttpStatus.OK, "즐겨찾기 목록 조회 성공", rList)
        );
    }

    @GetMapping("/emotions/stats")
    public ResponseEntity<CommonResponse<List<EmotionStatDTO>>> getEmotionStats(@AuthenticationPrincipal Jwt jwt) throws Exception {

        log.info("{}.getEmotionStats Start!", this.getClass().getName());

        String userNo = CmmUtil.nvl(jwt.getSubject());

        DiaryDTO pDTO = DiaryDTO.builder().userNo(userNo).build();

        List<EmotionStatDTO> rList = Optional.ofNullable(diaryService.getEmotionStats(pDTO))
                .orElseGet(ArrayList::new);

        log.info("{}.getEmotionStats End!", this.getClass().getName());

        return ResponseEntity.ok(
                CommonResponse.of(HttpStatus.OK, "감정 통계 조회 성공", rList)
        );
    }

    @GetMapping("/stats/summary")
    public ResponseEntity<CommonResponse<DiaryStatsDTO>> getDiaryStats(@AuthenticationPrincipal Jwt jwt) throws Exception {

        log.info("{}.getDiaryStats Start!", this.getClass().getName());

        String userNo = CmmUtil.nvl(jwt.getSubject());

        DiaryDTO pDTO = DiaryDTO.builder().userNo(userNo).build();

        DiaryStatsDTO rDTO = Optional.ofNullable(diaryService.getDiaryStats(pDTO))
                .orElseThrow(() -> new RuntimeException("통계 조회에 실패하였습니다."));

        log.info("{}.getDiaryStats End!", this.getClass().getName());

        return ResponseEntity.ok(
                CommonResponse.of(HttpStatus.OK, "통계 조회 성공", rDTO)
        );
    }

    @GetMapping("/stats/trend")
    public ResponseEntity<CommonResponse<List<DepressionTrendDTO>>> getDepressionTrend(@AuthenticationPrincipal Jwt jwt) throws Exception {

        log.info("{}.getDepressionTrend Start!", this.getClass().getName());

        String userNo = CmmUtil.nvl(jwt.getSubject());

        DiaryDTO pDTO = DiaryDTO.builder().userNo(userNo).build();

        List<DepressionTrendDTO> rList = Optional.ofNullable(diaryService.getDepressionTrend(pDTO))
                .orElseGet(ArrayList::new);

        log.info("{}.getDepressionTrend End!", this.getClass().getName());

        return ResponseEntity.ok(
                CommonResponse.of(HttpStatus.OK, "우울 지수 추이 조회 성공", rList)
        );
    }

    @GetMapping("/stats/top-music")
    public ResponseEntity<CommonResponse<List<TopMusicDTO>>> getTopRecommendedMusic(@AuthenticationPrincipal Jwt jwt) throws Exception {

        log.info("{}.getTopRecommendedMusic Start!", this.getClass().getName());

        String userNo = CmmUtil.nvl(jwt.getSubject());

        DiaryDTO pDTO = DiaryDTO.builder().userNo(userNo).build();

        List<TopMusicDTO> rList = Optional.ofNullable(diaryService.getTopRecommendedMusic(pDTO))
                .orElseGet(ArrayList::new);

        log.info("{}.getTopRecommendedMusic End!", this.getClass().getName());

        return ResponseEntity.ok(
                CommonResponse.of(HttpStatus.OK, "인기 추천곡 조회 성공", rList)
        );
    }

    @GetMapping("/stats/report")
    public ResponseEntity<CommonResponse<ReportCardDTO>> getWeeklyReport(@AuthenticationPrincipal Jwt jwt) throws Exception {

        log.info("{}.getWeeklyReport Start!", this.getClass().getName());

        String userNo = CmmUtil.nvl(jwt.getSubject());

        DiaryDTO pDTO = DiaryDTO.builder().userNo(userNo).build();

        ReportCardDTO rDTO = Optional.ofNullable(diaryService.getWeeklyReport(pDTO))
                .orElseThrow(() -> new RuntimeException("주간 리포트 조회에 실패하였습니다."));

        log.info("{}.getWeeklyReport End!", this.getClass().getName());

        return ResponseEntity.ok(
                CommonResponse.of(HttpStatus.OK, "주간 리포트 조회 성공", rDTO)
        );
    }

    @PostMapping("/favorite")
    public ResponseEntity<CommonResponse<Integer>> diaryFavorite(@RequestBody DiaryDTO dDTO,
                                                                 @AuthenticationPrincipal Jwt jwt) throws Exception {

        log.info("{}.diaryFavorite Start!", this.getClass().getName());

        String userNo = CmmUtil.nvl(jwt.getSubject());
        Integer diaryNo = dDTO.diaryNo();
        Integer isFavorite = dDTO.isFavorite();

        log.info("userNo: {}, diaryNo: {}, isFavorite: {}", userNo, diaryNo, isFavorite);

        DiaryDTO pDTO = DiaryDTO.builder()
                .diaryNo(diaryNo)
                .userNo(userNo)
                .isFavorite(isFavorite)
                .build();

        int res = diaryService.updateFavorite(pDTO);

        if (res == 0) {
            throw new IllegalArgumentException("본인의 일기만 변경할 수 있거나, 존재하지 않는 일기입니다.");
        }

        String msg = (isFavorite == 1) ? "즐겨찾기에 추가되었습니다." : "즐겨찾기가 해제되었습니다.";

        log.info("{}.diaryFavorite End!", this.getClass().getName());

        return ResponseEntity.ok(
                CommonResponse.of(HttpStatus.OK, msg, diaryNo)
        );
    }

    // 제목만 수정 (사이드바 인라인 이름변경용)
    @PostMapping("/title")
    public ResponseEntity<CommonResponse<Integer>> diaryUpdateTitle(@RequestBody DiaryDTO dDTO,
                                                                     @AuthenticationPrincipal Jwt jwt) throws Exception {

        log.info("{}.diaryUpdateTitle Start!", this.getClass().getName());

        String userNo = CmmUtil.nvl(jwt.getSubject());
        Integer diaryNo = dDTO.diaryNo();

        DiaryDTO pDTO = DiaryDTO.builder()
                .diaryNo(diaryNo)
                .userNo(userNo)
                .title(CmmUtil.nvl(dDTO.title()))
                .build();

        int res = diaryService.updateTitle(pDTO);

        if (res == 0) {
            throw new IllegalArgumentException("본인의 일기만 변경할 수 있거나, 존재하지 않는 일기입니다.");
        }

        log.info("{}.diaryUpdateTitle End!", this.getClass().getName());

        return ResponseEntity.ok(
                CommonResponse.of(HttpStatus.OK, "제목이 변경되었습니다.", diaryNo)
        );
    }

    @PostMapping("/pin")
    public ResponseEntity<CommonResponse<Integer>> diaryPin(@RequestBody DiaryDTO dDTO,
                                                             @AuthenticationPrincipal Jwt jwt) throws Exception {

        log.info("{}.diaryPin Start!", this.getClass().getName());

        String userNo = CmmUtil.nvl(jwt.getSubject());
        Integer diaryNo = dDTO.diaryNo();
        Integer isPinned = dDTO.isPinned();

        DiaryDTO pDTO = DiaryDTO.builder()
                .diaryNo(diaryNo)
                .userNo(userNo)
                .isPinned(isPinned)
                .build();

        int res = diaryService.updatePinned(pDTO);

        if (res == 0) {
            throw new IllegalArgumentException("본인의 일기만 변경할 수 있거나, 존재하지 않는 일기입니다.");
        }

        String msg = (isPinned == 1) ? "상단에 고정되었습니다." : "고정이 해제되었습니다.";

        log.info("{}.diaryPin End!", this.getClass().getName());

        return ResponseEntity.ok(
                CommonResponse.of(HttpStatus.OK, msg, diaryNo)
        );
    }

    // GCS에 업로드, 일기당 최대 3장까지 허용
    @PostMapping(value = "/{diaryNo}/images/upload", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<CommonResponse<List<DiaryImageDTO>>> uploadDiaryImages(
            @PathVariable Integer diaryNo,
            @RequestParam("images") List<MultipartFile> images,
            @AuthenticationPrincipal Jwt jwt) throws Exception {

        log.info("{}.uploadDiaryImages Start!", this.getClass().getName());

        String userNo = CmmUtil.nvl(jwt.getSubject());

        List<DiaryImageDTO> rList = diaryService.uploadDiaryImages(diaryNo, userNo, images);

        log.info("{}.uploadDiaryImages End!", this.getClass().getName());

        return ResponseEntity.ok(
                CommonResponse.of(HttpStatus.OK, "이미지가 업로드되었습니다.", rList)
        );
    }

    @PostMapping(value = "/images/delete")
    public ResponseEntity<CommonResponse<Integer>> deleteDiaryImage(@RequestBody DiaryImageDTO dDTO,
                                                                     @AuthenticationPrincipal Jwt jwt) throws Exception {

        log.info("{}.deleteDiaryImage Start!", this.getClass().getName());

        String userNo = CmmUtil.nvl(jwt.getSubject());
        Integer imageNo = dDTO.imageNo();

        DiaryImageDTO pDTO = DiaryImageDTO.builder()
                .imageNo(imageNo)
                .userNo(userNo)
                .build();

        MsgDTO rDTO = diaryService.deleteDiaryImage(pDTO);

        if (rDTO.result() != 1) {
            throw new IllegalArgumentException(rDTO.msg());
        }

        log.info("{}.deleteDiaryImage End!", this.getClass().getName());

        return ResponseEntity.ok(
                CommonResponse.of(HttpStatus.OK, rDTO.msg(), imageNo)
        );
    }
}