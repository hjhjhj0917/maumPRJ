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
            const currentPath = window.location.pathname;
            const publicPaths = [
                '/',
                '/account/login',
                '/account/register',
                '/account/findId',
                '/account/findPw'
            ];

            if (!publicPaths.includes(currentPath)) {
                window.location.href = '/account/login';
            }
        }
        return Promise.reject(error);
    }
);

export default apiClient;