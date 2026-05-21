import { useState, useEffect, useCallback } from 'react';
import { useParams, useNavigate } from 'react-router-dom';
import { getDiaryDetail, updateDiary, deleteDiary } from '../../api/diaryApi.js';

export const useDiaryDetail = () => {
    const { diaryNo } = useParams();
    const navigate = useNavigate();

    const [diary, setDiary] = useState(null);
    const [loading, setLoading] = useState(true);

    const [isEditing, setIsEditing] = useState(false);
    const [editTitle, setEditTitle] = useState('');
    const [editContent, setEditContent] = useState('');

    const fetchDiaryDetail = useCallback(async () => {
        try {
            const data = await getDiaryDetail(diaryNo);
            const diaryData = data.data || data;

            if (diaryData) {
                setDiary(diaryData);
            } else {
                alert("일기 정보를 찾을 수 없습니다.");
                navigate('/diary/list', { replace: true });
            }
        } catch (error) {
            const errorMsg = error.response?.data?.message || "일기를 불러오는 중 오류가 발생했습니다.";
            alert(errorMsg);
            navigate('/diary/list', { replace: true });
        } finally {
            setLoading(false);
        }
    }, [diaryNo, navigate]);

    useEffect(() => {
        if (diaryNo) {
            fetchDiaryDetail();
        }
    }, [diaryNo, fetchDiaryDetail]);

    const handleGoBack = () => {
        navigate('/diary/list');
    };

    const handleEditClick = () => {
        setEditTitle(diary.title);
        setEditContent(diary.content);
        setIsEditing(true);
    };

    const handleCancelEdit = () => {
        setIsEditing(false);
    };

    const handleSaveClick = async () => {
        if (!editTitle.trim() || !editContent.trim()) {
            alert('제목과 내용을 모두 입력해주세요.');
            return;
        }

        try {
            setLoading(true);
            const res = await updateDiary(diaryNo, editTitle, editContent);

            if (res && res.data) {
                alert(res.message || "일기가 수정 및 재분석 되었습니다.");
                setIsEditing(false);

                window.dispatchEvent(new CustomEvent('diary-updated'));

                await fetchDiaryDetail();
            } else {
                alert(res.message || "수정에 실패했습니다.");
                setLoading(false);
            }
        } catch (error) {
            alert(error.response?.data?.message || "서버 통신 중 오류가 발생했습니다.");
            setLoading(false);
        }
    };

    const handleDeleteClick = async () => {
        if (window.confirm("정말 이 일기를 삭제하시겠습니까? 삭제 후에는 복구할 수 없습니다.")) {
            try {
                setLoading(true);
                const res = await deleteDiary(diaryNo);

                if (res && res.data) {
                    alert(res.message || "일기가 삭제되었습니다.");

                    window.dispatchEvent(new CustomEvent('diary-updated'));

                    navigate('/diary/list', { replace: true });
                } else {
                    alert(res.message || "삭제에 실패했습니다.");
                    setLoading(false);
                }
            } catch (error) {
                alert(error.response?.data?.message || "서버 통신 중 오류가 발생했습니다.");
                setLoading(false);
            }
        }
    };

    return {
        diary,
        loading,
        handleGoBack,
        isEditing,
        editTitle, setEditTitle,
        editContent, setEditContent,
        handleEditClick,
        handleCancelEdit,
        handleSaveClick,
        handleDeleteClick
    };
};