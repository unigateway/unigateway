package com.mqgateway.core.hardware.raspberrypi

import com.diozero.api.AnalogInputDevice
import com.diozero.api.AnalogOutputDevice
import com.diozero.api.DebouncedDigitalInputDevice
import com.diozero.api.DigitalInputDevice
import com.diozero.api.DigitalOutputDevice
import com.diozero.api.GpioPullUpDown
import com.mqgateway.core.io.provider.HardwareInputOutputProvider

class RaspberryPiInputOutputProvider(
  private val platformConfiguration: RaspberryPiPlatformConfiguration
) : HardwareInputOutputProvider<RaspberryPiConnector> {

  override fun getBinaryInput(connector: RaspberryPiConnector): RaspberryPiDigitalPinInput {
    val pullUpDown = convert(connector.pullUpDown ?: platformConfiguration.defaultPullUpDown)
    val digitalInputDevice: DigitalInputDevice = DebouncedDigitalInputDevice.Builder
      .builder(connector.gpio, connector.debounceMs ?: platformConfiguration.defaultDebounceMs)
      .setPullUpDown(pullUpDown)
      .build()
    return RaspberryPiDigitalPinInput(digitalInputDevice)
  }

  override fun getBinaryOutput(connector: RaspberryPiConnector): RaspberryPiDigitalPinOutput {
    val digitalOutputDevice = DigitalOutputDevice.Builder.builder(connector.gpio)
      .build()
    return RaspberryPiDigitalPinOutput(digitalOutputDevice)
  }

  override fun getFloatInput(connector: RaspberryPiConnector): RaspberryPiAnalogPinInput {
    val analogInputDevice = AnalogInputDevice.Builder.builder(connector.gpio)
      .build()
    return RaspberryPiAnalogPinInput(analogInputDevice)
  }

  override fun getFloatOutput(connector: RaspberryPiConnector): RaspberryPiAnalogPinOutput {
    val analogOutputDevice = AnalogOutputDevice.Builder.builder(connector.gpio)
      .build()
    return RaspberryPiAnalogPinOutput(analogOutputDevice)
  }

  fun convert(pullUpDown: PullUpDown): GpioPullUpDown {
    return when (pullUpDown) {
      PullUpDown.PULL_UP -> {
        GpioPullUpDown.PULL_UP
      }
      PullUpDown.PULL_DOWN -> {
        GpioPullUpDown.PULL_DOWN
      }
    }
  }
}
