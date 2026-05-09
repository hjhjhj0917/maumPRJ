import React from 'react';
import RollerDatePicker from '../../components/common/RollerDatePicker';
import { useDiaryWriteForm } from '../../hooks/diary/useDiaryWriteForm';
import * as S from '../../style/pages/Diary/DiaryWrite.styles';

const DiaryWrite = () => {
    const {
        title, setTitle,
        content, setContent,
        showDatePicker, setShowDatePicker,
        date, setDate,
        formattedDate,
        handleSubmit,
        isLoading
    } = useDiaryWriteForm();

    return (
        <>
            {isLoading && (
                <S.LoadingOverlay>
                    <S.Spinner />
                    <S.LoadingText>
                        마음 AI가 당신의 하루를 분석하고 있어요...
                        <span>깊이 있는 이해를 위해 약간의 시간이 소요됩니다.</span>
                    </S.LoadingText>
                </S.LoadingOverlay>
            )}

            <S.WritePageContainer>
                <S.HeaderSection>
                    <S.TitleDateRow>
                        <S.TitleInput
                            type="text"
                            placeholder="제목을 입력하세요"
                            value={title}
                            onChange={(e) => setTitle(e.target.value)}
                            disabled={isLoading}
                        />
                        <S.DateWrapper>
                            <S.DateSelector onClick={() => !isLoading && setShowDatePicker(true)}>
                                {formattedDate}
                            </S.DateSelector>

                            {showDatePicker && !isLoading && (
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
                </S.HeaderSection>

                <S.EditorWrapper>
                    <textarea
                        placeholder="이곳에 당신의 하루를 자유롭게 기록해 보세요..."
                        value={content}
                        onChange={(e) => setContent(e.target.value)}
                        disabled={isLoading}
                    />
                    <S.FooterActions>
                        <S.SubmitButton
                            onClick={handleSubmit}
                            disabled={isLoading}
                        >
                            {isLoading ? '분석 중...' : '작성 완료'}
                        </S.SubmitButton>
                    </S.FooterActions>
                </S.EditorWrapper>
            </S.WritePageContainer>
        </>
    );
};

export default DiaryWrite;