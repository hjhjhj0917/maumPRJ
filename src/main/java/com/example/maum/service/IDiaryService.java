package com.example.maum.service;

import com.example.maum.dto.DiaryDTO;

import java.util.List;

public interface IDiaryService {

    /*
    일기 저장
    */
    int diaryInsert(DiaryDTO pDTO) throws Exception;

    /*
    월별 일기 목록 불러오기
    */
    List<DiaryDTO> getMonthlyDiaryList(DiaryDTO pDTO) throws Exception;

    /*
    일기 상세 보기
    */
    DiaryDTO getDiaryDetail(DiaryDTO pDTO) throws Exception;
}
