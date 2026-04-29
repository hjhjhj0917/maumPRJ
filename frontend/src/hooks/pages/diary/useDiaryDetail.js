import { useState, useEffect } from 'react';
import { useParams, useNavigate } from 'react-router-dom';
import { getDiaryDetail } from '../../../api/diaryAPI';

export const useDiaryDetail = () => {
    // App.jsx 라우터에서 /diary/:diaryNo 로 설정했다고 가정합니다.
    const { diaryNo } = useParams();
    const navigate = useNavigate();

    const [diary, setDiary] = useState(null);
    const [loading, setLoading] = useState(true);

    useEffect(() => {
        const fetchDiaryDetail = async () => {
            try {
                // 1. 상세 데이터 호출
                const data = await getDiaryDetail(diaryNo);

                if (data) {
                    setDiary(data);
                } else {
                    alert("일기 정보를 찾을 수 없습니다.");
                    navigate('/diary/list', { replace: true });
                }
            } catch (error) {
                // 2. 에러 핸들링
                const errorMsg = error.response?.data?.msg || "일기를 불러오는 중 오류가 발생했습니다.";
                alert(errorMsg);
                navigate('/diary/list', { replace: true });
            } finally {
                // 3. 로딩 상태 해제
                setLoading(false);
            }
        };

        if (diaryNo) {
            fetchDiaryDetail();
        }
    }, [diaryNo, navigate]);

    // 목록으로 돌아가기
    const handleGoBack = () => {
        navigate('/diary/list');
    };

    return {
        diary,
        loading,
        handleGoBack
    };
};