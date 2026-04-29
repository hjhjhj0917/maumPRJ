import styled from 'styled-components';

export const DetailPageContainer = styled.div`
    padding: 90px 50px 50px 50px;
    max-width: 1200px;
    margin: 0 auto;
    width: 100%;
    min-height: 100vh;
    display: flex;
    flex-direction: column;
    gap: 30px;
    box-sizing: border-box;

    @media (max-width: 768px) {
        padding: 80px 20px 30px 20px;
    }
`;

export const HeaderSection = styled.div`
    display: flex;
    flex-direction: column;
    gap: 20px;
`;

export const TopActions = styled.div`
    display: flex;
    justify-content: space-between;
`;

export const BackButton = styled.button`
    background: none;
    border: none;
    color: #8e918f;
    font-size: 16px;
    cursor: pointer;
    display: flex;
    align-items: center;
    gap: 8px;
    padding: 0;

    &:hover {
        color: #333;
    }
`;

export const TitleDateRow = styled.div`
    display: flex;
    justify-content: space-between;
    align-items: flex-end;
    gap: 20px;

    @media (max-width: 768px) {
        flex-direction: column;
        align-items: flex-start;
        gap: 10px;
    }
`;

export const TitleText = styled.h1`
    margin: 0;
    color: #333;
    font-size: 40px;
    font-weight: 600;
    line-height: 1.3;
    word-break: keep-all;
`;

export const DateText = styled.div`
    color: #8e918f;
    font-size: 18px;
    font-weight: 400;
    padding-bottom: 5px;
`;

/* 메인 레이아웃: 좌측 본문, 우측 사이드바 */
export const MainContentWrapper = styled.div`
    display: flex;
    gap: 30px;
    align-items: flex-start; /* 양쪽 높이가 달라도 위쪽 정렬 */

    @media (max-width: 900px) {
        flex-direction: column; /* 화면이 작아지면 위아래로 배치 */
    }
`;

/* 왼쪽 본문 영역 */
export const ContentArea = styled.div`
    flex: 1; /* 남은 공간을 모두 차지 */
    background-color: #fff;
    border-radius: 28px;
    padding: 40px;
    min-height: 400px;
    box-shadow: 0 4px 20px rgba(0,0,0,0.05); /* 작성 폼보다 조금 더 부드러운 그림자 */
    width: 100%;
    box-sizing: border-box;
`;

export const ContentText = styled.div`
    color: #333;
    font-size: 17px;
    line-height: 1.8;
    white-space: pre-wrap; /* \n 줄바꿈을 화면에 그대로 렌더링해주는 핵심 속성 */
    word-break: break-all;
`;

/* 오른쪽 사이드바 영역 */
export const SidebarArea = styled.div`
    width: 320px; /* 사이드바 고정 너비 */
    flex-shrink: 0; /* 창이 좁아져도 사이드바 크기 유지 */
    background-color: #fff;
    border-radius: 28px;
    padding: 30px;
    box-shadow: 0 4px 20px rgba(0,0,0,0.05);
    display: flex;
    flex-direction: column;
    gap: 25px;
    box-sizing: border-box;

    @media (max-width: 900px) {
        width: 100%; /* 모바일에서는 100% 꽉 차게 변경 */
    }
`;

export const SidebarTitle = styled.h3`
    margin: 0;
    font-size: 18px;
    color: #333;
    display: flex;
    align-items: center;
    gap: 8px;
    padding-bottom: 15px;
    border-bottom: 1px solid #f0f0f0;

    i {
        color: #FFD166; /* 작성완료 버튼 색상 활용 */
    }
`;

export const AnalysisCard = styled.div`
    display: flex;
    flex-direction: column;
    gap: 10px;
`;

export const AnalysisLabel = styled.span`
    font-size: 14px;
    color: #8e918f;
    font-weight: 500;
`;

export const EmotionRow = styled.div`
    display: flex;
    align-items: center;
    gap: 12px;
`;

export const ColorCircle = styled.div`
    width: 24px;
    height: 24px;
    border-radius: 50%;
    background-color: ${props => props.$color};
    box-shadow: 0 2px 4px rgba(0,0,0,0.1);
`;

export const EmotionText = styled.span`
    font-size: 18px;
    font-weight: 600;
    color: #333;
`;

export const SummaryText = styled.p`
    margin: 0;
    font-size: 16px;
    line-height: 1.5;
    color: #555;
    font-style: italic;
    background-color: #fcfcfc;
    padding: 15px;
    border-radius: 12px;
    border-left: 4px solid #FFD166; /* 강조 포인트 */
`;

export const LevelText = styled.div`
    font-size: 18px;
    font-weight: 600;
    color: #e74c3c;
`;

export const LoadingText = styled.div`
    text-align: center;
    padding-top: 100px;
    font-size: 18px;
    color: #8e918f;
`;