package com.mqgateway.core.device

import com.mqgateway.core.gatewayconfig.DevicePropertyType
import com.mqgateway.core.gatewayconfig.DeviceType
import com.mqgateway.core.hardware.MqGpioPinDigitalInput
import com.pi4j.io.gpio.PinState
import mu.KotlinLogging

private val LOGGER = KotlinLogging.logger {}

abstract class DigitalInputDevice(
  id: String,
  type: DeviceType,
  private val digitalInputPin: MqGpioPinDigitalInput,
  protected val debounceMs: Int
) : Device(id, type) {

  protected var state: PinState = digitalInputPin.getState()
    private set(value) {
      field = value
      val newState = if (value == PinState.HIGH) { highStateValue() } else { lowStateValue() }
      LOGGER.info { "Device($id) state changed to $newState" }
      notify(updatableProperty(), newState)
      additionalOnStateChanged(newState)
    }

  override fun initDevice() {
    super.initDevice()
    state = digitalInputPin.getState()
    digitalInputPin.addListener { event ->
      state = event.getState()
    }
  }

  protected abstract fun updatableProperty(): DevicePropertyType
  protected abstract fun highStateValue(): String
  protected abstract fun lowStateValue(): String

  protected open fun additionalOnStateChanged(newState: String) {}

  companion object {
    const val CONFIG_DEBOUNCE_KEY = "debounceMs"
  }
}
