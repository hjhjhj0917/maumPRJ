package com.example.maum.service;

import com.example.maum.dto.DiaryDTO;
import com.example.maum.dto.MsgDTO;

import java.util.List;

public interface IDiaryService {

    /* [Diary Management] */

    int diaryInsert(DiaryDTO pDTO) throws Exception;

    MsgDTO diaryUpdate(DiaryDTO pDTO) throws Exception;

    MsgDTO diaryDelete(DiaryDTO pDTO) throws Exception;


    /* [Diary Retrieval] */

    List<DiaryDTO> getMonthlyDiaryList(DiaryDTO pDTO) throws Exception;

    DiaryDTO getDiaryDetail(DiaryDTO pDTO) throws Exception;

    List<DiaryDTO> searchDiaryList(DiaryDTO pDTO) throws Exception;

    List<DiaryDTO> getDiaryListByColors(Integer userNo, List<String> colors) throws Exception;
}
