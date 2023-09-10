
import org.eclipse.paho.client.mqttv3.MqttClient
import org.eclipse.paho.client.mqttv3.MqttMessage

const val BROKER = "tcp://test.mosquitto.org:1883"

class MqttCenter(onGetDht: (String) -> Unit) {
    private val client = MqttClient(BROKER, MqttClient.generateClientId())

    init {
        client.connect()
        println(">> MQTT connected=${client.isConnected}")
        subscribe(DHT_TOPIC, onGetDht)
    }

    fun toggleLed(on: Boolean) {
        publish(LED_TOPIC, if (on) "true" else "false")
    }

    private fun subscribe(topic: String, callback: (String) -> Unit) {
        client.subscribe(topic) { _, message ->
            callback(message.toString())
        }
    }

    fun publish(topic: String, message: String) {
        client.publish(topic, MqttMessage(message.toByteArray()))
    }

    fun destroy() {
        client.disconnect()
    }

    companion object {
        const val DHT_TOPIC = "vstd/dht"
        const val LED_TOPIC = "vstd/led"
    }
}