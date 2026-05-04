import axios from 'axios';

const apiClient = axios.create({
    baseURL: '/api/v1',
    withCredentials: true,
    headers: {
        'Content-Type': 'application/json',
    }
});

apiClient.interceptors.response.use(
    (response) => response.data,
    (error) => {
        if (error.response && error.response.status === 401) {
            console.error('인증 에러: 로그인이 필요하거나 토큰이 만료되었습니다.');
            // window.location.href = '/login';
        }
        return Promise.reject(error);
    }
);

export default apiClient;