package com.mqgateway.core.device

import com.mqgateway.core.io.BinaryOutput

abstract class DigitalOutputDevice(
  id: String,
  name: String,
  type: DeviceType,
  protected val binaryOutput: BinaryOutput,
  properties: Set<DeviceProperty>,
  config: Map<String, String> = emptyMap(),
) : Device(id, name, type, properties, config)
