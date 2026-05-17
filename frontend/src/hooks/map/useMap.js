import { useState, useEffect } from 'react';
import { useKakaoLoader } from 'react-kakao-maps-sdk';
import { getInstitutions } from '../../api/mapApi';

export const useMentalMap = () => {
    const [institutions, setInstitutions] = useState([]);

    const [loading, error] = useKakaoLoader({
        appkey: import.meta.env.VITE_KAKAO_JS_KEY,
    });

    useEffect(() => {
        const fetchInstitutions = async () => {
            try {
                const data = await getInstitutions();
                setInstitutions(data);
            } catch (err) {
                console.error("데이터를 불러오는데 실패했습니다.", err);
            }
        };

        fetchInstitutions();
    }, []);

    return { institutions, loading, error };
};