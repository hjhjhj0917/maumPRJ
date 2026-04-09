import styled from 'styled-components';
import { Link } from 'react-router-dom';

export const HeaderContainer = styled.header`
    display: flex;
    justify-content: space-between;
    align-items: center;
    padding: 11px 50px;
    width: 100%;
    position: fixed;
    top: 0;
    left: 0;
    z-index: 1000;
    background-color: transparent;
    box-shadow: none;              
    border-bottom: none;        
    transition: all 0.3s ease;

    @media (max-width: 768px) {
        padding: 15px 20px;
    }
`;

export const LogoContainer = styled(Link)`
    display: flex;
    align-items: center;
    text-decoration: none;
    z-index: 1001;
`;

export const LogoImage = styled.img`
    height: 46px;
    width: auto;
    object-fit: contain;
    display: block;
`;

export const MenuToggle = styled.div`
    display: none;
    flex-direction: column;
    cursor: pointer;
    gap: 5px;
    z-index: 1001;
    @media (max-width: 768px) { display: flex; }
`;

export const Bar = styled.span`
    width: 25px;
    height: 3px;
    background-color: #fff; 
    border-radius: 3px;
`;

export const NavMenu = styled.ul`
    display: flex;
    list-style: none;
    align-items: center;
    gap: 30px;

    @media (max-width: 768px) {
        display: ${props => props.$isOpen ? 'flex' : 'none'};
        position: absolute;
        top: 100%;
        left: 0;
        width: 100%;
        flex-direction: column;
        background-color: rgba(30, 31, 32, 0.9); 
        padding: 20px;
        gap: 0;
    }
`;

export const NavItem = styled.li`
    display: flex;
    align-items: center;
    @media (max-width: 768px) { width: 100%; }
`;

export const NavLink = styled(Link)`
    text-decoration: none;
    color: #333; 
    font-weight: 500;
    font-size: 13px;
    transition: color 0.3s;
    &:hover { color: #FFD166; }
`;

export const UserProfileContainer = styled.div`
    display: flex;
    align-items: center;
    background-color: #fff;
    border: 2px solid #FFD166;
    border-radius: 40px;
    height: 44px;
    padding: 4px 15px 4px 4px;
    position: relative;
    overflow: hidden;
    transition: width 0.4s cubic-bezier(0.25, 0.8, 0.25, 1);
    width: ${props => props.$isExpanded ? '305px' : '142px'};
`;

export const ProfileImg = styled.img`
    width: 34px;
    height: 34px;
    border-radius: 50%;
    object-fit: cover;
    z-index: 2;
`;

export const UserName = styled.span`
    font-size: 13px;
    font-weight: 500;
    color: #000;
    margin-left: 10px;
    opacity: ${props => props.$isExpanded ? '0' : '1'};
`;

export const ExpandedMenus = styled.div`
    display: flex;
    align-items: center;
    gap: 20px;
    opacity: ${props => props.$isExpanded ? '1' : '0'};
    position: absolute;
    left: 55px;
    pointer-events: ${props => props.$isExpanded ? 'auto' : 'none'};
    transition: opacity 0.3s;

    a, button {
        font-size: 13px;
        font-weight: 500;
        color: #000;
        text-decoration: none;
        background: none;
        border: none;
        cursor: pointer;
        &:hover { color: #FFD166; }
    }
`;

export const BtnMore = styled.button`
    background: none;
    border: none;
    font-size: 20px;
    font-weight: bold;
    cursor: pointer;
    margin-left: auto;
`;