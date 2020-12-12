package com.mqgateway.utils

import com.mqgateway.core.hardware.MqGpioPinDigitalStateChangeEvent
import com.mqgateway.core.hardware.MqGpioPinListenerDigital
import com.mqgateway.core.hardware.simulated.SimulatedGpioPinDigitalInput
import com.mqgateway.core.hardware.simulated.SimulatedGpioPinDigitalOutput
import com.mqgateway.core.hardware.simulated.SimulatedSerial
import com.pi4j.io.gpio.PinState
import org.jetbrains.annotations.NotNull

class ExternalSerialDeviceSimulator {

	private SimulatedGpioPinDigitalOutput askForDataPin
	private SimulatedGpioPinDigitalInput pingPin
	private SimulatedSerial serial

	private Closure onBeforeSendingMessage = {}

	private String messageToSend

  ExternalSerialDeviceSimulator(SimulatedGpioPinDigitalOutput askForDataPin, SimulatedGpioPinDigitalInput pingPin, SimulatedSerial serial) {
		this.askForDataPin = askForDataPin
		this.pingPin = pingPin
		this.serial = serial
	}

	void ping() {
		pingPin.high()
		pingPin.low()
	}

	void beforeSendingMessage(Closure closure) {
		onBeforeSendingMessage = closure
	}

  void setMessageToSend(String messageToSend) {
    this.messageToSend = messageToSend
    askForDataPin.addListener(new MqGpioPinListenerDigital() {
      @Override
      void handleGpioPinDigitalStateChangeEvent(@NotNull MqGpioPinDigitalStateChangeEvent event) {
        if (event.getState() == PinState.LOW) {
          onBeforeSendingMessage.call()
          serial.sendFakeMessage(messageToSend)
        }
      }
    })
  }
}