package com.mqgateway.core.device

import com.pi4j.io.gpio.GpioPinDigitalOutput
import com.mqgateway.core.gatewayconfig.DeviceType

abstract class DigitalOutputDevice(id: String, type: DeviceType, protected val pin: GpioPinDigitalOutput): Device(id, type) {

}