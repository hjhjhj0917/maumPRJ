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

    int draftSave(DiaryDTO pDTO) throws Exception;

    MsgDTO diaryUpdate(DiaryDTO pDTO) throws Exception;

    MsgDTO diaryDelete(DiaryDTO pDTO) throws Exception;

    List<DiaryDTO> getMonthlyDiaryList(DiaryDTO pDTO) throws Exception;

    DiaryDTO getDiaryDetail(DiaryDTO pDTO) throws Exception;

    List<DiaryDTO> searchDiaryList(DiaryDTO pDTO) throws Exception;

    List<DiaryDTO> getDiaryListByColors(String userNo, List<String> colors) throws Exception;

    List<DiaryDTO> getRecentDiaryList(DiaryDTO pDTO) throws Exception;

    List<DiaryDTO> getFavoriteDiaryList(DiaryDTO pDTO) throws Exception;

    List<EmotionStatDTO> getEmotionStats(DiaryDTO pDTO) throws Exception;

    DiaryStatsDTO getDiaryStats(DiaryDTO pDTO) throws Exception;

    List<DepressionTrendDTO> getDepressionTrend(DiaryDTO pDTO) throws Exception;

    List<TopMusicDTO> getTopRecommendedMusic(DiaryDTO pDTO) throws Exception;

    ReportCardDTO getWeeklyReport(DiaryDTO pDTO) throws Exception;

    int updateFavorite(DiaryDTO pDTO) throws Exception;

    int updateTitle(DiaryDTO pDTO) throws Exception;

    int updatePinned(DiaryDTO pDTO) throws Exception;

    List<DiaryImageDTO> uploadDiaryImages(Integer diaryNo, String userNo, List<MultipartFile> images) throws Exception;

    MsgDTO deleteDiaryImage(DiaryImageDTO pDTO) throws Exception;
}
