package com.example.maum.controller;

import com.example.maum.controller.response.CommonResponse;
import com.example.maum.dto.DiaryDTO;
import com.example.maum.dto.DiaryImageDTO;
import com.example.maum.dto.MsgDTO;
import com.example.maum.service.IDiaryService;
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

// 사용자는 JWT subject(publicId)로만 식별하고, 요청 본문으로 받은 사용자 값은 쓰지 않는다.
// 통계·추이·인기곡·주간 리포트 API 는 5단계에서 새 구조로 다시 만든다.
@RestController
@Slf4j
@RequestMapping(value = "/api/v1/diary")
@RequiredArgsConstructor
public class DiaryController {

    private final IDiaryService diaryService;

    // 발행: 바로 응답하고 분석은 뒤에서 진행됨. 결과는 상세 조회의 analysisStatus / summaryStatus 로 확인
    @PostMapping(value = "/diaryInsert")
    public ResponseEntity<CommonResponse<String>> diaryInsert(@RequestBody DiaryDTO dDTO,
                                                              @AuthenticationPrincipal Jwt jwt) throws Exception {

        log.info("{}.diaryInsert Start!", this.getClass().getName());

        String userPublicId = CmmUtil.nvl(jwt.getSubject());

        log.info("userPublicId: {}, publicId: {}, diaryDate: {}, titleLength: {}, contentLength: {}",
                userPublicId, dDTO.publicId(), CmmUtil.nvl(dDTO.diaryDate()),
                CmmUtil.nvl(dDTO.title()).length(), CmmUtil.nvl(dDTO.content()).length());

        String publicId = diaryService.diaryInsert(withUser(dDTO, userPublicId));

        log.info("{}.diaryInsert End!", this.getClass().getName());

        return ResponseEntity.ok(
                CommonResponse.of(HttpStatus.OK, "저장이 완료되었습니다.", publicId)
        );
    }

    // AI 분석/음악 추천 없이 제목/내용만 저장 (자동저장용)
    @PostMapping(value = "/draftSave")
    public ResponseEntity<CommonResponse<String>> diaryDraftSave(@RequestBody DiaryDTO dDTO,
                                                                 @AuthenticationPrincipal Jwt jwt) throws Exception {

        log.info("{}.diaryDraftSave Start!", this.getClass().getName());

        String userPublicId = CmmUtil.nvl(jwt.getSubject());

        log.info("userPublicId: {}, publicId: {}, diaryDate: {}, titleLength: {}, contentLength: {}",
                userPublicId, dDTO.publicId(), CmmUtil.nvl(dDTO.diaryDate()),
                CmmUtil.nvl(dDTO.title()).length(), CmmUtil.nvl(dDTO.content()).length());

        String publicId = diaryService.draftSave(withUser(dDTO, userPublicId));

        log.info("{}.diaryDraftSave End!", this.getClass().getName());

        return ResponseEntity.ok(
                CommonResponse.of(HttpStatus.OK, "임시저장되었습니다.", publicId)
        );
    }

    @PostMapping(value = "/diaryUpdate")
    public ResponseEntity<CommonResponse<String>> diaryUpdate(@RequestBody DiaryDTO dDTO,
                                                              @AuthenticationPrincipal Jwt jwt) throws Exception {

        log.info("{}.diaryUpdate Start!", this.getClass().getName());

        String userPublicId = CmmUtil.nvl(jwt.getSubject());

        log.info("userPublicId: {}, publicId: {}, titleLength: {}, contentLength: {}",
                userPublicId, dDTO.publicId(), CmmUtil.nvl(dDTO.title()).length(), CmmUtil.nvl(dDTO.content()).length());

        MsgDTO rDTO = Optional.ofNullable(diaryService.diaryUpdate(withUser(dDTO, userPublicId)))
                .orElseThrow(() -> new RuntimeException("일기 수정에 실패하였습니다."));

        log.info("{}.diaryUpdate End!", this.getClass().getName());

        return ResponseEntity.ok(
                CommonResponse.of(HttpStatus.OK, rDTO.msg(), dDTO.publicId())
        );
    }

    @PostMapping(value = "/diaryDelete")
    public ResponseEntity<CommonResponse<String>> diaryDelete(@RequestBody DiaryDTO dDTO,
                                                              @AuthenticationPrincipal Jwt jwt) throws Exception {

        log.info("{}.diaryDelete Start!", this.getClass().getName());

        String userPublicId = CmmUtil.nvl(jwt.getSubject());

        log.info("userPublicId: {}, publicId: {}", userPublicId, dDTO.publicId());

        MsgDTO rDTO = Optional.ofNullable(diaryService.diaryDelete(withUser(dDTO, userPublicId)))
                .orElseThrow(() -> new RuntimeException("일기 삭제에 실패하였습니다."));

        log.info("{}.diaryDelete End!", this.getClass().getName());

        return ResponseEntity.ok(
                CommonResponse.of(HttpStatus.OK, rDTO.msg(), dDTO.publicId())
        );
    }

    @GetMapping(value = "/monthly")
    public ResponseEntity<CommonResponse<List<DiaryDTO>>> getMonthlyDiaryList(
            @RequestParam(value = "diaryDate") String diaryDate,
            @AuthenticationPrincipal Jwt jwt) throws Exception {

        log.info("{}.getMonthlyDiaryList Start!", this.getClass().getName());

        String userPublicId = CmmUtil.nvl(jwt.getSubject());

        log.info("userPublicId: {}, diaryDate: {}", userPublicId, diaryDate);

        DiaryDTO pDTO = DiaryDTO.builder().userPublicId(userPublicId).diaryDate(diaryDate).build();

        List<DiaryDTO> rList = Optional.ofNullable(diaryService.getMonthlyDiaryList(pDTO))
                .orElseGet(ArrayList::new);

        log.info("{}.getMonthlyDiaryList End!", this.getClass().getName());

        return ResponseEntity.ok(
                CommonResponse.of(HttpStatus.OK, "조회 성공", rList)
        );
    }

    @GetMapping(value = "/{publicId}")
    public ResponseEntity<CommonResponse<DiaryDTO>> getDiaryDetail(@PathVariable String publicId,
                                                                   @AuthenticationPrincipal Jwt jwt) throws Exception {

        log.info("{}.getDiaryDetail Start!", this.getClass().getName());

        String userPublicId = CmmUtil.nvl(jwt.getSubject());

        log.info("userPublicId: {}, publicId: {}", userPublicId, publicId);

        DiaryDTO pDTO = DiaryDTO.builder().publicId(publicId).userPublicId(userPublicId).build();

        DiaryDTO rDTO = Optional.ofNullable(diaryService.getDiaryDetail(pDTO))
                .orElseThrow(() -> new IllegalArgumentException("존재하지 않거나 조회할 수 없는 일기입니다."));

        log.info("{}.getDiaryDetail End!", this.getClass().getName());

        return ResponseEntity.ok(
                CommonResponse.of(HttpStatus.OK, "일기 조회 성공", rDTO)
        );
    }

    @GetMapping(value = "/search")
    public ResponseEntity<CommonResponse<List<DiaryDTO>>> searchDiaryList(@RequestParam(value = "keyword") String keyword,
                                                                          @AuthenticationPrincipal Jwt jwt) throws Exception {

        log.info("{}.searchDiaryList Start!", this.getClass().getName());

        String userPublicId = CmmUtil.nvl(jwt.getSubject());

        log.info("userPublicId: {}, keywordLength: {}", userPublicId, CmmUtil.nvl(keyword).length());

        DiaryDTO pDTO = DiaryDTO.builder().userPublicId(userPublicId).title(keyword).build();

        List<DiaryDTO> rList = Optional.ofNullable(diaryService.searchDiaryList(pDTO))
                .orElseGet(ArrayList::new);

        log.info("{}.searchDiaryList End!", this.getClass().getName());

        return ResponseEntity.ok(
                CommonResponse.of(HttpStatus.OK, "검색 성공", rList)
        );
    }

    // 대분류 감정 코드(JOY, SADNESS ...)로 필터
    @GetMapping(value = "/filter")
    public ResponseEntity<CommonResponse<List<DiaryDTO>>> filterDiaryList(@RequestParam(value = "groups") List<String> groups,
                                                                          @AuthenticationPrincipal Jwt jwt) throws Exception {

        log.info("{}.filterDiaryList Start!", this.getClass().getName());

        String userPublicId = CmmUtil.nvl(jwt.getSubject());

        log.info("userPublicId: {}, groups: {}", userPublicId, groups);

        List<DiaryDTO> rList = Optional.ofNullable(diaryService.getDiaryListByGroups(userPublicId, groups))
                .orElseGet(ArrayList::new);

        log.info("{}.filterDiaryList End!", this.getClass().getName());

        return ResponseEntity.ok(
                CommonResponse.of(HttpStatus.OK, "필터 조회 성공", rList)
        );
    }

    @GetMapping(value = "/recent")
    public ResponseEntity<CommonResponse<List<DiaryDTO>>> getRecentDiaryList(@AuthenticationPrincipal Jwt jwt) throws Exception {

        log.info("{}.getRecentDiaryList Start!", this.getClass().getName());

        String userPublicId = CmmUtil.nvl(jwt.getSubject());

        log.info("userPublicId: {}", userPublicId);

        List<DiaryDTO> rList = Optional.ofNullable(
                        diaryService.getRecentDiaryList(DiaryDTO.builder().userPublicId(userPublicId).build()))
                .orElseGet(ArrayList::new);

        log.info("{}.getRecentDiaryList End!", this.getClass().getName());

        return ResponseEntity.ok(
                CommonResponse.of(HttpStatus.OK, "최근 일기 조회 성공", rList)
        );
    }

    @GetMapping(value = "/favorites")
    public ResponseEntity<CommonResponse<List<DiaryDTO>>> getFavoriteDiaryList(@AuthenticationPrincipal Jwt jwt) throws Exception {

        log.info("{}.getFavoriteDiaryList Start!", this.getClass().getName());

        String userPublicId = CmmUtil.nvl(jwt.getSubject());

        log.info("userPublicId: {}", userPublicId);

        List<DiaryDTO> rList = Optional.ofNullable(
                        diaryService.getFavoriteDiaryList(DiaryDTO.builder().userPublicId(userPublicId).build()))
                .orElseGet(ArrayList::new);

        log.info("{}.getFavoriteDiaryList End!", this.getClass().getName());

        return ResponseEntity.ok(
                CommonResponse.of(HttpStatus.OK, "즐겨찾기 조회 성공", rList)
        );
    }

    @GetMapping(value = "/drafts")
    public ResponseEntity<CommonResponse<List<DiaryDTO>>> getDraftDiaryList(@AuthenticationPrincipal Jwt jwt) throws Exception {

        log.info("{}.getDraftDiaryList Start!", this.getClass().getName());

        String userPublicId = CmmUtil.nvl(jwt.getSubject());

        log.info("userPublicId: {}", userPublicId);

        List<DiaryDTO> rList = Optional.ofNullable(
                        diaryService.getDraftDiaryList(DiaryDTO.builder().userPublicId(userPublicId).build()))
                .orElseGet(ArrayList::new);

        log.info("{}.getDraftDiaryList End!", this.getClass().getName());

        return ResponseEntity.ok(
                CommonResponse.of(HttpStatus.OK, "임시 저장 목록 조회 성공", rList)
        );
    }

    @PostMapping(value = "/favorite")
    public ResponseEntity<CommonResponse<String>> diaryFavorite(@RequestBody DiaryDTO dDTO,
                                                                @AuthenticationPrincipal Jwt jwt) throws Exception {

        log.info("{}.diaryFavorite Start!", this.getClass().getName());

        String userPublicId = CmmUtil.nvl(jwt.getSubject());

        log.info("userPublicId: {}, publicId: {}, isFavorite: {}", userPublicId, dDTO.publicId(), dDTO.isFavorite());

        diaryService.updateFavorite(withUser(dDTO, userPublicId));

        log.info("{}.diaryFavorite End!", this.getClass().getName());

        String msg = Boolean.TRUE.equals(dDTO.isFavorite()) ? "즐겨찾기에 추가되었습니다." : "즐겨찾기가 해제되었습니다.";

        return ResponseEntity.ok(
                CommonResponse.of(HttpStatus.OK, msg, dDTO.publicId())
        );
    }

    @PostMapping(value = "/title")
    public ResponseEntity<CommonResponse<String>> diaryUpdateTitle(@RequestBody DiaryDTO dDTO,
                                                                   @AuthenticationPrincipal Jwt jwt) throws Exception {

        log.info("{}.diaryUpdateTitle Start!", this.getClass().getName());

        String userPublicId = CmmUtil.nvl(jwt.getSubject());

        log.info("userPublicId: {}, publicId: {}, titleLength: {}", userPublicId, dDTO.publicId(), CmmUtil.nvl(dDTO.title()).length());

        diaryService.updateTitle(withUser(dDTO, userPublicId));

        log.info("{}.diaryUpdateTitle End!", this.getClass().getName());

        return ResponseEntity.ok(
                CommonResponse.of(HttpStatus.OK, "제목이 변경되었습니다.", dDTO.publicId())
        );
    }

    @PostMapping(value = "/pin")
    public ResponseEntity<CommonResponse<String>> diaryPin(@RequestBody DiaryDTO dDTO,
                                                           @AuthenticationPrincipal Jwt jwt) throws Exception {

        log.info("{}.diaryPin Start!", this.getClass().getName());

        String userPublicId = CmmUtil.nvl(jwt.getSubject());

        log.info("userPublicId: {}, publicId: {}, isPinned: {}", userPublicId, dDTO.publicId(), dDTO.isPinned());

        diaryService.updatePinned(withUser(dDTO, userPublicId));

        log.info("{}.diaryPin End!", this.getClass().getName());

        String msg = Boolean.TRUE.equals(dDTO.isPinned()) ? "상단에 고정되었습니다." : "고정이 해제되었습니다.";

        return ResponseEntity.ok(
                CommonResponse.of(HttpStatus.OK, msg, dDTO.publicId())
        );
    }

    // GCS에 업로드, 일기당 최대 3장까지 허용
    @PostMapping(value = "/{publicId}/images/upload", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<CommonResponse<List<DiaryImageDTO>>> uploadDiaryImages(
            @PathVariable String publicId,
            @RequestParam("images") List<MultipartFile> images,
            @AuthenticationPrincipal Jwt jwt) throws Exception {

        log.info("{}.uploadDiaryImages Start!", this.getClass().getName());

        String userPublicId = CmmUtil.nvl(jwt.getSubject());

        log.info("userPublicId: {}, publicId: {}, imageCount: {}", userPublicId, publicId, images.size());

        List<DiaryImageDTO> rList = Optional.ofNullable(diaryService.uploadDiaryImages(publicId, userPublicId, images))
                .orElseThrow(() -> new RuntimeException("이미지 업로드에 실패하였습니다."));

        log.info("{}.uploadDiaryImages End!", this.getClass().getName());

        return ResponseEntity.ok(
                CommonResponse.of(HttpStatus.OK, "이미지가 업로드되었습니다.", rList)
        );
    }

    @PostMapping(value = "/images/delete")
    public ResponseEntity<CommonResponse<Long>> deleteDiaryImage(@RequestBody DiaryImageDTO dDTO,
                                                                 @AuthenticationPrincipal Jwt jwt) throws Exception {

        log.info("{}.deleteDiaryImage Start!", this.getClass().getName());

        String userPublicId = CmmUtil.nvl(jwt.getSubject());

        log.info("userPublicId: {}, imageNo: {}", userPublicId, dDTO.imageNo());

        DiaryImageDTO pDTO = DiaryImageDTO.builder()
                .imageNo(dDTO.imageNo())
                .userPublicId(userPublicId)
                .build();

        MsgDTO rDTO = Optional.ofNullable(diaryService.deleteDiaryImage(pDTO))
                .orElseThrow(() -> new RuntimeException("이미지 삭제에 실패하였습니다."));

        if (rDTO.result() != 1) {
            throw new IllegalArgumentException(rDTO.msg());
        }

        log.info("{}.deleteDiaryImage End!", this.getClass().getName());

        return ResponseEntity.ok(
                CommonResponse.of(HttpStatus.OK, rDTO.msg(), dDTO.imageNo())
        );
    }

    // 요청 본문에 사용자 값이 들어 있어도 쓰지 않고, 토큰의 사용자만 채워서 서비스에 넘김
    private DiaryDTO withUser(DiaryDTO dDTO, String userPublicId) {
        return DiaryDTO.builder()
                .publicId(dDTO.publicId())
                .userPublicId(userPublicId)
                .title(dDTO.title())
                .content(dDTO.content())
                .diaryDate(dDTO.diaryDate())
                .isFavorite(dDTO.isFavorite())
                .isPinned(dDTO.isPinned())
                .weatherCondition(dDTO.weatherCondition())
                .temperatureC(dDTO.temperatureC())
                .weatherRegion(dDTO.weatherRegion())
                .build();
    }
}
