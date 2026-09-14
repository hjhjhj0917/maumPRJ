package com.example.maum.controller;

import com.example.maum.controller.response.CommonResponse;
import com.example.maum.dto.DiaryDTO;
import com.example.maum.dto.DiaryImageDTO;
import com.example.maum.dto.EmotionStatDTO;
import com.example.maum.dto.MsgDTO;
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

    /*
    일기 등록
    */
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

    /*
    일기 수정
    */
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

    /*
    일기 삭제
    */
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

    /*
    월별 일기 목록 조회
    */
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

    /*
    일기 상세 보기
    */
    @GetMapping("/detail")
    public ResponseEntity<CommonResponse<DiaryDTO>> getDiaryDetail(@RequestParam(value = "diaryNo") Integer diaryNo,
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

    /*
    일기 제목 검색
    */
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

    /*
    감정 필터 검색
    */
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

    /*
    최근 일기 목록 조회
    */
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

    /*
    즐겨찾기 일기 목록 조회
    */
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

    /*
    마이페이지 감정 통계 조회
    */
    @GetMapping("/emotions/stats")
    public ResponseEntity<CommonResponse<List<EmotionStatDTO>>> getEmotionStats(@AuthenticationPrincipal Jwt jwt) throws Exception {

        log.info("{}.getEmotionStats Start!", this.getClass().getName());

        String userNo = CmmUtil.nvl(jwt.getSubject());

        List<EmotionStatDTO> rList = Optional.ofNullable(diaryService.getEmotionStats(userNo))
                .orElseGet(ArrayList::new);

        log.info("{}.getEmotionStats End!", this.getClass().getName());

        return ResponseEntity.ok(
                CommonResponse.of(HttpStatus.OK, "감정 통계 조회 성공", rList)
        );
    }

    /*
    즐겨찾기
    */
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

    /*
    제목만 수정 (사이드바 인라인 이름변경)
    */
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

    /*
    사이드바 상단 고정
    */
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

    /*
    일기 이미지 업로드 (GCS, 일기당 최대 3장)
    */
    @PostMapping(value = "/images/upload", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<CommonResponse<List<DiaryImageDTO>>> uploadDiaryImages(
            @RequestParam("diaryNo") Integer diaryNo,
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

    /*
    일기 이미지 삭제
    */
    @PostMapping(value = "/images/delete")
    public ResponseEntity<CommonResponse<Integer>> deleteDiaryImage(@RequestBody DiaryImageDTO dDTO,
                                                                     @AuthenticationPrincipal Jwt jwt) throws Exception {

        log.info("{}.deleteDiaryImage Start!", this.getClass().getName());

        String userNo = CmmUtil.nvl(jwt.getSubject());
        Integer imageNo = dDTO.imageNo();

        MsgDTO rDTO = diaryService.deleteDiaryImage(imageNo, userNo);

        if (rDTO.result() != 1) {
            throw new IllegalArgumentException(rDTO.msg());
        }

        log.info("{}.deleteDiaryImage End!", this.getClass().getName());

        return ResponseEntity.ok(
                CommonResponse.of(HttpStatus.OK, rDTO.msg(), imageNo)
        );
    }
}