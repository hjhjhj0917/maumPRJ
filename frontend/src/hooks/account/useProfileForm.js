import { useState, useEffect } from 'react';
import { useNavigate } from 'react-router-dom';
import apiClient from '../../api/apiClient';
import { updateProfileImg } from '../../api/authApi';

const characterData = [
    { url: '/images/account/profile1.png', color: '#D4C4FB' },
    { url: '/images/account/profile2.png', color: '#B2EBF2' },
    { url: '/images/account/profile3.png', color: '#B3E5FC' },
    { url: '/images/account/profile4.png', color: '#FFD1DC' },
    { url: '/images/account/profile5.png', color: '#C8E6C9' },
    { url: '/images/account/profile6.png', color: '#E1BEE7' },
    { url: '/images/account/profile7.png', color: '#FFF9C4' },
    { url: '/images/account/profile8.png', color: '#FFE0B2' },
    { url: '/images/account/profile9.png', color: '#BBDEFB' },
    { url: '/images/account/profile10.png', color: '#F8BBD0' },
    { url: '/images/account/profile11.png', color: '#BDE0FE' },
    { url: '/images/account/profile12.png', color: '#B2DFDB' }
];

const characters = characterData.map(c => c.url);

export const useProfile = () => {
    const navigate = useNavigate();

    const [userInfo, setUserInfo] = useState({
        userId: '',
        userName: '',
        email: '',
        birthDate: '',
        addr: '',
        detailAddr: '',
        profileImgUrl: characters[0],
        password: ''
    });

    const [isModalOpen, setIsModalOpen] = useState(false);
    const [selectedCharacterUrl, setSelectedCharacterUrl] = useState('');

    const currentColor = characterData.find(c => c.url === userInfo.profileImgUrl)?.color || '#7b83c7';

    useEffect(() => {
        const fetchUserInfo = async () => {
            try {
                const res = await apiClient.post('/account/userInfo');
                if (res && res.data) {
                    setUserInfo({
                        userId: res.data.userId || '',
                        userName: res.data.userName || '',
                        email: res.data.email || '',
                        birthDate: res.data.birthDate || '',
                        addr: res.data.addr || '',
                        detailAddr: res.data.detailAddr || '',
                        profileImgUrl: res.data.profileImgUrl || characters[0],
                        password: ''
                    });
                }
            } catch (error) {
                alert(error.response?.data?.message || "회원 정보를 불러오는데 실패했습니다.");
                navigate('/');
            }
        };

        fetchUserInfo();
    }, [navigate]);

    const handleChange = (e) => {
        const { name, value } = e.target;
        setUserInfo(prev => ({ ...prev, [name]: value }));
    };

    const openModal = () => {
        setSelectedCharacterUrl(userInfo.profileImgUrl);
        setIsModalOpen(true);
    };

    const selectCharacter = (url) => {
        setSelectedCharacterUrl(url);
    };

    const closeModal = async () => {
        try {
            const res = await updateProfileImg(selectedCharacterUrl);

            if (res.data && res.data.result === 1) {
                setUserInfo(prev => ({ ...prev, profileImgUrl: selectedCharacterUrl }));
                setIsModalOpen(false);
                alert("프로필 이미지가 변경되었습니다.");
                window.location.reload();
            } else {
                alert(res?.msg || "프로필 이미지 변경에 실패했습니다.");
            }
        } catch (error) {
            alert("서버 연결에 실패했습니다.");
        }
    };

    const cancelModal = () => {
        setIsModalOpen(false);
    };

    const handleUpdate = async () => {
        if (!userInfo.userName.trim()) return alert('이름을 입력해주세요.');
        if (!userInfo.birthDate) return alert('생년월일을 입력해주세요.');
        if (!userInfo.addr.trim()) return alert('주소를 입력해주세요.');

        if (window.confirm('회원 정보를 수정하시겠습니까?')) {
            try {
                const res = await apiClient.post('/account/updateUserInfo', userInfo);
                if (res && res.data) {
                    alert('정보가 성공적으로 수정되었습니다.');
                    setUserInfo(prev => ({ ...prev, password: '' }));
                }
            } catch (error) {
                alert(error.response?.data?.message || "정보 수정에 실패했습니다.");
            }
        }
    };

    const handleWithdrawal = async () => {
        if (window.confirm('정말 탈퇴하시겠습니까? 이 작업은 되돌릴 수 없으며 모든 데이터가 삭제됩니다.')) {
            try {
                await apiClient.post('/account/deleteUser');
                alert('회원 탈퇴가 완료되었습니다.');
                navigate('/');
            } catch (error) {
                alert(error.response?.data?.message || "탈퇴 처리에 실패했습니다.");
            }
        }
    };

    return {
        userInfo,
        characters,
        isModalOpen,
        selectedCharacterUrl,
        currentColor,
        handleChange,
        openModal,
        selectCharacter,
        closeModal,
        cancelModal,
        handleUpdate,
        handleWithdrawal
    };
};