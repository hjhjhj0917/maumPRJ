import styled from 'styled-components';

export const InputGroup = styled.div`
    margin-bottom: 30px;
    text-align: left;
    width: 100%;
    position: relative;
`;

export const LabelRow = styled.div`
    display: flex;
    align-items: center;
    gap: 12px;
    margin-bottom: 10px;

    label {
        font-size: 14px;
        font-weight: 600;
        color: #666;
        min-width: 70px;
    }
`;

export const FieldMessage = styled.span`
    flex: 1;
    font-size: 12px;
    font-weight: 500;
    opacity: ${props => (props.$show ? 1 : 0)};
    transition: opacity 0.25s ease;
    color: ${props => (props.$type === 'error' ? '#ef4444' : '#22c55e')};
`;

export const InputWrapper = styled.div`
    display: flex;
    gap: 10px;
    position: relative;
`;

export const StyledInput = styled.input`
    width: 100%;
    padding: 10px 0;
    border: none;
    border-bottom: 2px solid #eee;
    outline: none;
    font-size: 15px;
    color: #333;
    transition: border-color 0.3s;
    background-color: transparent;

    &:focus {
        border-bottom-color: #FFD166;
    }

    &::placeholder {
        color: #ccc;
    }
`;

export const ActionButton = styled.button`
    padding: 8px 20px;
    background-color: #FFD166;
    border: none;
    border-radius: 20px;
    font-size: 12px;
    font-weight: bold;
    color: #333;
    cursor: pointer;
    height: 35px;
    white-space: nowrap;

    &:hover { background-color: #E0B34A; }
    &:disabled { background-color: #eee; color: #999; }
`;

export const ToggleIcon = styled.i`
    color: ${props => (props.$active ? '#ffd166' : '#bbb')};
    font-size: 18px;
    position: absolute;
    top: 50%;
    transform: translateY(-50%);
    right: 15px;
    cursor: pointer;
`;