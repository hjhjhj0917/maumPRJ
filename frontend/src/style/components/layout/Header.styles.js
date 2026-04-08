import styled from 'styled-components';
import { Link } from 'react-router-dom';

export const HeaderContainer = styled.header`
    display: flex;
    justify-content: space-between;
    align-items: center;
    padding: 11px 50px;
    width: 100%;
    top: 0;
    left: 0;
    z-index: 1000;
    background-color: ${props => props.$isAccountPage ? 'transparent' : 'rgba(255, 255, 255, 0.95)'};
    position: ${props => props.$isAccountPage ? 'absolute' : 'fixed'};
    box-shadow: ${props => props.$isAccountPage ? 'none' : '0 2px 10px rgba(0, 0, 0, 0.05)'};
    border-bottom: ${props => props.$isAccountPage ? 'none' : '1px solid #f0f0f0'};

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

    @media (max-width: 768px) {
        height: 45px;
    }
`;

export const MenuToggle = styled.div`
    display: none;
    flex-direction: column;
    cursor: pointer;
    gap: 5px;
    z-index: 1001;

    @media (max-width: 768px) {
        display: flex;
    }
`;

export const Bar = styled.span`
    width: 25px;
    height: 3px;
    background-color: #333;
    border-radius: 3px;
    transition: 0.3s;
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
        background-color: #fff;
        box-shadow: 0 5px 5px rgba(0,0,0,0.1);
        padding: 20px;
        gap: 0;
        align-items: flex-start;
    }
`;

export const NavItem = styled.li`
    position: relative;
    display: flex;
    align-items: center;
    flex-shrink: 0;

    @media (max-width: 768px) {
        width: 100%;
        border-bottom: 1px solid #eee;
        padding: 10px 0;

        &:last-child {
            border-bottom: none;
            margin-top: 15px;
        }
    }
`;

export const NavLink = styled(Link)`
    text-decoration: none;
    color: #555;
    font-weight: 500;
    font-size: 13px;
    padding: 10px 0;
    transition: color 0.3s;
    display: block;
    white-space: nowrap;

    &:hover {
        color: #FFD166;
    }

    @media (max-width: 768px) {
        padding: 15px 0;
        width: 100%;
    }
`;

export const BtnSignin = styled.button`
    padding: 8px 20px;
    border: 1px solid;
    border-radius: 4px;
    background-color: transparent;
    color: #333;
    font-size: 11px;
    font-weight: bold;
    cursor: pointer;
    transition: all 0.3s;
    white-space: nowrap;

    &:hover {
        background-color: #FFD166;
        color: #333;
        border-color: #FFD166;
    }

    @media (max-width: 768px) {
        width: 100%;
        text-align: center;
    }
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
    width: ${props => props.$isExpanded ? '329px' : '150px'};
    z-index: 1000;

    @media (max-width: 768px) {
        width: 100%;
    }
`;

export const ProfileImg = styled.img`
    width: 34px;
    height: 34px;
    border-radius: 50%;
    object-fit: cover;
    flex-shrink: 0;
    border: 1px solid #ddd;
    z-index: 2;
`;

export const UserName = styled.span`
    font-size: 14px;
    font-weight: 600;
    color: #000;
    white-space: nowrap;
    margin-left: 10px;
    transition: opacity 0.2s;
    opacity: ${props => props.$isExpanded ? '0' : '1'};
    pointer-events: ${props => props.$isExpanded ? 'none' : 'auto'};
`;

export const ExpandedMenus = styled.div`
    display: flex;
    align-items: center;
    gap: 20px;
    white-space: nowrap;
    opacity: ${props => props.$isExpanded ? '1' : '0'};
    position: absolute;
    left: 55px;
    pointer-events: ${props => props.$isExpanded ? 'auto' : 'none'};
    transition: opacity 0.3s;

    a, button {
        font-size: 14px;
        font-weight: 600;
        color: #000;
        text-decoration: none;
        transition: color 0.2s;
        background: none;
        border: none;
        cursor: pointer;

        &:hover {
            color: #FFD166;
        }
    }

    @media (max-width: 768px) {
        gap: 12px;
        
        a, button {
            font-size: 14px;
        }
    }
`;

export const BtnMore = styled.button`
    background: none;
    border: none;
    font-size: 20px;
    font-weight: bold;
    color: #000;
    cursor: pointer;
    margin-left: auto;
    padding: 0 5px;
    z-index: 10;
    display: flex;
    align-items: center;
    justify-content: center;
`;