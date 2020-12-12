package com.mqgateway.core.device

import com.mqgateway.core.gatewayconfig.DeviceType
import com.mqgateway.core.hardware.MqGpioPinDigitalOutput

abstract class DigitalOutputDevice(id: String, type: DeviceType, protected val pin: MqGpioPinDigitalOutput) : Device(id, type)
