package com.mqgateway.core.device

import com.mqgateway.core.gatewayconfig.DeviceType
import com.pi4j.io.gpio.GpioPinDigitalOutput

abstract class DigitalOutputDevice(id: String, type: DeviceType, protected val pin: GpioPinDigitalOutput) : Device(id, type)
