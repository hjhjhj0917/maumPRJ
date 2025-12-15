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

});