import { useState, useMemo, useEffect } from 'react';
import { useNavigate } from 'react-router-dom';
import { getMonthlyDiaries } from '../../../api/diaryAPI';

export const useDiaryList = () => {
    const navigate = useNavigate();
    const [currentDate, setCurrentDate] = useState(new Date());
    const [diaries, setDiaries] = useState([]);

    const year = currentDate.getFullYear();
    const month = currentDate.getMonth() + 1;
    const dateQuery = `${year}-${String(month).padStart(2, '0')}`;

    useEffect(() => {
        const fetchDiaries = async () => {
            try {
                const data = await getMonthlyDiaries(dateQuery);

                if (data) {
                    setDiaries(data);
                } else {
                    setDiaries([]);
                }
            } catch (error) {
                console.error("데이터 로드 에러:", error);
                setDiaries([]);
            }
        };

        fetchDiaries();
    }, [dateQuery]);

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

    return {
        year, month, daysList,
        handlePrevMonth, handleNextMonth, handleDayClick
    };
};