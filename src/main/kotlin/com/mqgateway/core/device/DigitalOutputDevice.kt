package com.mqgateway.core.device

import com.mqgateway.core.io.BinaryOutput

abstract class DigitalOutputDevice(
  id: String,
  name: String,
  type: DeviceType,
  protected val binaryOutput: BinaryOutput,
  properties: Set<DeviceProperty>
) : Device(id, name, type, properties)
