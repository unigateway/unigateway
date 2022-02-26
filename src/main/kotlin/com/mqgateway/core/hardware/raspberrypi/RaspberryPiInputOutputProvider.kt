package com.mqgateway.core.hardware.raspberrypi

import com.mqgateway.core.io.provider.HardwareInputOutputProvider

class RaspberryPiInputOutputProvider : HardwareInputOutputProvider<RaspberryPiConnector> {

  override fun getBinaryInput(connector: RaspberryPiConnector): RaspberryPiDigitalPinInput {
    TODO("Not yet implemented")
  }

  override fun getBinaryOutput(connector: RaspberryPiConnector): RaspberryPiDigitalPinOutput {
    TODO("Not yet implemented")
  }

  override fun getFloatInput(connector: RaspberryPiConnector): RaspberryPiAnalogPinInput {
    TODO("Not yet implemented")
  }

  override fun getFloatOutput(connector: RaspberryPiConnector): RaspberryPiAnalogPinOutput {
    TODO("Not yet implemented")
  }
}
