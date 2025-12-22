$(document).ready(function() {
});

function selectAvatar(element) {
    $('.avatar-item').removeClass('selected');
    $(element).addClass('selected');

    const selectedSrc = $(element).attr('src');
    $('#currentAvatar').attr('src', selectedSrc);

    const url = new URL(element.src);
    const relativePath = url.pathname;

    $('#selectedAvatar').val(relativePath);
}