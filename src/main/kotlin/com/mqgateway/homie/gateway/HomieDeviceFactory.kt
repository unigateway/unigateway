package com.mqgateway.homie.gateway

import com.mqgateway.core.gatewayconfig.DataType
import com.mqgateway.core.gatewayconfig.DataUnit
import com.mqgateway.core.gatewayconfig.DeviceConfiguration
import com.mqgateway.core.gatewayconfig.DeviceType
import com.mqgateway.core.gatewayconfig.GatewayConfiguration
import com.mqgateway.homie.HomieDevice
import com.mqgateway.homie.HomieNode
import com.mqgateway.homie.HomieProperty
import com.mqgateway.homie.HomieReceiver
import com.mqgateway.homie.mqtt.MqttClientFactory
import mu.KotlinLogging
import java.net.NetworkInterface
import java.util.*

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

  fun toHomieDevice(gatewayConfiguration: GatewayConfiguration, networkAdapter: String): HomieDevice {
    val mqGatewayAsNode = HomieNode(
      gatewayConfiguration.id,
      gatewayConfiguration.id,
      "MqGateway ${gatewayConfiguration.name}",
      DeviceType.UNIGATEWAY.name.lowercase(Locale.getDefault()),
      getHomiePropertiesFor(gatewayConfiguration.id, gatewayConfiguration.id, DeviceType.UNIGATEWAY)
    )

    val homieNodes: Map<String, HomieNode> = gatewayConfiguration.devices
      .associate { it.id to toHomieNode(gatewayConfiguration.id, it) } + (gatewayConfiguration.id to mqGatewayAsNode)

    val networkInterface = NetworkInterface.getByName(networkAdapter)
    return HomieDevice(
      mqttClientFactory,
      homieReceiver,
      gatewayConfiguration.id,
      homieNodes,
      HOMIE_VERSION,
      gatewayConfiguration.name,
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

  private fun toHomieNode(gatewayId: String, deviceConfiguration: DeviceConfiguration) =
    HomieNode(
      gatewayId,
      deviceConfiguration.id,
      deviceConfiguration.name,
      deviceConfiguration.type.name.lowercase(Locale.getDefault()),
      getHomiePropertiesFor(gatewayId, deviceConfiguration.id, deviceConfiguration.type)
    )

  private fun getHomiePropertiesFor(deviceId: String, nodeId: String, type: DeviceType): Map<String, HomieProperty> {
    return type.properties.map { property ->
      property.toString() to HomieProperty(
        deviceId,
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
