package com.example.maum.util;

import java.util.Arrays;
import java.util.List;

/*
세부 감정 라벨(KOTE 40여 개)을 8개 색상 그룹으로 매핑함.
DiaryService의 감정 통계 조회, music.py의 감정 그룹 기반 음악 추천 등
여러 곳에서 같은 분류 기준이 필요해서 별도 유틸로 분리함
*/
public class EmotionColorMapper {

    private static final List<String> YELLOW = Arrays.asList("즐거움/신남", "행복", "기쁨", "뿌듯함", "흐뭇함(귀여움/예쁨)", "감동/감탄", "고마움", "환영/호의");
    private static final List<String> MINT = Arrays.asList("안심/신뢰", "존경", "아껴주는", "편안/쾌적");
    private static final List<String> PURPLE = Arrays.asList("공포/무서움", "불안/걱정", "부담/안_내킴", "의심/불신");
    private static final List<String> BLUE = Arrays.asList("놀람", "신기함/관심", "어이없음", "경악", "당황/난처");
    private static final List<String> DARK_BLUE = Arrays.asList("슬픔", "절망", "서러움", "불쌍함/연민", "안타까움/실망", "패배/자기혐오", "힘듦/지침");
    private static final List<String> OLIVE = Arrays.asList("역겨움/징그러움", "증오/혐오", "지긋지긋", "한심함");
    private static final List<String> RED = Arrays.asList("화남/분노", "짜증", "불평/불만");
    private static final List<String> ORANGE = Arrays.asList("기대감", "비장함", "깨달음");

    private static final String DEFAULT_COLOR = "#D9D9D9";

    private EmotionColorMapper() {
    }

    public static String getColor(String emotion) {
        if (YELLOW.contains(emotion)) return "#FFF0A8";
        if (MINT.contains(emotion)) return "#A8E6CF";
        if (PURPLE.contains(emotion)) return "#DDBDF1";
        if (BLUE.contains(emotion)) return "#A2D2FF";
        if (DARK_BLUE.contains(emotion)) return "#8EA4D2";
        if (OLIVE.contains(emotion)) return "#C5D8A4";
        if (RED.contains(emotion)) return "#FFB3B3";
        if (ORANGE.contains(emotion)) return "#FFDFBA";
        return DEFAULT_COLOR;
    }
}
