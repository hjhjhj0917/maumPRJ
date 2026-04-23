/*
일기 저장
*/
export const insertDiary = async (title, content, createdAt) => {
    const params = new URLSearchParams();
    params.append('title', title);
    params.append('content', content);
    params.append('createdAt', createdAt);

    const response = await fetch('/api/diary/diaryInsert', {
        method: 'POST',
        headers: { 'Content-Type': 'application/x-www-form-urlencoded' },
        body: params,
        credentials: 'include'
    });

    if (!response.ok) {
        throw new Error('Network response was not ok');
    }
    return await response.json();
};

export const getMonthlyDiaries = async (createdAt) => {
    try {
        const response = await fetch(`/api/diary/monthly?createdAt=${createdAt}`, {
            method: 'GET',
            credentials: 'include'
        });

        if (!response.ok) {
            throw new Error('목록을 불러오는 중 오류가 발생했습니다.');
        }

        const result = await response.json();

        if (result.httpStatus !== 'OK') {
            throw new Error(result.message || '조회 실패');
        }

        return result.data;

    } catch (error) {
        console.error('getMonthlyDiaries 에러:', error);
        throw error;
    }
};