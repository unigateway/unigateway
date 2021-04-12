package com.mqgateway.core.device

import com.mqgateway.core.gatewayconfig.DevicePropertyType.STATE
import com.mqgateway.core.gatewayconfig.DeviceType
import mu.KotlinLogging

private val LOGGER = KotlinLogging.logger {}

class ThreeButtonsGateDevice(
  id: String,
  private val stopButton: EmulatedSwitchButtonDevice,
  private val openButton: EmulatedSwitchButtonDevice,
  private val closeButton: EmulatedSwitchButtonDevice,
  private val openReedSwitch: ReedSwitchDevice?,
  private val closedReedSwitch: ReedSwitchDevice?
) : Device(id, DeviceType.GATE) {

  private var state: State = State.UNKNOWN
    private set(value) {
      field = value
      notify(STATE, value.name)
    }

  override fun initProperty(propertyId: String, value: String) {
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
    openReedSwitch?.addListener { _, _, newValue -> if (newValue == ReedSwitchDevice.CLOSED_STATE_VALUE) state = State.OPEN }
    closedReedSwitch?.addListener { _, _, newValue -> if (newValue == ReedSwitchDevice.CLOSED_STATE_VALUE) state = State.CLOSED }
  }

  override fun change(propertyId: String, newValue: String) {
    if (propertyId != STATE.toString()) {
      LOGGER.error { "Unexpected property change received for device '$id': $propertyId" }
      return
    }

    LOGGER.info { "Changing gate $id state to $newValue" }
    when (Command.valueOf(newValue.toUpperCase())) {
      Command.OPEN -> open()
      Command.CLOSE -> close()
      Command.STOP -> stop()
    }
  }

  fun close() {
    stop(true)
    closeButton.shortPress()
    state = if (hasClosedReedSwitch()) State.CLOSING else State.CLOSED
  }

  fun open() {
    stop(true)
    openButton.shortPress()
    state = if (hasOpenReedSwitch()) State.OPENING else State.OPEN
  }

  fun stop(blocking: Boolean = false) {
    stopButton.shortPress(blocking)
    if (state != State.CLOSED && state != State.OPEN) {
      state = State.OPEN
    }
  }

  private fun hasClosedReedSwitch() = closedReedSwitch != null
  private fun hasOpenReedSwitch() = openReedSwitch != null

  enum class Command {
    OPEN, CLOSE, STOP
  }

  enum class State {
    OPENING, CLOSING, OPEN, CLOSED, UNKNOWN
  }

  companion object {
    const val OPENING_STATE_VALUE = "OPENING"
    const val CLOSING_STATE_VALUE = "CLOSING"
    const val OPEN_STATE_VALUE = "OPEN"
    const val CLOSED_STATE_VALUE = "CLOSED"
  }
}
