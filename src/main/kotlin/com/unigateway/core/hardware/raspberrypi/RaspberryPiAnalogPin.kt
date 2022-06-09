package com.unigateway.core.hardware.raspberrypi

import com.diozero.api.AnalogInputDevice
import com.diozero.api.AnalogInputEvent
import com.diozero.api.AnalogOutputDevice
import com.unigateway.core.io.FloatInput
import com.unigateway.core.io.FloatOutput
import com.unigateway.core.io.FloatValueChangeEvent
import com.unigateway.core.io.FloatValueListener

class RaspberryPiAnalogPinInput(private val analogInputDevice: AnalogInputDevice) : FloatInput {

  override fun addListener(listener: FloatValueListener) {
    analogInputDevice.addListener { event -> listener.handle(RaspberryPiAnalogStateChangeEvent(event)) }
  }

  override fun getValue(): Float {
    return analogInputDevice.unscaledValue
  }
}

class RaspberryPiAnalogPinOutput(private val analogOutputDevice: AnalogOutputDevice) : FloatOutput {
  override fun setValue(newValue: Float) {
    analogOutputDevice.value = newValue
  }
}

data class RaspberryPiAnalogStateChangeEvent(private val event: AnalogInputEvent) : FloatValueChangeEvent {
  override fun newValue(): Float {
    return event.unscaledValue
  }
}
