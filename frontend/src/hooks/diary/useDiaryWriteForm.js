import { useState } from 'react';
import { useNavigate, useSearchParams } from 'react-router-dom';
import { insertDiary } from '../../api/diaryApi.js';

export const useDiaryWriteForm = () => {
    const navigate = useNavigate();
    const [searchParams] = useSearchParams();

    const queryDate = searchParams.get('date');
    const today = new Date();

    const [title, setTitle] = useState('');
    const [content, setContent] = useState('');
    const [showDatePicker, setShowDatePicker] = useState(false);

    const [isLoading, setIsLoading] = useState(false);

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

    const formattedDate = `${date.year}년 ${String(date.month).padStart(2, '0')}월 ${String(date.day).padStart(2, '0')}일`;
    const apiDate = `${date.year}-${String(date.month).padStart(2, '0')}-${String(date.day).padStart(2, '0')}`;

    const handleSubmit = async () => {
        if (!title.trim()) return alert('제목을 입력해주세요.');
        if (!content.trim()) return alert('내용을 입력해주세요.');

        try {
            setIsLoading(true);

            const res = await insertDiary(title, content, apiDate);

            if (res && res.data) {
                alert(res.message || '일기가 성공적으로 저장되었습니다.');

                window.dispatchEvent(new CustomEvent('diary-updated'));

                navigate(`/diary/${res.data}`);
            } else {
                alert(res.message || '저장에 실패했습니다.');
                setIsLoading(false);
            }
        } catch (error) {
            const errorMsg = error.response?.data?.message || "서버 통신 중 오류가 발생했습니다.";
            alert(errorMsg);
            setIsLoading(false);
        }
    };

    return {
        title, setTitle,
        content, setContent,
        showDatePicker, setShowDatePicker,
        date, setDate,
        formattedDate,
        handleSubmit,
        isLoading
    };
};