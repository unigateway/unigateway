package com.mqgateway.homie.gateway

import com.mqgateway.core.device.DataType
import com.mqgateway.core.device.DataUnit
import com.mqgateway.core.device.Device
import com.mqgateway.core.device.DeviceRegistry
import com.mqgateway.homie.HomieDevice
import com.mqgateway.homie.HomieNode
import com.mqgateway.homie.HomieProperty
import com.mqgateway.homie.HomieReceiver
import com.mqgateway.homie.mqtt.MqttClientFactory
import mu.KotlinLogging
import java.net.NetworkInterface
import java.util.Locale

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

  fun toHomieDevice(deviceRegistry: DeviceRegistry, networkAdapter: String): HomieDevice {
    val uniGatewayDevice = deviceRegistry.getUniGatewayDevice()
    val homieNodes: Map<String, HomieNode> = deviceRegistry.devices
      .associate { it.id to toHomieNode(uniGatewayDevice.id, it) }

    val networkInterface = NetworkInterface.getByName(networkAdapter)
    return HomieDevice(
      mqttClientFactory,
      homieReceiver,
      uniGatewayDevice.id,
      homieNodes,
      HOMIE_VERSION,
      uniGatewayDevice.name,
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

  private fun toHomieNode(gatewayId: String, device: Device) =
    HomieNode(
      gatewayId,
      device.id,
      device.name,
      device.type.name.lowercase(Locale.getDefault()),
      getHomiePropertiesFor(gatewayId, device)
    )

  private fun getHomiePropertiesFor(gatewayId: String, device: Device): Map<String, HomieProperty> {
    return device.properties.map { property ->
      property.toString() to HomieProperty(
        gatewayId,
        device.id,
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
