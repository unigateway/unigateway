package com.mqgateway.core.device.light

import com.mqgateway.core.device.DataType
import com.mqgateway.core.device.Device
import com.mqgateway.core.device.DeviceProperty
import com.mqgateway.core.device.DevicePropertyType.STATE
import com.mqgateway.core.device.DeviceType
import com.mqgateway.core.device.light.LightDevice.LightState.OFF
import com.mqgateway.core.device.light.LightDevice.LightState.ON
import com.mqgateway.core.device.relay.RelayDevice
import com.mqgateway.core.device.switchbutton.SwitchButtonDevice
import mu.KotlinLogging

private val LOGGER = KotlinLogging.logger {}

class LightDevice(
  id: String,
  name: String,
  private val relay: RelayDevice,
  private val switches: List<SwitchButtonDevice>,
  config: Map<String, String> = emptyMap()
) : Device(
  id, name, DeviceType.LIGHT,
  setOf(
    DeviceProperty(STATE, DataType.ENUM, "ON,OFF", settable = true, retained = true)
  ),
  config
) {

  private var currentState: LightState? = null
    private set(value) {
      field = value
      value?.let {
        notify(STATE, it.name)
      }
    }

  override fun initProperty(propertyId: String, value: String) {
    if (propertyId != STATE.toString()) {
      LOGGER.warn { "Trying to initialize unsupported property '$id.$propertyId'" }
      return
    }
    change(STATE.toString(), value)
  }

  override fun initDevice() {
    super.initDevice()
    switches.forEach { it.addListener(toggleOnPressed) }
  }

  private val toggleOnPressed = { deviceId: String, propertyId: String, newValue: String ->
    if (newValue == SwitchButtonDevice.PRESSED_STATE_VALUE) {
      LOGGER.info { "Toggle light on $deviceId $propertyId changed to $newValue" }
      toggle()
    }
  }

  override fun change(propertyId: String, newValue: String) {
    LOGGER.debug { "Changing state on light $id to $newValue" }
    when (newValue) {
      ON.name -> turnOn()
      OFF.name -> turnOff()
      else -> throw UnknownLightStateException(newValue)
    }
  }

  fun turnOn() {
    relay.changeState(RelayDevice.RelayState.CLOSED)
    currentState = ON
  }

  fun turnOff() {
    relay.changeState(RelayDevice.RelayState.OPEN)
    currentState = OFF
  }

  fun toggle() {
    if (currentState == OFF) {
      turnOn()
    } else {
      turnOff()
    }
  }

  enum class LightState {
    ON, OFF
  }

  class UnknownLightStateException(unknownState: String) : RuntimeException("Trying to change light state to unknown value '$unknownState'")
}
