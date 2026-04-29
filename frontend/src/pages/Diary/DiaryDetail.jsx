import React from 'react';
import { useDiaryDetail } from '../../hooks/pages/diary/useDiaryDetail';
import * as S from '../../style/pages/diary/DiaryDetail.styles';

const DiaryDetail = () => {
    const { diary, loading, handleGoBack } = useDiaryDetail();

    if (loading) {
        return <S.LoadingText>일기를 불러오는 중입니다...</S.LoadingText>;
    }

    if (!diary) return null;

    return (
        <S.DetailPageContainer>
            <S.HeaderSection>
                <S.TopActions>
                    <S.BackButton onClick={handleGoBack}>
                        <i className="fa-solid fa-arrow-left"></i> 목록으로
                    </S.BackButton>
                    {/* 필요하다면 여기에 수정/삭제 버튼 추가 가능 */}
                </S.TopActions>

                <S.TitleDateRow>
                    <S.TitleText>{diary.title}</S.TitleText>
                    <S.DateText>{diary.createdAt.substring(0, 10)}</S.DateText>
                </S.TitleDateRow>
            </S.HeaderSection>

            <S.MainContentWrapper>
                {/* 왼쪽: 일기 본문 영역 */}
                <S.ContentArea>
                    <S.ContentText>{diary.content}</S.ContentText>
                </S.ContentArea>

                {/* 오른쪽: AI 분석 결과 사이드바 */}
                {diary.summary && (
                    <S.SidebarArea>
                        <S.SidebarTitle>
                            <i className="fa-solid fa-wand-magic-sparkles"></i> AI 감정 분석
                        </S.SidebarTitle>

                        <S.AnalysisCard>
                            <S.AnalysisLabel>오늘의 감정 색상</S.AnalysisLabel>
                            <S.EmotionRow>
                                <S.ColorCircle $color={diary.emotionColor || '#e0e0e0'} />
                                <S.EmotionText>{diary.mainEmotion}</S.EmotionText>
                            </S.EmotionRow>
                        </S.AnalysisCard>

                        <S.AnalysisCard>
                            <S.AnalysisLabel>한 줄 요약</S.AnalysisLabel>
                            <S.SummaryText>"{diary.summary}"</S.SummaryText>
                        </S.AnalysisCard>

                        {/* 우울증 지수 등이 필요하다면 추가 */}
                        {diary.depLvl != null && (
                            <S.AnalysisCard>
                                <S.AnalysisLabel>마음 상태 지수</S.AnalysisLabel>
                                <S.LevelText>Level {diary.depLvl}</S.LevelText>
                            </S.AnalysisCard>
                        )}
                    </S.SidebarArea>
                )}
            </S.MainContentWrapper>
        </S.DetailPageContainer>
    );
};

export default DiaryDetail;