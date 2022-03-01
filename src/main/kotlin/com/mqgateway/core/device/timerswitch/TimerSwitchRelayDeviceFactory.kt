package com.mqgateway.core.device.timerswitch

import com.mqgateway.core.device.DeviceFactory
import com.mqgateway.core.gatewayconfig.DeviceConfiguration
import com.mqgateway.core.gatewayconfig.DeviceType
import com.mqgateway.core.io.provider.InputOutputProvider
import com.mqgateway.core.utils.TimersScheduler

private const val STATE_CONNECTOR = "state"

class TimerSwitchRelayDeviceFactory(
  private val ioProvider: InputOutputProvider<*>,
  private val timersScheduler: TimersScheduler
) : DeviceFactory<TimerSwitchRelayDevice> {

  override fun deviceType(): DeviceType {
    return DeviceType.TIMER_SWITCH
  }

  override fun create(deviceConfiguration: DeviceConfiguration): TimerSwitchRelayDevice {
    val stateBinaryOutput = ioProvider.getBinaryOutput(deviceConfiguration.connectors[STATE_CONNECTOR]!!)
    return TimerSwitchRelayDevice(deviceConfiguration.id, stateBinaryOutput, timersScheduler)
  }
}
