import { useState, useRef, useEffect } from 'react';
import { streamChatApi, getChatHistoryApi } from '../../api/chatApi';

export const useChatBot = () => {
    const [messages, setMessages] = useState([]);
    const [input, setInput] = useState('');
    const [isStreaming, setIsStreaming] = useState(false);
    const [isWaiting, setIsWaiting] = useState(false); // 응답 대기 상태 추가
    const [isLoading, setIsLoading] = useState(true);
    const messagesEndRef = useRef(null);

    const scrollToBottom = () => {
        messagesEndRef.current?.scrollIntoView({ behavior: 'smooth' });
    };

    // 채팅 내역 불러오기
    useEffect(() => {
        const fetchHistory = async () => {
            try {
                const history = await getChatHistoryApi();
                if (history && history.length > 0) {
                    // 불러온 내역에서도 태그 변환 처리 (기존에 저장된 데이터 대응)
                    const formattedHistory = history.map(msg => ({
                        ...msg,
                        content: msg.role === 'bot' 
                            ? msg.content.split('<br>').join('  \n').split('<sp>').join(' ')
                            : msg.content
                    }));
                    setMessages(formattedHistory);
                }
            } catch (error) {
                console.error("채팅 내역 불러오기 실패:", error);
            } finally {
                setIsLoading(false);
            }
        };
        fetchHistory();
    }, []);

    useEffect(() => {
        scrollToBottom();
    }, [messages]);

    const sendMessage = async () => {
        if (!input.trim() || isStreaming) return;

        const userMessage = input.trim();
        setInput('');
        setMessages(prev => [...prev, { role: 'user', content: userMessage }]);
        setIsStreaming(true);
        setIsWaiting(true); // 메시지 전송 직후 대기 상태 시작

        await streamChatApi(
            userMessage,
            (chunk) => {
                // 데이터가 한 글자라도 들어오면 대기 상태를 즉시 해제
                setIsWaiting(false); 
                setMessages(prev => {
                    const lastMessage = prev[prev.length - 1];
                    if (lastMessage && lastMessage.role === 'bot') {
                        const newMessages = [...prev];
                        const lastIndex = newMessages.length - 1;
                        newMessages[lastIndex] = {
                            ...newMessages[lastIndex],
                            content: newMessages[lastIndex].content + chunk
                        };
                        return newMessages;
                    } else {
                        return [...prev, { role: 'bot', content: chunk }];
                    }
                });
            },
            (error) => {
                console.error(error);
                setIsStreaming(false);
                setIsWaiting(false); // 에러 시에도 해제
            },
            () => {
                setIsStreaming(false);
                setIsWaiting(false); // 완료 시에도 확실히 해제
            }
        );
    };

    const handleKeyDown = (e) => {
        if (e.key === 'Enter' && !e.shiftKey) {
            e.preventDefault();
            sendMessage();
        }
    };

    return { messages, input, setInput, isStreaming, isWaiting, isLoading, messagesEndRef, sendMessage, handleKeyDown };
};