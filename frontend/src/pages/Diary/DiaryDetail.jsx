import React from 'react';
import { useDiaryDetail } from '../../hooks/diary/useDiaryDetail';
import * as S from '../../style/pages/diary/DiaryDetail.styles';

const DiaryDetail = () => {
    const {
        diary, loading, handleGoBack,
        isEditing, editTitle, setEditTitle, editContent, setEditContent,
        handleEditClick, handleCancelEdit, handleSaveClick, handleDeleteClick
    } = useDiaryDetail();

    if (loading) {
        return <S.LoadingText>AI가 감정을 분석하며 일기를 처리하는 중입니다...</S.LoadingText>;
    }

    if (!diary) return null;

    return (
        <S.DetailPageContainer>
            <S.HeaderSection>
                <S.TopActions>
                    <S.BackButton onClick={handleGoBack}>
                        <i className="fa-solid fa-arrow-left"></i> 목록으로
                    </S.BackButton>

                    <S.ButtonGroup>
                        {isEditing ? (
                            <>
                                <S.SaveButton onClick={handleSaveClick}>저장</S.SaveButton>
                                <S.ActionButton onClick={handleCancelEdit}>취소</S.ActionButton>
                            </>
                        ) : (
                            <>
                                <S.ActionButton onClick={handleEditClick}>수정</S.ActionButton>
                                <S.DeleteButton onClick={handleDeleteClick}>삭제</S.DeleteButton>
                            </>
                        )}
                    </S.ButtonGroup>
                </S.TopActions>

                <S.TitleDateRow>
                    {isEditing ? (
                        <S.TitleInput
                            value={editTitle}
                            onChange={(e) => setEditTitle(e.target.value)}
                            placeholder="제목을 입력하세요"
                        />
                    ) : (
                        <S.TitleText>{diary.title}</S.TitleText>
                    )}
                    <S.DateText>{diary.createdAt.substring(0, 10)}</S.DateText>
                </S.TitleDateRow>
            </S.HeaderSection>

            <S.MainContentWrapper>
                <S.ContentArea>
                    {isEditing ? (
                        <S.ContentTextarea
                            value={editContent}
                            onChange={(e) => setEditContent(e.target.value)}
                            placeholder="일기 내용을 입력하세요"
                        />
                    ) : (
                        <S.ContentText>{diary.content}</S.ContentText>
                    )}
                </S.ContentArea>

                {diary.summary && !isEditing && (
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