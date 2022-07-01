package com.mqgateway.homie

import com.mqgateway.core.device.PropertyInitializer
import com.mqgateway.homie.mqtt.MqttClient
import com.mqgateway.homie.mqtt.MqttClientFactory
import com.mqgateway.homie.mqtt.MqttClientStateException
import com.mqgateway.homie.mqtt.MqttMessage
import mu.KotlinLogging
import java.util.Locale

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
) : PropertyInitializer {

  private val baseTopic = "$HOMIE_PREFIX/$id"
  private var mqttClient: MqttClient? = null
  private val mqttConnectionListeners: MutableList<MqttConnectionListener> = mutableListOf()
  private var disconnectionPlanned: Boolean = false

  fun addMqttConnectedListener(listener: MqttConnectionListener) {
    mqttConnectionListeners.add(listener)
  }

  fun connect() {
    disconnectionPlanned = false
    val mqttClient = mqttClientFactory.create(id, { mqttConnectionListeners.forEach { it.onConnected() } }, { onDisconnected() })
    this.mqttClient = mqttClient
    LOGGER.info { "Connecting to MQTT" }
    mqttClient.connect(MqttMessage("$baseTopic/\$state", State.LOST.value, 1, true), true)

    LOGGER.info { "MQTT connection established" }

    LOGGER.debug { "Cleaning Homie configuration from MQTT" }
    val topicsToClean = mqttClient.findAllSubtopicsWithRetainedMessages(baseTopic)
    topicsToClean.forEach { topic ->
      LOGGER.debug { "Removing Homie configuration from topic: $topic" }
      mqttClient.publishSync(MqttMessage(topic, ""))
    }

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

  override fun initializeValues() {
    nodes.values.forEach { it.initializeValues(homieReceiver) }
  }

  fun disconnect() {
    disconnectionPlanned = true
    mqttClient?.disconnect()
  }

  private fun onDisconnected() {
    if (disconnectionPlanned) {
      LOGGER.error { "MQTT connection finished" }
    } else {
      LOGGER.error { "MQTT connection lost" }
    }
  }

  private fun changeState(newState: State) {
    (mqttClient ?: throw IllegalStateException("MQTT client is not instantiated. Call HomieDevice.connect() first."))
      .publishAsync(MqttMessage("$baseTopic/\$state", newState.value, HOMIE_CONFIGURATION_MQTT_MESSAGES_QOS, true))
  }

  enum class State(val value: String) {
    INIT("init"), READY("ready"), DISCONNECTED("disconnected"), SLEEPING("sleeping"), LOST("lost"), ALERT("alert")
  }

  interface MqttConnectionListener {
    fun onConnected()
    fun onDisconnect()
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

  internal fun initializeValues(homieReceiver: HomieReceiver) {
    properties.values.forEach { it.initializeValue(homieReceiver) }
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
    mqttClient.publishAsync(
      MqttMessage(
        "$baseTopic/\$settable",
        settable.toString().lowercase(Locale.getDefault()),
        HOMIE_CONFIGURATION_MQTT_MESSAGES_QOS,
        true
      )
    )
    mqttClient.publishAsync(
      MqttMessage(
        "$baseTopic/\$retained",
        retained.toString().lowercase(Locale.getDefault()),
        HOMIE_CONFIGURATION_MQTT_MESSAGES_QOS,
        true
      )
    )
    mqttClient.publishAsync(
      MqttMessage(
        "$baseTopic/\$datatype",
        datatype.toString().lowercase(Locale.getDefault()),
        HOMIE_CONFIGURATION_MQTT_MESSAGES_QOS,
        true
      )
    )
    if (format != null) {
      mqttClient.publishAsync(
        MqttMessage(
          "$baseTopic/\$format",
          format.toString().lowercase(Locale.getDefault()),
          HOMIE_CONFIGURATION_MQTT_MESSAGES_QOS,
          true
        )
      )
    }
    if (unit != Unit.NONE) {
      mqttClient.publishAsync(MqttMessage("$baseTopic/\$unit", unit.value, HOMIE_CONFIGURATION_MQTT_MESSAGES_QOS, true))
    }
    if (settable) {
      mqttClient.subscribeAsync("$baseTopic/set") { homieReceiver.propertySet(it.topic, it.payload) }
    }
  }

  internal fun initializeValue(homieReceiver: HomieReceiver) {
    if (retained) {
      LOGGER.debug { "Trying to read current status of ${this.nodeId}.${this.id} from MQTT" }
      (mqttClient ?: throw IllegalStateException("MQTT client is not instantiated. Call setup() first."))
        .read(baseTopic)?.let {
          homieReceiver.initProperty(nodeId, id, it)
        }
    }
  }

  fun onChange(newValue: String) {
    try {
      LOGGER.debug { "$deviceId.$nodeId.$id changed to $newValue" }
      (mqttClient ?: throw IllegalStateException("MQTT client is not instantiated. Call HomieDevice.connect() first."))
        .publishSync(MqttMessage(baseTopic, newValue, 0, retained))
    } catch (e: MqttClientStateException) {
      LOGGER.error(e) { "Unable to publish changed value to MQTT" }
    }
  }

  enum class DataType {
    INTEGER, FLOAT, BOOLEAN, STRING, ENUM, COLOR
  }

  enum class Unit(val value: String) {
    CELSIUS("°C"), FAHRENHEIT("°F"), DEGREE("°"), LITER("L"), GALON("gal"), VOLTS("V"), WATT("W"), AMPERE("A"), PERCENT("%"),
    METER("m"), FEET("ft"), PASCAL("Pa"), PSI("psi"), COUNT("#"), NONE("_")
  }
}
