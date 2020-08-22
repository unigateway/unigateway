package com.mqgateway.utils

import com.mqgateway.utils.SerialStub
import com.pi4j.io.gpio.PinState
import com.pi4j.io.gpio.event.GpioPinDigitalStateChangeEvent
import com.pi4j.io.gpio.event.GpioPinListenerDigital
import com.pi4j.io.gpio.impl.GpioPinImpl

class ExternalSerialDeviceSimulator {

	private GpioPinImpl askForDataPin
	private GpioPinImpl pingPin
	private SerialStub serial

	private Closure onBeforeSendingMessage = {}

	private String messageToSend


	ExternalSerialDeviceSimulator(GpioPinImpl askForDataPin, GpioPinImpl pingPin, SerialStub serial) {
		this.askForDataPin = askForDataPin
		this.pingPin = pingPin
		this.serial = serial

	}

	void ping() {
		pingPin.low()
		pingPin.high()
	}

	void beforeSendingMessage(Closure closure) {
		onBeforeSendingMessage = closure
	}

	void setMessageToSend(String messageToSend) {
		this.messageToSend = messageToSend
		askForDataPin.addListener(new GpioPinListenerDigital() {
			@Override
			void handleGpioPinDigitalStateChangeEvent(GpioPinDigitalStateChangeEvent event) {
				if (event.getState() == PinState.HIGH) {
					onBeforeSendingMessage.call()
					serial.sendFakeMessage(messageToSend)
				}
			}
		})
	}
}