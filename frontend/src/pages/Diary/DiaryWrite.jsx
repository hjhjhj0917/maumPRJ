import React, { useState } from 'react';
import RollerDatePicker from '../../components/common/RollerDatePicker';
import * as S from '../../style/pages/Diary/DiaryWrite.styles';

const DiaryWrite = () => {
    const [title, setTitle] = useState('');
    const [content, setContent] = useState('');
    const [showDatePicker, setShowDatePicker] = useState(false);

    const today = new Date();
    const [date, setDate] = useState({
        year: today.getFullYear(),
        month: today.getMonth() + 1,
        day: today.getDate()
    });

    const formattedDate = `${date.year}년 ${String(date.month).padStart(2, '0')}월 ${String(date.day).padStart(2, '0')}일`;

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
                    <S.SubmitButton>작성 완료</S.SubmitButton>
                </S.FooterActions>
            </S.EditorWrapper>
        </S.WritePageContainer>
    );
};

export default DiaryWrite;