$(document).ready(function() {
    const preview = $('#profilePreview');
    const hiddenInput = $('#selectedProfileImage');

    $('.zodiac-item').click(function() {
        const imgSrc = $(this).find('img').attr('src');
        const fileName = $(this).data('img');

        preview.attr('src', imgSrc);
        hiddenInput.val(fileName);

        $('.zodiac-item').removeClass('selected');
        $(this).addClass('selected');
    });

    $('.profile-form').on('submit', function(e) {
        e.preventDefault();

        const profileImage = hiddenInput.val();

        $.ajax({
            url: "/account/updateProfileImg",
            type: "post",
            dataType: "JSON",
            data: {
                profileImage: profileImage
            },
            success: function(json) {
                if(json.result === 1) {
                    if (typeof showCustomAlert === "function") {
                        showCustomAlert("프로필 설정 완료", "프로필 설정이 완료되었습니다.", function () {
                            location.href = "/account/login";
                        });
                    } else {
                        alert("프로필 설정이 완료되었습니다.");
                        location.href = "/account/login";
                    }
                } else {
                    if (typeof showCustomAlert === "function") {
                        showCustomAlert("오류", json.msg || "프로필 설정 중 오류가 발생했습니다.");
                    } else {
                        alert(json.msg || "프로필 설정 중 오류가 발생했습니다.");
                    }
                }
            },
            error: function() {
                if (typeof showCustomAlert === "function") {
                    showCustomAlert("시스템 오류", "서버 통신 중 오류가 발생했습니다.");
                } else {
                    alert("서버 통신 중 오류가 발생했습니다.");
                }
            }
        });
    });
});