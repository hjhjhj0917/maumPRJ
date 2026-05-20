import apiClient from './apiClient';

/* [Diary Management] */

export const insertDiary = (title, content, createdAt) => {
    return apiClient.post('/diary/diaryInsert', {
        title,
        content,
        createdAt
    });
};

export const updateDiary = (diaryNo, title, content) => {
    return apiClient.post('/diary/diaryUpdate', {
        diaryNo,
        title,
        content
    });
};

export const deleteDiary = (diaryNo) => {
    return apiClient.post('/diary/diaryDelete', {
        diaryNo
    });
};


/* [Diary Retrieval] */

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

export const searchDiaries = async (keyword) => {
    const response = await apiClient.get('/diary/search', {
        params: { keyword }
    });
    return response.data;
};

export const filterDiariesByColors = async (colors) => {
    const response = await apiClient.get('/diary/filter', {
        params: { colors: colors.join(',') }
    });
    return response.data;
};

export const getRecentDiaries = async () => {
    const response = await apiClient.get('/diary/recent');
    return response.data;
};