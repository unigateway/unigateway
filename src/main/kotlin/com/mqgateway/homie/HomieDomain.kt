package com.mqgateway.homie

import com.mqgateway.homie.mqtt.MqttClient
import com.mqgateway.homie.mqtt.MqttClientFactory
import com.mqgateway.homie.mqtt.MqttMessage
import mu.KotlinLogging

const val HOMIE_PREFIX = "homie"

private val LOGGER = KotlinLogging.logger {}

const val HOMIE_CONFIGURATION_MQTT_MESSAGES_QOS = 1

class HomieDevice(
  private val mqttClientFactory: MqttClientFactory,
  private val homieReceiver: HomieReceiver,
  val id: String,
  val nodes: Map<String, HomieNode>,
  val homie: String,
  val name: String,
  private val extensions: List<String> = emptyList(),
  private val implementation: String? = null,
  private val firmwareName: String?,
  private val firmwareVersion: String?,
  private val ip: String? = null,
  private val mac: String? = null
) {

  private val baseTopic = "$HOMIE_PREFIX/$id"
  private var mqttClient: MqttClient? = null
  private val mqttConnectedListeners: MutableList<() -> Unit> = mutableListOf()

  fun addMqttConnectedListener(listener: () -> Unit) {
    mqttConnectedListeners.add(listener)
  }

  fun connect() {
    val mqttClient = mqttClientFactory.create(id, { mqttConnectedListeners.forEach { it() } }, { onDisconnected() })
    this.mqttClient = mqttClient
    LOGGER.info { "Connecting to MQTT" }
    mqttClient.connect(MqttMessage("$baseTopic/\$state", State.LOST.value, 1, true), true)

    LOGGER.info { "MQTT connection established" }

    LOGGER.debug { "Publishing Homie configuration to MQTT" }
    changeState(State.INIT)
    mqttClient.publishAsync(MqttMessage("$baseTopic/\$homie", homie, HOMIE_CONFIGURATION_MQTT_MESSAGES_QOS, true))
    mqttClient.publishAsync(MqttMessage("$baseTopic/\$name", name, HOMIE_CONFIGURATION_MQTT_MESSAGES_QOS, true))
    mqttClient.publishAsync(MqttMessage("$baseTopic/\$extensions", extensions.joinToString(), HOMIE_CONFIGURATION_MQTT_MESSAGES_QOS, true))
    mqttClient.publishAsync(MqttMessage("$baseTopic/\$nodes", nodes.keys.joinToString(), HOMIE_CONFIGURATION_MQTT_MESSAGES_QOS, true))
    if (implementation != null) {
      mqttClient.publishAsync(MqttMessage("$baseTopic/\$implementation", implementation, HOMIE_CONFIGURATION_MQTT_MESSAGES_QOS, true))
    }
    if (firmwareName != null) {
      mqttClient.publishAsync(MqttMessage("$baseTopic/\$fw/name", firmwareName, HOMIE_CONFIGURATION_MQTT_MESSAGES_QOS, true))
    }
    if (firmwareVersion != null) {
      mqttClient.publishAsync(MqttMessage("$baseTopic/\$fw/version", firmwareVersion, HOMIE_CONFIGURATION_MQTT_MESSAGES_QOS, true))
    }
    if (ip != null) {
      mqttClient.publishAsync(MqttMessage("$baseTopic/\$localip", ip, HOMIE_CONFIGURATION_MQTT_MESSAGES_QOS, true))
    }
    if (mac != null) {
      mqttClient.publishAsync(MqttMessage("$baseTopic/\$mac", mac, HOMIE_CONFIGURATION_MQTT_MESSAGES_QOS, true))
    }
    nodes.values.forEach { it.setup(mqttClient, homieReceiver) }
    changeState(State.READY)
    LOGGER.debug { "Homie configuration published" }
  }

  private fun onDisconnected() {
    LOGGER.error { "MQTT connection lost" }
  }

  private fun changeState(newState: State) {
    (mqttClient ?: throw IllegalStateException("MQTT client is not instantiated. Call HomieDevice.connect() first."))
      .publishAsync(MqttMessage("$baseTopic/\$state", newState.value, HOMIE_CONFIGURATION_MQTT_MESSAGES_QOS, true))
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
    mqttClient.publishAsync(MqttMessage("$baseTopic/\$name", name, HOMIE_CONFIGURATION_MQTT_MESSAGES_QOS, true))
    mqttClient.publishAsync(MqttMessage("$baseTopic/\$type", type, HOMIE_CONFIGURATION_MQTT_MESSAGES_QOS, true))
    mqttClient.publishAsync(MqttMessage("$baseTopic/\$properties", properties.keys.joinToString(), HOMIE_CONFIGURATION_MQTT_MESSAGES_QOS, true))

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

    mqttClient.publishAsync(MqttMessage("$baseTopic/\$name", name, HOMIE_CONFIGURATION_MQTT_MESSAGES_QOS, true))
    mqttClient.publishAsync(MqttMessage("$baseTopic/\$settable", settable.toString().toLowerCase(), HOMIE_CONFIGURATION_MQTT_MESSAGES_QOS, true))
    mqttClient.publishAsync(MqttMessage("$baseTopic/\$retained", retained.toString().toLowerCase(), HOMIE_CONFIGURATION_MQTT_MESSAGES_QOS, true))
    mqttClient.publishAsync(MqttMessage("$baseTopic/\$datatype", datatype.toString().toLowerCase(), HOMIE_CONFIGURATION_MQTT_MESSAGES_QOS, true))
    if (format != null) {
      mqttClient.publishAsync(MqttMessage("$baseTopic/\$format", format.toString().toLowerCase(), HOMIE_CONFIGURATION_MQTT_MESSAGES_QOS, true))
    }
    if (unit != Unit.NONE) {
      mqttClient.publishAsync(MqttMessage("$baseTopic/\$unit", unit.value, HOMIE_CONFIGURATION_MQTT_MESSAGES_QOS, true))
    }
    if (retained) {
      LOGGER.debug { "Trying to read current status of ${this.nodeId}.${this.id} from MQTT" }
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
