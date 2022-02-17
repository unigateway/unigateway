package com.mqgateway.core.gatewayconfig.homeassistant

import com.mqgateway.core.gatewayconfig.DeviceConfiguration
import com.mqgateway.core.gatewayconfig.DevicePropertyType
import com.mqgateway.core.gatewayconfig.DeviceType
import com.mqgateway.core.gatewayconfig.GatewayConfiguration
import com.mqgateway.homie.HOMIE_PREFIX
import mu.KotlinLogging

private val LOGGER = KotlinLogging.logger {}

class HomeAssistantConverter(private val gatewayFirmwareVersion: String) {

  fun convert(gatewayConfiguration: GatewayConfiguration): List<HomeAssistantComponent> {
    LOGGER.info { "Converting Gateway configuration to HomeAssistant auto-discovery config" }
    val devices = gatewayConfiguration.devices

    val mqGatewayCoreComponents = convertMqGatewayRootDeviceToHaSensors(gatewayConfiguration)

    return mqGatewayCoreComponents + devices.flatMap { device ->
      val haDevice = HomeAssistantDevice(
        identifiers = listOf("${gatewayConfiguration.name}_${device.id}"),
        name = device.name,
        manufacturer = "Aetas",
        viaDevice = gatewayConfiguration.name,
        firmwareVersion = gatewayFirmwareVersion,
        model = "MqGateway ${device.type.name}"
      )
      val basicProperties = HomeAssistantComponentBasicProperties(haDevice, gatewayConfiguration.name, device.id)

      return@flatMap toHomeAssistantComponents(device, haDevice, basicProperties, gatewayConfiguration)
    }
  }

  private fun toHomeAssistantComponents(
    device: DeviceConfiguration,
    haDevice: HomeAssistantDevice,
    basicProperties: HomeAssistantComponentBasicProperties,
    gatewayConfiguration: GatewayConfiguration
  ): List<HomeAssistantComponent> {
    TODO()
  }

  private fun convertMqGatewayRootDeviceToHaSensors(gatewayConfiguration: GatewayConfiguration): List<HomeAssistantSensor> {
    val rootHaDevice = HomeAssistantDevice(
      identifiers = listOf(gatewayConfiguration.name),
      name = gatewayConfiguration.name,
      manufacturer = "Aetas",
      firmwareVersion = gatewayFirmwareVersion,
      model = "MqGateway"
    )
    val availabilityTopic = "$HOMIE_PREFIX/${gatewayConfiguration.name}/\$state"
    val availabilityOnline = "ready"
    val availabilityOffline = "lost"
    return listOf(
      HomeAssistantSensor(
        HomeAssistantComponentBasicProperties(rootHaDevice, gatewayConfiguration.name, "${gatewayConfiguration.name}_CPU_TEMPERATURE"),
        "CPU temperature",
        availabilityTopic,
        availabilityOnline,
        availabilityOffline,
        HomeAssistantSensor.DeviceClass.TEMPERATURE,
        homieStateTopic(gatewayConfiguration, gatewayConfiguration.name, DevicePropertyType.TEMPERATURE),
        DeviceType.MQGATEWAY.property(DevicePropertyType.TEMPERATURE).unit.value
      ),
      HomeAssistantSensor(
        HomeAssistantComponentBasicProperties(rootHaDevice, gatewayConfiguration.name, "${gatewayConfiguration.name}_MEMORY_FREE"),
        "Free memory",
        availabilityTopic,
        availabilityOnline,
        availabilityOffline,
        HomeAssistantSensor.DeviceClass.NONE,
        homieStateTopic(gatewayConfiguration, gatewayConfiguration.name, DevicePropertyType.MEMORY),
        DeviceType.MQGATEWAY.property(DevicePropertyType.MEMORY).unit.value
      ),
      HomeAssistantSensor(
        HomeAssistantComponentBasicProperties(rootHaDevice, gatewayConfiguration.name, "${gatewayConfiguration.name}_UPTIME"),
        "Uptime",
        availabilityTopic,
        availabilityOnline,
        availabilityOffline,
        HomeAssistantSensor.DeviceClass.NONE,
        homieStateTopic(gatewayConfiguration, gatewayConfiguration.name, DevicePropertyType.UPTIME),
        DeviceType.MQGATEWAY.property(DevicePropertyType.UPTIME).unit.value
      ),
      HomeAssistantSensor(
        HomeAssistantComponentBasicProperties(rootHaDevice, gatewayConfiguration.name, "${gatewayConfiguration.name}_IP_ADDRESS"),
        "IP address",
        availabilityTopic,
        availabilityOnline,
        availabilityOffline,
        HomeAssistantSensor.DeviceClass.NONE,
        homieStateTopic(gatewayConfiguration, gatewayConfiguration.name, DevicePropertyType.IP_ADDRESS),
        DeviceType.MQGATEWAY.property(DevicePropertyType.IP_ADDRESS).unit.value
      )
    )
  }

  private fun homieStateTopic(gatewayConfiguration: GatewayConfiguration, deviceId: String, propertyType: DevicePropertyType): String {
    return "$HOMIE_PREFIX/${gatewayConfiguration.name}/$deviceId/$propertyType"
  }

  private fun homieCommandTopic(gatewayConfiguration: GatewayConfiguration, device: DeviceConfiguration, propertyType: DevicePropertyType): String {
    return homieStateTopic(gatewayConfiguration, device.id, propertyType) + "/set"
  }

  companion object {
    const val DEVICE_CONFIG_HA_COMPONENT: String = "haComponent"
    const val DEVICE_CONFIG_HA_DEVICE_CLASS: String = "haDeviceClass"
  }
}
