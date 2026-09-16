package com.example.maum.service;

import com.example.maum.dto.DepressionTrendDTO;
import com.example.maum.dto.DiaryDTO;
import com.example.maum.dto.DiaryImageDTO;
import com.example.maum.dto.DiaryStatsDTO;
import com.example.maum.dto.EmotionStatDTO;
import com.example.maum.dto.MsgDTO;
import com.example.maum.dto.ReportCardDTO;
import com.example.maum.dto.TopMusicDTO;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

public interface IDiaryService {

    /*
    일기 등록
    */
    int diaryInsert(DiaryDTO pDTO) throws Exception;

    /*
    일기 임시저장 (AI 분석/음악 추천 없이 제목/내용만 저장)
    */
    int draftSave(DiaryDTO pDTO) throws Exception;

    /*
    일기 수정
    */
    MsgDTO diaryUpdate(DiaryDTO pDTO) throws Exception;

    /*
    일기 삭제
    */
    MsgDTO diaryDelete(DiaryDTO pDTO) throws Exception;

    /*
    월별 일기 목록 조회
    */
    List<DiaryDTO> getMonthlyDiaryList(DiaryDTO pDTO) throws Exception;

    /*
    일기 상세 보기
    */
    DiaryDTO getDiaryDetail(DiaryDTO pDTO) throws Exception;

    /*
    일기 제목 검색
    */
    List<DiaryDTO> searchDiaryList(DiaryDTO pDTO) throws Exception;

    /*
    감정 필터 검색
    */
    List<DiaryDTO> getDiaryListByColors(String userNo, List<String> colors) throws Exception;

    /*
    최근 일기 목록 조회
    */
    List<DiaryDTO> getRecentDiaryList(DiaryDTO pDTO) throws Exception;

    /*
    즐겨찾기 일기 목록 조회 (월 구분 없이 전체)
    */
    List<DiaryDTO> getFavoriteDiaryList(DiaryDTO pDTO) throws Exception;

    /*
    마이페이지 감정 통계 조회
    */
    List<EmotionStatDTO> getEmotionStats(String userNoStr) throws Exception;

    /*
    마이페이지 - 총 작성 수 / 연속 작성일 통계 조회
    */
    DiaryStatsDTO getDiaryStats(String userNo) throws Exception;

    /*
    마이페이지 - 최근 6개월 월별 우울 지수 추이 조회
    */
    List<DepressionTrendDTO> getDepressionTrend(String userNo) throws Exception;

    /*
    마이페이지 - 가장 많이 추천된 음악 Top N 조회
    */
    List<TopMusicDTO> getTopRecommendedMusic(String userNo) throws Exception;

    /*
    마이페이지 - 최근 일주일 일기를 바탕으로 한 주간 리포트 카드(Gemini 코멘트) 조회
    */
    ReportCardDTO getWeeklyReport(String userNo) throws Exception;

    /*
    즐겨찾기
    */
    int updateFavorite(DiaryDTO pDTO) throws Exception;

    /*
    제목만 수정 (사이드바 인라인 이름변경용)
    */
    int updateTitle(DiaryDTO pDTO) throws Exception;

    /*
    사이드바 상단 고정
    */
    int updatePinned(DiaryDTO pDTO) throws Exception;

    /*
    일기 이미지 업로드 (GCS)
    */
    List<DiaryImageDTO> uploadDiaryImages(Integer diaryNo, String userNo, List<MultipartFile> images) throws Exception;

    /*
    일기 이미지 삭제
    */
    MsgDTO deleteDiaryImage(Integer imageNo, String userNo) throws Exception;
}
