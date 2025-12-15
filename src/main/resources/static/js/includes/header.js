$(document).ready(function() {
    const $menuToggle = $('#mobileMenu');
    const $navMenu = $('.nav-menu');

    $menuToggle.click(function() {
        $navMenu.toggleClass('active');
    });

    $('.btn-signin').click(function() {
        window.location.href = '/account/login';
    });

});