package com.mqgateway.core.gatewayconfig

import com.mqgateway.core.device.Device
import com.mqgateway.core.device.DeviceFactoryProvider
import com.mqgateway.core.device.DeviceRegistry
import com.mqgateway.core.device.DeviceType
import mu.KotlinLogging

private val LOGGER = KotlinLogging.logger {}

class DeviceRegistryFactory(
  private val deviceFactoryProvider: DeviceFactoryProvider
) {
  fun create(gatewayConfiguration: GatewayConfiguration): DeviceRegistry {
    LOGGER.info { "Creating devices based on configuration" }

    val uniGatewayDeviceConfiguration = DeviceConfiguration(gatewayConfiguration.id, gatewayConfiguration.name, DeviceType.UNIGATEWAY)
    val gatewayDevice = deviceFactoryProvider.getFactory(DeviceType.UNIGATEWAY).create(uniGatewayDeviceConfiguration, emptySet())

    val configuredDevices = gatewayConfiguration.devices
      .sortedBy { isComplexDevice(it) }
      .fold(emptySet<Device>()) { acc, deviceConfiguration ->
        acc + deviceFactoryProvider
          .getFactory(deviceConfiguration.type)
          .create(deviceConfiguration, acc)
      }.toSet()

    return DeviceRegistry(setOf(gatewayDevice) + configuredDevices)
  }

  private fun isComplexDevice(deviceConfiguration: DeviceConfiguration): Boolean {
    return deviceConfiguration.internalDevices.isNotEmpty()
  }
}
