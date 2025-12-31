$(document).ready(function() {

    const defaultLat = 37.544;
    const defaultLng = 126.844;

    let map = L.map('map').setView([defaultLat, defaultLng], 14);

    L.tileLayer('https://{s}.tile.openstreetmap.org/{z}/{x}/{y}.png', {
        attribution: '&copy; <a href="https://www.openstreetmap.org/copyright">OpenStreetMap</a> contributors'
    }).addTo(map);

    const mockData = [
        {
            id: 1,
            name: "마음 편한 심리상담센터",
            lat: 37.546,
            lng: 126.842,
            address: "서울시 강서구 우장산로 15",
            phone: "02-123-4567",
            distance: "350m",
            tags: ["우울증", "청소년", "미술치료"]
        },
        {
            id: 2,
            name: "공감과 치유 센터",
            lat: 37.541,
            lng: 126.846,
            address: "서울시 강서구 화곡로 120",
            phone: "02-987-6543",
            distance: "600m",
            tags: ["스트레스", "직장인", "진로상담"]
        },
        {
            id: 3,
            name: "우리 아이 발달연구소",
            lat: 37.548,
            lng: 126.849,
            address: "서울시 강서구 강서로 300",
            phone: "02-555-5555",
            distance: "1.2km",
            tags: ["아동심리", "놀이치료", "부모교육"]
        },
        {
            id: 4,
            name: "행복 찾기 정신건강의학과",
            lat: 37.539,
            lng: 126.838,
            address: "서울시 양천구 화곡로 55",
            phone: "02-777-7777",
            distance: "1.5km",
            tags: ["불면증", "불안장애", "약물치료"]
        }
    ];

    let markers = {};
    const centerList = $('#centerList');

    const myIcon = L.icon({
        iconUrl: 'https://cdn-icons-png.flaticon.com/512/7902/7902169.png',
        iconSize: [40, 40],
        iconAnchor: [20, 40],
        popupAnchor: [0, -40]
    });

    const markerIcon = L.icon({
        iconUrl: 'https://cdn-icons-png.flaticon.com/512/684/684908.png',
        iconSize: [35, 35],
        iconAnchor: [17, 35],
        popupAnchor: [0, -35]
    });

    L.marker([defaultLat, defaultLng], {icon: myIcon}).addTo(map)
        .bindPopup("<b>현재 내 위치</b><br>서울시 강서구").openPopup();

    function renderList() {
        centerList.empty();
        mockData.forEach(item => {
            const tagsHtml = item.tags.map(tag => `<span class="tag">#${tag}</span>`).join('');

            const html = `
                <div class="center-item" data-id="${item.id}">
                    <div class="center-name">
                        ${item.name}
                        <span class="center-dist">${item.distance}</span>
                    </div>
                    <div class="center-addr">${item.address}</div>
                    <div class="center-phone"><i class="fas fa-phone-alt"></i> ${item.phone}</div>
                    <div class="center-tags">${tagsHtml}</div>
                </div>
            `;
            centerList.append(html);

            const marker = L.marker([item.lat, item.lng], {icon: markerIcon}).addTo(map);
            marker.bindPopup(`<b>${item.name}</b><br>${item.phone}`);

            marker.on('click', function() {
                activateItem(item.id);
            });

            markers[item.id] = marker;
        });
    }

    function activateItem(id) {
        $('.center-item').removeClass('active');
        $(`.center-item[data-id="${id}"]`).addClass('active');

        const target = mockData.find(d => d.id === id);
        if (target) {
            map.setView([target.lat, target.lng], 16, {animate: true});
            markers[id].openPopup();

            const listItem = $(`.center-item[data-id="${id}"]`);
            centerList.scrollTop(centerList.scrollTop() + listItem.position().top - 20);
        }
    }

    renderList();

    $(document).on('click', '.center-item', function() {
        const id = $(this).data('id');
        activateItem(id);
    });

    $('#btnMyLocation').click(function() {
        map.setView([defaultLat, defaultLng], 14, {animate: true});
    });
});