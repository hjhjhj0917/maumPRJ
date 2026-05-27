import styled from 'styled-components';

export const SidebarWrapper = styled.aside`
    width: ${props => (props.$isOpen ? '240px' : '68px')};
    height: 100vh;
    background-color: #333;
    display: flex;
    flex-direction: column;
    flex-shrink: 0;
    transition: width 0.3s cubic-bezier(0.4, 0, 0.2, 1);
    z-index: 100;
    //border-radius: 0 10px 10px 0;
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
    color: #ffffff;
    border-radius: 50%;
    cursor: pointer;
    font-size: 20px;
    display: flex;
    justify-content: center;
    align-items: center;
    margin: ${props => props.$isOpen ? '0' : '0 auto'};
    transition: color 0.2s;

    &:hover {
        color: #FFD166;
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
    border-radius: ${props => props.$isOpen ? '22px' : '50%'};
    border: none;
    background-color: #FFD166;
    color: #333;
    font-weight: 600;
    cursor: ${props => props.$isOpen ? 'pointer' : 'default'};
    pointer-events: ${props => props.$isOpen ? 'auto' : 'none'};
    overflow: hidden;
    white-space: nowrap;
    box-sizing: border-box;
    transition: all 0.2s ease;

    &:hover {
        background-color: #e5bc5c;
        color: #111;
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
    color: ${props => props.$active ? '#FFD166' : '#ffffff'};
    background-color: transparent;
    white-space: nowrap;
    overflow: hidden;
    transition: color 0.2s;

    &:hover {
        color: #FFD166;
        span, i {
            color: #FFD166;
        }
    }

    span {
        color: ${props => props.$active ? '#FFD166' : '#ffffff'};
        font-size: 15px;
        transition: color 0.2s;
    }

    i {
        color: ${props => props.$active ? '#FFD166' : '#ffffff'};
        font-size: 15px;
        min-width: 20px;
        text-align: center;
        transition: color 0.2s;
    }
`;

export const BottomSection = styled.div`
    padding: 12px;
`;

export const RecentDiarySection = styled.div`
    padding: 12px 4px;
    margin-top: 30px;
`;

export const RecentDiaryTitle = styled.div`
    font-size: 11px;
    color: #aaaaaa;
    margin-bottom: 8px;
    padding-left: 12px;
    font-weight: 600;
`;

export const RecentDiaryItem = styled.div`
    cursor: pointer;
    padding: 10px 12px;
    margin: 2px 0;
    font-size: 13.5px;
    color: #ffffff;
    border-radius: 24px;
    white-space: nowrap;
    overflow: hidden;
    text-overflow: ellipsis;
    transition: color 0.2s;

    &:hover {
        color: #FFD166;
    }
`;