import React, { useState, useEffect, useRef } from 'react';

const RollerDatePicker = ({ onConfirm, onClose, initialDate }) => {
    const [date, setDate] = useState(initialDate || { year: 2000, month: 1, day: 1 });

    const years = Array.from({ length: new Date().getFullYear() - 1900 + 1 }, (_, i) => 1900 + i);
    const months = Array.from({ length: 12 }, (_, i) => i + 1);
    const days = Array.from({ length: 31 }, (_, i) => i + 1);

    const yearRef = useRef(null);
    const monthRef = useRef(null);
    const dayRef = useRef(null);

    useEffect(() => {
        if (yearRef.current) yearRef.current.scrollTop = (date.year - 1900) * 30;
        if (monthRef.current) monthRef.current.scrollTop = (date.month - 1) * 30;
        if (dayRef.current) dayRef.current.scrollTop = (date.day - 1) * 30;
    }, []);

    const handleScroll = (e, type) => {
        const index = Math.round(e.target.scrollTop / 30);
        const source = type === 'year' ? years : type === 'month' ? months : days;
        setDate(prev => ({ ...prev, [type]: source[index] }));
    };

    return (
        <>
            <div className="picker-overlay" onClick={onClose}></div>
            <div className="roller-picker-container">
                <div className="roller-picker-body">
                    <div className="roller-highlight"></div>
                    <div className="roller-col" ref={yearRef} onScroll={(e) => handleScroll(e, 'year')}>
                        <div className="roller-pad"></div>
                        {years.map(val => (
                            <div key={val} className={`roller-item ${date.year === val ? 'active' : ''}`}>{val}</div>
                        ))}
                        <div className="roller-pad"></div>
                    </div>
                    <div className="roller-col" ref={monthRef} onScroll={(e) => handleScroll(e, 'month')}>
                        <div className="roller-pad"></div>
                        {months.map(val => (
                            <div key={val} className={`roller-item ${date.month === val ? 'active' : ''}`}>{val < 10 ? `0${val}` : val}</div>
                        ))}
                        <div className="roller-pad"></div>
                    </div>
                    <div className="roller-col" ref={dayRef} onScroll={(e) => handleScroll(e, 'day')}>
                        <div className="roller-pad"></div>
                        {days.map(val => (
                            <div key={val} className={`roller-item ${date.day === val ? 'active' : ''}`}>{val < 10 ? `0${val}` : val}</div>
                        ))}
                        <div className="roller-pad"></div>
                    </div>
                </div>
                <div className="roller-picker-footer">
                    <button type="button" id="btnPickerConfirm" onClick={() => onConfirm(date)}>
                        {`${date.year}-${String(date.month).padStart(2, '0')}-${String(date.day).padStart(2, '0')} 확인`}
                    </button>
                </div>
            </div>
        </>
    );
};

export default RollerDatePicker;