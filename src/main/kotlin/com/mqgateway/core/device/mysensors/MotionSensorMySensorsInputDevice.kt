package com.mqgateway.core.device.mysensors

import com.mqgateway.core.gatewayconfig.DeviceType
import com.mqgateway.core.hardware.MqGpioPinDigitalOutput
import com.mqgateway.mysensors.MySensorsSerialConnection

class MotionSensorMySensorsInputDevice @JvmOverloads constructor(
  id: String,
  mySensorsNodeId: Int,
  serialConnection: MySensorsSerialConnection,
  motionSensorId: Int = DEFAULT_MOTION_CHILD_SENSOR_ID,
  debugChildSensorId: Int = DEFAULT_DEBUG_CHILD_SENSOR_ID,
  resetPin: MqGpioPinDigitalOutput? = null
) : MySensorsDevice(id, DeviceType.MOTION_DETECTOR, mySensorsNodeId, serialConnection, resetPin) {

  private val sensorsTypes = mapOf(
    Pair(motionSensorId, SensorType.MOTION),
    Pair(debugChildSensorId, SensorType.DEBUG)
  )

  override fun sensorsTypes() = sensorsTypes

  companion object {
    const val DEFAULT_MOTION_CHILD_SENSOR_ID = 3
  }
}
