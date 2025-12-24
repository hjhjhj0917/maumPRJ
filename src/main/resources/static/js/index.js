document.addEventListener("DOMContentLoaded", function() {

    let currentSectionIndex = 0;
    const sections = document.querySelectorAll(".full-page");
    const totalSections = sections.length;
    let isScrolling = false;

    sections[0].classList.add("active");

    window.addEventListener("wheel", function(e) {
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

        const mainContent = document.querySelector(".main-content");
        mainContent.style.transform = `translateY(-${index * 100}vh)`;

        sections.forEach(sec => sec.classList.remove("active"));
        sections[index].classList.add("active");

        setTimeout(() => {
            isScrolling = false;
        }, 800);
    }

    window.addEventListener("keydown", function(e) {
        if(isScrolling) return;

        if(e.key === "ArrowDown") {
            if (currentSectionIndex < totalSections - 1) changeSection(currentSectionIndex + 1);
        } else if(e.key === "ArrowUp") {
            if (currentSectionIndex > 0) changeSection(currentSectionIndex - 1);
        }
    });
});