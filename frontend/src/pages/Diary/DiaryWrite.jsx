import React from 'react';
import RollerDatePicker from '../../components/common/RollerDatePicker';
import { useDiaryWriteForm } from '../../hooks/pages/diary/useDiaryWriteForm';
import * as S from '../../style/pages/Diary/DiaryWrite.styles';

const DiaryWrite = () => {
    const {
        title, setTitle,
        content, setContent,
        showDatePicker, setShowDatePicker,
        date, setDate,
        formattedDate,
        handleSubmit
    } = useDiaryWriteForm();

    return (
        <S.WritePageContainer>
            <S.HeaderSection>
                <S.TitleDateRow>
                    <S.TitleInput
                        type="text"
                        placeholder="제목을 입력하세요"
                        value={title}
                        onChange={(e) => setTitle(e.target.value)}
                    />
                    <S.DateWrapper>
                        {/* 클릭 시 날짜 선택기가 열립니다. */}
                        <S.DateSelector onClick={() => setShowDatePicker(true)}>
                            {formattedDate}
                        </S.DateSelector>

                        {showDatePicker && (
                            <>
                                <S.PickerOverlay onClick={() => setShowDatePicker(false)} />
                                <S.PickerContainer>
                                    <RollerDatePicker
                                        initialDate={date}
                                        onClose={() => setShowDatePicker(false)}
                                        onConfirm={(selectedDate) => {
                                            setDate(selectedDate);
                                            setShowDatePicker(false);
                                        }}
                                    />
                                </S.PickerContainer>
                            </>
                        )}
                    </S.DateWrapper>
                </S.TitleDateRow>
                <p>오늘 하루는 어떠셨나요? 마음을 들려주세요.</p>
            </S.HeaderSection>

            <S.EditorWrapper>
                <textarea
                    placeholder="이곳에 당신의 하루를 자유롭게 기록해 보세요..."
                    value={content}
                    onChange={(e) => setContent(e.target.value)}
                />
                <S.FooterActions>
                    <S.SubmitButton onClick={handleSubmit}>작성 완료</S.SubmitButton>
                </S.FooterActions>
            </S.EditorWrapper>
        </S.WritePageContainer>
    );
};

export default DiaryWrite;