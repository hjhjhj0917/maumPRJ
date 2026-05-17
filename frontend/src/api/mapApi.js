import apiClient from './apiClient';

export const getInstitutions = () =>
    apiClient.get('/map/institutions');