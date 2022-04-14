package com.mqgateway.core.device.timerswitch

import com.mqgateway.core.device.Device
import com.mqgateway.core.device.DeviceFactory
import com.mqgateway.core.device.MissingConnectorInDeviceConfigurationException
import com.mqgateway.core.gatewayconfig.DeviceConfiguration
import com.mqgateway.core.device.DeviceType
import com.mqgateway.core.io.provider.InputOutputProvider
import com.mqgateway.core.utils.TimersScheduler

class TimerSwitchRelayDeviceFactory(
  private val ioProvider: InputOutputProvider<*>,
  private val timersScheduler: TimersScheduler
) : DeviceFactory<TimerSwitchRelayDevice> {

  override fun deviceType(): DeviceType {
    return DeviceType.TIMER_SWITCH
  }

  override fun create(deviceConfiguration: DeviceConfiguration, devices: Set<Device>): TimerSwitchRelayDevice {
    val connector = deviceConfiguration.connectors[STATE_CONNECTOR]
      ?: throw MissingConnectorInDeviceConfigurationException(deviceConfiguration.id, STATE_CONNECTOR)
    val stateBinaryOutput = ioProvider.getBinaryOutput(connector)
    return TimerSwitchRelayDevice(deviceConfiguration.id, deviceConfiguration.name, stateBinaryOutput, timersScheduler, deviceConfiguration.config)
  }

  companion object {
    private const val STATE_CONNECTOR = "state"
  }
}
