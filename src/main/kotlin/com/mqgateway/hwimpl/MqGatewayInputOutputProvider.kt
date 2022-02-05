package com.mqgateway.hwimpl

import com.diozero.api.DigitalInputDevice
import com.diozero.api.DigitalInputEvent
import com.diozero.api.DigitalOutputDevice
import com.diozero.api.GpioPullUpDown
import com.diozero.api.I2CConstants
import com.diozero.devices.MCP23017
import com.mqgateway.core.gatewayconfig.WireColor
import com.mqgateway.core.hardware.io.BinaryInput
import com.mqgateway.core.hardware.io.BinaryOutput
import com.mqgateway.core.hardware.io.BinaryState
import com.mqgateway.core.hardware.io.BinaryStateChangeEvent
import com.mqgateway.core.hardware.io.BinaryStateListener
import com.mqgateway.core.hardware.io.FloatInput
import com.mqgateway.core.hardware.provider.ConnectorConfiguration
import com.mqgateway.core.hardware.provider.HardwareInputOutputProvider

class MqGatewayInputOutputProvider : HardwareInputOutputProvider {

  // TODO should be passed from the Hardware configuration on application.yaml
  private val mcpExpanders: MqGatewayMcpExpanders = MqGatewayMcpExpanders(listOf(0x20, 0x21, 0x22, 0x23))

  override fun getBinaryInput(connectorConfiguration: ConnectorConfiguration): MqGatewayDigitalPinInput {
    val config = connectorConfiguration as MqGatewayConnectorConfiguration
    // TODO support debounce somehow - it is not easily achievable in diozero with MCP23017
    val digitalInputDevice = DigitalInputDevice.Builder.builder(pinOnMcp(config.portNumber, config.wireColor))
      .setDeviceFactory(mcpExpanders.getByPort(config.portNumber))
      .setPullUpDown(GpioPullUpDown.PULL_UP)
      .build()
    return MqGatewayDigitalPinInput(digitalInputDevice)
  }

  override fun getBinaryOutput(connectorConfiguration: ConnectorConfiguration): MqGatewayDigitalPinOutput {
    val config = connectorConfiguration as MqGatewayConnectorConfiguration
    val digitalOutputDevice = DigitalOutputDevice.Builder.builder(pinOnMcp(config.portNumber, config.wireColor))
      .setDeviceFactory(mcpExpanders.getByPort(config.portNumber))
      .setInitialValue(false)
      .build()
    return MqGatewayDigitalPinOutput(digitalOutputDevice)
  }

  override fun getFloatInput(connectorConfiguration: ConnectorConfiguration): FloatInput {
    TODO("Not yet implemented")
  }

  private fun pinOnMcp(portNumber: Int, wireColor: WireColor): Int {
    return ((portNumber - 1) % 4) * 4 + (wireColor.number - 3)
  }
}

data class MqGatewayConnectorConfiguration(
  val portNumber: Int,
  val wireColor: WireColor,
  val debounceMs: Int = 100 // TODO should it be here of implemented and configured in SwitchButtonDevice?
) : ConnectorConfiguration

class MqGatewayDigitalPinInput(private val digitalInputDevice: DigitalInputDevice) : BinaryInput {

  override fun addListener(listener: BinaryStateListener) {
    digitalInputDevice.addListener { listener.handleBinaryStateChangeEvent(MqGatewayBinaryStateChangeEvent(it)) }
  }

  override fun getState() = if (digitalInputDevice.value) BinaryState.HIGH else BinaryState.LOW
}

class MqGatewayDigitalPinOutput(private val digitalOutputDevice: DigitalOutputDevice) : BinaryOutput {
  override fun setState(state: BinaryState) {
    val booleanState = state == BinaryState.HIGH
    digitalOutputDevice.setValue(booleanState)
  }
}

class MqGatewayBinaryStateChangeEvent(private val event: DigitalInputEvent) : BinaryStateChangeEvent {
  override fun getState() = if (event.value) BinaryState.HIGH else BinaryState.LOW
}

/**
 * Initializes collection of MCP23017 expanders based on given bus number and addresses in I2c bus
 *
 * @param expanderAddresses list of MCP23017 expanders addresses ORDERED in the same way they are connected with ports
 */
class MqGatewayMcpExpanders(expanderAddresses: List<Int>) {

  private val expanders: List<MCP23017> = expanderAddresses.map { MCP23017(I2CConstants.CONTROLLER_0, it, -1, -1) }

  fun getByPort(portNumber: Int) = expanders[(portNumber - 1) / 4]
}
