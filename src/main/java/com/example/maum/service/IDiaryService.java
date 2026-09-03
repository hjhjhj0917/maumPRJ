package com.example.maum.service;

import com.example.maum.dto.DiaryDTO;
import com.example.maum.dto.EmotionStatDTO;
import com.example.maum.dto.MsgDTO;

import java.util.List;

public interface IDiaryService {

    /*
    일기 등록
    */
    int diaryInsert(DiaryDTO pDTO) throws Exception;

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
    즐겨찾기
    */
    int updateFavorite(DiaryDTO pDTO) throws Exception;
}
