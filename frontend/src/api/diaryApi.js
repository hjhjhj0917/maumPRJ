import apiClient from './apiClient';

/*
일기 저장
*/
export const insertDiary = (title, content, createdAt) => {
    const params = new URLSearchParams({ title, content, createdAt });

    return apiClient.post('/diary/diaryInsert', params);
};

/*
월별 일기 목록 조회
*/
export const getMonthlyDiaries = async (createdAt) => {
    const response = await apiClient.get('/diary/monthly', {
        params: { createdAt }
    });

    return response.data;
};

/*
일기 상세 조회
*/
export const getDiaryDetail = async (diaryNo) => {
    const response = await apiClient.get('/diary/detail', {
        params: { diaryNo }
    });

    return response.data;
};