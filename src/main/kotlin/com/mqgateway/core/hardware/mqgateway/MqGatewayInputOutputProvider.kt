package com.mqgateway.core.hardware.mqgateway

import com.mqgateway.core.io.BinaryInput
import com.mqgateway.core.io.BinaryOutput
import com.mqgateway.core.io.BinaryState
import com.mqgateway.core.io.BinaryStateListener
import com.mqgateway.core.io.FloatInput
import com.mqgateway.core.io.FloatOutput
import com.mqgateway.core.io.provider.HardwareConnector
import com.mqgateway.core.io.provider.HardwareInputOutputProvider

class MqGatewayInputOutputProvider : HardwareInputOutputProvider<MqGatewayConnector> {

  override fun getBinaryInput(connector: MqGatewayConnector): MqGatewayDigitalPinInput {
    TODO("Not yet implemented")
  }

  override fun getBinaryOutput(connector: MqGatewayConnector): MqGatewayDigitalPinOutput {
    TODO("Not yet implemented")
  }

  override fun getFloatInput(connector: MqGatewayConnector): FloatInput {
    throw UnsupportedMqGatewayConnectorException("MqGateway do not support analog GPIOs")
  }

  override fun getFloatOutput(connector: MqGatewayConnector): FloatOutput {
    throw UnsupportedMqGatewayConnectorException("MqGateway do not support analog GPIOs")
  }
}

data class MqGatewayConnector(
  val portNumber: Int,
  val wireColor: WireColor,
  val debounceMs: Int = 100
) : HardwareConnector

enum class WireColor(val number: Int) {
  ORANGE_WHITE(1),
  ORANGE(2),
  GREEN_WHITE(3),
  BLUE(4),
  BLUE_WHITE(5),
  GREEN(6),
  BROWN_WHITE(7),
  BROWN(8)
}

class MqGatewayDigitalPinInput : BinaryInput {
  override fun addListener(listener: BinaryStateListener) {
    TODO("Not yet implemented")
  }

  override fun getState(): BinaryState {
    TODO("Not yet implemented")
  }
}

class MqGatewayDigitalPinOutput : BinaryOutput {
  override fun setState(newState: BinaryState) {
    TODO("Not yet implemented")
  }
}

class UnsupportedMqGatewayConnectorException(message: String) : RuntimeException(message)
