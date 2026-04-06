import React from 'react';
import { useRegisterForm } from '../../hooks/account/useRegisterForm';
import CustomModal from '../../components/common/CustomModal';
import InputField from '../../components/common/InputField';
import RollerDatePicker from '../../components/common/RollerDatePicker';
import * as S from '../../style/pages/Account/Register.styles';

const Register = () => {
    const {
        step, setStep, nextStep,
        formData, setFormData, handleChange,
        messages, flags,
        showDatePicker, setShowDatePicker,
        modal, setModal,
        handleEmailSend, handleCodeVerify, handleUserIdCheck, handleKakaoPost, handleSubmit
    } = useRegisterForm();

    return (
        <S.RegisterWrapper>
            <CustomModal
                isOpen={modal.show} title={modal.title} message={modal.message}
                isConfirm={false} onCancel={() => setModal({ show: false, title: '', message: '', onConfirm: null })}
                onConfirm={() => {
                    setModal({ show: false, title: '', message: '', onConfirm: null });
                    if (modal.onConfirm) modal.onConfirm();
                }}
            />

            <S.Container>
                <S.RegisterCard>
                    <S.StepperWrapper>
                        {[1, 2, 3].map((num, idx) => (
                            <React.Fragment key={num}>
                                <S.StepperItem $active={step === num} $completed={step > num}>
                                    <div className="step-label">{num === 1 ? '인증' : num === 2 ? '계정정보' : '개인정보'}</div>
                                    <div className="step-circle"><i className="fa-solid fa-check"></i></div>
                                </S.StepperItem>
                                {idx < 2 && <S.StepLine />}
                            </React.Fragment>
                        ))}
                    </S.StepperWrapper>

                    <form onSubmit={handleSubmit}>
                        <S.SlideViewport>
                            <S.SlideTrack $step={step}>

                                <S.FormStep $active={step === 1}>
                                    <S.AuthInputs>
                                        <InputField label="E-mail" name="email" value={formData.email}
                                                    onChange={handleChange} errorMsg={messages.emailMsg}
                                                    readOnly={flags.emailVerified} actionBtn={{
                                            text: '인증번호 발송',
                                            onClick: handleEmailSend,
                                            disabled: flags.emailVerified
                                        }} placeholder="이메일을 입력하세요." />
                                        <InputField label="Code" name="code" value={formData.code}
                                                    onChange={handleChange} errorMsg={messages.codeMsg}
                                                    readOnly={flags.emailVerified} actionBtn={{
                                            text: '인증 확인',
                                            onClick: handleCodeVerify,
                                            disabled: flags.emailVerified
                                        }} placeholder="인증번호를 입력하세요." />
                                    </S.AuthInputs>
                                </S.FormStep>

                                <S.FormStep $active={step === 2}>
                                    <S.AuthInputs>
                                        <InputField label="User ID" name="userId" value={formData.userId}
                                                    onChange={handleChange} errorMsg={messages.userIdMsg}
                                                    readOnly={flags.userIdChecked} actionBtn={{
                                            text: '중복확인',
                                            onClick: handleUserIdCheck,
                                            disabled: flags.userIdChecked
                                        }} placeholder="아이디를 입력하세요." />
                                        <InputField label="Password" name="password" isPassword={true}
                                                    value={formData.password} onChange={handleChange}
                                                    errorMsg={messages.passwordMsg} placeholder="비밀번호를 입력하세요." />
                                        <InputField label="Confirm Password" name="passwordConfirm" isPassword={true}
                                                    value={formData.passwordConfirm} onChange={handleChange}
                                                    errorMsg={messages.passwordConfirmMsg}
                                                    placeholder="비밀번호를 다시 입력하세요." />
                                    </S.AuthInputs>
                                </S.FormStep>

                                <S.FormStep $active={step === 3}>
                                    <S.AuthInputs>
                                        <InputField label="Name" name="userName" value={formData.userName}
                                                    onChange={handleChange} errorMsg={messages.userNameMsg}
                                                    placeholder="이름을 입력하세요." />

                                        <InputField label="Birth Date" name="birthDate" value={formData.birthDate}
                                                    readOnly={true} onClick={() => setShowDatePicker(true)}
                                                    errorMsg={messages.birthDateMsg} placeholder="YYYY-MM-DD">
                                            {showDatePicker && (
                                                <RollerDatePicker
                                                    initialDate={{ year: 2000, month: 1, day: 1 }}
                                                    onClose={() => setShowDatePicker(false)}
                                                    onConfirm={(date) => {
                                                        setFormData(prev => ({
                                                            ...prev,
                                                            birthDate: `${date.year}-${String(date.month).padStart(2, '0')}-${String(date.day).padStart(2, '0')}`
                                                        }));
                                                        setShowDatePicker(false);
                                                    }}
                                                />
                                            )}
                                        </InputField>

                                        <InputField label="Address" name="addr" value={formData.addr} readOnly={true}
                                                    errorMsg={messages.addrMsg}
                                                    actionBtn={{ text: '우편번호', onClick: handleKakaoPost }}
                                                    placeholder="주소를 입력하세요." />
                                        <InputField label="Apartment, etc" name="detailAddr" value={formData.detailAddr}
                                                    onChange={handleChange} errorMsg={messages.detailAddrMsg}
                                                    placeholder="상세주소를 입력하세요." />
                                    </S.AuthInputs>
                                </S.FormStep>

                            </S.SlideTrack>
                        </S.SlideViewport>

                        <S.ActionButtons>
                            <S.BtnStepGroup $active={step === 1}>
                                <S.BtnRow>
                                    <S.BtnNext type="button" onClick={() => nextStep(2)}>다음</S.BtnNext>
                                </S.BtnRow>
                            </S.BtnStepGroup>

                            <S.BtnStepGroup $active={step === 2}>
                                <S.BtnRow $split={true}>
                                    <S.BtnPrev type="button" onClick={() => setStep(1)}>이전</S.BtnPrev>
                                    <S.BtnNext type="button" onClick={() => nextStep(3)}>다음</S.BtnNext>
                                </S.BtnRow>
                            </S.BtnStepGroup>

                            <S.BtnStepGroup $active={step === 3}>
                                <S.BtnRow $split={true}>
                                    <S.BtnPrev type="button" onClick={() => setStep(2)}>이전</S.BtnPrev>
                                    <S.BtnSubmit type="submit">가입하기</S.BtnSubmit>
                                </S.BtnRow>
                            </S.BtnStepGroup>
                        </S.ActionButtons>
                    </form>

                    <S.LoginBox>
                        이미 마음(MAUM) 회원이신가요?
                        <S.LinkLogin to="/account/login">로그인</S.LinkLogin>
                    </S.LoginBox>
                </S.RegisterCard>
            </S.Container>
        </S.RegisterWrapper>
    );
};

export default Register;