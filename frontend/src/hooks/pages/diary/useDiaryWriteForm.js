import { useState } from 'react';
import { useNavigate } from 'react-router-dom';
import { insertDiary } from '../../../api/diaryApi';

export const useDiaryWriteForm = () => {
    const navigate = useNavigate();

    const [title, setTitle] = useState('');
    const [content, setContent] = useState('');
    const [showDatePicker, setShowDatePicker] = useState(false);

    const today = new Date();
    const [date, setDate] = useState({
        year: today.getFullYear(),
        month: today.getMonth() + 1,
        day: today.getDate()
    });

    const formattedDate = `${date.year}년 ${String(date.month).padStart(2, '0')}월 ${String(date.day).padStart(2, '0')}일`;

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
            const res = await insertDiary(title, content, formattedDate);

            if (res.result === 1) {
                alert(res.msg);
                navigate('/diary/write');
            } else {
                alert(res.msg);
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