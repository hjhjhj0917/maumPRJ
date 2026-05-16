import React from 'react';
import ReactMarkdown from 'react-markdown';
import { useChatBot } from '../../hooks/chatbot/useChatBot';
import * as S from '../../style/pages/ChatBot/ChatBot.styles';

const ChatBot = () => {
    const { messages, input, setInput, isStreaming, messagesEndRef, sendMessage, handleKeyDown } = useChatBot();

    const suggestions = [
        {icon: 'fa-solid fa-hashtag', text: '내가 작성한 일기를 바탕으로 나에대해 분석해줘.'},
        {icon: 'fa-solid fa-hashtag', text: '나를 분석한 결과를 참고해서 필요한 정책 알려줘.'},
        {icon: 'fa-solid fa-hashtag', text: '오늘 저녁은 뭐 먹을까?'}
    ];

    const isEmptyState = messages.length === 0;

    const renderInputArea = () => (
        <S.InputWrapper>
            <S.StyledTextarea
                rows={1}
                placeholder="대화 내용을 입력해주세요."
                value={input}
                onChange={(e) => setInput(e.target.value)}
                onKeyDown={handleKeyDown}
                disabled={isStreaming}
            />
            <S.RightActions>
                <S.SendButton onClick={sendMessage} disabled={!input.trim() || isStreaming}>
                    <i className="fa-solid fa-arrow-up"></i>
                </S.SendButton>
            </S.RightActions>
        </S.InputWrapper>
    );

    return (
        <S.ChatContainer>
            {isEmptyState ? (
                <S.EmptyStateContainer>
                    <S.CenterInputArea>
                        {renderInputArea()}
                        <S.SuggestionContainer>
                            {suggestions.map((item, idx) => (
                                <S.SuggestionButton
                                    key={idx}
                                    onClick={() => setInput(item.text)}
                                    disabled={isStreaming}
                                >
                                    <i className={item.icon}></i>
                                    {item.text}
                                </S.SuggestionButton>
                            ))}
                        </S.SuggestionContainer>
                    </S.CenterInputArea>
                </S.EmptyStateContainer>
            ) : (
                <>
                    <S.MessageList>
                        {messages.map((msg, index) => {
                            const isThinkingNow = msg.content.includes('<think>') && !msg.content.includes('</think>');
                            const cleanContent = msg.content.replace(/<think>[\s\S]*?(<\/think>|$)/gi, '').trim();

                            return (
                                <S.MessageWrapper key={index} $isUser={msg.role === 'user'}>
                                    <S.Bubble $isUser={msg.role === 'user'}>
                                        {msg.role === 'bot' && isThinkingNow && (
                                            <S.ThinkingIndicator>
                                                <i className="fa-solid fa-circle-notch fa-spin"></i> 생각하는 중...
                                            </S.ThinkingIndicator>
                                        )}
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
                    <S.BottomInputArea>
                        {renderInputArea()}
                    </S.BottomInputArea>
                </>
            )}
        </S.ChatContainer>
    );
};

export default ChatBot;