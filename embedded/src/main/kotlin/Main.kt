
import com.google.auth.oauth2.GoogleCredentials
import com.google.firebase.FirebaseApp
import com.google.firebase.FirebaseOptions
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener
import java.io.FileInputStream

fun main() {
    // Initialize Firebase
    val options = FirebaseOptions.builder()
        .setCredentials(GoogleCredentials.fromStream(FileInputStream("firebase-adminsdk-key.json")))
        .setDatabaseUrl("https://iot-center-6bcc3-default-rtdb.asia-southeast1.firebasedatabase.app/").build()

    FirebaseApp.initializeApp(options)
    val sensorDataRef = FirebaseDatabase.getInstance().getReference("sensorData")
    val ledRef = FirebaseDatabase.getInstance().getReference("led")

    val mqtt = MqttCenter {
        println(">> MQTT received: $it")
        sensorDataRef.push().setValueAsync(it)
    }

    // Listen to led toggle variable and notify to MQTT
    ledRef.addValueEventListener(object : ValueEventListener {
        override fun onDataChange(snapshot: DataSnapshot) {
            val bool = snapshot.getValue(Boolean::class.java)
            println(">> LED toggled=$bool")
            mqtt.toggleLed(bool)
        }

        override fun onCancelled(error: DatabaseError) {
            println(error.code.toString() + ": " + error.message)
        }
    })
}