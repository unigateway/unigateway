package com.mqgateway.core.gatewayconfig

import com.mqgateway.core.device.DeviceFactoryProvider
import com.mqgateway.core.device.DeviceRegistry
import com.mqgateway.core.device.DeviceType

class DeviceRegistryFactory(
  private val deviceFactoryProvider: DeviceFactoryProvider
) {
  fun create(gatewayConfiguration: GatewayConfiguration): DeviceRegistry {
    val uniGatewayDeviceConfiguration = DeviceConfiguration(gatewayConfiguration.id, gatewayConfiguration.name, DeviceType.UNIGATEWAY)
    val gatewayDevice = deviceFactoryProvider.getFactory(DeviceType.UNIGATEWAY).create(uniGatewayDeviceConfiguration)

    val configuredDevices = gatewayConfiguration.devices.map {
      deviceFactoryProvider
        .getFactory(it.type)
        .create(it)
    }.toSet()

    return DeviceRegistry(setOf(gatewayDevice) + configuredDevices)
  }
}
