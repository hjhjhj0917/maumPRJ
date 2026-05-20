import React from 'react';
import { useDiaryList, EMOTION_GROUPS } from '../../hooks/diary/useDiaryList';
import * as S from '../../style/pages/diary/DiaryList.styles';

const DiaryList = () => {
    const {
        year, month, daysList,
        handlePrevMonth, handleNextMonth, handleDayClick,
        keyword, setKeyword, searchResults, handleResultClick,
        selectedColors, toggleColorFilter, filterResults
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
                        <input
                            type="text"
                            placeholder="제목을 입력하세요"
                            value={keyword}
                            onChange={(e) => setKeyword(e.target.value)}
                        />
                        <i className="fa-solid fa-magnifying-glass"></i>
                    </S.SearchBox>
                </S.Controls>

                {!keyword && (
                    <S.FilterSection>
                        {Object.entries(EMOTION_GROUPS).map(([name, color]) => (
                            <S.FilterChip
                                key={name}
                                $color={color}
                                $isSelected={selectedColors.includes(color)}
                                onClick={() => toggleColorFilter(color)}
                            >
                                <S.ChipCircle $color={color} $isSelected={selectedColors.includes(color)} />
                                {name}
                            </S.FilterChip>
                        ))}
                    </S.FilterSection>
                )}
            </S.StickyHeader>

            <S.ListWrapper>
                {keyword.trim() ? (
                    searchResults.length > 0 ? (
                        searchResults.map((result) => (
                            <S.ListItem key={result.diaryNo} onClick={() => handleResultClick(result.diaryNo)} $hasDiary={true}>
                                <S.ResultDateText>{result.displayDate}</S.ResultDateText>
                                <S.TitleText>{result.title}</S.TitleText>
                                <S.ColorCircle $color={result.emotionColor || '#e0e0e0'} />
                            </S.ListItem>
                        ))
                    ) : (
                        <S.NoResultText>검색 결과가 없습니다.</S.NoResultText>
                    )
                ) : selectedColors.length > 0 ? (
                    filterResults.length > 0 ? (
                        filterResults.map((result) => (
                            <S.ListItem key={result.diaryNo} onClick={() => handleResultClick(result.diaryNo)} $hasDiary={true}>
                                <S.ResultDateText>{result.displayDate}</S.ResultDateText>
                                <S.TitleText>{result.title}</S.TitleText>
                                <S.ColorCircle $color={result.emotionColor || '#e0e0e0'} />
                            </S.ListItem>
                        ))
                    ) : (
                        <S.NoResultText>해당 감정의 일기가 없습니다.</S.NoResultText>
                    )
                ) : (
                    daysList.map((item) => (
                        <S.ListItem
                            key={item.day}
                            onClick={() => handleDayClick(item)}
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

                            <S.ColorCircle
                                $color={item.diary ? item.diary.emotionColor : '#e0e0e0'}
                            />
                        </S.ListItem>
                    ))
                )}
            </S.ListWrapper>
        </S.Container>
    );
};

export default DiaryList;