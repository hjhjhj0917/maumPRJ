-- 감정 코드 시드: 대분류 9종(색상은 v1 maumPy emotion.py 와 동일) + KOTE 세부 감정 44종
-- 세부 감정의 name_ko 는 감성모델(KOTE) 출력 라벨 그대로이며, Spring 이 name_ko -> id 로 변환해 점수를 저장한다.
INSERT INTO emotion_group (code, name_ko, color, sort_order) VALUES
    ('JOY',          '기쁨',   '#FFD700', 1),
    ('TRUST',        '신뢰',   '#66CDAA', 2),
    ('FEAR',         '공포',   '#4B0082', 3),
    ('SURPRISE',     '놀람',   '#00BFFF', 4),
    ('SADNESS',      '슬픔',   '#1E3A8A', 5),
    ('DISGUST',      '혐오',   '#556B2F', 6),
    ('ANGER',        '분노',   '#FF3B30', 7),
    ('ANTICIPATION', '기대',   '#FFA500', 8),
    ('NEUTRAL',      '무감정', '#9E9E9E', 9);

-- 그룹 배정은 v1 EMOTION_GROUPS 를 따르되 두 가지를 정리했다.
--  1) '의심/불신'은 v1에서 공포·무감정 두 그룹에 중복 -> 먼저 매칭되던 FEAR 로 확정(v1 동작과 동일)
--  2) '부끄러움','죄책감'은 v1 그룹에 없어 회색(무감정) 처리되던 라벨 -> SADNESS 로 배정(임의 판단, 검토 필요)
-- id 는 점수 테이블이 참조하는 고정값(= sort_order)이다. 번호를 바꾸거나 재사용하지 않는다(감정 추가는 45번부터).
INSERT INTO emotion (id, code, name_ko, group_code, sort_order) VALUES
    (1, 'EXCITED', '즐거움/신남', 'JOY', 1),
    (2, 'HAPPINESS', '행복', 'JOY', 2),
    (3, 'GLADNESS', '기쁨', 'JOY', 3),
    (4, 'PROUD', '뿌듯함', 'JOY', 4),
    (5, 'HEARTWARMED', '흐뭇함(귀여움/예쁨)', 'JOY', 5),
    (6, 'MOVED', '감동/감탄', 'JOY', 6),
    (7, 'GRATEFUL', '고마움', 'JOY', 7),
    (8, 'WELCOMING', '환영/호의', 'JOY', 8),

    (9, 'TRUSTING', '안심/신뢰', 'TRUST', 9),
    (10, 'RESPECT', '존경', 'TRUST', 10),
    (11, 'CARING', '아껴주는', 'TRUST', 11),
    (12, 'COMFORTABLE', '편안/쾌적', 'TRUST', 12),

    (13, 'SCARED', '공포/무서움', 'FEAR', 13),
    (14, 'ANXIOUS', '불안/걱정', 'FEAR', 14),
    (15, 'RELUCTANT', '부담/안_내킴', 'FEAR', 15),
    (16, 'DISTRUST', '의심/불신', 'FEAR', 16),

    (17, 'SURPRISED', '놀람', 'SURPRISE', 17),
    (18, 'CURIOUS', '신기함/관심', 'SURPRISE', 18),
    (19, 'ABSURD', '어이없음', 'SURPRISE', 19),
    (20, 'SHOCKED', '경악', 'SURPRISE', 20),
    (21, 'FLUSTERED', '당황/난처', 'SURPRISE', 21),

    (22, 'SAD', '슬픔', 'SADNESS', 22),
    (23, 'DESPAIR', '절망', 'SADNESS', 23),
    (24, 'GRIEVED', '서러움', 'SADNESS', 24),
    (25, 'PITY', '불쌍함/연민', 'SADNESS', 25),
    (26, 'DISAPPOINTED', '안타까움/실망', 'SADNESS', 26),
    (27, 'SELF_LOATHING', '패배/자기혐오', 'SADNESS', 27),
    (28, 'EXHAUSTED', '힘듦/지침', 'SADNESS', 28),
    (29, 'ASHAMED', '부끄러움', 'SADNESS', 29),
    (30, 'GUILTY', '죄책감', 'SADNESS', 30),

    (31, 'REPULSED', '역겨움/징그러움', 'DISGUST', 31),
    (32, 'HATRED', '증오/혐오', 'DISGUST', 32),
    (33, 'FED_UP', '지긋지긋', 'DISGUST', 33),
    (34, 'PATHETIC', '한심함', 'DISGUST', 34),

    (35, 'ANGRY', '화남/분노', 'ANGER', 35),
    (36, 'IRRITATED', '짜증', 'ANGER', 36),
    (37, 'COMPLAINING', '불평/불만', 'ANGER', 37),

    (38, 'ANTICIPATING', '기대감', 'ANTICIPATION', 38),
    (39, 'RESOLUTE', '비장함', 'ANTICIPATION', 39),
    (40, 'ENLIGHTENED', '깨달음', 'ANTICIPATION', 40),

    (41, 'NONE', '없음', 'NEUTRAL', 41),
    (42, 'BOTHERED', '귀찮음', 'NEUTRAL', 42),
    (43, 'BORED', '재미없음', 'NEUTRAL', 43),
    (44, 'SMUG', '우쭐댐/무시함', 'NEUTRAL', 44);
