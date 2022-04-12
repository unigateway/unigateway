package com.mqgateway.core.hardware.mqgateway.mcp

import com.diozero.api.GpioEventTrigger
import com.diozero.api.GpioPullUpDown
import com.diozero.devices.MCP23017
import com.mqgateway.core.io.BinaryState
import com.mqgateway.core.io.BinaryState.HIGH
import com.mqgateway.core.io.BinaryState.LOW
import com.mqgateway.core.io.BinaryStateListener
import java.time.Clock
import java.util.*
import java.util.concurrent.atomic.AtomicBoolean
import kotlin.concurrent.thread

class MqGatewayMcpExpander(
  private val mcp23017: MCP23017,
  private val clock: Clock = Clock.systemDefaultZone(),
  private val threadSleeper: ThreadSleeper = ThreadSleeper()
) {

  private val listeners: MutableMap<Int, MutableList<InputPinStateListener>> = mutableMapOf()
  private val isStarted: AtomicBoolean = AtomicBoolean(false)
  private val usedPins: MutableMap<Int, GpioType> = mutableMapOf()

  fun getPinState(gpioNumber: Int): BinaryState {
    if (usedPins[gpioNumber] == GpioType.OUTPUT) {
      throw IncorrectGpioTypeException("Cannot read state from OUTPUT pin")
    }
    return readState().getState(gpioNumber)
  }

  fun setPinState(gpioNumber: Int, state: BinaryState) {
    if (usedPins[gpioNumber] == GpioType.INPUT) {
      throw IncorrectGpioTypeException("Cannot set state to INPUT pin")
    }
    mcp23017.setValue(gpioNumber, state == HIGH)
  }

  fun start() {
    if (isStarted.compareAndSet(false, true)) {
      // TODO Using ThreadPool?
      thread(isDaemon = true, name = "${mcp23017.name}-state-loop") {
        while (true) {
          readState().getPinStates().forEachIndexed { gpioNumber, state ->
            listeners.getOrDefault(gpioNumber, emptyList()).forEach { listener ->
              listener.handle(state, clock.instant())
            }
          }
          threadSleeper.sleep(BUSY_LOOP_SLEEP_TIME_MS)
        }
      }
    }
  }

  fun addListener(gpioNumber: Int, debounceMs: Long, listener: BinaryStateListener) {
    listeners.getOrPut(gpioNumber) { mutableListOf() }.add(InputPinStateListener(debounceMs, listener))
  }

  fun getInputPin(gpioNumber: Int, debounceMs: Long): MqGatewayMcpExpanderInputPin {
    usedPins[gpioNumber]?.let { throw McpExpanderPinAlreadyInUseException(gpioNumber, it) }
    mcp23017.createDigitalInputDevice(
      UUID.randomUUID().toString(),
      mcp23017.boardPinInfo.getByGpioNumberOrThrow(gpioNumber),
      GpioPullUpDown.PULL_UP,
      GpioEventTrigger.NONE
    )

    usedPins[gpioNumber] = GpioType.INPUT
    return MqGatewayMcpExpanderInputPin(this, gpioNumber, debounceMs)
  }

  fun getOutputPin(gpioNumber: Int): MqGatewayMcpExpanderOutputPin {
    usedPins[gpioNumber]?.let { throw McpExpanderPinAlreadyInUseException(gpioNumber, it) }
    mcp23017.createDigitalOutputDevice(
      UUID.randomUUID().toString(),
      mcp23017.boardPinInfo.getByGpioNumberOrThrow(gpioNumber),
      false
    )

    usedPins[gpioNumber] = GpioType.OUTPUT
    return MqGatewayMcpExpanderOutputPin(this, gpioNumber)
  }

  private fun readState(): ExpanderState {
    val gpioAValues = mcp23017.getValues(GPIOA)
    val gpioBValues = mcp23017.getValues(GPIOB)
    val gpioABits = toBits(gpioAValues)
    val gpioBBits = toBits(gpioBValues)
    return ExpanderState(gpioABits, gpioBBits)
  }
  // TODO how should it set bits? whats the order?
  private fun toBits(byte: Byte): List<BinaryState> {
    return (0 until Byte.SIZE_BITS).map {
      getBit(byte, it)
    }
  }

  private fun getBit(byte: Byte, position: Int): BinaryState {
    return if ((byte.toInt() shl position.inv()) < 0) {
      HIGH
    } else {
      LOW
    }
  }

  private data class ExpanderState(private val gpioABits: List<BinaryState>, private val gpioBBits: List<BinaryState>) {
    fun getState(gpioNumber: Int): BinaryState {
      return if (gpioNumber <= 7) {
        gpioABits[gpioNumber]
      } else {
        gpioBBits[gpioNumber % 8]
      }
    }

    fun getPinStates(): List<BinaryState> {
      return gpioABits + gpioBBits
    }
  }

  companion object {
    const val GPIOA = 0
    const val GPIOB = 1
    const val BUSY_LOOP_SLEEP_TIME_MS = 20L
  }
}

open class ThreadSleeper {
  open fun sleep(millis: Long) {
    Thread.sleep(millis)
  }
}

enum class GpioType {
  INPUT, OUTPUT
}

class McpExpanderPinAlreadyInUseException(val gpioNumber: Int, val gpioType: GpioType) :
  RuntimeException("Gpio '$gpioNumber' has been already used as ${gpioType.name}")

class IncorrectGpioTypeException(message: String) : RuntimeException(message)
