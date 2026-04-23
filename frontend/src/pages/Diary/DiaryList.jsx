import React from 'react';
import { useDiaryList } from '../../hooks/pages/diary/useDiaryList';
import * as S from '../../style/pages/diary/DiaryList.styles';

const DiaryList = () => {
    const {
        year,
        month,
        daysList,
        handlePrevMonth,
        handleNextMonth,
        handleDayClick
    } = useDiaryList();

    return (
        <S.Container>
            <S.StickyHeader>
                <S.MonthNav>
                    <S.ArrowButton onClick={handlePrevMonth}>
                        <i className="fa-solid fa-chevron-left"></i>
                    </S.ArrowButton>
                    <S.MonthText>{year}년 {month}월</S.MonthText>
                    <S.ArrowButton onClick={handleNextMonth}>
                        <i className="fa-solid fa-chevron-right"></i>
                    </S.ArrowButton>
                </S.MonthNav>

                <S.Controls>
                    <S.SearchBox>
                        <input type="text" placeholder="제목을 입력하세요" />
                        <i className="fa-solid fa-magnifying-glass"></i>
                    </S.SearchBox>
                    <S.FilterButton>
                        필터 <i className="fa-solid fa-filter"></i>
                    </S.FilterButton>
                </S.Controls>
            </S.StickyHeader>

            <S.ListWrapper>
                {daysList.map((item) => (
                    <S.ListItem
                        key={item.day}
                        onClick={() => handleDayClick(item)}
                        // 스타일드 컴포넌트에서 레이아웃 분기를 위해 사용
                        $hasDiary={!!item.diary}
                    >
                        <S.DayText>{item.day}일</S.DayText>

                        {item.diary ? (
                            <S.TitleText>{item.diary.title}</S.TitleText>
                        ) : (
                            <S.AddIcon>
                                <i className="fa-solid fa-plus"></i>
                            </S.AddIcon>
                        )}

                        {/* emotionColor 필드 매핑 확인 */}
                        <S.ColorCircle
                            $color={item.diary ? item.diary.emotionColor : '#e0e0e0'}
                        />
                    </S.ListItem>
                ))}
            </S.ListWrapper>
        </S.Container>
    );
};

export default DiaryList;