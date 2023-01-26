package com.mqgateway.core.hardware.diozero

import com.diozero.api.GpioEventTrigger
import com.diozero.api.GpioPullUpDown
import com.diozero.api.I2CDevice
import com.diozero.devices.mcp23xxx.MCP23x17
import com.diozero.util.BitManipulation
import com.diozero.util.MutableByte
import org.tinylog.Logger

/**
 * This is modified copy of class com.diozero.devices.MCP23017
 * It is modifying how MCP is initialised to avoid zeroing of all pins, which may cause light blinking or gates opening on UniGateway restart.
 * This class also do not support interrupts for now, but it could be potentially implemented.
 */
open class MCP23017(private val controller: Int, private val address: Int, interruptGpioA: Int, interruptGpioB: Int) :
  MCP23x17("$DEVICE_NAME-$controller-$address", interruptGpioA, interruptGpioB) {

  private val device = I2CDevice.builder(address).setController(controller).build()
  private val directions = arrayOf(MutableByte(), MutableByte())
  private val pullUps = arrayOf(MutableByte(), MutableByte())

  init {
    initialiseWithoutZeroing()
  }

  override fun readByte(register: Int): Byte {
    return device.readByteData(register)
  }

  override fun writeByte(register: Int, value: Byte) {
    device.writeByteData(register, value)
  }

  private fun initialiseWithoutZeroing() {
    // Initialise
    // Read the I/O configuration value
    val start_iocon = readByte(getIOConReg(0))
    Logger.debug("Default power-on values for IOCON: 0x{}", Integer.toHexString(start_iocon.toInt()))
    Logger.debug("IOCONB: 0x{}", Integer.toHexString(readByte(getIOConReg(1)).toInt()))

    // Configure interrupts
    val iocon = MutableByte(start_iocon)
    iocon.unsetBit(IOCON_BANK_BIT)
    iocon.setBit(IOCON_SEQOP_BIT)
    iocon.unsetBit(IOCON_DISSLW_BIT)
    iocon.setBit(IOCON_HAEN_BIT)
    iocon.unsetBit(IOCON_ODR_BIT)
    if (!iocon.equals(start_iocon)) {
      writeByte(getIOConReg(0), iocon.value)
    }
    for (port in 0 until NUM_PORTS) {
      // Read currently set directions
      directions[port] = MutableByte(readByte(getIODirReg(port)))
      // Read current state of pull-up resistors
      pullUps[port] = MutableByte(readByte(getGPPullUpReg(port)))

      // Default to normal input polarity - IPOLA/IPOLB
      writeByte(getIPolReg(port), 0.toByte())
      // Disable interrupt-on-change for all GPIOs
      writeByte(getGPIntEnReg(port), 0.toByte())
      // Set default compare values to 0
      writeByte(getDefValReg(port), 0.toByte())
      // Disable interrupt comparison control
      writeByte(getIntConReg(port), 0.toByte())
    }
  }

  override fun setInputMode(gpio: Int, pud: GpioPullUpDown, trigger: GpioEventTrigger) {
    val bit = (gpio % GPIOS_PER_PORT).toByte()
    val port = gpio / GPIOS_PER_PORT

    // Set the following values: direction, pullUp, interruptCompare, defaultValue,
    // interruptOnChange
    directions[port].setBit(bit)
    writeByte(getIODirReg(port), directions[port].value)
    val new_dir = readByte(getIODirReg(port))
    if (directions[port].value != new_dir) {
      Logger.error(
        "Error setting input mode for gpio {}, expected {}, read {}", Integer.valueOf(gpio),
        java.lang.Byte.valueOf(directions[port].value), java.lang.Byte.valueOf(new_dir)
      )
    }
    if (pud == GpioPullUpDown.PULL_UP) {
      pullUps[port].setBit(bit)
      writeByte(getGPPullUpReg(port), pullUps[port].value)
    }
  }

  override fun setOutputMode(gpio: Int) {
    val bit = (gpio % GPIOS_PER_PORT).toByte()
    val port = gpio / GPIOS_PER_PORT

    // Set the following values: direction, pullUp, interruptCompare, defaultValue,
    // interruptOnChange
    directions[port].unsetBit(bit)
    writeByte(getIODirReg(port), directions[port].value)
  }

  override fun setValue(gpio: Int, value: Boolean) {
    require(!(gpio < 0 || gpio >= NUM_GPIOS)) {
      ("Invalid GPIO: " + gpio + ". " + deviceName() + " has " + NUM_GPIOS + " GPIOs; must be 0.." + (NUM_GPIOS - 1))
    }
    val bit = (gpio % GPIOS_PER_PORT).toByte()
    val port = gpio / GPIOS_PER_PORT

    // Check the direction of the GPIO - can't set the output value for input GPIOs
    // (direction bit is set)
    check(!directions[port].isBitSet(bit.toInt())) { "Can't set value for input GPIO: $gpio" }
    // Read the current state of this bank of GPIOs
    val oldVal = readByte(getGPIOReg(port))
    val newVal = BitManipulation.setBitValue(oldVal, bit.toInt(), value)
    writeByte(getOLatReg(port), newVal)
  }

  private fun deviceName() = "$DEVICE_NAME-$controller-$address"

  companion object {
    private const val DEVICE_NAME = "MCP23017"
    private const val IOCON_BANK_BIT: Byte = 7
    private const val IOCON_SEQOP_BIT: Byte = 5
    private const val IOCON_DISSLW_BIT: Byte = 4
    private const val IOCON_HAEN_BIT: Byte = 3
    private const val IOCON_ODR_BIT: Byte = 2
    private const val NUM_PORTS = 2
    private const val NUM_GPIOS = NUM_PORTS * 8
  }
}
