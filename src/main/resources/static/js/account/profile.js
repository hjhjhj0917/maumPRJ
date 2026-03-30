$(document).ready(function() {
    const modal = $('#zodiacModal');
    const preview = $('#profilePreview');
    const hiddenInput = $('#selectedProfileImage');

    $('#openProfileModal').click(function() {
        modal.addClass('active');
    });

    $('.close-modal').click(function() {
        modal.removeClass('active');
    });

    modal.click(function(e) {
        if ($(e.target).hasClass('modal-overlay')) {
            modal.removeClass('active');
        }
    });

    $('.zodiac-item').click(function() {
        const imgSrc = $(this).find('img').attr('src');
        const fileName = imgSrc.substring(imgSrc.lastIndexOf('/') + 1);

        preview.attr('src', imgSrc);
        hiddenInput.val(fileName);

        modal.removeClass('active');

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
                    alert(json.msg || "프로필 설정이 완료되었습니다.");
                    location.href = "/account/login";
                } else {
                    alert(json.msg || "프로필 설정 중 오류가 발생했습니다.");
                }
            },
            error: function() {
                alert("서버 통신 중 오류가 발생했습니다.");
            }
        });
    });
});