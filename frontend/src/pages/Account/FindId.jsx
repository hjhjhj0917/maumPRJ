import React from 'react';
import { Link } from 'react-router-dom';
import InputField from '../../components/common/InputField';
import CustomModal from '../../components/common/CustomModal';
import { useFindIdForm } from '../../hooks/account/useFindIdForm';
import * as S from '../../style/pages/Account/FindId.styles';

const FindId = () => {
    const {
        step,
        formData,
        handleChange,
        messages,
        foundId,
        modal,
        handleModalConfirm,
        handleModalCancel,
        handleStep1Submit,
        handleStep2Submit,
        handleResend,
        navigate
    } = useFindIdForm();

    return (
        <S.FindIdWrapper>
            <CustomModal
                isOpen={modal.show}
                title={modal.title}
                message={modal.message}
                isConfirm={false}
                onCancel={handleModalCancel}
                onConfirm={handleModalConfirm}
            />

            <S.Container>
                <S.FindIdCard>
                    <S.Title>아이디 찾기</S.Title>

                    <S.StepContainer>
                        {step === 1 && (
                            <S.FadeInForm onSubmit={handleStep1Submit}>
                                <InputField label="E-mail" name="userEmail" value={formData.userEmail}
                                            onChange={handleChange} errorMsg={messages.userEmailMsg}
                                            placeholder="이메일을 입력하세요." />
                                <InputField label="Name" name="userName" value={formData.userName}
                                            onChange={handleChange} errorMsg={messages.userNameMsg}
                                            placeholder="이름을 입력하세요." />
                                <S.BtnConfirm type="submit">확인</S.BtnConfirm>
                            </S.FadeInForm>
                        )}

                        {step === 2 && (
                            <S.FadeInForm onSubmit={handleStep2Submit}>
                                <InputField label="Code" name="code" value={formData.code} onChange={handleChange}
                                            errorMsg={messages.codeMsg} placeholder="인증번호를 입력하세요."
                                            actionBtn={{ text: '재전송', onClick: handleResend }} />
                                <S.BtnConfirm type="submit">확인</S.BtnConfirm>
                            </S.FadeInForm>
                        )}

                        {step === 3 && (
                            <S.FadeInResult>
                                <S.CheckCircle>
                                    <i className="fa-solid fa-check"></i>
                                </S.CheckCircle>
                                <S.ResultText>
                                    {formData.userName}님의 아이디는<br />
                                    <S.HighlightId>{foundId}</S.HighlightId> 입니다.
                                </S.ResultText>
                                <S.BtnConfirm type="button" onClick={() => navigate('/account/login')}>
                                    확인
                                </S.BtnConfirm>
                            </S.FadeInResult>
                        )}
                    </S.StepContainer>

                    <S.AuthLinks>
                        <Link to="/account/login">로그인</Link>
                        <S.Separator>|</S.Separator>
                        <Link to="/account/find-pw">비밀번호 찾기</Link>
                    </S.AuthLinks>

                    <S.SignupBox>
                        아직 회원이 아니시라면, 지금 바로 마음을 시작해 보세요.
                        <S.LinkSignup to="/account/register">회원가입</S.LinkSignup>
                    </S.SignupBox>
                </S.FindIdCard>
            </S.Container>
        </S.FindIdWrapper>
    );
};

export default FindId;