package com.mqgateway.core.device

import com.mqgateway.core.gatewayconfig.DevicePropertyType
import com.mqgateway.core.gatewayconfig.DeviceType
import com.mqgateway.core.io.BinaryInput
import com.mqgateway.core.io.BinaryState
import mu.KotlinLogging

private val LOGGER = KotlinLogging.logger {}

abstract class DigitalInputDevice(
  id: String,
  type: DeviceType,
  private val binaryInput: BinaryInput
) : Device(id, type) {

  protected var state: BinaryState = binaryInput.getState()
    private set(value) {
      field = value
      val newState = if (value == BinaryState.HIGH) { highStateValue() } else { lowStateValue() }
      LOGGER.info { "Device($id) state changed to $newState" }
      notify(updatableProperty(), newState)
      additionalOnStateChanged(newState)
    }

  override fun initDevice() {
    super.initDevice()
    state = binaryInput.getState()
    binaryInput.addListener { event ->
      state = event.newState()
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
