package com.example.maum.service;

import java.math.BigDecimal;
import java.util.List;
import java.util.Map;

// Python 분석 API 호출 같은 느린 외부 호출과 분리해서, 짧은 DB 쓰기만 하나의 트랜잭션으로 묶어 처리함
// ★ 즐겨찾기 이후 추가/수정
public interface IDiaryWriteService {

    void updateDiaryContent(Integer diaryNo, String title, String content);

    void applyAnalysisResult(Integer diaryNo, String summary, String mainEmotion, String emotionColor,
                             Integer depLvl, BigDecimal depScore, Integer symptomYn,
                             List<Map<String, Object>> tracks);
}
