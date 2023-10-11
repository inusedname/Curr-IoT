import com.google.auth.oauth2.GoogleCredentials
import com.google.firebase.FirebaseApp
import com.google.firebase.FirebaseOptions
import com.google.firebase.database.ChildEventListener
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import java.io.FileInputStream

fun main() {
    // Initialize Firebase
    val options = FirebaseOptions.builder()
        .setCredentials(GoogleCredentials.fromStream(FileInputStream("firebase-adminsdk-key.json")))
        .setDatabaseUrl("https://iot-center-6bcc3-default-rtdb.asia-southeast1.firebasedatabase.app/").build()

    FirebaseApp.initializeApp(options)
    val sensorPush = FirebaseDatabase.getInstance().getReference("sensorData")
    val statusPull = FirebaseDatabase.getInstance().getReference("status")
    val ledHistory = FirebaseDatabase.getInstance().getReference("ledHistory")
    val statusSnapshot = emptyMap<String, Boolean>().toMutableMap()
    val mqtt = MqttCenter {
        println(">> MQTT received: $it")
        sensorPush.push().setValueAsync(it)
    }

    // Listen to led toggle variable and notify to MQTT
    statusPull.addChildEventListener(object : ChildEventListener {
        override fun onChildAdded(snapshot: DataSnapshot?, previousChildName: String?) {
            snapshot?.let {
                val key = it.key
                val body = it.value as Map<String, Any>
                val bool = body["on"] as Boolean
                println(">> Startup: Device#$key, value=$bool")
                when (key) {
                    "l1" -> mqtt.toggleLed(1, bool)
                    "l2" -> mqtt.toggleLed(2, bool)
                }
                statusSnapshot.put(key, bool)
            }
        }

        override fun onChildChanged(snapshot: DataSnapshot?, previousChildName: String?) {
            snapshot?.let {
                val key = it.key
                val body = it.value as Map<String, Any>
                val bool = body["on"] as Boolean
                println(">> Update: Device#${key} value=${bool}")
                when (key) {
                    "l1" -> mqtt.toggleLed(1, bool)
                    "l2" -> mqtt.toggleLed(2, bool)
                }
                ledHistory.push().setValueAsync(
                    """
                        { "id": "$key", "on": $bool, "time": ${System.currentTimeMillis() / 1000} }
                    """.trimIndent()
                )
            }
        }

        override fun onChildRemoved(snapshot: DataSnapshot?) {}

        override fun onChildMoved(snapshot: DataSnapshot?, previousChildName: String?) {}

        override fun onCancelled(error: DatabaseError) {
            println(error.code.toString() + ": " + error.message)
        }
    })
}