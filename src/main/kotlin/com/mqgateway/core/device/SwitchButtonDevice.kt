package com.mqgateway.core.device

import com.mqgateway.core.gatewayconfig.DevicePropertyType.STATE
import com.mqgateway.core.gatewayconfig.DeviceType
import com.pi4j.io.gpio.GpioPinDigitalInput
import mu.KotlinLogging
import java.time.Instant
import java.util.Timer
import kotlin.concurrent.schedule

private val LOGGER = KotlinLogging.logger {}

class SwitchButtonDevice(
  id: String,
  private val pin: GpioPinDigitalInput,
  debounceMs: Int = CONFIG_DEBOUNCE_DEFAULT,
  private val longPressTimeMs: Long = CONFIG_LONG_PRESS_TIME_MS_DEFAULT
) : DigitalInputDevice(id, DeviceType.SWITCH_BUTTON, pin, debounceMs) {

  private var pressedTime: Instant? = null
  private var longPressTimer: Timer? = null
  private var longPressDone = false

  override fun initDevice() {
    super.initDevice()
    pin.setDebounce(debounceMs)
  }

  override fun updatableProperty() = STATE
  override fun highStateValue() = RELEASED_STATE_VALUE
  override fun lowStateValue() = PRESSED_STATE_VALUE

  override fun additionalOnStateChanged(newState: String) {
    super.additionalOnStateChanged(newState)
    if (newState == PRESSED_STATE_VALUE) {
      pressedTime = Instant.now()
      longPressTimer = Timer("SwitchButtonLongPress", false)
      longPressTimer!!.schedule(longPressTimeMs) {
        if (pressedTime?.isBefore(Instant.now().minusMillis(longPressTimeMs)) == true) {
          LOGGER.debug { "SwitchButtonDevice '$id' pressed for more then $longPressTimeMs ms" }
          notify(updatableProperty(), LONG_PRESSED_STATE_VALUE)
          longPressDone = true
        }
      }
    } else if (longPressDone && newState == RELEASED_STATE_VALUE) {
      LOGGER.debug { "SwitchButtonDevice '$id' released after long press" }
      longPressDone = false
      pressedTime = null
      notify(updatableProperty(), LONG_RELEASED_STATE_VALUE)
    } else {
      longPressTimer?.cancel()
      pressedTime = null
    }
  }

  companion object {
    const val CONFIG_DEBOUNCE_DEFAULT = 50

    const val CONFIG_LONG_PRESS_TIME_MS_KEY = "longPressTimeMs"
    const val CONFIG_LONG_PRESS_TIME_MS_DEFAULT = 3000L

    const val PRESSED_STATE_VALUE = "PRESSED"
    const val RELEASED_STATE_VALUE = "RELEASED"
    const val LONG_PRESSED_STATE_VALUE = "LONG_PRESSED"
    const val LONG_RELEASED_STATE_VALUE = "LONG_RELEASED"
  }
}
