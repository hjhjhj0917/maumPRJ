import axios from 'axios';

const apiClient = axios.create({
    baseURL: '/api/v1',
    withCredentials: true,
    headers: {
        'Content-Type': 'application/json',
    }
});

let isTokenRefreshing = false;
let refreshSubscribers = [];

const onTokenRefreshed = () => {
    refreshSubscribers.map((callback) => callback());
    refreshSubscribers = [];
};

const addRefreshSubscriber = (callback) => {
    refreshSubscribers.push(callback);
};

apiClient.interceptors.response.use(
    (response) => response.data,
    async (error) => {
        const originalRequest = error.config;

        if (error.response && error.response.status === 401 && !originalRequest._retry) {
            const currentPath = window.location.pathname;
            const publicPaths = [
                '/',
                '/account/login',
                '/account/register',
                '/account/findId',
                '/account/findPw'
            ];

            if (publicPaths.includes(currentPath)) {
                return Promise.reject(error);
            }

            originalRequest._retry = true;

            if (!isTokenRefreshing) {
                isTokenRefreshing = true;

                try {
                    await axios.post('/api/v1/login/refresh', {}, {
                        withCredentials: true
                    });

                    isTokenRefreshing = false;
                    onTokenRefreshed();

                    return apiClient(originalRequest);

                } catch (refreshError) {
                    isTokenRefreshing = false;
                    refreshSubscribers = [];

                    window.location.href = '/account/login';
                    return Promise.reject(refreshError);
                }
            }

            return new Promise((resolve) => {
                addRefreshSubscriber(() => {
                    resolve(apiClient(originalRequest));
                });
            });
        }

        return Promise.reject(error);
    }
);

export default apiClient;