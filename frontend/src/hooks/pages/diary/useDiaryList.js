import { useState, useMemo, useEffect } from 'react';
import { useNavigate } from 'react-router-dom';

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
                const response = await fetch(`/api/diary/monthly?createdAt=${dateQuery}`, {
                    method: 'GET',
                    credentials: 'include'
                });

                if (!response.ok) throw new Error();

                const result = await response.json();

                // ★ 필독: 서버 응답 전체 구조를 확인하세요!
                console.log("1. 서버 응답 전체 구조:", result);
                console.log("2. httpStatus 값 확인:", result.httpStatus);

                // 더 확실한 조건으로 변경: result.data 배열이 존재하면 세팅합니다.
                if (result.data) {
                    console.log("3. 일기 리스트 세팅 성공:", result.data);
                    setDiaries(result.data || []);
                } else {
                    console.warn("일기 리스트 세팅 실패: result.data가 없습니다.");
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

            // ★ 날짜 비교 디버깅 로그 (일기가 있는 날만 확인하세요)
            const diary = diaries.find(d => {
                // substring(0, 10)을 통해 '2026-04-23' 형식만 비교합니다.
                const serverDate = d.createdAt ? d.createdAt.substring(0, 10) : '';

                // 일기가 있는 특정 날짜에 로그를 찍고 싶다면:
                if (serverDate === "2026-04-23") { // 테스트용 날짜
                    console.log("비교 중: 서버", serverDate, "vs 생성", fullDateStr);
                    console.log("매칭 결과:", serverDate === fullDateStr);
                }

                return serverDate === fullDateStr;
            });

            return {
                day,
                dateStr: fullDateStr,
                diary: diary || null
            };
        });
    }, [diaries, daysInMonth, dateQuery]);

    const handlePrevMonth = () => {
        setCurrentDate(new Date(year, month - 2, 1));
    };

    const handleNextMonth = () => {
        setCurrentDate(new Date(year, month, 1));
    };

    const handleDayClick = (item) => {
        if (item.diary) {
            navigate(`/diary/${item.diary.diaryNo}`);
        } else {
            navigate(`/diary/write?date=${item.dateStr}`);
        }
    };

    return {
        year,
        month,
        daysList,
        handlePrevMonth,
        handleNextMonth,
        handleDayClick
    };
};