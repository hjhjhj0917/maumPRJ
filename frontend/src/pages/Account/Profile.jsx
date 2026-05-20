import React, { useState, useEffect } from 'react';
import * as S from '../../style/pages/Account/Profile.styles';
import { useNavigate } from 'react-router-dom';

const ProfilePage = () => {
    const navigate = useNavigate();
    const [userInfo, setUserInfo] = useState({
        userId: '',
        userName: '',
        email: '',
        birthDate: '',
        addr: '',
        detailAddr: '',
        profileImgUrl: '',
        password: ''
    });

    // 1. 페이지 로드 시 기존 회원 정보 불러오기 (API 연동 필요)
    useEffect(() => {
        // fetch('/api/v1/user/info').then(...)
        // 예시 데이터입니다.
        setUserInfo({
            userId: 'user123',
            userName: '양준모',
            email: 'user@example.com',
            birthDate: '1998-05-20',
            addr: '서울시 강서구',
            detailAddr: '어느 아파트 101호',
            profileImgUrl: 'https://example.com/profile.jpg',
            password: ''
        });
    }, []);

    const handleChange = (e) => {
        const { name, value } = e.target;
        setUserInfo(prev => ({ ...prev, [name]: value }));
    };

    const handleUpdate = () => {
        if (window.confirm('정보를 수정하시겠습니까?')) {
            console.log('수정할 데이터:', userInfo);
            // axios.post('/api/v1/user/update', userInfo).then(...)
            alert('수정이 완료되었습니다.');
        }
    };

    const handleWithdrawal = () => {
        if (window.confirm('정말 탈퇴하시겠습니까? 이 작업은 되돌릴 수 없습니다.')) {
            // axios.post('/api/v1/user/delete').then(...)
            alert('탈퇴 처리되었습니다.');
            navigate('/');
        }
    };

    return (
        <S.Container>
            <S.Title>프로필 설정</S.Title>
            <S.ProfileCard>
                <S.Section>
                    <S.Label>프로필 이미지 URL</S.Label>
                    <S.Input name="profileImgUrl" value={userInfo.profileImgUrl} onChange={handleChange} />
                </S.Section>

                <S.Row>
                    <S.Section>
                        <S.Label>아이디 (수정불가)</S.Label>
                        <S.Input value={userInfo.userId} disabled />
                    </S.Section>
                    <S.Section>
                        <S.Label>이메일 (수정불가)</S.Label>
                        <S.Input value={userInfo.email} disabled />
                    </S.Section>
                </S.Row>

                <S.Section>
                    <S.Label>이름</S.Label>
                    <S.Input name="userName" value={userInfo.userName} onChange={handleChange} />
                </S.Section>

                <S.Section>
                    <S.Label>생년월일</S.Label>
                    <S.Input type="date" name="birthDate" value={userInfo.birthDate} onChange={handleChange} />
                </S.Section>

                <S.Section>
                    <S.Label>주소</S.Label>
                    <S.Input name="addr" value={userInfo.addr} onChange={handleChange} />
                    <S.Input name="detailAddr" value={userInfo.detailAddr} onChange={handleChange} placeholder="상세주소" />
                </S.Section>

                <S.Section>
                    <S.Label>비밀번호 변경</S.Label>
                    <S.Input type="password" name="password" placeholder="비밀번호 변경 시에만 입력하세요" onChange={handleChange} />
                </S.Section>

                <S.ButtonContainer>
                    <S.SaveButton onClick={handleUpdate}>정보 수정 완료</S.SaveButton>
                    <S.DeleteButton onClick={handleWithdrawal}>회원 탈퇴</S.DeleteButton>
                </S.ButtonContainer>
            </S.ProfileCard>
        </S.Container>
    );
};

export default ProfilePage;