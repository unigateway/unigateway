package com.mqgateway.core.device.humidity

import com.mqgateway.core.device.DataType
import com.mqgateway.core.device.DataUnit
import com.mqgateway.core.device.Device
import com.mqgateway.core.device.DeviceProperty
import com.mqgateway.core.device.DevicePropertyType
import com.mqgateway.core.device.DeviceType
import com.mqgateway.core.io.FloatInput

class HumidityDevice(
  id: String,
  name: String,
  private val input: FloatInput,
  config: Map<String, String> = emptyMap()
) :
  Device(
    id, name, DeviceType.HUMIDITY,
    setOf(
      DeviceProperty(DevicePropertyType.HUMIDITY, DataType.FLOAT, null, unit = DataUnit.PERCENT)
    ),
    config
  ) {

  override fun initDevice() {
    super.initDevice()
    input.addListener { event ->
      notify(DevicePropertyType.HUMIDITY, event.newValue())
    }
  }
}
