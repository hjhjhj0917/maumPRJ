import styled from 'styled-components';

export const SidebarWrapper = styled.aside`
    width: ${props => (props.$isOpen ? '280px' : '68px')};
    height: 100vh;
    background-color: #333;
    display: flex;
    flex-direction: column;
    flex-shrink: 0;
    transition: width 0.3s cubic-bezier(0.4, 0, 0.2, 1);
    z-index: 100;
`;

export const TopSection = styled.div`
    padding: 12px;
    display: flex;
    flex-direction: column;
    gap: 20px;
`;

export const IconButton = styled.button`
    width: 44px;
    height: 44px;
    border: none;
    background: transparent;
    color: #e3e3e3;
    border-radius: 50%;
    cursor: pointer;
    font-size: 20px;

    &:hover {
        background-color: #333537;
    }
`;

export const NewPostBtn = styled.button`
    display: flex;
    align-items: center;
    gap: 12px;
    padding: 12px ${props => props.$isOpen ? '16px' : '12px'};
    border-radius: 24px;
    border: none;
    background-color: #333537;
    color: #e3e3e3;
    cursor: pointer;
    overflow: hidden;
    white-space: nowrap;
    width: fit-content;

    &:hover {
        background-color: #3f4143;
    }
`;

export const NavSection = styled.nav`
    flex: 1;
    padding: 12px;
    display: flex;
    flex-direction: column;
    gap: 4px;
`;

export const NavItem = styled.div`
    display: flex;
    align-items: center;
    gap: 12px;
    padding: 12px 16px;
    border-radius: 24px;
    cursor: pointer;
    color: ${props => props.$active ? '#ffffff' : '#e3e3e3'};
    background-color: ${props => props.$active ? '#333537' : 'transparent'};
    white-space: nowrap;
    overflow: hidden;

    &:hover {
        background-color: #333537;
    }
    
    span {
        font-size: 15px;
    }

    i {
        color: #FFD166;
        font-size: 15px;
        min-width: 20px;
        text-align: center;
    }
`;

export const BottomSection = styled.div`
    padding: 12px;
    border-top: 1px solid #333537;
`;