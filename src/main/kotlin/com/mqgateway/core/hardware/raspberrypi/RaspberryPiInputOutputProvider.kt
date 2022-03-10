package com.mqgateway.core.hardware.raspberrypi

import com.diozero.api.AnalogInputDevice
import com.diozero.api.AnalogOutputDevice
import com.diozero.api.DebouncedDigitalInputDevice
import com.diozero.api.DigitalInputDevice
import com.diozero.api.DigitalOutputDevice
import com.mqgateway.core.io.provider.HardwareInputOutputProvider

class RaspberryPiInputOutputProvider(
  private val platformConfiguration: RaspberryPiPlatformConfiguration
) : HardwareInputOutputProvider<RaspberryPiConnector> {

  override fun getBinaryInput(connector: RaspberryPiConnector): RaspberryPiDigitalPinInput {
    val digitalInputDevice: DigitalInputDevice = DebouncedDigitalInputDevice.Builder
      .builder(connector.pin, connector.debounceMs ?: platformConfiguration.defaultDebounceMs)
      .build()
    return RaspberryPiDigitalPinInput(digitalInputDevice)
  }

  override fun getBinaryOutput(connector: RaspberryPiConnector): RaspberryPiDigitalPinOutput {
    val digitalOutputDevice = DigitalOutputDevice.Builder.builder(connector.pin)
      .build()
    return RaspberryPiDigitalPinOutput(digitalOutputDevice)
  }

  override fun getFloatInput(connector: RaspberryPiConnector): RaspberryPiAnalogPinInput {
    val analogInputDevice = AnalogInputDevice.Builder.builder(connector.pin)
      .build()
    return RaspberryPiAnalogPinInput(analogInputDevice)
  }

  override fun getFloatOutput(connector: RaspberryPiConnector): RaspberryPiAnalogPinOutput {
    val analogOutputDevice = AnalogOutputDevice.Builder.builder(connector.pin)
      .build()
    return RaspberryPiAnalogPinOutput(analogOutputDevice)
  }
}
