import { initializeApp } from 'https://www.gstatic.com/firebasejs/10.4.0/firebase-app.js'
import { getDatabase, ref, onChildAdded, set, onValue } from "https://www.gstatic.com/firebasejs/10.4.0/firebase-database.js"
import { firebaseConfig } from './secrets.js'

const ctx = document.getElementById('myChart');

let chart = new Chart(ctx, {
    type: 'line',
    data: {
        datasets: [{
            label: 'Temp',
        },
        {
            label: 'Humidity',
        }]
    }
});

function addData (temp, humid, light) {
    chart.data.labels.push('10:00');
    if (chart.data.datasets[0].data.length > 9) {
        chart.data.labels.shift();
        chart.data.datasets[0].data.shift();
        chart.data.datasets[1].data.shift();
    }
    chart.data.datasets[0].data.push(temp);
    chart.data.datasets[1].data.push(humid);
    chart.update();
}

const app = initializeApp(firebaseConfig);
const database = getDatabase(app);
const sensorRef = ref(database, 'sensorData');
const ledRef = ref(database, 'led');

let textHumid = document.getElementById('text-humid');
let textTemp = document.getElementById('text-temp');
let textLight = document.getElementById('text-light');
let livingRoomSwitch = document.getElementById('living-room-switch');


onChildAdded(sensorRef, (snapshot) => {
    const data = snapshot.val();
    const obj = JSON.parse(data);
    textHumid.innerHTML = obj.humid.toFixed(1);
    textTemp.innerHTML = obj.temp.toFixed(1);
    textLight.innerHTML = obj.lux.toFixed(1);
    addData(obj.temp, obj.humid);
});

onValue(ledRef, (snapshot) => {
    const data = snapshot.val();
    livingRoomSwitch.checked = data;
});

livingRoomSwitch.addEventListener('change', function () {
    set(ledRef, this.checked);
});