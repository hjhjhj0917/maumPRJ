import React from 'react';
import * as S from '../../style/pages/Diary/DiaryWrite.styles';

const DiaryWrite = () => {
    return (
        <S.WritePageContainer>
            <S.HeaderSection>
                <h1>안녕하세요,</h1>
                <p>오늘 하루는 어떠셨나요? 마음을 들려주세요.</p>
            </S.HeaderSection>

            <S.EditorWrapper>
                <textarea placeholder="이곳에 당신의 하루를 자유롭게 기록해 보세요..." />
                <S.FooterActions>
                    <S.SubmitButton>작성 완료</S.SubmitButton>
                </S.FooterActions>
            </S.EditorWrapper>
        </S.WritePageContainer>
    );
};

export default DiaryWrite;