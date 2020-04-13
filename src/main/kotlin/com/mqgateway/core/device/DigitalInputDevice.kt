package com.mqgateway.core.device

import com.mqgateway.core.gatewayconfig.DevicePropertyType
import com.mqgateway.core.gatewayconfig.DeviceType
import com.pi4j.io.gpio.GpioPinDigitalInput
import com.pi4j.io.gpio.PinState
import com.pi4j.io.gpio.event.GpioPinListenerDigital
import mu.KotlinLogging

private val LOGGER = KotlinLogging.logger {}

abstract class DigitalInputDevice(
  id: String,
  type: DeviceType,
  private val digitalInputPin: GpioPinDigitalInput,
  protected val debounceMs: Int
) : Device(id, type) {

  override fun initDevice() {
    super.initDevice()
    digitalInputPin.addListener(GpioPinListenerDigital { event ->
      val newState = if (event.state == PinState.HIGH) { highStateValue() } else { lowStateValue() }
      LOGGER.info { "Device($id) state changed to $newState" }
      notify(updatableProperty().toString(), newState)
    })
  }

  protected abstract fun updatableProperty(): DevicePropertyType
  protected abstract fun highStateValue(): String
  protected abstract fun lowStateValue(): String

  companion object {
    const val CONFIG_DEBOUNCE_KEY = "debounceMs"
  }
}
