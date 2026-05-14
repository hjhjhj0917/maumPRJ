export const streamChatApi = async (message, onChunk, onError, onComplete) => {
    try {
        const response = await fetch('/api/v1/chat/stream', {
            method: 'POST',
            headers: { 'Content-Type': 'application/json' },
            credentials: 'include',
            body: JSON.stringify({ message })
        });

        const reader = response.body.getReader();
        const decoder = new TextDecoder('utf-8');

        while (true) {
            const { done, value } = await reader.read();
            if (done) break;

            const chunk = decoder.decode(value, { stream: true });
            const lines = chunk.split('\n');

            for (let line of lines) {
                if (line.trim().startsWith('data:')) {
                    const text = line.substring(line.indexOf(':') + 1).trim();
                    if (text === '[DONE]') break;

                    for (let char of text) {
                        onChunk(char);
                        await new Promise(r => setTimeout(r, 15));
                    }
                }
            }
        }
        onComplete();
    } catch (error) {
        onError('연결에 문제가 발생했습니다.');
    }
};