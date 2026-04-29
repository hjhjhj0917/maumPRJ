import { useState } from 'react';
import { useNavigate } from 'react-router-dom';
import { updateProfileImg } from '../../../api/authApi'; // API 함수 임포트

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
            // fetch 대신 apiClient 기반 함수 호출
            const res = await updateProfileImg(selectedZodiac.img);

            if (res.result === 1) {
                showAlert("프로필 설정 완료", "프로필 설정이 완료되었습니다.", () => navigate('/account/login'));
            } else {
                showAlert("오류", res.msg || "프로필 설정 중 오류가 발생했습니다.");
            }
        } catch (error) {
            const errorMsg = error.response?.data?.msg || "서버 통신 중 오류가 발생했습니다.";
            showAlert("시스템 오류", errorMsg);
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