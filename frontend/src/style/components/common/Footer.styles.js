import styled from 'styled-components';

export const FooterWrapper = styled.footer`
    width: 100%;
    background-color: #ffffff;
    border-top: 1px solid #eeeeee;
    padding: 40px 0;
    font-family: 'Noto Sans KR', sans-serif;
`;

export const FooterContainer = styled.div`
    max-width: 1200px;
    margin: 0 auto;
    padding: 0 20px;
    display: flex;
    justify-content: space-between;
    align-items: flex-end;

    @media (max-width: 768px) {
        flex-direction: column;
        align-items: flex-start;
        gap: 30px;
    }
`;

export const LeftSection = styled.div`
    display: flex;
    flex-direction: column;
    gap: 12px;
`;

export const InfoText = styled.div`
    display: flex;
    flex-wrap: wrap;
    gap: 10px;
    font-size: 13px;
    color: #888888;
    line-height: 1.4;

    span {
        display: flex;
        align-items: center;
    }

    span::after {
        content: "";
        display: inline-block;
        width: 1px;
        height: 10px;
        background-color: #dddddd;
        margin-left: 10px;
        vertical-align: middle;
    }

    span:last-child::after {
        display: none;
    }
`;

export const Copyright = styled.div`
    font-size: 13px;
    color: #aaaaaa;
`;

export const RightSection = styled.div`
    display: flex;
    align-items: center;
    gap: 20px;
`;

export const SocialLink = styled.a`
    color: #333;
    font-size: 22px;
    transition: transform 0.2s, color 0.2s;

    &:hover {
        color: #FFD166;
        transform: translateY(-2px);
    }
`;