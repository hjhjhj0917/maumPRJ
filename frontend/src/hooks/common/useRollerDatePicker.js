import { useState, useEffect, useRef } from 'react';

export const useRollerDatePicker = (initialDate) => {
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

    return {
        date,
        years,
        months,
        days,
        yearRef,
        monthRef,
        dayRef,
        handleScroll
    };
};