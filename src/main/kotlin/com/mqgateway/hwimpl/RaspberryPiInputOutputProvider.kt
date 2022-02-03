package com.mqgateway.hwimpl

import com.diozero.api.DigitalInputDevice
import com.diozero.api.DigitalInputEvent
import com.diozero.api.GpioPullUpDown
import com.mqgateway.core.hardware.io.BinaryInput
import com.mqgateway.core.hardware.io.BinaryOutput
import com.mqgateway.core.hardware.io.BinaryState
import com.mqgateway.core.hardware.io.BinaryStateChangeEvent
import com.mqgateway.core.hardware.io.BinaryStateListener
import com.mqgateway.core.hardware.io.FloatInput
import com.mqgateway.core.hardware.provider.ConnectorConfiguration
import com.mqgateway.core.hardware.provider.HardwareInputOutputProvider

class RaspberryPiInputOutputProvider : HardwareInputOutputProvider {

  override fun getBinaryInput(connectorConfiguration: ConnectorConfiguration): RaspberryPiDigitalPinInput {
    val config = connectorConfiguration as RaspberryPiConnectorConfiguration
    val digitalInputDevice: DigitalInputDevice = DigitalInputDevice.Builder.builder(config.pinNumber)
      .setPullUpDown(GpioPullUpDown.PULL_UP)
      .build()
    return RaspberryPiDigitalPinInput(digitalInputDevice)
  }

  override fun getBinaryOutput(connectorConfiguration: ConnectorConfiguration): BinaryOutput {
    TODO("Not yet implemented")
  }

  override fun getFloatInput(connectorConfiguration: ConnectorConfiguration): FloatInput {
    TODO("Not yet implemented")
  }
}

data class RaspberryPiConnectorConfiguration(
  val pinNumber: Int
) : ConnectorConfiguration

class RaspberryPiDigitalPinInput(private val digitalInputDevice: DigitalInputDevice) : BinaryInput {

  override fun addListener(listener: BinaryStateListener) {
    digitalInputDevice.addListener { listener.handleBinaryStateChangeEvent(RaspberryPiBinaryStateChangeEvent(it)) }
  }

  override fun getState() = if (digitalInputDevice.value) BinaryState.HIGH else BinaryState.LOW
}

class RaspberryPiBinaryStateChangeEvent(private val event: DigitalInputEvent) : BinaryStateChangeEvent {
  override fun getState() = if (event.value) BinaryState.HIGH else BinaryState.LOW
}
