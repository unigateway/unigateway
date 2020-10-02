package com.mqgateway.homie

import com.mqgateway.homie.mqtt.MqttClient
import com.mqgateway.homie.mqtt.MqttClientFactory
import com.mqgateway.homie.mqtt.MqttMessage
import mu.KotlinLogging

const val HOMIE_PREFIX = "homie"

private val LOGGER = KotlinLogging.logger {}

const val HOMIE_CONFIGURATION_MQTT_MESSAGES_QOS = 1

class HomieDevice(
  mqttClientFactory: MqttClientFactory,
  val id: String,
  val nodes: Map<String, HomieNode>,
  val homie: String,
  val name: String,
  val extensions: List<String> = emptyList(),
  val implementation: String? = null,
  val firmwareName: String?,
  val firmwareVersion: String?,
  val ip: String? = null,
  val mac: String? = null
) {

  private val baseTopic = "$HOMIE_PREFIX/$id"
  private var mqttClient: MqttClient = mqttClientFactory.create(
      id,
      { LOGGER.info { "MQTT connection established" }; this.changeState(State.READY) },
      { LOGGER.error { "MQTT connection lost" } }
  )

  fun connect(homieReceiver: HomieReceiver) {
    LOGGER.info { "Connecting to MQTT" }
    mqttClient.connect(MqttMessage("$baseTopic/\$state", State.LOST.value, 1, true), false)

    LOGGER.debug { "Publishing Homie configuration to MQTT" }
    changeState(State.INIT)
    mqttClient.publishSync(MqttMessage("$baseTopic/\$homie", homie, HOMIE_CONFIGURATION_MQTT_MESSAGES_QOS, true))
    mqttClient.publishSync(MqttMessage("$baseTopic/\$name", name, HOMIE_CONFIGURATION_MQTT_MESSAGES_QOS, true))
    mqttClient.publishSync(MqttMessage("$baseTopic/\$extensions", extensions.joinToString(), HOMIE_CONFIGURATION_MQTT_MESSAGES_QOS, true))
    mqttClient.publishSync(MqttMessage("$baseTopic/\$nodes", nodes.keys.joinToString(), HOMIE_CONFIGURATION_MQTT_MESSAGES_QOS, true))
    if (implementation != null) {
      mqttClient.publishSync(MqttMessage("$baseTopic/\$implementation", implementation, HOMIE_CONFIGURATION_MQTT_MESSAGES_QOS, true))
    }
    if (firmwareName != null) {
      mqttClient.publishSync(MqttMessage("$baseTopic/\$fw/name", firmwareName, HOMIE_CONFIGURATION_MQTT_MESSAGES_QOS, true))
    }
    if (firmwareVersion != null) {
      mqttClient.publishSync(MqttMessage("$baseTopic/\$fw/version", firmwareVersion, HOMIE_CONFIGURATION_MQTT_MESSAGES_QOS, true))
    }
    if (ip != null) {
      mqttClient.publishSync(MqttMessage("$baseTopic/\$localip", ip, HOMIE_CONFIGURATION_MQTT_MESSAGES_QOS, true))
    }
    if (mac != null) {
      mqttClient.publishSync(MqttMessage("$baseTopic/\$mac", mac, HOMIE_CONFIGURATION_MQTT_MESSAGES_QOS, true))
    }
    nodes.values.forEach { it.setup(mqttClient, homieReceiver) }
    changeState(State.READY)
    LOGGER.debug { "Homie configuration published" }
  }

  private fun changeState(newState: State) {
    mqttClient.publishAsync(MqttMessage("$baseTopic/\$state", newState.value, HOMIE_CONFIGURATION_MQTT_MESSAGES_QOS, true))
  }

  enum class State(val value: String) {
    INIT("init"), READY("ready"), DISCONNECTED("disconnected"), SLEEPING("sleeping"), LOST("lost"), ALERT("alert")
  }
}

data class HomieNode(
  val deviceId: String,
  val id: String,
  val name: String,
  val type: String,
  val properties: Map<String, HomieProperty>
) {

  private val baseTopic = "$HOMIE_PREFIX/$deviceId/$id"

  internal fun setup(mqttClient: MqttClient, homieReceiver: HomieReceiver) {
    mqttClient.publishSync(MqttMessage("$baseTopic/\$name", name, HOMIE_CONFIGURATION_MQTT_MESSAGES_QOS, true))
    mqttClient.publishSync(MqttMessage("$baseTopic/\$type", type, HOMIE_CONFIGURATION_MQTT_MESSAGES_QOS, true))
    mqttClient.publishSync(MqttMessage("$baseTopic/\$properties", properties.keys.joinToString(), HOMIE_CONFIGURATION_MQTT_MESSAGES_QOS, true))

    properties.values.forEach { it.setup(mqttClient, homieReceiver) }
  }
}

data class HomieProperty(
  val deviceId: String,
  val nodeId: String,
  val id: String,
  val name: String,
  val datatype: DataType,
  val format: String? = null,
  val settable: Boolean = false,
  val retained: Boolean = false,
  val unit: Unit = Unit.NONE
) {

  private val baseTopic = "$HOMIE_PREFIX/$deviceId/$nodeId/$id"
  private var mqttClient: MqttClient? = null

  internal fun setup(mqttClient: MqttClient, homieReceiver: HomieReceiver) {

    this.mqttClient = mqttClient

    mqttClient.publishSync(MqttMessage("$baseTopic/\$name", name, HOMIE_CONFIGURATION_MQTT_MESSAGES_QOS, true))
    mqttClient.publishSync(MqttMessage("$baseTopic/\$settable", settable.toString().toLowerCase(), HOMIE_CONFIGURATION_MQTT_MESSAGES_QOS, true))
    mqttClient.publishSync(MqttMessage("$baseTopic/\$retained", retained.toString().toLowerCase(), HOMIE_CONFIGURATION_MQTT_MESSAGES_QOS, true))
    mqttClient.publishSync(MqttMessage("$baseTopic/\$datatype", datatype.toString().toLowerCase(), HOMIE_CONFIGURATION_MQTT_MESSAGES_QOS, true))
    if (format != null) {
      mqttClient.publishSync(MqttMessage("$baseTopic/\$format", format.toString().toLowerCase(), HOMIE_CONFIGURATION_MQTT_MESSAGES_QOS, true))
    }
    if (unit != Unit.NONE) {
      mqttClient.publishSync(MqttMessage("$baseTopic/\$unit", unit.value, HOMIE_CONFIGURATION_MQTT_MESSAGES_QOS, true))
    }
    // TODO test: should read initial property value from MQTT when property is retained and value already exists
    if (retained) {
      mqttClient.read(baseTopic)?.let {
        homieReceiver.initProperty(nodeId, id, it)
      }
    }
    if (settable) {
      mqttClient.subscribeAsync("$baseTopic/set") { homieReceiver.propertySet(it.topic, it.payload) }
    }
  }

  fun onChange(newValue: String) {
    LOGGER.debug { "$deviceId.$nodeId.$id changed to $newValue" }
    (mqttClient ?: throw IllegalStateException("MQTT client is not instantiated. Call HomieDevice.connect() first."))
        .publishSync(MqttMessage(baseTopic, newValue, 0, retained))
  }

  enum class DataType {
    INTEGER, FLOAT, BOOLEAN, STRING, ENUM, COLOR
  }

  enum class Unit(val value: String) {
    CELSIUS("°C"), FAHRENHEIT("°F"), DEGREE("°"), LITER("L"), GALON("gal"), VOLTS("V"), WATT("W"), AMPERE("A"), PERCENT("%"),
    METER("m"), FEET("ft"), PASCAL("Pa"), PSI("psi"), COUNT("#"), NONE("_")
  }
}
