package com.unigateway.core.device

import com.unigateway.core.device.emulatedswitch.EmulatedSwitchButtonDeviceFactory
import com.unigateway.core.device.gate.GateDeviceFactory
import com.unigateway.core.device.motiondetector.MotionSensorDeviceFactory
import com.unigateway.core.device.reedswitch.ReedSwitchDeviceFactory
import com.unigateway.core.device.relay.RelayDeviceFactory
import com.unigateway.core.device.shutter.ShutterDeviceFactory
import com.unigateway.core.device.switchbutton.SwitchButtonDeviceFactory
import com.unigateway.core.device.humidity.HumidityDeviceFactory
import com.unigateway.core.device.temperature.TemperatureDeviceFactory
import com.unigateway.core.device.timerswitch.TimerSwitchRelayDeviceFactory
import com.unigateway.core.device.unigateway.UniGatewayDeviceFactory
import com.unigateway.core.io.provider.InputOutputProvider
import com.unigateway.core.utils.SystemInfoProvider
import com.unigateway.core.utils.TimersScheduler

class DeviceFactoryProvider(
  ioProvider: InputOutputProvider<*>,
  timersScheduler: TimersScheduler,
  systemInfoProvider: SystemInfoProvider
) {

  private val deviceFactories: Map<DeviceType, DeviceFactory<*>> = listOf(
    UniGatewayDeviceFactory(systemInfoProvider),

    EmulatedSwitchButtonDeviceFactory(ioProvider),
    GateDeviceFactory(),
    MotionSensorDeviceFactory(ioProvider),
    ReedSwitchDeviceFactory(ioProvider),
    RelayDeviceFactory(ioProvider),
    ShutterDeviceFactory(),
    SwitchButtonDeviceFactory(ioProvider),
    TimerSwitchRelayDeviceFactory(ioProvider, timersScheduler),
    TemperatureDeviceFactory(ioProvider),
    HumidityDeviceFactory(ioProvider),
  ).associateBy { it.deviceType() }

  fun getFactory(deviceType: DeviceType): DeviceFactory<*> {
    return deviceFactories[deviceType] ?: throw IllegalStateException("No factory found for device type: $deviceType")
  }
}
