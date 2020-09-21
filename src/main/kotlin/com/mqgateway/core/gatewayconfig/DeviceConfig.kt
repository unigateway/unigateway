package com.mqgateway.core.gatewayconfig

import com.mqgateway.core.gatewayconfig.DataType.DATETIME
import com.mqgateway.core.gatewayconfig.DataType.ENUM
import com.mqgateway.core.gatewayconfig.DataType.FLOAT
import com.mqgateway.core.gatewayconfig.DataType.INTEGER
import com.mqgateway.core.gatewayconfig.DataUnit.CELSIUS
import com.mqgateway.core.gatewayconfig.DataUnit.PASCAL
import com.mqgateway.core.gatewayconfig.DataUnit.PERCENT
import com.mqgateway.core.gatewayconfig.DataUnit.SECOND
import com.mqgateway.core.gatewayconfig.DevicePropertyType.HUMIDITY
import com.mqgateway.core.gatewayconfig.DevicePropertyType.LAST_PING
import com.mqgateway.core.gatewayconfig.DevicePropertyType.POWER
import com.mqgateway.core.gatewayconfig.DevicePropertyType.PRESSURE
import com.mqgateway.core.gatewayconfig.DevicePropertyType.STATE
import com.mqgateway.core.gatewayconfig.DevicePropertyType.TEMPERATURE
import com.mqgateway.core.gatewayconfig.DevicePropertyType.TIMER
import com.mqgateway.core.gatewayconfig.DevicePropertyType.UPTIME
import kotlinx.serialization.Serializable
import com.mqgateway.core.gatewayconfig.DeviceProperty as Property

@Serializable
data class DeviceConfig(
  val id: String,
  val name: String,
  val type: DeviceType,
  val wires: List<WireColor>,
  val config: Map<String, String>? = null
)

data class DeviceProperty(
  val type: DevicePropertyType,
  val dataType: DataType,
  val format: String?,
  val settable: Boolean = false,
  val retained: Boolean = false,
  val unit: DataUnit = DataUnit.NONE
) {

  fun name() = type.toString()
  override fun toString() = name()
}

enum class DeviceType(vararg val properties: Property) {
  RELAY(
    Property(STATE, ENUM, "ON,OFF", settable = true, retained = true)
  ),
  SWITCH_BUTTON(
    Property(STATE, ENUM, "PRESSED,RELEASED")
  ),
  REED_SWITCH(
    Property(STATE, ENUM, "OPEN,CLOSED", retained = true)
  ),
  BME280(
    Property(TEMPERATURE, FLOAT, null, retained = true, unit = CELSIUS),
    Property(HUMIDITY, FLOAT, "0:100", retained = true, unit = PERCENT),
    Property(PRESSURE, INTEGER, null, retained = true, unit = PASCAL),
    Property(UPTIME, INTEGER, null, retained = false, unit = SECOND),
    Property(LAST_PING, DATETIME, null, retained = true),
    Property(STATE, ENUM, "ONLINE,OFFLINE", retained = true)
  ),
  SCT013(
    Property(POWER, INTEGER, null, retained = true, unit = DataUnit.WATT),
    Property(LAST_PING, DATETIME, null, retained = true),
    Property(STATE, ENUM, "ONLINE,OFFLINE", retained = true)
  ),
  DHT22(
    Property(TEMPERATURE, FLOAT, null, retained = true, unit = CELSIUS),
    Property(HUMIDITY, FLOAT, "0:100", retained = true, unit = PERCENT),
    Property(UPTIME, INTEGER, null, retained = false, unit = SECOND),
    Property(LAST_PING, DATETIME, null, retained = true),
    Property(STATE, ENUM, "ONLINE,OFFLINE", retained = true)
  ),
  MOTION_DETECTOR(
    Property(STATE, ENUM, "ON,OFF", retained = true)
  ),
  EMULATED_SWITCH(
    Property(STATE, ENUM, "PRESSED,RELEASED", settable = true)
  ),
  TIMER_SWITCH(
    Property(STATE, ENUM, "ON,OFF", retained = true),
    Property(TIMER, INTEGER, "0:1440", settable = true, retained = true, unit = SECOND)
  );

  fun isSerialDevice() = this in SERIAL_BASED_DEVICES

  fun property(type: DevicePropertyType): Property = this.properties.find { it.type == type }!!

  companion object {
    val SERIAL_BASED_DEVICES = listOf(BME280, DHT22, SCT013)
  }
}

enum class DevicePropertyType {
  STATE, POWER, TEMPERATURE, HUMIDITY, PRESSURE, UPTIME, LAST_PING, TIMER;

  override fun toString(): String = this.name.toLowerCase()
}

enum class DataType {
  INTEGER, FLOAT, BOOLEAN, STRING, ENUM, COLOR, DATETIME
}

enum class DataUnit(val value: String) {
  CELSIUS("°C"), FAHRENHEIT("°F"), DEGREE("°"), LITER("L"), GALON("gal"), VOLTS("V"), WATT("W"), AMPERE("A"), PERCENT("%"),
  METER("m"), FEET("ft"), PASCAL("Pa"), PSI("psi"), COUNT("#"), NONE("_"), SECOND("s")
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
