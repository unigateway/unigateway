package com.mqgateway.core.device

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

enum class DevicePropertyType {
  STATE, POWER, TEMPERATURE, HUMIDITY, PRESSURE, UPTIME, LAST_PING, TIMER, POSITION, MEMORY, IP_ADDRESS, AVAILABILITY;

  override fun toString(): String = this.name.toLowerCase()
}

enum class DataType {
  INTEGER, FLOAT, BOOLEAN, STRING, ENUM, COLOR, DATETIME
}

enum class DataUnit(val value: String?) {
  CELSIUS("°C"), FAHRENHEIT("°F"), DEGREE("°"), LITER("L"), GALON("gal"), VOLTS("V"), WATT("W"), AMPERE("A"), PERCENT("%"),
  METER("m"), FEET("ft"), PASCAL("Pa"), PSI("psi"), COUNT("#"), SECOND("s"), BYTES("B"), NONE(null)
}
