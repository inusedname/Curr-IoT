#define Log(X) Serial.println(">> " + String(X))

#define dhtPort 2
#define dhtType DHT11
#define ledPort 15
#define photoPort A0

/************************* WiFi Access Point *********************************/
// #define wifi_username "PhongNgu3"
// #define wifi_password "246813579"
#define wifi_username "1021_PhongNgu3N"
#define wifi_password "Quanglinh21"

/************************* MQTT Setup *********************************/
#define MQTT_SERVER      "test.mosquitto.org"
#define MQTT_SERVERPORT  1883
#define MQTT_DHT_TOPIC   "vstd/dht"
#define MQTT_LED_TOPIC   "vstd/led"

#include "DHT.h"
#include <NTPClient.h>
#include <ESP8266WiFi.h>
#include <WiFiUdp.h>
#include <ArduinoMqttClient.h>
#include <ArduinoJson.h>

WiFiClient wifi;
MqttClient mqttClient(wifi);
DHT dht(dhtPort, dhtType);

/**
* System.currentTimeMillis() need this
*/
WiFiUDP ntpUDP;
NTPClient timeClient(ntpUDP);

void setup() {
  Serial.begin(115200);
  Log("Hello, ESP32!");

// Setup components
  dht.begin();
  pinMode(ledPort, OUTPUT);

// Connect to wifi
  WiFi.begin(wifi_username, wifi_password);
  while (WiFi.status() != WL_CONNECTED) {
    delay(100);
    Serial.print(".");
  }
  Serial.print("\nWifi Connected! as");
  Serial.println(WiFi.localIP());

  // Connect to mqtt
  if (!mqttClient.connect(MQTT_SERVER, MQTT_SERVERPORT)) {
    Serial.print(">> MQTT connection failed! Error code = ");
    Serial.println(mqttClient.connectError());
    while (1);
  }
  Log("MQTT Connected");

  timeClient.begin();
  mqttClient.onMessage(onMqttMessage);
  mqttClient.subscribe(MQTT_LED_TOPIC);
}

void onMqttMessage(int messageSize) {
  fetchLed();
}

void loop() {
  mqttClient.poll();
  timeClient.update();
  readDht();
}

int lastMs = millis();
void readDht() {
  if (millis() - lastMs >= 1000) {
    lastMs = millis();
    float humidity = dht.readHumidity();
    float temperature = dht.readTemperature();
    float lux = getLux();

    mqttClient.beginMessage(MQTT_DHT_TOPIC);
    mqttClient.print(jsonify(humidity, temperature, lux));
    mqttClient.endMessage();
  }
}

char* jsonify(float humid, float temp, float lux) {
  DynamicJsonDocument doc(1024);
  doc["humid"] = humid;
  doc["temp"] = temp;
  doc["lux"] = lux;
  doc["seconds"] = timeClient.getEpochTime();
  
  char* json = new char[256];
  serializeJson(doc, json, 256);

  return json;
}

float getLux() {
  int analogValue = 1024 - analogRead(A0);
  float voltage = analogValue / 1024. * 3.3;
  float resistance = 2000 * voltage / (1 - voltage / 3.3);
  float lux = pow(50 * 1e3 * pow(10, 0.7) / resistance, (1 / 0.7));
  return lux;
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