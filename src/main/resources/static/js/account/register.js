let currentStep = 1;
let userIdCheck = "Y";
let emailAuthNumber = "";
const messageTimers = {};

$(document).ready(function () {
    updateStepper(1);
    updateButtons(1);

    const f = document.getElementById("registerForm");

    const $step1 = $("#step1");
    const $step2 = $("#step2");
    const $step3 = $("#step3");

    const $emailSendBtn = $("#btnEmail");
    const $emailVerifyBtn = $step1.find(".btn-check").eq(1);
    const $userIdCheckBtn = $step2.find(".btn-check").eq(0);
    const $addrBtn = $step3.find(".btn-check").eq(0);

    $emailSendBtn.on("click", function () {
        emailExists(f);
    });

    $emailVerifyBtn.on("click", function () {
        doAuthCheck(f);
    });

    $userIdCheckBtn.on("click", function () {
        userIdExists(f);
    });

    $addrBtn.on("click", function () {
        kakaoPost(f);
    });

    $("#email").on("input", function () {
        clearMessage("emailMsg");
        clearMessage("codeMsg");
    });

    $("#code").on("input", function () {
        clearMessage("codeMsg");
    });

    $("#userId").on("input", function () {
        clearMessage("userIdMsg");
    });

    $("#password").on("input", function () {
        clearMessage("passwordMsg");
    });

    $("#passwordConfirm").on("input", function () {
        const f = document.getElementById("registerForm");
        const pass = f.password.value;
        const confirm = $(this).val();

        clearMessage("passwordConfirmMsg");

        if (pass.length > 0 && pass === confirm) {
            if (userIdCheck !== "N") {
                setMessage("userIdMsg", "아이디 중복 체크를 완료해 주세요.", "error");
                return;
            }

            setMessage("passwordConfirmMsg", "비밀번호가 일치합니다.", "success");

            setTimeout(function() {
                nextStep(3);
            }, 1000);
        }
    });

    $("#userName, #birthDate, #address, #detailAddr").on("input", function () {
        const msgId = $(this).attr("id") === "address" ? "addrMsg" : $(this).attr("id") + "Msg";
        clearMessage(msgId);
    });

    $("#registerForm").on("submit", function (e) {
        e.preventDefault();
        doSubmit(f);
    });

    $('.toggle-password').on('click', function() {
        const icon = $(this);
        const passwordInput = icon.siblings('input');

        const currentType = passwordInput.attr('type');

        if (currentType === 'password') {
            passwordInput.attr('type', 'text');
            icon.removeClass('fa-eye').addClass('fa-eye-slash active');
        } else {
            passwordInput.attr('type', 'password');
            icon.removeClass('fa-eye-slash active').addClass('fa-eye');
        }
    });
});

function setMessage(id, message, type) {
    const $target = $("#" + id);
    const duration = type === "success" ? 2000 : 3000;

    if (messageTimers[id]) clearTimeout(messageTimers[id]);
    $target.removeClass("error success show");

    if (!message) {
        $target.text("");
        return;
    }

    $target.text(message).addClass(type).addClass("show");
    messageTimers[id] = setTimeout(function () {
        clearMessage(id);
    }, duration);
}

function clearMessage(id) {
    const $target = $("#" + id);
    if (messageTimers[id]) {
        clearTimeout(messageTimers[id]);
        delete messageTimers[id];
    }
    $target.removeClass("error success show").text("");
}

function emailExists(f) {

    const emailValue = f.email.value.trim();

    if (f.email.value.trim() === "") {
        setMessage("emailMsg", "이메일을 입력하세요.", "error");
        f.email.focus();
        return;
    }

    if (!validator.isEmail(emailValue)) {
        setMessage("emailMsg", "유효한 이메일 형식이 아닙니다.", "error");
        f.email.focus();
        return;
    }

    $.ajax({
        url: "/account/getEmailExists",
        type: "post",
        dataType: "JSON",
        data: {email: emailValue},
        success: function (json) {
            if (json.exists === true) {
                setMessage("emailMsg", "이미 가입된 이메일 주소가 존재합니다.", "error");
                f.email.focus();
            } else {
                setMessage("emailMsg", "이메일로 인증번호가 발송되었습니다.", "success");
                emailAuthNumber = json.authNumber;
                f.email.readOnly = true;
            }
        },
        error: function () {
            setMessage("emailMsg", "서버 통신 중 오류가 발생했습니다.", "error");
        }
    });
}

function doAuthCheck(f) {
    const codeInput = document.getElementById("code");

    if (codeInput.value.trim() === "") {
        setMessage("codeMsg", "인증번호를 입력하세요.", "error");
        codeInput.focus();
        return;
    }

    if (parseInt(codeInput.value) !== emailAuthNumber) {
        setMessage("codeMsg", "잘못된 인증번호 입니다.", "error");
        codeInput.focus();
        return;
    } else {
        setMessage("codeMsg", "인증번호가 확인되었습니다.", "success");

        f.email.readOnly = true;
        codeInput.readOnly = true;

        $("#btnEmail").prop("disabled", true).css({
            backgroundColor: "#eee",
            color: "#999",
            cursor: "not-allowed"
        });
        $("#step1").find(".btn-check").eq(1).prop("disabled", true).css({
            backgroundColor: "#eee",
            color: "#999",
            cursor: "not-allowed"
        });
        setTimeout(function() {
            nextStep(2);
        }, 800);
    }
}

function userIdExists(f) {
    if (f.userId.value.trim() === "") {
        setMessage("userIdMsg", "아이디를 입력하세요.", "error");
        f.userId.focus();
        return;
    }

    $.ajax({
        url: "/account/getUserIdExists",
        type: "post",
        dataType: "JSON",
        data: {userId: f.userId.value},
        success: function (json) {
            if (json.exists === true) {
                setMessage("userIdMsg", "이미 가입된 아이디가 존재합니다.", "error");
                f.userId.focus();
            } else {
                setMessage("userIdMsg", "사용 가능한 아이디입니다.", "success");

                f.userId.readOnly = true;
                $("#step2").find(".btn-check").eq(0).prop("disabled", true).css({
                    backgroundColor: "#eee",
                    color: "#999",
                    cursor: "not-allowed"
                });

                userIdCheck = "N";
            }
        },
        error: function () {
            setMessage("userIdMsg", "서버 통신 중 오류가 발생했습니다.", "error");
        }
    });
}

function kakaoPost(f) {
    new daum.Postcode({
        autoClose: true,
        oncomplete: function (data) {
            let address = data.address;
            let zonecode = data.zonecode;

            f.addr.value = "(" + zonecode + ") " + address;
            clearMessage("addrMsg");

            f.detailAddr.focus();
        }
    }).open();
}

function validateStep1(f) {
    const codeInput = document.getElementById("code");
    let isValid = true;

    if (f.email.value.trim() === "") {
        setMessage("emailMsg", "이메일을 입력하세요.", "error");
        isValid = false;
    }

    if (!validator.isEmail(f.email.value)) {
        setMessage("emailMsg", "유효한 이메일 형식이 아닙니다.", "error");
        isValid = false;
    }

    if (codeInput.value.trim() === "") {
        setMessage("codeMsg", "인증번호를 입력하세요.", "error");
        isValid = false;
    } else if (emailAuthNumber === "" || parseInt(codeInput.value) !== emailAuthNumber) {
        setMessage("codeMsg", "이메일 인증번호가 일치하지 않습니다.", "error");
        isValid = false;
    }

    return isValid;
}

function validateStep2(f) {
    let isValid = true;

    if (f.userId.value.trim() === "") {
        setMessage("userIdMsg", "아이디를 입력하세요.", "error");
        isValid = false;
    } else if (userIdCheck !== "N") {
        setMessage("userIdMsg", "아이디 중복 체크를 완료해 주세요.", "error");
        isValid = false;
    }

    if (f.password.value.trim() === "") {
        setMessage("passwordMsg", "비밀번호를 입력하세요.", "error");
        isValid = false;
    }

    const passConfirm = document.getElementById("passwordConfirm").value;
    if (passConfirm.trim() === "") {
        setMessage("passwordConfirmMsg", "비밀번호 확인을 입력하세요.", "error");
        isValid = false;
    } else if (f.password.value !== passConfirm) {
        setMessage("passwordConfirmMsg", "비밀번호가 일치하지 않습니다.", "error");
        isValid = false;
    }

    return isValid;
}

function validateStep3(f) {
    let isValid = true;

    if (f.userName.value.trim() === "") {
        setMessage("userNameMsg", "이름을 입력하세요.", "error");
        isValid = false;
    }

    if (f.birthDate && f.birthDate.value.trim() === "") {
        setMessage("birthDateMsg", "생년월일을 입력하세요.", "error");
        isValid = false;
    }

    if (f.addr.value.trim() === "") {
        setMessage("addrMsg", "주소를 입력하세요.", "error");
        isValid = false;
    }

    if (f.detailAddr.value.trim() === "") {
        setMessage("detailAddrMsg", "상세주소를 입력하세요.", "error");
        isValid = false;
    }

    return isValid;
}

function doSubmit(f) {
    if (!validateStep1(f)) {
        showStep(1);
        return;
    }
    if (!validateStep2(f)) {
        showStep(2);
        return;
    }
    if (!validateStep3(f)) {
        showStep(3);
        return;
    }

    $.ajax({
        url: "/account/insertUserInfo",
        type: "post",
        dataType: "JSON",
        data: $("#registerForm").serialize(),
        success: function (json) {
            if (json.result === 1) {
                alert(json.msg || "회원가입이 완료되었습니다.");
                location.href = "/account/profile";
            } else {
                alert(json.msg || "회원가입 중 문제가 발생했습니다.");
            }
        },
        error: function () {
            alert("서버 통신 중 오류가 발생했습니다.");
        }
    });
}

function nextStep(step) {
    const f = document.getElementById("registerForm");

    if (step === 2 && !validateStep1(f)) return;
    if (step === 3 && !validateStep2(f)) return;

    showStep(step);
}

function prevStep(step) {
    showStep(step);
}

function showStep(step) {
    currentStep = step;

    let translateValue = (step - 1) * -100;
    $("#slideTrack").css("transform", "translateX(" + translateValue + "%)");

    $(".form-step").removeClass("active-slide");
    $("#step" + step).addClass("active-slide");

    updateButtons(step);
    updateStepper(step);
}

function updateButtons(step) {
    $(".btn-step-group").removeClass("active");
    $("#btns" + step).addClass("active");
}

function updateStepper(step) {
    $(".stepper-item").removeClass("active completed");

    for (let i = 1; i <= 3; i++) {
        if (i < step) {
            $("#stepper" + i).addClass("completed");
        } else if (i === step) {
            $("#stepper" + i).addClass("active");
        }
    }
}