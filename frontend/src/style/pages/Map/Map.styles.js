import styled from 'styled-components';

export const Container = styled.div`
    width: 100%;
    height: 100vh;
    box-sizing: border-box;
    background-color: #f8f9fa;
    display: flex;
    flex-direction: column;
    padding: 20px;
`;

export const MapWrapper = styled.div`
    width: 100%;
    flex: 1;
    border-radius: 12px;
    overflow: hidden;
    box-shadow: 0 4px 12px rgba(0, 0, 0, 0.1);
    position: relative;

    &::after {
        content: '';
        position: absolute;
        top: 0;
        left: 0;
        width: 100%;
        height: 80px;
        background: linear-gradient(to bottom, rgba(248, 249, 250, 1) 0%, rgba(248, 249, 250, 0) 100%);
        pointer-events: none;
        z-index: 2;
    }
`;

export const LoadingErrorText = styled.div`
    display: flex;
    justify-content: center;
    align-items: center;
    height: 100vh;
    font-size: 1.2rem;
    color: ${(props) => (props.$isError ? '#d9534f' : '#666')};
`;

export const MyLocationButton = styled.button`
    position: absolute;
    bottom: 30px;
    right: 30px;
    z-index: 10;
    width: 50px;
    height: 50px;
    display: flex;
    justify-content: center;
    align-items: center;
    font-size: 22px;
    color: #333;
    background-color: white;
    border: 1px solid #ccc;
    border-radius: 50%;
    box-shadow: 0 2px 6px rgba(0,0,0,0.2);
    cursor: pointer;

    &:hover {
        background-color: #f0f0f0;
    }
`;

export const SearchContainer = styled.form`
    position: absolute;
    top: 50px;
    left: 50%;
    transform: translateX(-50%);
    z-index: 10;
    display: flex;
    width: 90%;
    max-width: 600px;
    background-color: white;
    border-radius: 25px;
    box-shadow: 0 2px 6px rgba(0,0,0,0.2);
    overflow: hidden;
`;

export const SearchInput = styled.input`
    flex: 1;
    border: none;
    padding: 15px 20px;
    font-size: 16px;
    outline: none;
    background: transparent;
`;

export const SearchButton = styled.button`
    border: none;
    background-color: transparent;
    padding: 0 20px;
    cursor: pointer;
    color: #666;
    font-size: 18px;

    &:hover {
        color: #333;
    }
`;

export const OverlayContainer = styled.div`
    position: absolute;
    bottom: 55px;
    left: 50%;
    transform: translateX(-50%);
    background-color: white;
    border-radius: 100px;
    border: 1px solid #ccc;
    box-shadow: 0px 2px 10px rgba(0, 0, 0, 0.2);
    padding: 30px;
    padding-left: 40px;
    min-width: 280px;
    width: max-content;
    height: 140px;
    max-width: 450px;
    z-index: 5;
    display: flex;
    flex-direction: row;
    align-items: center;
    gap: 20px;
`;

export const OverlayLeftSection = styled.div`
    flex: 1;
    display: flex;
    flex-direction: column;
`;

export const OverlayHeader = styled.div`
    display: flex;
    justify-content: flex-start;
    align-items: flex-start;
    margin-bottom: 10px;
    padding-bottom: 8px;
`;

export const OverlayTitle = styled.h3`
    margin: 0;
    font-size: 16px;
    font-weight: bold;
    color: #333;
    line-height: 1.3;
    word-break: keep-all;
`;

export const CategoryBadge = styled.span`
    display: inline-block;
    padding: 3px 6px;
    background-color: #f1f3f5;
    color: #495057;
    font-size: 11px;
    border-radius: 4px;
    margin-top: 4px;
`;

export const OverlayBody = styled.div`
    display: flex;
    flex-direction: column;
    gap: 8px;
`;

export const InfoText = styled.div`
    display: flex;
    align-items: flex-start;
    gap: 6px;
    margin: 0;
    font-size: 13px;
    color: #666;
    line-height: 1.4;
    word-break: keep-all;

    i {
        margin-top: 3px;
        color: #adb5bd;
        min-width: 14px;
        text-align: center;
    }

    span, a {
        flex: 1;
    }

    a {
        color: #4A90E2;
        text-decoration: none;
        word-break: break-all;
        &:hover { text-decoration: underline; }
    }
`;

export const OverlayRightSection = styled.div`
    width: auto;
    display: flex;
    justify-content: center;
    align-items: center;
`;

export const CloseButton = styled.button`
    background: none;
    border: none;
    font-size: 18px;
    cursor: pointer;
    color: #999;
    padding: 0;
    position: absolute;
    top: 10px;
    right: 10px;

    &:hover {
        color: #555;
    }
`;

export const RouteButtonRound = styled.a`
    display: flex;
    justify-content: center;
    align-items: center;
    width: 100px;
    height: 100px;
    border-radius: 50%;
    background-color: #FFC130;
    color: black;
    text-decoration: none;
    box-shadow: 0 2px 6px rgba(0,0,0,0.2);
    font-size: 20px;
    transition: all 0.2s;

    &:hover {
        background-color: #e6ae2b;
        transform: translateY(-2px);
    }
    
    i {
        width: 20px;
    }
`;