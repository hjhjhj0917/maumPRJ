import axios from 'axios';

const apiClient = axios.create({
    baseURL: '/api',
    withCredentials: true,
    headers: {
        'Content-Type': 'application/x-www-form-urlencoded',
    }
});

apiClient.interceptors.response.use(
    (response) => response.data, // 가공된 데이터(JSON)만 반환
    (error) => {
        console.error('API 호출 에러:', error);
        return Promise.reject(error);
    }
);

export default apiClient;