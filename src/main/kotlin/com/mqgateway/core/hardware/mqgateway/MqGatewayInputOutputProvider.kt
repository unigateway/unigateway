package com.mqgateway.core.hardware.mqgateway

import com.diozero.api.DebouncedDigitalInputDevice
import com.diozero.api.DigitalInputDevice
import com.diozero.api.DigitalOutputDevice
import com.diozero.api.GpioPullUpDown
import com.mqgateway.core.io.FloatInput
import com.mqgateway.core.io.FloatOutput
import com.mqgateway.core.io.provider.HardwareInputOutputProvider

class MqGatewayInputOutputProvider(
  private val platformConfiguration: MqGatewayPlatformConfiguration,
  private val mcpExpanders: MqGatewayMcpExpanders
) : HardwareInputOutputProvider<MqGatewayConnector> {

  override fun getBinaryInput(connector: MqGatewayConnector): MqGatewayDigitalPinInput {
    val digitalInputDevice: DigitalInputDevice = DebouncedDigitalInputDevice.Builder
      .builder(pinOnMcp(connector.portNumber, connector.wireColor), connector.debounceMs ?: platformConfiguration.defaultDebounceMs)
      .setDeviceFactory(mcpExpanders.getByPort(connector.portNumber))
      .setPullUpDown(GpioPullUpDown.PULL_UP)
      .build()

    return MqGatewayDigitalPinInput(digitalInputDevice)
  }

  override fun getBinaryOutput(connector: MqGatewayConnector): MqGatewayDigitalPinOutput {
    val digitalOutputDevice = DigitalOutputDevice.Builder
      .builder(pinOnMcp(connector.portNumber, connector.wireColor))
      .setDeviceFactory(mcpExpanders.getByPort(connector.portNumber))
      .setInitialValue(false)
      .build()
    return MqGatewayDigitalPinOutput(digitalOutputDevice)
  }

  override fun getFloatInput(connector: MqGatewayConnector): FloatInput {
    throw UnsupportedMqGatewayConnectorException("MqGateway do not support analog GPIOs")
  }

  override fun getFloatOutput(connector: MqGatewayConnector): FloatOutput {
    throw UnsupportedMqGatewayConnectorException("MqGateway do not support analog GPIOs")
  }

  private fun pinOnMcp(portNumber: Int, wireColor: WireColor): Int {
    return ((portNumber - 1) % 4) * 4 + (wireColor.number - 3)
  }
}

class UnsupportedMqGatewayConnectorException(message: String) : RuntimeException(message)
