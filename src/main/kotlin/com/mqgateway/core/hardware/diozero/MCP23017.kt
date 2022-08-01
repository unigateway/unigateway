package com.mqgateway.core.hardware.diozero

import com.diozero.api.I2CDevice
import com.diozero.devices.mcp23xxx.MCP23x17
import com.diozero.util.MutableByte
import org.tinylog.Logger

/**
 * This is modified copy of class com.diozero.devices.MCP23017
 * It is modifying how MCP is initialised to avoid zeroing of all pins, which may cause light blinking or gates opening on UniGateway restart.
 * This class also do not support interrupts for now, but it could be potentially implemented.
 */
open class MCP23017(controller: Int, address: Int, interruptGpioA: Int, interruptGpioB: Int) :
  MCP23x17("$DEVICE_NAME-$controller-$address", interruptGpioA, interruptGpioB) {

  private val device = I2CDevice.builder(address).setController(controller).build()

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
      // Default all GPIOs to output
      writeByte(getIODirReg(port), 0.toByte())
      // Default to normal input polarity - IPOLA/IPOLB
      writeByte(getIPolReg(port), 0.toByte())
      // Disable interrupt-on-change for all GPIOs
      writeByte(getGPIntEnReg(port), 0.toByte())
      // Set default compare values to 0
      writeByte(getDefValReg(port), 0.toByte())
      // Disable interrupt comparison control
      writeByte(getIntConReg(port), 0.toByte())
      // Disable pull-up resistors
      writeByte(getGPPullUpReg(port), 0.toByte())
    }
  }

  companion object {
    private const val DEVICE_NAME = "MCP23017"
    private const val IOCON_BANK_BIT: Byte = 7
    private const val IOCON_SEQOP_BIT: Byte = 5
    private const val IOCON_DISSLW_BIT: Byte = 4
    private const val IOCON_HAEN_BIT: Byte = 3
    private const val IOCON_ODR_BIT: Byte = 2
    private const val NUM_PORTS = 2
  }
}
