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
        const imgName = $(this).data('img');
        const imgSrc = $(this).find('img').attr('src');

        preview.attr('src', imgSrc);

        hiddenInput.val(imgName);

        modal.removeClass('active');

        $('.zodiac-item').removeClass('selected');
        $(this).addClass('selected');
    });
});