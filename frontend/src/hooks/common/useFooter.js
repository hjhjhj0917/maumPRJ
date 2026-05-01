import { useMemo } from 'react';

export const useFooter = () => {
    const currentYear = useMemo(() => new Date().getFullYear(), []);

    const socialLinks = [
        { id: 'github', icon: 'fa-brands fa-github', url: 'https://github.com/hjhjhj0917' },
        { id: 'instagram', icon: 'fa-brands fa-instagram', url: 'https://www.instagram.com/poly_kangseo/' },
        { id: 'youtube', icon: 'fa-brands fa-youtube', url: 'https://www.youtube.com/channel/UCmzyR9BA0gHRM58o8SjdYjw' }
    ];

    return {
        currentYear,
        socialLinks
    };
};