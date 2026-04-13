import React from 'react';
import { Link } from 'react-router-dom';
import InputField from '../../components/common/InputField';
import CustomModal from '../../components/common/CustomModal';
import { useRegisterForm } from '../../hooks/pages/account/useRegisterForm';
import RollerDatePicker from '../../components/common/RollerDatePicker';
import * as S from '../../style/pages/Account/Register.styles';

const Register = () => {
    const {
        step, handleStepClick,
        formData, setFormData, handleChange, handleOtpChange, handleKeyDown, handlePaste,
        messages, flags,
        showDatePicker, setShowDatePicker,
        modal, setModal, inputRefs,
        handleEmailSend, handleCodeVerify, handleUserIdCheck, handleKakaoPost, handleSubmit
    } = useRegisterForm();

    const steps = [
        { num: 1, label: '인증' },
        { num: 2, label: '계정정보' },
        { num: 3, label: '개인정보' }
    ];

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
                        {steps.map((s, idx) => (
                            <React.Fragment key={s.num}>
                                <S.StepperItem
                                    $active={step === s.num}
                                    $completed={step > s.num}
                                    onClick={() => handleStepClick(s.num)}
                                >
                                    <div className="step-label">{s.label}</div>
                                    <div className="step-circle"><i className="fa-solid fa-check"></i></div>
                                </S.StepperItem>
                                {idx < steps.length - 1 && <S.StepLine />}
                            </React.Fragment>
                        ))}
                    </S.StepperWrapper>

                    <form onSubmit={handleSubmit}>
                        <S.SlideViewport>
                            <S.SlideTrack $step={step}>
                                <S.FormStep $active={step === 1}>
                                    <S.StepTitle>회원가입</S.StepTitle>
                                    <S.StepSubTitle>사용하실 메일 인증을 진행해 주세요.</S.StepSubTitle>
                                    <S.AuthInputs>
                                        <InputField label="E-mail" name="email" value={formData.email}
                                                    onChange={handleChange} errorMsg={messages.emailMsg}
                                                    readOnly={flags.emailVerified} actionBtn={{
                                            text: '인증번호 발송',
                                            onClick: handleEmailSend,
                                            disabled: flags.emailVerified
                                        }} placeholder="이메일을 입력하세요." />

                                        <S.VerificationContainer>
                                            <div className="label-row">
                                                <label>Code</label>
                                                <span className={`field-message ${messages.codeMsg ? 'show' : ''} ${messages.codeMsg?.type === 'error' ? 'error' : 'success'}`}>
                                                    {messages.codeMsg?.text}
                                                </span>
                                            </div>
                                            <S.VerificationWrapper onPaste={handlePaste}>
                                                {[0, 1, 2, 3, 4, 5].map((idx) => (
                                                    <input
                                                        key={idx}
                                                        type="text"
                                                        inputMode="numeric"
                                                        ref={(el) => (inputRefs.current[idx] = el)}
                                                        value={formData.code?.[idx] || ""}
                                                        onChange={(e) => handleOtpChange(e, idx)}
                                                        onKeyDown={(e) => handleKeyDown(e, idx)}
                                                        placeholder="0"
                                                        disabled={flags.emailVerified}
                                                    />
                                                ))}
                                            </S.VerificationWrapper>

                                            <S.ResendText>
                                                혹시 이메일을 못 받으셨나요?
                                                <button type="button" onClick={handleEmailSend} disabled={flags.emailVerified}>재전송</button>
                                            </S.ResendText>

                                            <S.BtnVerify type="button" onClick={handleCodeVerify} disabled={flags.emailVerified || (formData.code?.replace(/\s/g, '').length !== 6)}>
                                                확인
                                            </S.BtnVerify>
                                        </S.VerificationContainer>
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
                                                            birthDate: `${date.year}년 ${String(date.month).padStart(2, '0')}월 ${String(date.day).padStart(2, '0')}일`
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

                        {step === 3 && (
                            <S.ActionButtons>
                                <S.BtnSubmit type="submit">가입하기</S.BtnSubmit>
                            </S.ActionButtons>
                        )}
                    </form>

                    <S.LoginBox>
                        이미 마음(MAÜM) 회원이신가요?
                        <S.LinkLogin to="/account/login">로그인</S.LinkLogin>
                    </S.LoginBox>
                </S.RegisterCard>
            </S.Container>
        </S.RegisterWrapper>
    );
};

export default Register;