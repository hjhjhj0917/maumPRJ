import React from 'react';

const ZodiacItem = ({ data, isSelected, onClick }) => {
    return (
        <div
            className={`zodiac-item ${isSelected ? 'selected' : ''}`}
            onClick={() => onClick(data)}
        >
            <img src={data.img} alt={data.name} />
            <span>{data.name}</span>
        </div>
    );
};

export default ZodiacItem;