package com.mqgateway.core.device.gate

import com.mqgateway.core.device.DevicePropertyType.STATE
import com.mqgateway.core.device.emulatedswitch.EmulatedSwitchButtonDevice
import com.mqgateway.core.device.reedswitch.ReedSwitchDevice
import io.github.oshai.kotlinlogging.KotlinLogging
import java.util.Locale

private val LOGGER = KotlinLogging.logger {}

class ThreeButtonsGateDevice(
  id: String,
  name: String,
  private val stopButton: EmulatedSwitchButtonDevice,
  private val openButton: EmulatedSwitchButtonDevice,
  private val closeButton: EmulatedSwitchButtonDevice,
  private val openReedSwitch: ReedSwitchDevice?,
  private val closedReedSwitch: ReedSwitchDevice?,
  config: Map<String, String> = emptyMap(),
) : GateDevice(
    id,
    name,
    config,
  ) {
  override fun initProperty(
    propertyId: String,
    value: String,
  ) {
    if (propertyId != STATE.toString()) {
      LOGGER.warn { "Trying to initialize unsupported property '$id.$propertyId'" }
      return
    }
    state = State.valueOf(value)
  }

  override fun initDevice() {
    super.initDevice()
    stopButton.init(false)
    openButton.init(false)
    closeButton.init(false)
    openReedSwitch?.init(false)
    closedReedSwitch?.init(false)
    if (closedReedSwitch?.isClosed() == true) {
      state = State.CLOSED
    } else if (openReedSwitch?.isClosed() == true) {
      state = State.OPEN
    }
    if (state == State.UNKNOWN) {
      LOGGER.warn { "Closing gate because its state is UNKNOWN during initialization" }
      close()
    }
    openReedSwitch?.addListener { _, _, newValue ->
      if (newValue == ReedSwitchDevice.CLOSED_STATE_VALUE) {
        state = State.OPEN
      } else if (hasClosedReedSwitch()) {
        state = State.CLOSING
      } else {
        state = State.CLOSED
      }
    }

    closedReedSwitch?.addListener { _, _, newValue ->
      if (newValue == ReedSwitchDevice.CLOSED_STATE_VALUE) {
        state = State.CLOSED
      } else if (hasOpenReedSwitch()) {
        state = State.OPENING
      } else {
        state = State.OPEN
      }
    }
  }

  override fun change(
    propertyId: String,
    newValue: String,
  ) {
    if (propertyId != STATE.toString()) {
      LOGGER.error { "Unexpected property change received for device '$id': $propertyId" }
      return
    }

    LOGGER.info { "Changing gate $id state to $newValue" }
    when (Command.valueOf(newValue.uppercase(Locale.getDefault()))) {
      Command.OPEN -> open()
      Command.CLOSE -> close()
      Command.STOP -> stop()
    }
  }

  override fun close() {
    stop(true)
    closeButton.shortPress()
    state = if (hasClosedReedSwitch()) State.CLOSING else State.CLOSED
  }

  override fun open() {
    stop(true)
    openButton.shortPress()
    state = if (hasOpenReedSwitch()) State.OPENING else State.OPEN
  }

  override fun stop(blocking: Boolean) {
    stopButton.shortPress(blocking)
    if (state != State.CLOSED && state != State.OPEN) {
      state = State.OPEN
    }
  }

  private fun hasClosedReedSwitch() = closedReedSwitch != null

  private fun hasOpenReedSwitch() = openReedSwitch != null

  companion object {
    const val OPENING_STATE_VALUE = "OPENING"
    const val CLOSING_STATE_VALUE = "CLOSING"
    const val OPEN_STATE_VALUE = "OPEN"
    const val CLOSED_STATE_VALUE = "CLOSED"
  }
}
