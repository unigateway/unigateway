package com.mqgateway.core.device.mysensors

import com.mqgateway.core.gatewayconfig.DeviceType
import com.mqgateway.mysensors.MySensorsSerialConnection

class Dht22MySensorsInputDevice @JvmOverloads constructor(
  id: String,
  mySensorsNodeId: Int,
  serialConnection: MySensorsSerialConnection,
  humidityChildSensorId: Int = DEFAULT_HUMIDITY_CHILD_SENSOR_ID,
  temperatureChildSensorId: Int = DEFAULT_TEMPERATURE_CHILD_SENSOR_ID,
  debugChildSensorId: Int = DEFAULT_DEBUG_CHILD_SENSOR_ID,
) : MySensorsDevice(id, DeviceType.DHT22, mySensorsNodeId, serialConnection) {

  private val sensorsTypes = mapOf(
    Pair(humidityChildSensorId, SensorType.HUMIDITY),
    Pair(temperatureChildSensorId, SensorType.TEMPERATURE),
    Pair(debugChildSensorId, SensorType.DEBUG)
  )

  override fun sensorsTypes() = sensorsTypes

  companion object {
    const val DEFAULT_HUMIDITY_CHILD_SENSOR_ID = 0
    const val DEFAULT_TEMPERATURE_CHILD_SENSOR_ID = 1
    const val DEFAULT_DEBUG_CHILD_SENSOR_ID = 2
  }
}
