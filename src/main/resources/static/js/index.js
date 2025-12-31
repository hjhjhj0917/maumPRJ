document.addEventListener("DOMContentLoaded", function() {

    let currentSectionIndex = 0;
    const sections = document.querySelectorAll(".full-page");
    const mainContent = document.querySelector(".main-content");
    const totalSections = sections.length;
    let isScrolling = false;
    const mobileBreakpoint = 768;

    sections[0].classList.add("active");

    function isMobile() {
        return window.innerWidth <= mobileBreakpoint;
    }

    if (isMobile()) {
        sections.forEach(sec => sec.classList.add("active"));
    }

    window.addEventListener("wheel", function(e) {
        if (isMobile()) return;

        e.preventDefault();

        if (isScrolling) return;

        if (e.deltaY > 0) {
            if (currentSectionIndex < totalSections - 1) {
                changeSection(currentSectionIndex + 1);
            }
        } else {
            if (currentSectionIndex > 0) {
                changeSection(currentSectionIndex - 1);
            }
        }
    }, { passive: false });

    function changeSection(index) {
        isScrolling = true;
        currentSectionIndex = index;

        mainContent.style.transform = `translateY(-${index * 100}vh)`;

        sections.forEach(sec => sec.classList.remove("active"));
        sections[index].classList.add("active");

        setTimeout(() => {
            isScrolling = false;
        }, 800);
    }

    window.addEventListener("keydown", function(e) {
        if (isMobile()) return;
        if (isScrolling) return;

        if (e.key === "ArrowDown") {
            if (currentSectionIndex < totalSections - 1) changeSection(currentSectionIndex + 1);
        } else if (e.key === "ArrowUp") {
            if (currentSectionIndex > 0) changeSection(currentSectionIndex - 1);
        }
    });

    window.addEventListener("resize", function() {
        if (isMobile()) {
            mainContent.style.transform = "none";
            sections.forEach(sec => sec.classList.add("active"));
        } else {
            mainContent.style.transform = `translateY(-${currentSectionIndex * 100}vh)`;
            sections.forEach(sec => sec.classList.remove("active"));
            sections[currentSectionIndex].classList.add("active");
        }
    });
});