package com.unigateway.core.hardware.raspberrypi

import com.diozero.api.DigitalInputDevice
import com.diozero.api.DigitalInputEvent
import com.diozero.api.DigitalOutputDevice
import com.unigateway.core.io.BinaryInput
import com.unigateway.core.io.BinaryOutput
import com.unigateway.core.io.BinaryState
import com.unigateway.core.io.BinaryStateChangeEvent
import com.unigateway.core.io.BinaryStateListener

class RaspberryPiDigitalPinInput(private val digitalInputDevice: DigitalInputDevice) : BinaryInput {

  override fun addListener(listener: BinaryStateListener) {
    digitalInputDevice.addListener { event -> listener.handle(RaspberryPiBinaryStateChangeEvent(event)) }
  }

  override fun getState(): BinaryState {
    return BinaryStateConverter.fromBoolean(digitalInputDevice.value)
  }
}

class RaspberryPiDigitalPinOutput(private val digitalOutputDevice: DigitalOutputDevice) : BinaryOutput {
  override fun setState(newState: BinaryState) {
    digitalOutputDevice.setValue(BinaryStateConverter.toBoolean(newState))
  }
}

data class RaspberryPiBinaryStateChangeEvent(val digitalInputEvent: DigitalInputEvent) : BinaryStateChangeEvent {
  override fun newState(): BinaryState = BinaryStateConverter.fromBoolean(digitalInputEvent.value)
}
