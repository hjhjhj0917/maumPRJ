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
                const trimmed = line.trim();

                if (trimmed.startsWith('data:')) {
                    let text = trimmed.substring(5).trim();
                    if (text === '[DONE]') break;
                    if (text) onChunk(text);
                }
                else if (trimmed !== '' && !trimmed.startsWith('event:')) {
                    onChunk(trimmed);
                }
            }
        }

        if (buffer.trim() !== '') {
            let text = buffer.trim();
            if (text.startsWith('data:')) {
                text = text.substring(5);
                if (text.startsWith(' ')) text = text.substring(1);
            }
            if (text && text !== '[DONE]') onChunk(text);
        }

        onComplete();
    } catch (error) {
        console.error("채팅 스트리밍 에러:", error);
        onError('연결에 문제가 발생했습니다.');
    }
};