package com.mqgateway.core.gatewayconfig

import com.mqgateway.core.gatewayconfig.DevicePropertyType.HUMIDITY
import com.mqgateway.core.gatewayconfig.DevicePropertyType.PRESSURE
import com.mqgateway.core.gatewayconfig.DevicePropertyType.STATE
import com.mqgateway.core.gatewayconfig.DevicePropertyType.TEMPERATURE
import com.mqgateway.core.gatewayconfig.DevicePropertyType.UPTIME
import com.mqgateway.core.gatewayconfig.DevicePropertyType.VALUE
import kotlinx.serialization.Serializable

@Serializable
data class DeviceConfig(
  val id: String,
  val name: String,
  val type: DeviceType,
  val wires: List<WireColor>,
  val config: Map<String, String>? = null
)

enum class DeviceType(vararg val properties: DevicePropertyType) {
  RELAY(STATE),
  SWITCH_BUTTON(STATE),
  REED_SWITCH(STATE),
  BME280(TEMPERATURE, HUMIDITY, PRESSURE, UPTIME, STATE),
  SCT013(VALUE),
  DHT22(TEMPERATURE, HUMIDITY),
  MOTION_DETECTOR(STATE),
  EMULATED_SWITCH(STATE);
}

enum class DevicePropertyType {
  STATE, VALUE, TEMPERATURE, HUMIDITY, PRESSURE, UPTIME;
  override fun toString(): String = this.name.toLowerCase()
}

enum class WireColor(val number: Int) {
  ORANGE_WHITE(1),
  ORANGE(2),
  GREEN_WHITE(3),
  BLUE(4),
  BLUE_WHITE(5),
  GREEN(6),
  BROWN_WHITE(7),
  BROWN(8)
}
