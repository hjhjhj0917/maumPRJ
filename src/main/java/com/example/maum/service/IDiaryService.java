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

    int diaryInsert(DiaryDTO pDTO) throws Exception;

    // ★ 즐겨찾기 이후 추가/수정
    int draftSave(DiaryDTO pDTO) throws Exception;

    MsgDTO diaryUpdate(DiaryDTO pDTO) throws Exception;

    MsgDTO diaryDelete(DiaryDTO pDTO) throws Exception;

    List<DiaryDTO> getMonthlyDiaryList(DiaryDTO pDTO) throws Exception;

    DiaryDTO getDiaryDetail(DiaryDTO pDTO) throws Exception;

    List<DiaryDTO> searchDiaryList(DiaryDTO pDTO) throws Exception;

    List<DiaryDTO> getDiaryListByColors(String userNo, List<String> colors) throws Exception;

    List<DiaryDTO> getRecentDiaryList(DiaryDTO pDTO) throws Exception;

    // ★ 즐겨찾기 이후 추가/수정
    List<DiaryDTO> getFavoriteDiaryList(DiaryDTO pDTO) throws Exception;

    // ★ 즐겨찾기 이후 추가/수정
    List<EmotionStatDTO> getEmotionStats(DiaryDTO pDTO) throws Exception;

    // ★ 즐겨찾기 이후 추가/수정
    DiaryStatsDTO getDiaryStats(DiaryDTO pDTO) throws Exception;

    // ★ 즐겨찾기 이후 추가/수정
    List<DepressionTrendDTO> getDepressionTrend(DiaryDTO pDTO) throws Exception;

    // ★ 즐겨찾기 이후 추가/수정
    List<TopMusicDTO> getTopRecommendedMusic(DiaryDTO pDTO) throws Exception;

    // ★ 즐겨찾기 이후 추가/수정
    ReportCardDTO getWeeklyReport(DiaryDTO pDTO) throws Exception;

    // ★ 즐겨찾기 이후 추가/수정
    int updateFavorite(DiaryDTO pDTO) throws Exception;

    // ★ 즐겨찾기 이후 추가/수정
    int updateTitle(DiaryDTO pDTO) throws Exception;

    // ★ 즐겨찾기 이후 추가/수정
    int updatePinned(DiaryDTO pDTO) throws Exception;

    // ★ 즐겨찾기 이후 추가/수정
    List<DiaryImageDTO> uploadDiaryImages(Integer diaryNo, String userNo, List<MultipartFile> images) throws Exception;

    // ★ 즐겨찾기 이후 추가/수정
    MsgDTO deleteDiaryImage(DiaryImageDTO pDTO) throws Exception;
}
