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