package com.mqgateway.core.device

import com.mqgateway.core.gatewayconfig.DeviceType
import com.mqgateway.core.hardware.io.BinaryOutput

abstract class DigitalOutputDevice(id: String, type: DeviceType, protected val binaryOutput: BinaryOutput) : Device(id, type)
