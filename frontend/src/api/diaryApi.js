import apiClient from './apiClient';

export const insertDiary = (title, content, createdAt) => {
    return apiClient.post('/diary/diaryInsert', {
        title,
        content,
        createdAt
    });
};

export const getMonthlyDiaries = async (createdAt) => {
    const response = await apiClient.get('/diary/monthly', {
        params: { createdAt }
    });

    return response.data;
};

export const getDiaryDetail = async (diaryNo) => {
    const response = await apiClient.get('/diary/detail', {
        params: { diaryNo }
    });

    return response.data;
};