import styled from 'styled-components';

export const LayoutWrapper = styled.div`
    display: flex;
    width: 100%;
    min-height: 100vh;
    background-color: #131314;
`;

export const MainWrapper = styled.div`
    display: flex;
    flex-direction: column;
    flex: 1;
    min-width: 0;
    transition: all 0.3s cubic-bezier(0.4, 0, 0.2, 1);
`;

export const LayoutContent = styled.main`
    flex: 1;
    width: 100%;
    overflow-y: auto;
    position: relative;
`;