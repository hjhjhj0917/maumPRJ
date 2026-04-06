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
    padding: 0 20px;
`;

export const FindIdCard = styled.div`
    width: 100%;
    max-width: 403px;
    text-align: center;
    padding-bottom: 50px;

    @media (max-width: 480px) {
        padding: 20px;
    }
`;

export const Title = styled.h3`
    font-size: 24px;
    font-weight: 700;
    color: #000;
    margin-bottom: 60px;

    @media (max-width: 480px) {
        margin-bottom: 40px;
    }
`;

export const StepContainer = styled.div`
    min-height: 250px;
    display: flex;
    flex-direction: column;
    justify-content: center;
`;

export const FadeInForm = styled.form`
    animation: ${fadeInStep} 0.4s ease-out forwards;
`;

export const FadeInResult = styled.div`
    display: flex;
    flex-direction: column;
    align-items: center;
    animation: ${fadeInStep} 0.4s ease-out forwards;
`;

export const BtnConfirm = styled.button`
    width: 100%;
    padding: 15px;
    background-color: #FFD166;
    color: #000;
    border: none;
    border-radius: 30px;
    font-size: 16px;
    font-weight: 700;
    cursor: pointer;
    margin-top: 20px;
    margin-bottom: 40px;
    transition: background-color 0.3s;
    box-shadow: 0 4px 10px rgba(255, 209, 102, 0.3);

    &:hover {
        background-color: #E0B34A;
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
    color: #333;
    margin-bottom: 30px;
`;

export const ResultText = styled.p`
    font-size: 24px;
    font-weight: 700;
    color: #000;
    line-height: 1.6;
    margin-bottom: 40px;

    @media (max-width: 480px) {
        font-size: 20px;
    }
`;

export const HighlightId = styled.span`
    color: #FFD166;
    font-size: 32px;

    @media (max-width: 480px) {
        font-size: 28px;
    }
`;

export const AuthLinks = styled.div`
    font-size: 13px;
    color: #888;
    margin-bottom: 40px;

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