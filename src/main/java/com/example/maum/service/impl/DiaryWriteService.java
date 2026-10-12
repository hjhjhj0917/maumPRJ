package com.example.maum.service.impl;

import com.example.maum.repository.DiaryMusicRepository;
import com.example.maum.repository.DiaryRepository;
import com.example.maum.repository.entity.DiaryMusicEntity;
import com.example.maum.service.IDiaryWriteService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.List;
import java.util.Map;

// DiaryService가 Python 분석 API(수 초 걸리는 외부 호출)를 트랜잭션 밖에서 실행한 뒤, 그 결과를 DB에 쓰는 부분만
// 이 서비스로 분리함. 같은 클래스 안에서 this.로 호출하면 Spring AOP 프록시를 거치지 않아 @Transactional이
// 무시되는 문제(자기 호출)가 있어서, 별도 빈으로 두고 주입받아 호출하면 프록시를 거쳐 트랜잭션이 적용됨
// ★ 즐겨찾기 이후 추가/수정
@Service
@Slf4j
@RequiredArgsConstructor
public class DiaryWriteService implements IDiaryWriteService {

    private final DiaryRepository diaryRepository;
    private final DiaryMusicRepository diaryMusicRepository;

    // updateDiaryDirectly는 @Modifying 커스텀 쿼리라 활성 트랜잭션이 반드시 필요함
    // ★ 즐겨찾기 이후 추가/수정
    @Override
    @Transactional
    public void updateDiaryContent(Integer diaryNo, String title, String content) {
        diaryRepository.updateDiaryDirectly(Long.valueOf(diaryNo), title, content);
    }

    // 분석 결과(감정/우울도) DB 반영과 음악 추천 저장을 하나의 트랜잭션으로 묶음 — 감정 분석
    // 결과만 반영되고 음악 추천 저장은 실패하는(또는 그 반대) 반쪽짜리 상태를 막기 위함.
    // updateAnalysisResultDirectly는 @Modifying 커스텀 쿼리라 활성 트랜잭션이 반드시 필요함
    // ★ 즐겨찾기 이후 추가/수정
    @Override
    @Transactional
    public void applyAnalysisResult(Integer diaryNo, String summary, String mainEmotion, String emotionColor,
                                    Integer depLvl, BigDecimal depScore, Integer symptomYn,
                                    List<Map<String, Object>> tracks) {

        diaryRepository.updateAnalysisResultDirectly(
                Long.valueOf(diaryNo), summary, mainEmotion, emotionColor, depLvl, depScore, symptomYn
        );

        saveMusicTracks(diaryNo, tracks);
    }

    // ★ 즐겨찾기 이후 추가/수정
    // 재분석(수정)일 경우 기존 추천곡은 지우고 새로 저장함 (일기 내용이 바뀌면 추천도 바뀌어야 하므로)
    private void saveMusicTracks(Integer diaryNo, List<Map<String, Object>> tracks) {

        diaryMusicRepository.deleteByDiaryNo(diaryNo);

        if (tracks == null || tracks.isEmpty()) {
            log.warn("음악 추천 결과 없음 (diaryNo: {})", diaryNo);
            return;
        }

        int order = 0;
        for (Map<String, Object> track : tracks) {
            DiaryMusicEntity musicEntity = DiaryMusicEntity.builder()
                    .diaryNo(diaryNo)
                    .trackId((String) track.get("trackId"))
                    .trackName((String) track.get("trackName"))
                    .artistName((String) track.get("artistName"))
                    .albumImageUrl((String) track.get("albumImageUrl"))
                    .spotifyUrl((String) track.get("spotifyUrl"))
                    .trackOrder(order++)
                    .build();

            diaryMusicRepository.save(musicEntity);
        }

        log.info("음악 추천 저장 완료 (diaryNo: {}, {}곡)", diaryNo, tracks.size());
    }
}
