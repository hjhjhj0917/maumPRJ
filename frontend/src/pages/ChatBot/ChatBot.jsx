// src/pages/ChatBot/ChatBot.jsx
import React from 'react';
import ReactMarkdown from 'react-markdown';
import { useChatBot } from '../../hooks/chatbot/useChatBot';
import * as S from '../../style/pages/ChatBot/ChatBot.styles';

const ChatBot = () => {
    const { messages, input, setInput, isStreaming, messagesEndRef, sendMessage, handleKeyDown } = useChatBot();

    return (
        <S.ChatContainer>
            <S.MessageList>
                {messages.map((msg, index) => {
                    const isThinkingNow = msg.content.includes('<think>') && !msg.content.includes('</think>');

                    const cleanContent = msg.content.replace(/<think>[\s\S]*?(<\/think>|$)/gi, '').trim();

                    return (
                        <S.MessageWrapper key={index} $isUser={msg.role === 'user'}>
                            <S.ProfileName $isUser={msg.role === 'user'}>
                                {msg.role === 'user' ? '나' : '담소'}
                            </S.ProfileName>
                            <S.Bubble $isUser={msg.role === 'user'}>

                                {msg.role === 'bot' && isThinkingNow && (
                                    <S.ThinkingIndicator>
                                        <i className="fa-solid fa-lightbulb"></i> 담소가 깊게 고민하고 있어요...
                                    </S.ThinkingIndicator>
                                )}

                                {/* 마크다운 렌더링 및 스트리밍 커서 깜빡임 적용 */}
                                {msg.role === 'user' ? (
                                    msg.content
                                ) : (
                                    <ReactMarkdown>
                                        {cleanContent + (isStreaming && index === messages.length - 1 ? ' ▌' : '')}
                                    </ReactMarkdown>
                                )}
                            </S.Bubble>
                        </S.MessageWrapper>
                    );
                })}
                <div ref={messagesEndRef} />
            </S.MessageList>

            <S.InputContainer>
                <S.InputWrapper>
                    <S.StyledTextarea
                        rows={1}
                        placeholder="메시지를 입력하세요 (Shift + Enter로 줄바꿈)"
                        value={input}
                        onChange={(e) => setInput(e.target.value)}
                        onKeyDown={handleKeyDown}
                        disabled={isStreaming}
                    />
                    <S.SendButton onClick={sendMessage} disabled={!input.trim() || isStreaming}>
                        ↑
                    </S.SendButton>
                </S.InputWrapper>
            </S.InputContainer>
        </S.ChatContainer>
    );
};

export default ChatBot;