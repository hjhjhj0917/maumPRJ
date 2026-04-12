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
    display: flex;
    justify-content: center;
    align-items: center;
    margin: ${props => props.$isOpen ? '0' : '0 auto'};

    &:hover {
        background-color: #333537;
    }
`;

export const NewPostBtn = styled.button`
    display: flex;
    align-items: center;
    justify-content: ${props => props.$isOpen ? 'flex-start' : 'center'};
    gap: 12px;
    width: ${props => props.$isOpen ? 'fit-content' : '44px'};
    height: 44px;
    padding: 0 ${props => props.$isOpen ? '16px' : '0'};
    margin: ${props => props.$isOpen ? '0' : '0 auto'};
    border-radius: ${props => props.$isOpen ? '24px' : '50%'};
    border: none;
    background-color: #333537;
    color: ${props => props.$isOpen ? '#e3e3e3' : '#888'};
    cursor: ${props => props.$isOpen ? 'pointer' : 'default'};
    pointer-events: ${props => props.$isOpen ? 'auto' : 'none'};
    overflow: hidden;
    white-space: nowrap;
    box-sizing: border-box;

    &:hover {
        background-color: ${props => props.$isOpen ? '#3f4143' : '#333537'};
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
    justify-content: ${props => props.$isOpen ? 'flex-start' : 'center'};
    gap: 12px;
    width: ${props => props.$isOpen ? '100%' : '44px'};
    height: 44px;
    padding: 0 ${props => props.$isOpen ? '16px' : '0'};
    margin: 0 auto;
    border-radius: ${props => props.$isOpen ? '24px' : '50%'};
    box-sizing: border-box;
    cursor: ${props => props.$isOpen ? 'pointer' : 'default'};
    pointer-events: ${props => props.$isOpen ? 'auto' : 'none'};
    color: ${props => props.$active ? '#ffffff' : '#e3e3e3'};
    background-color: ${props => props.$active ? '#333537' : 'transparent'};
    white-space: nowrap;
    overflow: hidden;

    &:hover {
        background-color: ${props => props.$isOpen ? '#333537' : 'transparent'};
    }

    span {
        font-size: 15px;
    }

    i {
        color: ${props => props.$isOpen ? '#FFD166' : '#888'};
        font-size: 15px;
        min-width: 20px;
        text-align: center;
    }
`;

export const BottomSection = styled.div`
    padding: 12px;
    border-top: 1px solid #333537;
`;