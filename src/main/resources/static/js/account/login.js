const messageTimers = {};

$(document).ready(function() {
    let slideIndex = 0;
    const $slides = $(".slide");
    const $bgSlides = $(".bg-slide");
    const $dots = $(".dot");
    let timer;

    if ($slides.length > 0) {
        showSlides(slideIndex);
        startAutoSlide();
    }

    function showSlides(n) {
        if (n >= $slides.length) slideIndex = 0;
        if (n < 0) slideIndex = $slides.length - 1;

        $slides.removeClass("active").eq(slideIndex).addClass("active");
        $bgSlides.removeClass("active").eq(slideIndex).addClass("active");
        $dots.removeClass("active").eq(slideIndex).addClass("active");
    }

    function startAutoSlide() {
        if (timer) clearInterval(timer);
        timer = setInterval(function() {
            slideIndex++;
            showSlides(slideIndex);
        }, 4000);
    }

    $dots.click(function() {
        clearInterval(timer);
        slideIndex = $(this).index();
        showSlides(slideIndex);
        startAutoSlide();
    });

    $("#userId").on("input", function() {
        clearMessage("userIdMsg");
    });

    $("#password").on("input", function() {
        clearMessage("passwordMsg");
    });

    $("#loginForm input").on("keyup", function(e) {
        if (e.key === "Enter") {
            doLogin();
        }
    });

    $('#togglePassword').on('click', function() {
        const passwordInput = $('#password');
        const icon = $(this);

        const currentType = passwordInput.attr('type');

        if (currentType === 'password') {
            passwordInput.attr('type', 'text');
            icon.removeClass('fa-eye').addClass('fa-eye-slash');
            icon.addClass('active');
        } else {
            passwordInput.attr('type', 'password');
            icon.removeClass('fa-eye-slash').addClass('fa-eye');
            icon.removeClass('active');
        }
    });
});

function setMessage(id, message, type) {
    const $target = $("#" + id);

    if (messageTimers[id]) {
        clearTimeout(messageTimers[id]);
    }

    $target.removeClass("error success show");

    if (!message) {
        $target.text("");
        return;
    }

    $target.text(message).addClass(type).addClass("show");

    messageTimers[id] = setTimeout(function() {
        clearMessage(id);
    }, 3000);
}

function clearMessage(id) {
    const $target = $("#" + id);
    if (messageTimers[id]) {
        clearTimeout(messageTimers[id]);
        delete messageTimers[id];
    }
    $target.removeClass("error success show").text("");
}

function doLogin() {
    const userId = $("#userId").val().trim();
    const password = $("#password").val().trim();

    if (userId === "") {
        setMessage("userIdMsg", "아이디를 입력해주세요.", "error");
        $("#userId").focus();
        return;
    }

    if (password === "") {
        setMessage("passwordMsg", "비밀번호를 입력해주세요.", "error");
        $("#password").focus();
        return;
    }

    $.ajax({
        url: "/account/loginProc",
        type: "post",
        dataType: "JSON",
        data: $("#loginForm").serialize(),
        success: function(json) {
            if (json.result === 1) {
                location.href = "/main";
            } else {
                setMessage("userIdMsg", json.msg, "error");
                $("#password").val("");
                $("#userId").focus();
            }
        },
        error: function() {
            alert("서버 통신 중 오류가 발생했습니다.");
        }
    });
}