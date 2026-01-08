$(document).ready(function() {

    let trendChartInstance = null;
    let distChartInstance = null;
    let currentPeriod = 'weekly';

    const chartColors = {
        happy: '#FFD166',
        happyAlpha: 'rgba(255, 209, 102, 0.2)',
        sad: '#A0D2EB',
        sadAlpha: 'rgba(160, 210, 235, 0.2)',
        stress: '#FF8B94',
        stressAlpha: 'rgba(255, 139, 148, 0.2)',
        text: '#666',
        grid: '#f0f0f0'
    };

    const dataSets = {
        weekly: {
            labels: ['월', '화', '수', '목', '금', '토', '일'],
            happy: [60, 65, 50, 70, 85, 90, 80],
            sad: [20, 15, 30, 10, 5, 5, 10],
            stress: [40, 50, 60, 30, 20, 10, 15],
            distribution: [55, 25, 20]
        },
        monthly: {
            labels: ['1주차', '2주차', '3주차', '4주차'],
            happy: [70, 60, 85, 80],
            sad: [15, 30, 10, 15],
            stress: [30, 40, 20, 25],
            distribution: [60, 20, 20]
        }
    };

    function initTrendChart(period) {
        const ctx = document.getElementById('trendChart').getContext('2d');
        const data = dataSets[period];

        if (trendChartInstance) {
            trendChartInstance.destroy();
        }

        trendChartInstance = new Chart(ctx, {
            type: 'line',
            data: {
                labels: data.labels,
                datasets: [
                    {
                        label: '긍정',
                        data: data.happy,
                        borderColor: chartColors.happy,
                        backgroundColor: chartColors.happyAlpha,
                        borderWidth: 3,
                        tension: 0.4,
                        fill: true,
                        pointBackgroundColor: '#fff',
                        pointBorderWidth: 2,
                        pointRadius: 4
                    },
                    {
                        label: '부정',
                        data: data.sad,
                        borderColor: chartColors.sad,
                        backgroundColor: chartColors.sadAlpha,
                        borderWidth: 3,
                        tension: 0.4,
                        fill: true,
                        pointBackgroundColor: '#fff',
                        pointBorderWidth: 2,
                        pointRadius: 4
                    },
                    {
                        label: '스트레스',
                        data: data.stress,
                        borderColor: chartColors.stress,
                        backgroundColor: 'transparent',
                        borderWidth: 2,
                        borderDash: [5, 5],
                        tension: 0.4,
                        pointBackgroundColor: '#fff',
                        pointBorderWidth: 2,
                        pointRadius: 3
                    }
                ]
            },
            options: {
                responsive: true,
                maintainAspectRatio: false,
                plugins: {
                    legend: { display: false },
                    tooltip: {
                        backgroundColor: 'rgba(255, 255, 255, 0.9)',
                        titleColor: '#333',
                        bodyColor: '#555',
                        borderColor: '#eee',
                        borderWidth: 1,
                        padding: 10,
                        displayColors: true,
                        boxWidth: 8
                    }
                },
                scales: {
                    y: {
                        beginAtZero: true,
                        max: 100,
                        grid: { color: chartColors.grid },
                        ticks: { color: '#aaa', font: { size: 11 } }
                    },
                    x: {
                        grid: { display: false },
                        ticks: { color: '#666', font: { size: 12 } }
                    }
                }
            }
        });
    }

    function initDistributionChart(period) {
        const ctx = document.getElementById('distributionChart').getContext('2d');
        const data = dataSets[period].distribution;

        if (distChartInstance) {
            distChartInstance.destroy();
        }

        distChartInstance = new Chart(ctx, {
            type: 'doughnut',
            data: {
                labels: ['긍정', '부정', '스트레스'],
                datasets: [{
                    data: data,
                    backgroundColor: [chartColors.happy, chartColors.sad, chartColors.stress],
                    borderWidth: 0,
                    hoverOffset: 4
                }]
            },
            options: {
                responsive: true,
                maintainAspectRatio: false,
                cutout: '70%',
                plugins: {
                    legend: {
                        position: 'bottom',
                        labels: {
                            usePointStyle: true,
                            padding: 20,
                            font: { size: 12 }
                        }
                    }
                }
            }
        });
    }

    initTrendChart('weekly');
    initDistributionChart('weekly');

    $('.toggle-btn').click(function() {
        $('.toggle-btn').removeClass('active');
        $(this).addClass('active');

        currentPeriod = $(this).data('period');

        const dateText = currentPeriod === 'weekly' ? '2025년 12월 4주차' : '2025년 12월';
        $('#currentDateRange').text(dateText);

        initTrendChart(currentPeriod);
        initDistributionChart(currentPeriod);
    });
});