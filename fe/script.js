import { initializeApp } from 'https://www.gstatic.com/firebasejs/10.4.0/firebase-app.js'
import { getDatabase, ref, onChildAdded, set, onValue, child, onChildChanged, query, limitToLast } from "./firebase-database.js"
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
const statusPull = ref(database, 'status');

let textHumid = document.getElementById('text-humid');
let textTemp = document.getElementById('text-temp');
let textLight = document.getElementById('text-light');
let livingRoomSwitch = document.getElementById('living-room-switch');
let bedroomSwith = document.getElementById('bedroom-switch');

let disableInit = true;
onChildAdded(sensorRef, (snapshot) => {
    if (!disableInit) {
        const data = snapshot.val();
        const obj = JSON.parse(data);
        textHumid.innerHTML = obj.humid.toFixed(1);
        document.getElementById('card-temp').style.backgroundImage = getTempLevel(obj.temp);
        textTemp.innerHTML = obj.temp.toFixed(1);
        document.getElementById('card-humid').style.backgroundImage = getHumidLevel(obj.humid);
        if (obj.lux != null) {
            textLight.innerHTML = obj.lux.toFixed(1);
            document.getElementById('card-light').style.backgroundImage = getLightLevel(obj.lux);
        }
        addData(obj.temp, obj.humid);
    }
});

onChildAdded(query(sensorRef, limitToLast(10)), (snapshot) => {
    const data = snapshot.val();
    const obj = JSON.parse(data);
    textHumid.innerHTML = obj.humid.toFixed(1);
    document.getElementById('card-temp').style.backgroundImage = getTempLevel(obj.temp);
    textTemp.innerHTML = obj.temp.toFixed(1);
    document.getElementById('card-humid').style.backgroundImage = getHumidLevel(obj.humid);
    if (obj.lux != null) {
        textLight.innerHTML = obj.lux.toFixed(1);
        document.getElementById('card-light').style.backgroundImage = getLightLevel(obj.lux);
    }
    addData(obj.temp, obj.humid);
});


onValue(sensorRef, () => {
    disableInit = false;
}, { onlyOnce: true });

onChildAdded(statusPull, (snapshot) => {
    console.log(snapshot.val())
    if (snapshot.key == "l1") {
        livingRoomSwitch.checked = snapshot.val().on;
    } else if (snapshot.key == "l2") {
        bedroomSwith.checked = snapshot.val().on;
    }
});

onChildChanged(statusPull, (snapshot) => {
    if (snapshot.key == "l1") {
        livingRoomSwitch.checked = snapshot.val().on;
    } else if (snapshot.key == "l2") {
        bedroomSwith.checked = snapshot.val().on;
    }
});

livingRoomSwitch.addEventListener('change', function () {
    set(child(statusPull, "l1/on"), this.checked);
});

bedroomSwith.addEventListener('change', function () {
    set(child(statusPull, "l2/on"), this.checked);
});

let getTempLevel = temp => {
    if (temp < 20) {
        return getVar("--good-gradient");
    } else if (temp < 30) {
        return getVar("--warning-gradient");
    } else {
        return getVar("--severe-gradient");
    }
}

let getHumidLevel = humid => {
    if (humid <= 60) {
        return getVar("--good-gradient");
    } else if (humid < 80) {
        return getVar("--warning-gradient");
    } else {
        return getVar("--severe-gradient");
    }
}

let getLightLevel = light => {
    if (light < 900) {
        return getVar("--good-gradient");
    } else if (light <= 1024) {
        return getVar("--warning-gradient");
    } else {
        return getVar("--severe-gradient");
    }
}

let getVar = str => getComputedStyle(document.documentElement).getPropertyValue(str);