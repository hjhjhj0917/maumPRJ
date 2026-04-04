import React, { useState } from 'react';
import { useNavigate } from 'react-router-dom';
import Header from '../../components/common/Header';
import CustomModal from '../../components/common/CustomModal';
import ZodiacItem from '../../components/account/ZodiacItem';
import './Profile.css';

const zodiacList = [
    { id: 'mouse', name: '쥐', img: '/images/account/profile1.png', file: 'mouse.png' },
    { id: 'cow', name: '소', img: '/images/account/profile2.png', file: 'cow.png' },
    { id: 'tiger', name: '호랑이', img: '/images/account/profile3.png', file: 'tiger.png' },
    { id: 'rabbit', name: '토끼', img: '/images/account/profile4.png', file: 'rabbit.png' },
    { id: 'dragon', name: '용', img: '/images/account/profile5.png', file: 'dragon.png' },
    { id: 'snake', name: '뱀', img: '/images/account/profile6.png', file: 'snake.png' },
    { id: 'horse', name: '말', img: '/images/account/profile7.png', file: 'horse.png' },
    { id: 'sheep', name: '양', img: '/images/account/profile8.png', file: 'sheep.png' },
    { id: 'monkey', name: '원숭이', img: '/images/account/profile9.png', file: 'monkey.png' },
    { id: 'chicken', name: '닭', img: '/images/account/profile10.png', file: 'chicken.png' },
    { id: 'dog', name: '개', img: '/images/account/profile11.png', file: 'dog.png' },
    { id: 'pig', name: '돼지', img: '/images/account/profile12.png', file: 'pig.png' },
];

const Profile = () => {
    const navigate = useNavigate();
    const [selectedZodiac, setSelectedZodiac] = useState(zodiacList[6]);
    const [modal, setModal] = useState({ show: false, title: '', message: '', onConfirm: null });

    const showAlert = (title, message, onConfirm = null) => {
        setModal({ show: true, title, message, onConfirm });
    };

    const handleSubmit = async (e) => {
        e.preventDefault();

        try {
            const params = new URLSearchParams({ profileImage: selectedZodiac.img });

            const response = await fetch('/api/account/updateProfileImg', {
                method: 'POST',
                headers: { 'Content-Type': 'application/x-www-form-urlencoded' },
                body: params
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

    return (
        <div className="profile-wrapper">
            <Header />
            <CustomModal
                isOpen={modal.show}
                title={modal.title}
                message={modal.message}
                isConfirm={false}
                onCancel={() => setModal({ show: false, title: '', message: '', onConfirm: null })}
                onConfirm={() => {
                    setModal({ show: false, title: '', message: '', onConfirm: null });
                    if (modal.onConfirm) modal.onConfirm();
                }}
            />

            <div className="profile-container fade-in">
                <form onSubmit={handleSubmit} className="profile-form">
                    <div className="profile-layout">
                        <div className="profile-preview-area">
                            <div className="preview-circle">
                                <img src={selectedZodiac.img} alt="프로필 프리뷰" />
                            </div>
                        </div>

                        <div className="profile-selection-area">
                            <div className="zodiac-grid">
                                {zodiacList.map((zodiac) => (
                                    <ZodiacItem
                                        key={zodiac.id}
                                        data={zodiac}
                                        isSelected={selectedZodiac.id === zodiac.id}
                                        onClick={setSelectedZodiac}
                                    />
                                ))}
                            </div>
                        </div>
                    </div>

                    <div className="button-area">
                        <button type="submit" className="btn-save">완료</button>
                    </div>
                </form>
            </div>
        </div>
    );
};

export default Profile;