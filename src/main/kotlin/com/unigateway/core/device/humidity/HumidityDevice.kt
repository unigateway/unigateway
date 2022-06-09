package com.unigateway.core.device.humidity

import com.unigateway.core.device.DataType
import com.unigateway.core.device.Device
import com.unigateway.core.device.DeviceProperty
import com.unigateway.core.device.DevicePropertyType
import com.unigateway.core.device.DeviceType
import com.unigateway.core.io.FloatInput

class HumidityDevice(
  id: String,
  name: String,
  private val input: FloatInput,
  config: Map<String, String> = emptyMap()
) :
  Device(
    id, name, DeviceType.HUMIDITY,
    setOf(
      DeviceProperty(DevicePropertyType.HUMIDITY, DataType.FLOAT, null)
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
