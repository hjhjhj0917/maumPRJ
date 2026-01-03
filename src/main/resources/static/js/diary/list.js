$(document).ready(function() {

    const cards = document.querySelectorAll('.diary-card');
    cards.forEach((card, index) => {
        card.style.animationDelay = `${index * 0.1}s`;
    });

    const searchInput = document.querySelector('.search-box input');
    const searchBtn = document.querySelector('.search-box button');

    function performSearch() {
        const keyword = searchInput.value.toLowerCase();

        cards.forEach(card => {
            const title = card.querySelector('.diary-title').innerText.toLowerCase();
            const content = card.querySelector('.diary-preview').innerText.toLowerCase();

            if (title.includes(keyword) || content.includes(keyword)) {
                card.style.display = 'flex';
            } else {
                card.style.display = 'none';
            }
        });
    }

    if(searchBtn && searchInput) {
        searchBtn.addEventListener('click', performSearch);
        searchInput.addEventListener('keyup', function(e) {
            if (e.key === 'Enter') performSearch();
        });
    }

});