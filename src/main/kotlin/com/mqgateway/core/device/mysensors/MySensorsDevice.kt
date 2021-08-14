package com.mqgateway.core.device.mysensors

import com.mqgateway.core.device.Device
import com.mqgateway.core.gatewayconfig.DevicePropertyType
import com.mqgateway.core.gatewayconfig.DeviceType
import com.mqgateway.mysensors.Message
import com.mqgateway.mysensors.MySensorsSerialConnection
import com.mqgateway.mysensors.MySensorsSerialListener
import mu.KotlinLogging
import java.time.LocalDateTime

private val LOGGER = KotlinLogging.logger {}

abstract class MySensorsDevice(
  id: String,
  type: DeviceType,
  private val mySensorsNodeId: Int,
  private val serialConnection: MySensorsSerialConnection
) : Device(id, type), MySensorsSerialListener {

  override fun initDevice() {
    super.initDevice()
    serialConnection.registerDeviceListener(mySensorsNodeId, this)
  }

  protected abstract fun sensorsTypes(): Map<Int, SensorType>

  override fun onMessageReceived(message: Message) {
    LOGGER.info { "MySensor message received for device '$id': $message" }
    val sensorType = sensorsTypes()[message.childSensorId]
    when (sensorType) {
      SensorType.HUMIDITY -> notify(DevicePropertyType.HUMIDITY, message.payload)
      SensorType.TEMPERATURE -> notify(DevicePropertyType.TEMPERATURE, message.payload)
      SensorType.PRESSURE -> notify(DevicePropertyType.PRESSURE, message.payload)
      SensorType.DEBUG -> LOGGER.error { "Error message from device '$id': ${message.payload}" }
      null -> LOGGER.trace { "Message with unknown childSensorId: ${message.childSensorId}. Probably another device on the same node." }
    }
    if (sensorType == SensorType.DEBUG && message.payload.contains("error", true)) {
      notify(DevicePropertyType.STATE, AVAILABILITY_OFFLINE_STATE)
    } else {
      notify(DevicePropertyType.STATE, AVAILABILITY_ONLINE_STATE)
    }
    notify(DevicePropertyType.LAST_PING, LocalDateTime.now().toString())
  }

  enum class SensorType {
    HUMIDITY, TEMPERATURE, PRESSURE, DEBUG
  }

  companion object {
    const val AVAILABILITY_ONLINE_STATE = "ONLINE"
    const val AVAILABILITY_OFFLINE_STATE = "OFFLINE"
    const val CONFIG_MY_SENSORS_NODE_ID = "mySensorsNodeId"
    const val CONFIG_HUMIDITY_CHILD_SENSOR_ID = "humidityChildSensorId"
    const val CONFIG_TEMPERATURE_CHILD_SENSOR_ID = "temperatureChildSensorId"
    const val CONFIG_PRESSURE_CHILD_SENSOR_ID = "pressureChildSensorId"
    const val CONFIG_DEBUG_CHILD_SENSOR_ID = "debugChildSensorId"
  }
}
