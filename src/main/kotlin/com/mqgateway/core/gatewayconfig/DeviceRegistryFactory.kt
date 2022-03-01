package com.mqgateway.core.gatewayconfig

import com.mqgateway.core.device.DeviceFactoryProvider
import com.mqgateway.core.device.DeviceRegistry

class DeviceRegistryFactory(
  private val deviceFactoryProvider: DeviceFactoryProvider
) {
  fun create(gatewayConfiguration: GatewayConfiguration): DeviceRegistry {
    val mqGatewayDeviceConfiguration = DeviceConfiguration(gatewayConfiguration.name, gatewayConfiguration.name, DeviceType.MQGATEWAY)
    val gatewayDevice = deviceFactoryProvider.getFactory(DeviceType.MQGATEWAY).create(mqGatewayDeviceConfiguration)

    val configuredDevices = gatewayConfiguration.devices.map {
      deviceFactoryProvider
        .getFactory(it.type)
        .create(it)
    }.toSet()

    return DeviceRegistry(setOf(gatewayDevice) + configuredDevices)
  }
}
