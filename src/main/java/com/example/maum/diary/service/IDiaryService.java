package com.example.maum.diary.service;

import com.example.maum.diary.dto.DiaryDTO;

import java.util.List;

public interface IDiaryService {

    /*
    일기 저장
    */
    int diaryInsert(DiaryDTO pDTO) throws Exception;

    /*
    일기 목록 불러오기
    */
    List<DiaryDTO> getMonthlyDiaryList(DiaryDTO pDTO) throws Exception;
}
