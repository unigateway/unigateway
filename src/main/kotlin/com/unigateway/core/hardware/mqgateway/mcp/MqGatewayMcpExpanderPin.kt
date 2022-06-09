package com.unigateway.core.hardware.mqgateway.mcp

import com.unigateway.core.io.BinaryInput
import com.unigateway.core.io.BinaryOutput
import com.unigateway.core.io.BinaryState
import com.unigateway.core.io.BinaryStateChangeEvent
import com.unigateway.core.io.BinaryStateListener

class MqGatewayMcpExpanderOutputPin(
  private val mqGatewayMcpExpander: MqGatewayMcpExpander,
  private val expanderGpioNumber: Int
) : BinaryOutput {

  override fun setState(newState: BinaryState) {
    mqGatewayMcpExpander.setPinState(expanderGpioNumber, newState)
  }
}

class MqGatewayMcpExpanderInputPin(
  private val mqGatewayMcpExpander: MqGatewayMcpExpander,
  private val expanderGpioNumber: Int,
  private val debounceMs: Long
) : BinaryInput {

  override fun getState(): BinaryState = mqGatewayMcpExpander.getPinState(expanderGpioNumber)

  override fun addListener(listener: BinaryStateListener) {
    mqGatewayMcpExpander.addListener(expanderGpioNumber, debounceMs, listener)
  }
}

class MqGatewayExpanderPinStateChangeEvent(val previousState: BinaryState, val newState: BinaryState) : BinaryStateChangeEvent {
  override fun newState() = newState
}
