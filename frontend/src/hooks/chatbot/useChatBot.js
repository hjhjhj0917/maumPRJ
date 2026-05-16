import { useState, useRef, useEffect } from 'react';
import { streamChatApi } from '../../api/chatApi';

export const useChatBot = () => {
    const [messages, setMessages] = useState([
        { role: 'bot', content: '안녕하세요! 당신의 이야기를 듣고 싶은 마음입니다. 오늘 하루는 어떠셨나요?' }
    ]);
    const [input, setInput] = useState('');
    const [isStreaming, setIsStreaming] = useState(false);
    const messagesEndRef = useRef(null);

    const scrollToBottom = () => {
        messagesEndRef.current?.scrollIntoView({ behavior: 'smooth' });
    };

    useEffect(() => {
        scrollToBottom();
    }, [messages]);

    const sendMessage = async () => {
        if (!input.trim() || isStreaming) return;

        const userMessage = input.trim();
        setInput('');
        setMessages(prev => [...prev, { role: 'user', content: userMessage }, { role: 'bot', content: '' }]);
        setIsStreaming(true);

        await streamChatApi(
            userMessage,
            (chunk) => {
                setMessages(prev => {
                    const newMessages = [...prev];
                    newMessages[newMessages.length - 1].content += chunk;
                    return [...newMessages];
                });
            },
            () => setIsStreaming(false),
            () => setIsStreaming(false)
        );
    };

    const handleKeyDown = (e) => {
        if (e.key === 'Enter' && !e.shiftKey) {
            e.preventDefault();
            sendMessage();
        }
    };

    return { messages, input, setInput, isStreaming, messagesEndRef, sendMessage, handleKeyDown };
};