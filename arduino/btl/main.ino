#define Log Serial.println

#define dhtPort 27
#define dhtType DHT22
#define ledPort 26

/************************* WiFi Access Point *********************************/
#define wifi_username "Wokwi-GUEST"
#define wifi_password ""

/************************* MQTT Setup *********************************/
#define MQTT_SERVER      "test.mosquitto.org"
#define MQTT_SERVERPORT  1883
#define MQTT_DHT_TOPIC   "vstd/dht"
#define MQTT_LED_TOPIC   "vstd/led"

#include "DHT.h"
#include <WiFi.h> // <ESP8266WiFi.h>
#include <ArduinoMqttClient.h>

WiFiClient wifi;
MqttClient mqttClient(wifi);
DHT dht(dhtPort, dhtType);

void setup() {
  Serial.begin(115200);
  Log("Hello, ESP32!");

  // Setup components
  dht.begin();
  pinMode(ledPort, OUTPUT);

  WiFi.begin(wifi_username, wifi_password, 6);
  // Connect to wifi
  while (WiFi.status() != WL_CONNECTED) {
    delay(100);
    Serial.print(".");
  }
  Log("Wifi Connected!");
  Log();

  // Connect to mqtt
  if (!mqttClient.connect(MQTT_SERVER, MQTT_SERVERPORT)) {
    Serial.print(">> MQTT connection failed! Error code = ");
    Log(mqttClient.connectError());

    while (1);
  }
  Log(">> You're connected to the MQTT broker!");

  mqttClient.onMessage(onMqttMessage);
  mqttClient.subscribe(MQTT_LED_TOPIC);
}

void onMqttMessage(int messageSize) {
  fetchLed();
}

void loop() {
  mqttClient.poll();
  readDht();
}

int lastMs = millis() - 1000;
void readDht() {
  if (millis() - lastMs >= 1000) {
    lastMs = millis();
    float humidity = dht.readHumidity();
    float temperature = dht.readTemperature();

    mqttClient.beginMessage(MQTT_DHT_TOPIC);
    mqttClient.print(temperature);
    mqttClient.endMessage();

  }
}

void fetchLed() {
  Log("Received a message with topic");
  Log(mqttClient.messageTopic());
  String state = "";
  while (mqttClient.available()) {
    char c = mqttClient.read();
    state += String(c);
  }
  Log(state);
  if (state == "true") {
    setLed(true);
  } else {
    setLed(false);
  }
}

void setLed(bool enable) {
  if (enable) {
    digitalWrite(ledPort, HIGH);
  } else {
    digitalWrite(ledPort, LOW);
  }
}