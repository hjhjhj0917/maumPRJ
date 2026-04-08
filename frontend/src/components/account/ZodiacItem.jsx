import React from 'react';
import * as S from '../../style/components/account/ZodiacItem.styles.js';

const ZodiacItem = ({ data, isSelected, onClick }) => {
    return (
        <S.ItemWrapper onClick={() => onClick(data)}>
            <S.ItemImage
                src={data.img}
                alt={data.name}
                $isSelected={isSelected}
            />
            <S.ItemName>{data.name}</S.ItemName>
        </S.ItemWrapper>
    );
};

export default ZodiacItem;