import { useState, useMemo, useEffect } from 'react';
import { useNavigate } from 'react-router-dom';
import { getMonthlyDiaries, searchDiaries } from '../../api/diaryApi.js';

export const useDiaryList = () => {
    const navigate = useNavigate();
    const [currentDate, setCurrentDate] = useState(new Date());
    const [diaries, setDiaries] = useState([]);
    const [keyword, setKeyword] = useState('');
    const [searchResults, setSearchResults] = useState([]);

    const year = currentDate.getFullYear();
    const month = currentDate.getMonth() + 1;
    const dateQuery = `${year}-${String(month).padStart(2, '0')}`;

    useEffect(() => {
        if (!keyword.trim()) {
            const fetchDiaries = async () => {
                try {
                    const data = await getMonthlyDiaries(dateQuery);
                    setDiaries(data || []);
                } catch (error) {
                    setDiaries([]);
                }
            };
            fetchDiaries();
        }
    }, [dateQuery, keyword]);

    useEffect(() => {
        if (keyword.trim()) {
            const delayDebounceFn = setTimeout(async () => {
                try {
                    const data = await searchDiaries(keyword);
                    setSearchResults(data || []);
                } catch (error) {
                    setSearchResults([]);
                }
            }, 300);
            return () => clearTimeout(delayDebounceFn);
        } else {
            setSearchResults([]);
        }
    }, [keyword]);

    const daysInMonth = new Date(year, month, 0).getDate();

    const daysList = useMemo(() => {
        return Array.from({ length: daysInMonth }, (_, i) => {
            const day = i + 1;
            const fullDateStr = `${dateQuery}-${String(day).padStart(2, '0')}`;

            const diary = diaries.find(d => {
                const serverDate = d.createdAt ? d.createdAt.substring(0, 10) : '';
                return serverDate === fullDateStr;
            });

            return {
                day,
                dateStr: fullDateStr,
                diary: diary || null
            };
        });
    }, [diaries, daysInMonth, dateQuery]);

    const handlePrevMonth = () => setCurrentDate(new Date(year, month - 2, 1));
    const handleNextMonth = () => setCurrentDate(new Date(year, month, 1));

    const handleDayClick = (item) => {
        if (item.diary) {
            navigate(`/diary/${item.diary.diaryNo}`);
        } else {
            navigate(`/diary/write?date=${item.dateStr}`);
        }
    };

    const handleResultClick = (diaryNo) => {
        navigate(`/diary/${diaryNo}`);
    };

    return {
        year, month, daysList,
        handlePrevMonth, handleNextMonth, handleDayClick,
        keyword, setKeyword, searchResults, handleResultClick
    };
};