document.addEventListener("DOMContentLoaded", function() {

    let currentSectionIndex = 0;
    const sections = document.querySelectorAll(".full-page");
    const mainContent = document.querySelector(".main-content");
    const totalSections = sections.length;
    let isScrolling = false;
    const mobileBreakpoint = 768;

    sections[0].classList.add("active");

    const statsObserver = new IntersectionObserver((entries) => {
        entries.forEach(entry => {
            if (entry.isIntersecting) {
                const target = entry.target;
                animateValue(target);
                statsObserver.unobserve(target);
            }
        });
    }, { threshold: 0.5 });

    const statNumbers = document.querySelectorAll('.stat-number');
    statNumbers.forEach(stat => statsObserver.observe(stat));

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

    function animateValue(obj) {
        const targetText = obj.innerText;
        const target = parseInt(targetText.replace(/,/g, ""), 10);
        let startTimestamp = null;
        const duration = 2000;

        const step = (timestamp) => {
            if (!startTimestamp) startTimestamp = timestamp;
            const progress = Math.min((timestamp - startTimestamp) / duration, 1);

            const easeOutQuart = 1 - Math.pow(1 - progress, 4);

            const currentVal = Math.floor(easeOutQuart * target);
            obj.innerHTML = currentVal.toLocaleString();

            if (progress < 1) {
                window.requestAnimationFrame(step);
            } else {
                obj.innerHTML = target.toLocaleString();
            }
        };

        window.requestAnimationFrame(step);
    }
});