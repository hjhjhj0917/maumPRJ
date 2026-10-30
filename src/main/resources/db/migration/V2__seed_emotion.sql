-- 감정 코드 시드 (v1 maumPy emotion.py 의 대분류 9종과 색상 그대로)
-- v1 FastAPI는 main_emotion 으로 한글명을 반환하므로, 저장 시 name_ko -> code 매핑이 필요하다.
INSERT INTO emotion (code, name_ko, color, sort_order) VALUES
    ('JOY',          '기쁨',   '#FFD700', 1),
    ('TRUST',        '신뢰',   '#66CDAA', 2),
    ('FEAR',         '공포',   '#4B0082', 3),
    ('SURPRISE',     '놀람',   '#00BFFF', 4),
    ('SADNESS',      '슬픔',   '#1E3A8A', 5),
    ('DISGUST',      '혐오',   '#556B2F', 6),
    ('ANGER',        '분노',   '#FF3B30', 7),
    ('ANTICIPATION', '기대',   '#FFA500', 8),
    ('NEUTRAL',      '무감정', '#9E9E9E', 9);
