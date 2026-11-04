package com.example.maum.service;

import com.example.maum.dto.DiaryDTO;
import com.example.maum.dto.DiaryImageDTO;
import com.example.maum.dto.MsgDTO;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

// 일기 식별은 모두 publicId(UUID 문자열), 사용자 식별은 JWT subject 인 userPublicId 로 한다.
// 통계·추이·인기곡·주간 리포트는 5단계에서 새 구조로 다시 만든다.
public interface IDiaryService {

    // 임시 저장. publicId 가 있으면 그 임시 저장본을 덮어쓰고 없으면 새로 만든다. 저장된 publicId 를 돌려줌
    String draftSave(DiaryDTO pDTO) throws Exception;

    // 발행. publicId 가 있으면 임시 저장본을 발행하고 없으면 바로 새로 발행한다. 분석은 뒤에서 시작됨
    String diaryInsert(DiaryDTO pDTO) throws Exception;

    // 발행된 일기의 제목·본문 수정. 제목이나 본문이 바뀐 경우에만 재분석한다
    MsgDTO diaryUpdate(DiaryDTO pDTO) throws Exception;

    MsgDTO diaryDelete(DiaryDTO pDTO) throws Exception;

    List<DiaryDTO> getMonthlyDiaryList(DiaryDTO pDTO) throws Exception;

    DiaryDTO getDiaryDetail(DiaryDTO pDTO) throws Exception;

    // 제목 검색 (pDTO.title 이 검색어)
    List<DiaryDTO> searchDiaryList(DiaryDTO pDTO) throws Exception;

    // 대분류 감정 코드로 필터
    List<DiaryDTO> getDiaryListByGroups(String userPublicId, List<String> groupCodes) throws Exception;

    List<DiaryDTO> getRecentDiaryList(DiaryDTO pDTO) throws Exception;

    List<DiaryDTO> getFavoriteDiaryList(DiaryDTO pDTO) throws Exception;

    List<DiaryDTO> getDraftDiaryList(DiaryDTO pDTO) throws Exception;

    int updateFavorite(DiaryDTO pDTO) throws Exception;

    int updateTitle(DiaryDTO pDTO) throws Exception;

    int updatePinned(DiaryDTO pDTO) throws Exception;

    List<DiaryImageDTO> uploadDiaryImages(String diaryPublicId, String userPublicId, List<MultipartFile> images) throws Exception;

    MsgDTO deleteDiaryImage(DiaryImageDTO pDTO) throws Exception;
}
