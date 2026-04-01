$(document).ready(function() {
    const $menuToggle = $('#mobileMenu');
    const $navMenu = $('.nav-menu');
    const $btnSignin = $('.btn-signin');

    $menuToggle.click(function() {
        $navMenu.toggleClass('active');
    });

    const path = window.location.pathname;
    let pageName = path.split('/').pop();

    if (pageName === "") {
        pageName = "index";
    }

    if (pageName === 'login') {
        $btnSignin.text('회원가입');
        $btnSignin.click(function() {
            window.location.href = '/account/register';
        });
    } else {
        $btnSignin.click(function() {
            window.location.href = '/account/login';
        });
    }

    const menu1 = '.nav-menu li:nth-child(1)';
    const menu2 = '.nav-menu li:nth-child(2)';
    const menu3 = '.nav-menu li:nth-child(3)';
    const btnLogin = '.btn-signin';

    const hideElementsByPage = {
        index: [],
        login: [menu1, menu2, menu3],
        register: [menu1, menu2, menu3, btnLogin],
        profile: [btnLogin],
        chat: [btnLogin],
        list: [btnLogin]
    };

    if (hideElementsByPage[pageName]) {
        hideElementsByPage[pageName].forEach(selector => {
            $(selector).hide();
        });
    }

    $(document).on('click', '#btnMoreMenu', function(e) {
        e.preventDefault();
        e.stopPropagation();
        $('#userProfileContainer').toggleClass('expanded');
    });

    $(document).on('click', function(e) {
        if (!$(e.target).closest('#userProfileContainer').length) {
            $('#userProfileContainer').removeClass('expanded');
        }
    });

    $(document).on('click', '#btnLogout', function(e) {
        e.preventDefault();

        if (typeof showCustomConfirm === "function") {
            showCustomConfirm("로그아웃 하시겠습니까?", function () {
                executeLogout();
            });
        } else if (confirm("로그아웃 하시겠습니까?")) {
            executeLogout();
        }
    });

    function executeLogout() {
        $.ajax({
            url: "/account/logout",
            type: "POST",
            dataType: "json",
            success: function (res) {
                if (res.result === 1) {
                    location.href = "/";
                } else {
                    if (typeof showCustomAlert === "function") {
                        showCustomAlert("실패: " + res.msg);
                    } else {
                        alert("실패: " + res.msg);
                    }
                }
            },
            error: function () {
                location.href = "/";
            }
        });
    }
});