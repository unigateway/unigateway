package com.mqgateway.core.hardware.mqgateway

import com.diozero.api.DigitalInputDevice
import com.diozero.api.DigitalInputEvent
import com.diozero.api.DigitalOutputDevice
import com.mqgateway.core.io.BinaryInput
import com.mqgateway.core.io.BinaryOutput
import com.mqgateway.core.io.BinaryState
import com.mqgateway.core.io.BinaryStateChangeEvent
import com.mqgateway.core.io.BinaryStateListener

class MqGatewayDigitalPinInput(private val digitalInputDevice: DigitalInputDevice) : BinaryInput {
  override fun addListener(listener: BinaryStateListener) {
    digitalInputDevice.addListener { listener.handle(MqGatewayBinaryStateChangeEvent(it)) }
  }

  override fun getState(): BinaryState = if (digitalInputDevice.value) BinaryState.HIGH else BinaryState.LOW
}

class MqGatewayDigitalPinOutput(private val digitalOutputDevice: DigitalOutputDevice) : BinaryOutput {
  override fun setState(newState: BinaryState) {
    val booleanState = newState == BinaryState.HIGH
    digitalOutputDevice.setValue(booleanState)
  }
}

data class MqGatewayBinaryStateChangeEvent(val event: DigitalInputEvent) : BinaryStateChangeEvent {
  override fun newState(): BinaryState = if (event.value) BinaryState.HIGH else BinaryState.LOW
}
