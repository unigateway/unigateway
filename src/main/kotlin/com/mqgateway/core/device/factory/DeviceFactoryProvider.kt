package com.mqgateway.core.device.factory

import GateDeviceFactory
import com.mqgateway.core.gatewayconfig.DeviceType
import com.mqgateway.core.io.provider.InputOutputProvider
import com.mqgateway.core.utils.SystemInfoProvider
import com.mqgateway.core.utils.TimersScheduler

class DeviceFactoryProvider(
  ioProvider: InputOutputProvider<*>,
  timersScheduler: TimersScheduler,
  systemInfoProvider: SystemInfoProvider
) {

  private val deviceFactories: Map<DeviceType, DeviceFactory<*>> = listOf(
    MqGatewayDeviceFactory(systemInfoProvider),

    EmulatedSwitchButtonDeviceFactory(ioProvider),
    GateDeviceFactory(),
    MotionSensorDeviceFactory(ioProvider),
    ReedSwitchDeviceFactory(ioProvider),
    RelayDeviceFactory(ioProvider),
    ShutterDeviceFactory(),
    SwitchButtonDeviceFactory(ioProvider),
    TimerSwitchRelayDeviceFactory(ioProvider, timersScheduler)
  ).associateBy { it.deviceType() }

  fun getFactory(deviceType: DeviceType): DeviceFactory<*> {
    return deviceFactories[deviceType] ?: throw IllegalStateException("No factory found for device type: $deviceType")
  }
}
