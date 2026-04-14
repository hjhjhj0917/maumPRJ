package com.example.maum.diary.service;

import com.example.maum.diary.dto.DiaryDTO;

public interface IDiaryService {

    /*
    일기 저장
    */
    int diaryInsert(DiaryDTO pDTO) throws Exception;
}
