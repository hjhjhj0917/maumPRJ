import { useState } from 'react';
import { useNavigate } from 'react-router-dom';

export const useProfileForm = (initialZodiac) => {
    const navigate = useNavigate();
    const [selectedZodiac, setSelectedZodiac] = useState(initialZodiac);
    const [modal, setModal] = useState({ show: false, title: '', message: '', onConfirm: null });

    const showAlert = (title, message, onConfirm = null) => {
        setModal({ show: true, title, message, onConfirm });
    };

    const handleModalClose = () => {
        setModal({ show: false, title: '', message: '', onConfirm: null });
    };

    const handleModalConfirm = () => {
        if (modal.onConfirm) modal.onConfirm();
        handleModalClose();
    };

    const handleSubmit = async (e) => {
        e.preventDefault();

        try {
            const params = new URLSearchParams({ profileImage: selectedZodiac.img });

            const response = await fetch('/api/account/updateProfileImg', {
                method: 'POST',
                headers: { 'Content-Type': 'application/x-www-form-urlencoded' },
                body: params,
                credentials: 'include'
            });
            const json = await response.json();

            if (json.result === 1) {
                showAlert("프로필 설정 완료", "프로필 설정이 완료되었습니다.", () => navigate('/account/login'));
            } else {
                showAlert("오류", json.msg || "프로필 설정 중 오류가 발생했습니다.");
            }
        } catch (error) {
            showAlert("시스템 오류", "서버 통신 중 오류가 발생했습니다.");
        }
    };

    return {
        selectedZodiac,
        setSelectedZodiac,
        modal,
        setModal,
        handleSubmit,
        handleModalClose,
        handleModalConfirm
    };
};