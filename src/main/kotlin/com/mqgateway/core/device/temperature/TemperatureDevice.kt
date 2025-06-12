package com.mqgateway.core.device.temperature

import com.mqgateway.core.device.DataType
import com.mqgateway.core.device.DataUnit
import com.mqgateway.core.device.Device
import com.mqgateway.core.device.DeviceProperty
import com.mqgateway.core.device.DevicePropertyType
import com.mqgateway.core.device.DeviceType
import com.mqgateway.core.device.util.CallThrottler
import com.mqgateway.core.io.FloatInput

class TemperatureDevice(
  id: String,
  name: String,
  private val input: FloatInput,
  minUpdateIntervalMillis: Long,
  config: Map<String, String> = emptyMap()
) :
  Device(
    id, name, DeviceType.TEMPERATURE,
    setOf(
      DeviceProperty(DevicePropertyType.TEMPERATURE, DataType.FLOAT, null, unit = DataUnit.CELSIUS)
    ),
    config
  ) {

  private val callThrottler = CallThrottler(minUpdateIntervalMillis)

  override fun initDevice() {
    super.initDevice()
    input.addListener { event ->
      callThrottler.throttle { notify(DevicePropertyType.TEMPERATURE, event.newValue()) }
    }
  }
}
