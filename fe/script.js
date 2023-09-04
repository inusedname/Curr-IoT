const ctx = document.getElementById('myChart');

new Chart(ctx, {
    type: 'line',
    data: {
        labels: ['7:00', '7:30', '8:00', '8:30', '9:00', '9:30'],
        datasets: [{
            label: 'Temp',
            data: [28, 28.5, 28.5, 30, 29.1, 32],
        },
        {
            label: 'Humidity',
            data: [60, 65, 65, 70, 70, 75],
        }]
    }
});