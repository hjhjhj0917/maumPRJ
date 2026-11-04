package com.example.maum.diary;

import com.example.maum.dto.EmotionScoreDTO;
import com.example.maum.repository.EmotionGroupRepository;
import com.example.maum.repository.EmotionRepository;
import com.example.maum.repository.entity.EmotionEntity;
import com.example.maum.repository.entity.EmotionGroupEntity;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.function.Function;
import java.util.stream.Collectors;

// 감정 44종·대분류 9종은 시드 데이터라 실행 중 바뀌지 않는다. 처음 쓸 때 한 번 읽어 메모리에 두고,
// 모델 라벨(name_ko) → 점수 테이블 키(id) 변환과 id → 이름·색상 변환에 쓴다.
@Slf4j
@Component
@RequiredArgsConstructor
public class EmotionCatalog {

    private static final String NEUTRAL_GROUP = "NEUTRAL";
    private static final String NONE_EMOTION = "없음";

    // v1 규칙 그대로: 1위가 '없음'이거나 점수가 이보다 낮으면 뚜렷한 감정이 없는 것으로 보고 무감정으로 분류
    static final float MIN_MAIN_SCORE = 0.15f;

    private final EmotionRepository emotionRepository;
    private final EmotionGroupRepository emotionGroupRepository;

    private volatile Snapshot snapshot;

    public record RankedEmotion(EmotionEntity emotion, float score) {
    }

    private record Snapshot(Map<String, EmotionEntity> byName,
                            Map<Short, EmotionEntity> byId,
                            Map<String, EmotionGroupEntity> groups) {
    }

    private Snapshot snapshot() {
        Snapshot loaded = snapshot;

        if (loaded == null) {
            synchronized (this) {
                loaded = snapshot;

                if (loaded == null) {
                    var emotions = emotionRepository.findAll();

                    loaded = new Snapshot(
                            emotions.stream().collect(Collectors.toUnmodifiableMap(EmotionEntity::getNameKo, Function.identity())),
                            emotions.stream().collect(Collectors.toUnmodifiableMap(EmotionEntity::getId, Function.identity())),
                            emotionGroupRepository.findAll().stream()
                                    .collect(Collectors.toUnmodifiableMap(EmotionGroupEntity::getCode, Function.identity())));
                    snapshot = loaded;

                    log.info("감정 카탈로그 로드 완료: 세부 감정 {}개, 대분류 {}개", loaded.byId().size(), loaded.groups().size());
                }
            }
        }

        return loaded;
    }

    // 모델이 돌려준 라벨로 감정을 찾음. 없으면 비어 있음(알 수 없는 라벨은 호출하는 쪽에서 건너뜀)
    public Optional<EmotionEntity> findByName(String nameKo) {
        return Optional.ofNullable(snapshot().byName().get(nameKo));
    }

    public Optional<EmotionEntity> findById(Short id) {
        return Optional.ofNullable(snapshot().byId().get(id));
    }

    // 점수 계산 결과에서 이름을 아는 감정만 골라 점수 높은 순(같으면 감정 번호 순)으로 정렬. 모르는 라벨은 건너뜀
    public List<RankedEmotion> rank(Map<String, Float> scoresByName) {
        List<RankedEmotion> ranked = new ArrayList<>();

        for (Map.Entry<String, Float> entry : scoresByName.entrySet()) {
            Optional<EmotionEntity> emotion = findByName(entry.getKey());

            if (emotion.isEmpty() || entry.getValue() == null) {
                log.warn("알 수 없는 감정 라벨이거나 점수가 없어 건너뜀: {}", entry.getKey());
                continue;
            }

            ranked.add(new RankedEmotion(emotion.get(), entry.getValue()));
        }

        ranked.sort(Comparator.comparingDouble((RankedEmotion r) -> -r.score())
                .thenComparing(r -> r.emotion().getId()));

        return ranked;
    }

    public int size() {
        return snapshot().byId().size();
    }

    // 대분류 코드의 색상. 분석 전이라 코드가 없으면 null
    public String colorOf(String groupCode) {
        return Optional.ofNullable(groupCode)
                .map(code -> snapshot().groups().get(code))
                .map(EmotionGroupEntity::getColor)
                .orElse(null);
    }

    // 점수 1위 감정으로 필터·달력 색상용 대분류를 정함
    public String resolveMainGroup(EmotionEntity top, float topScore) {
        if (NONE_EMOTION.equals(top.getNameKo()) || topScore < MIN_MAIN_SCORE) {
            return NEUTRAL_GROUP;
        }

        return top.getGroupCode();
    }

    public Optional<EmotionScoreDTO> toScoreDTO(Short emotionId, float score) {
        return findById(emotionId).map(emotion -> EmotionScoreDTO.builder()
                .name(emotion.getNameKo())
                .groupCode(emotion.getGroupCode())
                .color(colorOf(emotion.getGroupCode()))
                .score(score)
                .build());
    }
}
