import styled, { keyframes } from 'styled-components';
import { Link } from 'react-router-dom';

const fadeInStep = keyframes`
    from { opacity: 0; transform: translateY(10px); }
    to { opacity: 1; transform: translateY(0); }
`;

export const FindIdWrapper = styled.div`
    display: flex;
    flex-direction: column;
    min-height: 100vh;
    background-color: #ffffff;
`;

export const Container = styled.div`
    flex: 1;
    display: flex;
    justify-content: center;
    align-items: center;
    width: 100%;
    padding: 40px 20px;
`;

export const FindIdCard = styled.div`
    width: 100%;
    max-width: 475px;
`;

export const StepperWrapper = styled.div`
    display: flex;
    justify-content: space-between;
    align-items: flex-end;
    margin-bottom: 60px;
    padding: 0 20px;
    position: relative;
    height: 60px;
`;

export const StepperItem = styled.div`
    display: flex;
    flex-direction: column;
    align-items: center;
    gap: 10px;
    z-index: 2;
    position: relative;

    .step-circle {
        background-color: ${props => (props.$active || props.$completed ? '#FFD166' : '#fff')};
        border-color: ${props => (props.$active || props.$completed ? '#FFD166' : '#ddd')};
        color: ${props => (props.$active || props.$completed ? '#fff' : '#ddd')};
        width: 32px;
        height: 32px;
        border-radius: 50%;
        border: 2px solid;
        display: flex;
        justify-content: center;
        align-items: center;
        font-size: 14px;
        transition: all 0.3s ease;
    }

    .step-label {
        color: ${props => (props.$active || props.$completed ? '#333' : '#999')};
        font-weight: ${props => (props.$active || props.$completed ? 'bold' : 'normal')};
        font-size: 11px;
        position: absolute;
        top: -25px;
        width: 60px;
        text-align: center;
    }
`;

export const StepLine = styled.div`
    flex: 1;
    height: 1px;
    background-color: #eee;
    margin-bottom: 16px;
    margin-left: -5px;
    margin-right: -5px;
`;

export const StepTitle = styled.h2`
    font-size: 32px;
    font-weight: 800;
    color: #000;
    margin-bottom: 12px;
    text-align: left;
`;

export const StepSubTitle = styled.p`
    font-size: 16px;
    color: #666;
    line-height: 1.5;
    margin-bottom: 40px;
    text-align: left;
    min-height: 48px;

    span {
        color: #333;
        font-weight: 600;
    }
`;

export const SlideViewport = styled.div`
    width: 100%;
    overflow: hidden;
`;

export const SlideTrack = styled.div`
    display: flex;
    width: 100%;
    transition: transform 0.6s cubic-bezier(0.25, 1, 0.5, 1);
    transform: translateX(${props => (props.$step - 1) * -100}%);
`;

export const FormStep = styled.div`
    min-width: 100%;
    box-sizing: border-box;
    transform: ${props => (props.$active ? 'scale(1)' : 'scale(0.95)')};
    opacity: ${props => (props.$active ? '1' : '0')};
    transition: all 0.6s ease;
    display: flex;
    flex-direction: column;
    justify-content: flex-start;
    min-height: 350px;
`;

export const FormStepFieldWrapper = styled.div`
    .input-group {
        margin-bottom: 10px;
    }

    .field-message {
        display: block;
        min-height: 18px;
        margin-top: 4px;
        font-size: 12px;
        visibility: ${props => props.$hasMessage ? 'visible' : 'hidden'};
    }
`;

export const VerificationWrapper = styled.div`
    display: flex;
    justify-content: space-between;
    gap: 10px;
    margin-bottom: 10px;

    input {
        width: 60px;
        height: 70px;
        background-color: #f8f9fa;
        border: 2px solid #eee;
        border-radius: 12px;
        font-size: 24px;
        font-weight: 700;
        text-align: center;
        transition: all 0.3s;

        &:focus {
            border-color: #FFD166;
            background-color: #fff;
            box-shadow: 0 0 0 4px rgba(255, 209, 102, 0.1);
            outline: none;
        }

        &::placeholder {
            color: #eee;
        }
    }
`;

export const FieldMessage = styled.span`
    display: block;
    font-size: 13px;
    font-weight: 500;
    margin-bottom: 15px;
    text-align: left;
    min-height: 18px;
    color: ${props => (props.$type === 'error' ? '#ef4444' : '#22c55e')};
    visibility: ${props => (props.$show ? 'visible' : 'hidden')};
`;

export const ResendText = styled.div`
    text-align: left;
    font-size: 14px;
    color: #888;
    margin-bottom: 20px;

    button {
        background: none;
        border: none;
        color: #3b82f6;
        text-decoration: underline;
        cursor: pointer;
        font-weight: 500;
        margin-left: 6px;
        padding: 0;

        &:hover {
            color: #2563eb;
        }
    }
`;

export const FadeInResult = styled.div`
    display: flex;
    flex-direction: column;
    align-items: center;
    animation: ${fadeInStep} 0.4s ease-out forwards;
`;

export const BtnConfirm = styled.button`
    width: 100%;
    padding: 16px;
    background-color: #333;
    color: #fff;
    border: none;
    border-radius: 12px;
    font-size: 16px;
    font-weight: 700;
    cursor: pointer;
    margin-top: 10px;
    margin-bottom: 40px;
    transition: background-color 0.3s;

    &:hover {
        background-color: #000;
    }
`;

export const CheckCircle = styled.div`
    width: 65px;
    height: 65px;
    background-color: #FFD166;
    border-radius: 50%;
    display: flex;
    justify-content: center;
    align-items: center;
    font-size: 32px;
    color: #fff;
    margin-bottom: 30px;
`;

export const ResultText = styled.p`
    font-size: 24px;
    font-weight: 700;
    color: #000;
    line-height: 1.6;
    margin-bottom: 40px;
    text-align: center;
`;

export const HighlightId = styled.span`
    color: #FFD166;
    font-size: 32px;
`;

export const AuthLinks = styled.div`
    font-size: 13px;
    color: #888;
    margin-bottom: 40px;
    text-align: center;

    a {
        color: #888;
        text-decoration: none;
        transition: color 0.2s;

        &:hover {
            color: #333;
            font-weight: 600;
        }
    }
`;

export const Separator = styled.span`
    margin: 0 10px;
    color: #ddd;
    font-size: 10px;
    vertical-align: middle;
`;

export const SignupBox = styled.div`
    font-size: 14px;
    color: #999;
    line-height: 1.6;
    text-align: center;
    width: 100%;
    margin-top: 20px;
`;

export const LinkSignup = styled(Link)`
    color: #FFD166;
    text-decoration: none;
    font-weight: 700;
    margin-left: 5px;

    &:hover {
        text-decoration: underline;
    }
`;