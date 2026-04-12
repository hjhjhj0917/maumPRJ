import React from 'react';
import { useRollerDatePicker } from '../../hooks/common/useRollerDatePicker';
import * as S from '../../style/components/common/RollerDatePicker.styles';

const RollerDatePicker = ({ onConfirm, onClose, initialDate }) => {
    const {
        date, years, months, days,
        yearRef, monthRef, dayRef,
        handleScroll
    } = useRollerDatePicker(initialDate);

    return (
        <>
            <S.PickerOverlay onClick={onClose} />
            <S.PickerContainer>
                <S.PickerBody>
                    <S.Highlight />

                    <S.Column ref={yearRef} onScroll={(e) => handleScroll(e, 'year')}>
                        <S.Pad />
                        {years.map(val => (
                            <S.Item key={val} $active={date.year === val}>{val}</S.Item>
                        ))}
                        <S.Pad />
                    </S.Column>

                    <S.Column ref={monthRef} onScroll={(e) => handleScroll(e, 'month')}>
                        <S.Pad />
                        {months.map(val => (
                            <S.Item key={val} $active={date.month === val}>
                                {val < 10 ? `0${val}` : val}
                            </S.Item>
                        ))}
                        <S.Pad />
                    </S.Column>

                    <S.Column ref={dayRef} onScroll={(e) => handleScroll(e, 'day')}>
                        <S.Pad />
                        {days.map(val => (
                            <S.Item key={val} $active={date.day === val}>
                                {val < 10 ? `0${val}` : val}
                            </S.Item>
                        ))}
                        <S.Pad />
                    </S.Column>

                </S.PickerBody>

                <S.Footer>
                    <S.ConfirmButton type="button" onClick={() => onConfirm(date)}>
                        {`${date.year}년 ${String(date.month).padStart(2, '0')}월 ${String(date.day).padStart(2, '0')}일 확인`}
                    </S.ConfirmButton>
                </S.Footer>
            </S.PickerContainer>
        </>
    );
};

export default RollerDatePicker;