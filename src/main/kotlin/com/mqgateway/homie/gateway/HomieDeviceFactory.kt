package com.mqgateway.homie.gateway

import com.mqgateway.core.gatewayconfig.DataType
import com.mqgateway.core.gatewayconfig.DataUnit
import com.mqgateway.core.gatewayconfig.DeviceConfig
import com.mqgateway.core.gatewayconfig.DeviceType
import com.mqgateway.core.gatewayconfig.Gateway
import com.mqgateway.homie.HomieDevice
import com.mqgateway.homie.HomieNode
import com.mqgateway.homie.HomieProperty
import com.mqgateway.homie.HomieReceiver
import com.mqgateway.homie.mqtt.MqttClientFactory
import mu.KotlinLogging
import java.net.NetworkInterface

private val LOGGER = KotlinLogging.logger {}

class HomieDeviceFactory(
  private val mqttClientFactory: MqttClientFactory,
  private val homieReceiver: HomieReceiver,
  private val firmwareVersion: String
) {

  companion object {
    private const val HOMIE_VERSION = "4.0.0"
    private const val HOMIE_IMPLEMENTATION = "Aetas"
    private const val FIRMWARE_NAME = "Aetas MqGateway"
  }

  fun toHomieDevice(gateway: Gateway, networkAdapter: String): HomieDevice {
    val mqGatewayAsNode = HomieNode(
      gateway.name,
      gateway.name,
      "MqGateway ${gateway.name}",
      DeviceType.MQGATEWAY.name.toLowerCase(),
      getHomiePropertiesFor(gateway.name, gateway.name, DeviceType.MQGATEWAY)
    )

    val homieNodes: Map<String, HomieNode> = gateway.rooms
      .flatMap { it.points }
      .flatMap { it.devices }
      .map { it.id to toHomieNode(gateway.name, it) }
      .toMap() + (gateway.name to mqGatewayAsNode)

    val networkInterface = NetworkInterface.getByName(networkAdapter)
    return HomieDevice(
      mqttClientFactory,
      homieReceiver,
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
    HomieNode(
      gatewayName,
      deviceConfig.id,
      deviceConfig.name,
      deviceConfig.type.name.toLowerCase(),
      getHomiePropertiesFor(gatewayName, deviceConfig.id, deviceConfig.type)
    )

  private fun getHomiePropertiesFor(deviceName: String, nodeId: String, type: DeviceType): Map<String, HomieProperty> {
    return type.properties.map { property ->
      property.toString() to HomieProperty(
        deviceName,
        nodeId,
        property.toString(),
        property.toString(),
        homieDataType(property.dataType),
        property.format,
        property.settable,
        property.retained,
        homieDataUnit(property.unit)
      )
    }.toMap()
  }

  private fun homieDataType(dataType: DataType): HomieProperty.DataType {
    return HomieProperty.DataType.values().find { it.toString() == dataType.toString() } ?: HomieProperty.DataType.STRING
  }

  private fun homieDataUnit(unit: DataUnit): HomieProperty.Unit {
    return HomieProperty.Unit.values().find { it.toString() == unit.toString() } ?: HomieProperty.Unit.NONE
  }
}
