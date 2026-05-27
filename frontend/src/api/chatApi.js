import apiClient from './apiClient';

export const getChatHistoryApi = async () => {
    try {
        const response = await apiClient.get('/chat/history');
        return response;
    } catch (error) {
        console.error("채팅 내역 불러오기 에러:", error);
        throw error;
    }
};

export const streamChatApi = async (message, onChunk, onError, onComplete) => {
    try {
        const response = await fetch('/api/v1/chat/stream', {
            method: 'POST',
            headers: {
                'Content-Type': 'application/json',
                'Accept': 'text/event-stream'
            },
            credentials: 'include',
            body: JSON.stringify({ message })
        });

        if (!response.ok) {
            throw new Error(`서버 에러: ${response.status}`);
        }

        const reader = response.body.getReader();
        const decoder = new TextDecoder('utf-8');
        let buffer = '';

        while (true) {
            const { done, value } = await reader.read();
            if (done) break;

            buffer += decoder.decode(value, { stream: true });
            const lines = buffer.split('\n');

            buffer = lines.pop();

            for (let line of lines) {
                if (line.startsWith('data:')) {
                    let text = line.substring(5);

                    if (text.startsWith(' ')) {
                        text = text.substring(1);
                    }

                    if (text === '[DONE]') break;

                    if (text) {
                        // <br>을 마크다운 줄바꿈(공백 2개 + \n)으로 변환하고 <sp>를 공백으로 변환
                        text = text.split('<br>').join('  \n').split('<sp>').join(' ');
                        onChunk(text);
                    }
                }
            }
        }

        if (buffer.startsWith('data:')) {
            let text = buffer.substring(5);
            if (text.startsWith(' ')) text = text.substring(1);
            if (text && text !== '[DONE]') {
                // <br>을 마크다운 줄바꿈으로 변환하고 <sp>를 공백으로 변환
                text = text.split('<br>').join('  \n').split('<sp>').join(' ');
                onChunk(text);
            }
        }

        onComplete();
    } catch (error) {
        console.error("채팅 스트리밍 에러:", error);
        onError('연결에 문제가 발생했습니다.');
    }
};