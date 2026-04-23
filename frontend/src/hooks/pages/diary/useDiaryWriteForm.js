import { useState, useEffect } from 'react';
import { useNavigate, useSearchParams } from 'react-router-dom';
import { insertDiary } from '../../../api/diaryApi';

export const useDiaryWriteForm = () => {
    const navigate = useNavigate();
    const [searchParams] = useSearchParams();

    // 1. URL 파라미터에서 날짜 추출 (없으면 오늘 날짜)
    const queryDate = searchParams.get('date'); // "2026-04-23" 형태
    const today = new Date();

    const [title, setTitle] = useState('');
    const [content, setContent] = useState('');
    const [showDatePicker, setShowDatePicker] = useState(false);

    // 2. 날짜 상태 초기화 (목록에서 넘어온 날짜가 있으면 해당 날짜로 세팅)
    const [date, setDate] = useState(() => {
        if (queryDate) {
            const [y, m, d] = queryDate.split('-').map(Number);
            return { year: y, month: m, day: d };
        }
        return {
            year: today.getFullYear(),
            month: today.getMonth() + 1,
            day: today.getDate()
        };
    });

    // 화면 표시용 (예: 2026년 04월 23일)
    const formattedDate = `${date.year}년 ${String(date.month).padStart(2, '0')}월 ${String(date.day).padStart(2, '0')}일`;

    // 서버 전송용 (예: 2026-04-23)
    const apiDate = `${date.year}-${String(date.month).padStart(2, '0')}-${String(date.day).padStart(2, '0')}`;

    const handleSubmit = async () => {
        if (!title.trim()) {
            alert('제목을 입력해주세요.');
            return;
        }
        if (!content.trim()) {
            alert('내용을 입력해주세요.');
            return;
        }

        try {
            // 백엔드 DiaryDTO 규격에 맞춰 전송
            const res = await insertDiary(title, content, apiDate);

            // CommonResponse 규격 (httpStatus === 'OK') 확인
            if (res.httpStatus === 'OK' || res.status === 200) {
                alert('일기가 성공적으로 저장되었습니다.');
                navigate('/diary/list'); // 저장 후 목록으로 이동
            } else {
                alert(res.message || '저장에 실패했습니다.');
            }
        } catch (error) {
            console.error("일기 저장 통신 오류:", error);
            alert("서버 통신 중 오류가 발생했습니다.");
        }
    };

    return {
        title, setTitle,
        content, setContent,
        showDatePicker, setShowDatePicker,
        date, setDate,
        formattedDate,
        handleSubmit
    };
};