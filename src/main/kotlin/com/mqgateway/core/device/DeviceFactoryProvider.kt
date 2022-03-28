package com.mqgateway.core.device

import com.mqgateway.core.device.emulatedswitch.EmulatedSwitchButtonDeviceFactory
import com.mqgateway.core.device.gate.GateDeviceFactory
import com.mqgateway.core.device.shutter.ShutterDeviceFactory
import com.mqgateway.core.device.unigateway.UniGatewayDeviceFactory
import com.mqgateway.core.device.motiondetector.MotionSensorDeviceFactory
import com.mqgateway.core.device.reedswitch.ReedSwitchDeviceFactory
import com.mqgateway.core.device.relay.RelayDeviceFactory
import com.mqgateway.core.device.switchbutton.SwitchButtonDeviceFactory
import com.mqgateway.core.device.timerswitch.TimerSwitchRelayDeviceFactory
import com.mqgateway.core.io.provider.InputOutputProvider
import com.mqgateway.core.utils.SystemInfoProvider
import com.mqgateway.core.utils.TimersScheduler

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
    TimerSwitchRelayDeviceFactory(ioProvider, timersScheduler)
  ).associateBy { it.deviceType() }

  fun getFactory(deviceType: DeviceType): DeviceFactory<*> {
    return deviceFactories[deviceType] ?: throw IllegalStateException("No factory found for device type: $deviceType")
  }
}
