import { useState, useEffect } from 'react';
import { useNavigate } from 'react-router-dom';
import {
    getUserStatus,
    updateProfileImg,
    checkEmailExists,
    verifyEmailCode,
    verifyCurrentPassword,
    updateAccount,
    sendWithdrawEmailCode,
    deleteUser
} from '../../api/authApi';

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
        profileImgUrl: characters[0]
    });

    const [isModalOpen, setIsModalOpen] = useState(false);
    const [selectedCharacterUrl, setSelectedCharacterUrl] = useState('');

    const [isDropdownOpen, setIsDropdownOpen] = useState(false);
    const [activeModalType, setActiveModalType] = useState(null);
    const [withdrawStep, setWithdrawStep] = useState(1);

    const [editForm, setEditForm] = useState({
        currentPassword: '',
        newPassword: '',
        newEmail: '',
        emailCode: '',
        newAddr: '',
        newDetailAddr: ''
    });

    const [withdrawForm, setWithdrawForm] = useState({
        password: '',
        emailCode: '',
        confirmUserId: ''
    });

    const [verifyState, setVerifyState] = useState({
        isPasswordVerified: false,
        isEmailCodeSent: false,
        isEmailVerified: false,
        isWithdrawEmailCodeSent: false,
        isWithdrawEmailVerified: false
    });

    const currentColor = characterData.find(c => c.url === userInfo.profileImgUrl)?.color || '#7b83c7';

    useEffect(() => {
        const fetchUserInfo = async () => {
            try {
                const res = await getUserStatus();
                if (res && res.data) {
                    setUserInfo({
                        userId: res.data.userId || '',
                        userName: res.data.userName || '',
                        email: res.data.email || '',
                        birthDate: res.data.birthDate || '',
                        addr: res.data.addr || '',
                        detailAddr: res.data.detailAddr || '',
                        profileImgUrl: res.data.profileImgUrl || characters[0]
                    });
                }
            } catch (error) {
                alert(error.response?.data?.message || "회원 정보를 불러오는데 실패했습니다.");
                navigate('/');
            }
        };

        fetchUserInfo();

        const script = document.createElement('script');
        script.src = '//t1.daumcdn.net/mapjsapi/bundle/postcode/prod/postcode.v2.js';
        script.async = true;
        document.body.appendChild(script);

        return () => {
            document.body.removeChild(script);
        };
    }, [navigate]);

    const isProfileModified =
        (verifyState.isPasswordVerified && editForm.newPassword.trim() !== '') ||
        (verifyState.isEmailVerified && editForm.newEmail.trim() !== '' && editForm.newEmail !== userInfo.email) ||
        ((editForm.newAddr || '').trim() !== (userInfo.addr || '').trim()) ||
        ((editForm.newDetailAddr || '').trim() !== (userInfo.detailAddr || '').trim());

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

    const toggleDropdown = () => {
        setIsDropdownOpen(prev => !prev);
    };

    const openActionModal = (type) => {
        if (type === 'edit') {
            setEditForm({
                currentPassword: '',
                newPassword: '',
                newEmail: '',
                emailCode: '',
                newAddr: userInfo.addr || '',
                newDetailAddr: userInfo.detailAddr || ''
            });
            setVerifyState(prev => ({
                ...prev,
                isPasswordVerified: false,
                isEmailCodeSent: false,
                isEmailVerified: false
            }));
        } else if (type === 'withdraw') {
            setWithdrawStep(1);
            setWithdrawForm({
                password: '',
                emailCode: '',
                confirmUserId: ''
            });
            setVerifyState(prev => ({
                ...prev,
                isWithdrawEmailCodeSent: false,
                isWithdrawEmailVerified: false
            }));
        }
        setActiveModalType(type);
        setIsDropdownOpen(false);
    };

    const closeActionModal = () => {
        setActiveModalType(null);
    };

    const handleEditChange = (e) => {
        const { name, value } = e.target;
        setEditForm(prev => ({ ...prev, [name]: value }));
    };

    const handleWithdrawChange = (e) => {
        const { name, value } = e.target;
        setWithdrawForm(prev => ({ ...prev, [name]: value }));
    };

    const verifyCurrentPasswordAction = async () => {
        if (!editForm.currentPassword) return;
        try {
            const res = await verifyCurrentPassword(editForm.currentPassword);
            if (res.data && res.data.result === 1) {
                setVerifyState(prev => ({ ...prev, isPasswordVerified: true }));
                alert('비밀번호가 확인되었습니다.');
            } else {
                alert(res.data?.msg || '비밀번호가 일치하지 않습니다.');
            }
        } catch (error) {
            alert('인증 중 오류가 발생했습니다.');
        }
    };

    const sendEmailCodeAction = async () => {
        if (!editForm.newEmail) return;
        try {
            const res = await checkEmailExists(editForm.newEmail);
            if (res && res.exists === false) {
                setVerifyState(prev => ({ ...prev, isEmailCodeSent: true }));
                alert('인증 코드가 발송되었습니다.');
            } else {
                alert('이미 사용중인 이메일입니다.');
            }
        } catch (error) {
            alert('인증 코드 발송 중 오류가 발생했습니다.');
        }
    };

    const verifyEmailCodeAction = async () => {
        if (!editForm.emailCode) return;
        try {
            const res = await verifyEmailCode(editForm.newEmail, editForm.emailCode);
            if (res && res.result === 1) {
                setVerifyState(prev => ({ ...prev, isEmailVerified: true }));
                alert('이메일 인증이 완료되었습니다.');
            } else {
                alert(res?.msg || '인증 코드가 일치하지 않습니다.');
            }
        } catch (error) {
            alert('이메일 인증 중 오류가 발생했습니다.');
        }
    };

    const searchAddressAction = () => {
        if (window.daum && window.daum.Postcode) {
            new window.daum.Postcode({
                oncomplete: function (data) {
                    setEditForm(prev => ({ ...prev, newAddr: data.address }));
                }
            }).open();
        }
    };

    const updateAccountAction = async () => {
        try {
            const payload = {
                password: verifyState.isPasswordVerified ? editForm.newPassword : '',
                email: verifyState.isEmailVerified ? editForm.newEmail : '',
                addr: editForm.newAddr,
                detailAddr: editForm.newDetailAddr
            };

            const res = await updateAccount(payload);
            if (res.data && res.data.result === 1) {
                alert('프로필이 성공적으로 수정되었습니다.');
                window.location.reload();
            } else {
                alert(res.data?.msg || '수정에 실패했습니다.');
            }
        } catch (error) {
            alert('수정 처리 중 오류가 발생했습니다.');
        }
    };

    const verifyWithdrawPasswordAction = async () => {
        if (!withdrawForm.password) return;
        try {
            const res = await verifyCurrentPassword(withdrawForm.password);
            if (res.data && res.data.result === 1) {
                setWithdrawStep(2);
            } else {
                alert(res.data?.msg || '비밀번호가 일치하지 않습니다.');
            }
        } catch (error) {
            alert('인증 중 오류가 발생했습니다.');
        }
    };

    const sendWithdrawEmailCodeAction = async () => {
        try {
            const res = await sendWithdrawEmailCode(userInfo.email);
            if (res.data && res.data.result === 1) {
                setVerifyState(prev => ({ ...prev, isWithdrawEmailCodeSent: true }));
                alert('인증 코드가 발송되었습니다.');
            } else {
                alert(res.data?.msg || '코드 발송에 실패했습니다.');
            }
        } catch (error) {
            alert('인증 코드 발송 중 오류가 발생했습니다.');
        }
    };

    const verifyWithdrawEmailCodeAction = async () => {
        if (!withdrawForm.emailCode) return;
        try {
            const res = await verifyEmailCode(userInfo.email, withdrawForm.emailCode);
            if (res && res.result === 1) {
                setVerifyState(prev => ({ ...prev, isWithdrawEmailVerified: true }));
                alert('이메일 인증이 완료되었습니다.');
                setWithdrawStep(3);
            } else {
                alert(res?.msg || '인증 코드가 일치하지 않습니다.');
            }
        } catch (error) {
            alert('이메일 인증 중 오류가 발생했습니다.');
        }
    };

    const processWithdrawalAction = async () => {
        if (withdrawForm.confirmUserId !== userInfo.userId) {
            return alert('아이디가 일치하지 않습니다.');
        }
        try {
            await deleteUser();
            alert('회원 탈퇴가 완료되었습니다.');
            navigate('/');
        } catch (error) {
            alert('탈퇴 처리에 실패했습니다.');
        }
    };

    return {
        userInfo,
        characters,
        isModalOpen,
        selectedCharacterUrl,
        currentColor,
        isDropdownOpen,
        activeModalType,
        withdrawStep,
        editForm,
        withdrawForm,
        verifyState,
        isProfileModified,
        openModal,
        selectCharacter,
        closeModal,
        cancelModal,
        toggleDropdown,
        openActionModal,
        closeActionModal,
        handleEditChange,
        handleWithdrawChange,
        verifyCurrentPasswordAction,
        sendEmailCodeAction,
        verifyEmailCodeAction,
        searchAddressAction,
        updateAccountAction,
        verifyWithdrawPasswordAction,
        sendWithdrawEmailCodeAction,
        verifyWithdrawEmailCodeAction,
        processWithdrawalAction
    };
};