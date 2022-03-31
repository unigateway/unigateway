package com.mqgateway.core.device.gate

import com.mqgateway.core.device.DevicePropertyType.STATE
import com.mqgateway.core.device.emulatedswitch.EmulatedSwitchButtonDevice
import com.mqgateway.core.device.reedswitch.ReedSwitchDevice
import mu.KotlinLogging
import java.util.Locale

private val LOGGER = KotlinLogging.logger {}

class SingleButtonsGateDevice(
  id: String,
  name: String,
  private val actionButton: EmulatedSwitchButtonDevice,
  private val openReedSwitch: ReedSwitchDevice?,
  private val closedReedSwitch: ReedSwitchDevice?,
  config: Map<String, String> = emptyMap()
) : GateDevice(
  id, name, config
) {

  override fun initProperty(propertyId: String, value: String) {
    if (propertyId != STATE.toString()) {
      LOGGER.warn { "Trying to initialize unsupported property '$id.$propertyId'" }
      return
    }
    state = State.valueOf(value)
  }

  override fun initDevice() {
    super.initDevice()
    actionButton.init(false)
    openReedSwitch?.init(false)
    closedReedSwitch?.init(false)
    if (closedReedSwitch?.isClosed() == true) {
      state = State.CLOSED
    } else if (openReedSwitch?.isClosed() == true) {
      state = State.OPEN
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

  override fun change(propertyId: String, newValue: String) {
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
    if (isClosedForSure()) {
      LOGGER.debug { "Nothing to be done - gate is closed already" }
    } else if (state == State.CLOSING) {
      LOGGER.debug { "Nothing to be done - gate is closing already" }
    } else if (state == State.OPENING) {
      LOGGER.debug { "Gate is opening - need to stop first" }
      stop()
      close()
    } else {
      LOGGER.debug { "Activating action button to close the gate" }
      actionButton.shortPress(true)
      state = if (hasClosedReedSwitch()) State.CLOSING else State.CLOSED
    }
  }

  override fun open() {
    if (isOpenForSure()) {
      LOGGER.debug { "Nothing to be done - gate is open already" }
    } else if (state == State.OPENING) {
      LOGGER.debug { "Nothing to be done - gate is opening already" }
    } else if (state == State.CLOSING) {
      LOGGER.debug { "Gate is closing - need to stop first" }
      stop()
      open()
    } else {
      LOGGER.debug { "Activating action button to open the gate" }
      actionButton.shortPress(true)
      state = if (hasOpenReedSwitch()) State.OPENING else State.OPEN
    }
  }

  override fun stop(blocking: Boolean) {
    if (isOpenForSure() || isClosedForSure()) {
      LOGGER.debug { "Nothing to be done - gate is stopped already" }
    } else {
      LOGGER.debug { "Activating action button to stop the gate" }
      actionButton.shortPress(true)
      state = State.OPEN
    }
  }

  private fun hasClosedReedSwitch() = closedReedSwitch != null
  private fun hasOpenReedSwitch() = openReedSwitch != null
  private fun isOpenForSure() = openReedSwitch?.isClosed() == true
  private fun isClosedForSure() = closedReedSwitch?.isClosed() == true
}
