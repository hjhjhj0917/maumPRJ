import styled from 'styled-components';

export const LayoutWrapper = styled.div`
    display: flex;
    width: 100vw;
    height: 100vh;
    overflow: hidden;
    background-color: #f4f7f9;
`;

export const MainWrapper = styled.div`
    display: flex;
    flex-direction: column;
    flex: 1;
    height: 100%;
    min-width: 0;
    position: relative;
    background-color: #f4f7f9;
`;

export const LayoutContent = styled.main`
    flex: 1;
    width: 100%;
    overflow-y: auto;
    overflow-x: hidden;
    position: relative;
`;