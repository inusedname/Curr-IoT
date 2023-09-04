import com.google.auth.oauth2.GoogleCredentials
import com.google.firebase.FirebaseApp
import com.google.firebase.FirebaseOptions
import com.google.firebase.database.*
import java.io.FileInputStream

lateinit var sensorDataRef: DatabaseReference
val mqtt = MqttCenter()

fun main() {
    // Initialize Firebase
    val options = FirebaseOptions.builder()
        .setCredentials(GoogleCredentials.fromStream(FileInputStream("firebase-adminsdk-key.json")))
        .setDatabaseUrl("https://iot-center-6bcc3-default-rtdb.asia-southeast1.firebasedatabase.app/").build()

    FirebaseApp.initializeApp(options)
    sensorDataRef = FirebaseDatabase.getInstance().getReference("sensorData")

    // Listen to led toggle variable and notify to MQTT
    val ledRef = FirebaseDatabase.getInstance().getReference("led")
    ledRef.addListenerForSingleValueEvent(object : ValueEventListener {
        override fun onDataChange(snapshot: DataSnapshot) {
            val bool = snapshot.getValue(Boolean::class.java)
            mqtt.toggleLed(bool)
        }

        override fun onCancelled(error: DatabaseError) {
            println(error.code.toString() + ": " + error.message)
        }
    })

    // Start monitoring MQTT and push data to Firebase
    mqtt.onGetDht = {
        // TODO
        println(it)
    }
}

fun pushSensorData() {
    val dhtTemp = 27.0
    val dhtHumid = 50.0
    val timestamp = System.currentTimeMillis()
    sensorDataRef.push().setValueAsyncQuick {
        param("humid", dhtTemp)
        param("temp", dhtHumid)
        param("timestamp", timestamp)
    }
    Thread.sleep(3000)
}