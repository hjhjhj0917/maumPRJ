package com.example.maum.util;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;

import static org.junit.jupiter.api.Assertions.assertEquals;

class EmotionColorMapperTest {

    @ParameterizedTest
    @CsvSource({
            "즐거움/신남, #FFF0A8",
            "행복, #FFF0A8",
            "안심/신뢰, #A8E6CF",
            "편안/쾌적, #A8E6CF",
            "불안/걱정, #DDBDF1",
            "놀람, #A2D2FF",
            "슬픔, #8EA4D2",
            "힘듦/지침, #8EA4D2",
            "역겨움/징그러움, #C5D8A4",
            "화남/분노, #FFB3B3",
            "기대감, #FFDFBA"
    })
    void 그룹에_속한_감정은_해당_그룹의_색상을_반환한다(String emotion, String expectedColor) {
        assertEquals(expectedColor, EmotionColorMapper.getColor(emotion));
    }

    @Test
    void 어느_그룹에도_속하지_않는_감정은_기본_색상을_반환한다() {
        assertEquals("#D9D9D9", EmotionColorMapper.getColor("존재하지_않는_감정"));
    }

    @Test
    void null이_들어와도_예외_없이_기본_색상을_반환한다() {
        assertEquals("#D9D9D9", EmotionColorMapper.getColor(null));
    }
}
