package com.mqgateway.homie.gateway

import com.mqgateway.core.gatewayconfig.DeviceConfig
import com.mqgateway.core.gatewayconfig.DevicePropertyType.HUMIDITY
import com.mqgateway.core.gatewayconfig.DevicePropertyType.LAST_PING
import com.mqgateway.core.gatewayconfig.DevicePropertyType.PRESSURE
import com.mqgateway.core.gatewayconfig.DevicePropertyType.STATE
import com.mqgateway.core.gatewayconfig.DevicePropertyType.TEMPERATURE
import com.mqgateway.core.gatewayconfig.DevicePropertyType.UPTIME
import com.mqgateway.core.gatewayconfig.DeviceType
import com.mqgateway.core.gatewayconfig.Gateway
import com.mqgateway.homie.HomieDevice
import com.mqgateway.homie.HomieNode
import com.mqgateway.homie.HomieProperty
import com.mqgateway.homie.HomieProperty.DataType
import com.mqgateway.homie.HomieProperty.Unit
import com.mqgateway.homie.mqtt.MqttClientFactory
import mu.KotlinLogging
import java.net.NetworkInterface

private val LOGGER = KotlinLogging.logger {}

class HomieDeviceFactory(private val mqttClientFactory: MqttClientFactory, private val firmwareVersion: String) {

  companion object {
    private const val HOMIE_VERSION = "4.0.0"
    private const val HOMIE_IMPLEMENTATION = "Aetas"
    private const val FIRMWARE_NAME = "Aetas MqGateway"
  }

  fun toHomieDevice(gateway: Gateway, networkAdapter: String): HomieDevice {

    val homieNodes: Map<String, HomieNode> = gateway.rooms
        .flatMap { it.points }
        .flatMap { it.devices }
        .map { it.id to toHomieNode(gateway.name, it) }
        .toMap()

    val networkInterface = NetworkInterface.getByName(networkAdapter)
    return HomieDevice(
        mqttClientFactory,
        gateway.name,
        homieNodes,
        HOMIE_VERSION,
        gateway.name,
        listOf("org.homie.legacy-firmware"),
        HOMIE_IMPLEMENTATION,
        FIRMWARE_NAME,
        firmwareVersion,
        networkInterface?.inetAddresses?.asSequence()?.map { it.hostAddress }?.joinToString(),
        getMacAddress(networkInterface)
    )
  }

  private fun getMacAddress(networkInterface: NetworkInterface?): String {
    val mac = networkInterface?.hardwareAddress
    if (mac == null) {
      LOGGER.warn { "Could not find MAC address of network interface" }
      return ""
    }
    val sb = StringBuilder()
    for (i in mac.indices) {
      sb.append(String.format("%02X%s", mac[i], if (i < mac.size - 1) "-" else ""))
    }
    return sb.toString()
  }

  private fun toHomieNode(gatewayName: String, deviceConfig: DeviceConfig) =
      HomieNode(gatewayName, deviceConfig.id, deviceConfig.name, deviceConfig.type.name.toLowerCase(),
        getHomiePropertiesFor(gatewayName, deviceConfig.id, deviceConfig.type))

  private fun getHomiePropertiesFor(deviceName: String, nodeId: String, type: DeviceType): Map<String, HomieProperty> {
    return when (type) {
      DeviceType.RELAY ->
        mapOf(STATE.toString() to HomieProperty(deviceName, nodeId, STATE.toString(), STATE.toString(), DataType.ENUM, "ON,OFF",
          settable = true, retained = true))
      DeviceType.SWITCH_BUTTON ->
        mapOf(STATE.toString() to HomieProperty(deviceName, nodeId, STATE.toString(), STATE.toString(), DataType.ENUM, "PRESSED,RELEASED"))
      DeviceType.MOTION_DETECTOR ->
        mapOf(STATE.toString() to HomieProperty(deviceName, nodeId, STATE.toString(), STATE.toString(), DataType.ENUM, "ON,OFF"))
      DeviceType.BME280 -> mapOf(
        TEMPERATURE.toString() to HomieProperty(deviceName, nodeId, TEMPERATURE.toString(), TEMPERATURE.toString(), DataType.FLOAT, null,
          settable = false, retained = true, unit = Unit.CELSIUS),
        HUMIDITY.toString() to HomieProperty(deviceName, nodeId, HUMIDITY.toString(), HUMIDITY.toString(), DataType.FLOAT, "0:100",
          settable = false, retained = true, unit = Unit.PERCENT),
        PRESSURE.toString() to HomieProperty(deviceName, nodeId, PRESSURE.toString(), PRESSURE.toString(), DataType.INTEGER, null,
          settable = false, retained = true, unit = Unit.PASCAL),
        UPTIME.toString() to HomieProperty(deviceName, nodeId, UPTIME.toString(), UPTIME.toString(), DataType.INTEGER, null,
          settable = false, retained = false),
        LAST_PING.toString() to HomieProperty(deviceName, nodeId, LAST_PING.toString(), LAST_PING.toString(), DataType.STRING, null,
          settable = false, retained = true)
      )
      DeviceType.REED_SWITCH ->
        mapOf(STATE.toString() to HomieProperty(deviceName, nodeId, STATE.toString(), STATE.toString(), DataType.ENUM, "OPEN,CLOSED",
          retained = true))
      DeviceType.EMULATED_SWITCH ->
        mapOf(STATE.toString() to HomieProperty(deviceName, nodeId, STATE.toString(), STATE.toString(), DataType.ENUM, "PRESSED,RELEASED",
          settable = true, retained = false))
      DeviceType.DHT22 -> mapOf(
        TEMPERATURE.toString() to HomieProperty(deviceName, nodeId, TEMPERATURE.toString(), TEMPERATURE.toString(), DataType.FLOAT, null,
          settable = false, retained = true, unit = Unit.CELSIUS),
        HUMIDITY.toString() to HomieProperty(deviceName, nodeId, HUMIDITY.toString(), HUMIDITY.toString(), DataType.FLOAT, "0:100",
          settable = false, retained = true, unit = Unit.PERCENT),
        UPTIME.toString() to HomieProperty(deviceName, nodeId, UPTIME.toString(), UPTIME.toString(), DataType.INTEGER, null,
          settable = false, retained = false),
        LAST_PING.toString() to HomieProperty(deviceName, nodeId, LAST_PING.toString(), LAST_PING.toString(), DataType.STRING, null,
          settable = false, retained = true)
      )
      DeviceType.SCT013 -> TODO()
    }
  }
}
