import React from 'react';
import { useDiaryDetail } from '../../hooks/diary/useDiaryDetail';
import * as S from '../../style/pages/diary/DiaryDetail.styles';

const DiaryDetail = () => {
    const {
        diary, loading, handleGoBack,
        isEditing, editTitle, setEditTitle, editContent, setEditContent,
        handleEditClick, handleCancelEdit, handleSaveClick, handleDeleteClick
    } = useDiaryDetail();

    if (loading && !diary) {
        return (
            <S.LoadingOverlay>
                <S.Spinner />
                <S.LoadingText>
                    일기 정보를 불러오는 중입니다...
                    <span>잠시만 기다려주세요.</span>
                </S.LoadingText>
            </S.LoadingOverlay>
        );
    }

    if (!diary) return null;

    return (
        <>
            {loading && isEditing && (
                <S.LoadingOverlay>
                    <S.Spinner />
                    <S.LoadingText>
                        마음 AI가 일기를 다시 분석하고 있어요...
                        <span>수정된 내용을 바탕으로 새로운 위로를 준비 중입니다.</span>
                    </S.LoadingText>
                </S.LoadingOverlay>
            )}

            {loading && !isEditing && (
                <S.LoadingOverlay>
                    <S.Spinner />
                    <S.LoadingText>
                        요청을 처리 중입니다...
                        <span>잠시만 기다려주세요.</span>
                    </S.LoadingText>
                </S.LoadingOverlay>
            )}

            <S.DetailPageContainer>
                <S.HeaderSection>
                    <S.TopActions>
                        <S.BackButton onClick={handleGoBack} disabled={loading}>
                            <i className="fa-solid fa-arrow-left"></i> 목록으로
                        </S.BackButton>

                        <S.ButtonGroup>
                            {isEditing ? (
                                <>
                                    <S.SaveButton onClick={handleSaveClick} disabled={loading}>
                                        저장
                                    </S.SaveButton>
                                    <S.ActionButton onClick={handleCancelEdit} disabled={loading}>
                                        취소
                                    </S.ActionButton>
                                </>
                            ) : (
                                <>
                                    <S.ActionButton onClick={handleEditClick} disabled={loading}>
                                        수정
                                    </S.ActionButton>
                                    <S.DeleteButton onClick={handleDeleteClick} disabled={loading}>
                                        삭제
                                    </S.DeleteButton>
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
                                disabled={loading}
                            />
                        ) : (
                            <S.TitleText>{diary.title}</S.TitleText>
                        )}
                        <S.DateText>{diary?.createdAt?.substring(0, 10)}</S.DateText>
                    </S.TitleDateRow>
                </S.HeaderSection>

                <S.MainContentWrapper>
                    <S.ContentArea>
                        {isEditing ? (
                            <S.ContentTextarea
                                value={editContent}
                                onChange={(e) => setEditContent(e.target.value)}
                                placeholder="일기 내용을 입력하세요"
                                disabled={loading}
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
        </>
    );
};

export default DiaryDetail;