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