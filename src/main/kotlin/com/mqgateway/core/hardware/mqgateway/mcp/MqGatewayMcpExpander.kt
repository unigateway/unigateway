package com.mqgateway.core.hardware.mqgateway.mcp

import com.diozero.api.GpioEventTrigger
import com.diozero.api.GpioPullUpDown
import com.diozero.devices.MCP23017
import com.mqgateway.core.io.BinaryState
import com.mqgateway.core.io.BinaryState.HIGH
import com.mqgateway.core.io.BinaryState.LOW
import com.mqgateway.core.io.BinaryStateListener
import mu.KotlinLogging
import java.time.Clock
import java.util.UUID
import java.util.concurrent.atomic.AtomicBoolean
import kotlin.concurrent.thread

private val LOGGER = KotlinLogging.logger {}

class MqGatewayMcpExpander(
  private val mcp23017: MCP23017,
  private val clock: Clock = Clock.systemDefaultZone(),
  private val threadSleeper: ThreadSleeper = ThreadSleeper()
) {

  private val listeners: MutableMap<Int, MutableList<InputPinStateListener>> = mutableMapOf()
  private val isStarted: AtomicBoolean = AtomicBoolean(false)
  private var runCheckingThread = true
  private val usedPins: MutableMap<Int, GpioType> = mutableMapOf()

  fun getPinState(gpioNumber: Int): BinaryState {
    LOGGER.trace { "Getting state of pin $gpioNumber" }
    if (usedPins[gpioNumber] == GpioType.OUTPUT) {
      throw IncorrectGpioTypeException("Cannot read state from OUTPUT pin")
    }
    return readState().getState(gpioNumber)
  }

  fun setPinState(gpioNumber: Int, state: BinaryState) {
    LOGGER.debug { "Setting pin $gpioNumber state to $state" }
    if (usedPins[gpioNumber] == GpioType.INPUT) {
      throw IncorrectGpioTypeException("Cannot set state to INPUT pin")
    }
    mcp23017.setValue(gpioNumber, state == HIGH)
  }

  /**
   * This class is NOT thread-safe, but this method is secured against creating multiple threads to avoid exceptionally hard to predict behaviour.
   */
  fun start() {
    LOGGER.info { "Starting listening for changes on expander '${mcp23017.name}'" }

    if (isStarted.getAndSet(true)) {
      LOGGER.error { "This method cannot be run more than once. It may cause hard to predict behaviour." }
      throw McpExpanderAlreadyStartedException("This McpExpander has been already started: ${mcp23017.name}")
    }

    thread(isDaemon = true, name = "${mcp23017.name}-state-loop") {
      while (runCheckingThread) {
        val now = clock.instant()
        readState().getPinStates().forEachIndexed { gpioNumber, state ->
          listeners.getOrDefault(gpioNumber, emptyList()).forEach { listener ->
              listener.handle(state, now)
            }
        }
        threadSleeper.sleep(BUSY_LOOP_SLEEP_TIME_MS)
      }
    }
  }

  fun stop() {
    runCheckingThread = false
  }

  fun addListener(gpioNumber: Int, debounceMs: Long, listener: BinaryStateListener) {
    LOGGER.info { "Listener set for ${mcp23017.name} pin $gpioNumber with debounceMs $debounceMs" }
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

class McpExpanderAlreadyStartedException(message: String) : RuntimeException(message)

class IncorrectGpioTypeException(message: String) : RuntimeException(message)
