package com.mqgateway.core.gatewayconfig

import com.mqgateway.core.gatewayconfig.DataType.ENUM
import com.mqgateway.core.gatewayconfig.DataType.FLOAT
import com.mqgateway.core.gatewayconfig.DataType.INTEGER
import com.mqgateway.core.gatewayconfig.DataType.STRING
import com.mqgateway.core.gatewayconfig.DataUnit.BYTES
import com.mqgateway.core.gatewayconfig.DataUnit.CELSIUS
import com.mqgateway.core.gatewayconfig.DataUnit.PERCENT
import com.mqgateway.core.gatewayconfig.DataUnit.SECOND
import com.mqgateway.core.gatewayconfig.DevicePropertyType.IP_ADDRESS
import com.mqgateway.core.gatewayconfig.DevicePropertyType.MEMORY
import com.mqgateway.core.gatewayconfig.DevicePropertyType.POSITION
import com.mqgateway.core.gatewayconfig.DevicePropertyType.STATE
import com.mqgateway.core.gatewayconfig.DevicePropertyType.TEMPERATURE
import com.mqgateway.core.gatewayconfig.DevicePropertyType.TIMER
import com.mqgateway.core.gatewayconfig.DevicePropertyType.UPTIME
import com.mqgateway.core.io.provider.Connector
import kotlinx.serialization.Serializable
import java.util.Locale
import com.mqgateway.core.gatewayconfig.DeviceProperty as Property

@Serializable
data class DeviceConfiguration
@JvmOverloads constructor(
  val id: String,
  val name: String,
  val type: DeviceType,
  val connectors: Map<String, Connector> = emptyMap(),
  val internalDevices: Map<String, InternalDeviceConfiguration> = emptyMap(),
  val config: Map<String, String> = emptyMap()
)

@Serializable
data class InternalDeviceConfiguration(
  val referenceId: String
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
  UNIGATEWAY(
    Property(TEMPERATURE, FLOAT, null, retained = true, unit = CELSIUS),
    Property(MEMORY, INTEGER, null, retained = true, unit = BYTES),
    Property(UPTIME, INTEGER, null, retained = true, unit = SECOND),
    Property(IP_ADDRESS, STRING, null, retained = true)
  ),
  RELAY(
    Property(STATE, ENUM, "ON,OFF", settable = true, retained = true)
  ),
  SWITCH_BUTTON(
    Property(STATE, ENUM, "PRESSED,RELEASED")
  ),
  REED_SWITCH(
    Property(STATE, ENUM, "OPEN,CLOSED", retained = true)
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
  ),
  SHUTTER(
    Property(POSITION, INTEGER, "0:100", settable = true, retained = true, unit = PERCENT),
    Property(STATE, ENUM, "OPEN,CLOSE,STOP", retained = true, settable = true)
  ),
  GATE(
    Property(STATE, ENUM, "OPEN,CLOSE,STOP", retained = true, settable = true)
  );

  fun property(type: DevicePropertyType): Property = this.properties.find { it.type == type }!!
}

enum class DevicePropertyType {
  STATE, POWER, TEMPERATURE, HUMIDITY, PRESSURE, UPTIME, LAST_PING, TIMER, POSITION, MEMORY, IP_ADDRESS, AVAILABILITY;

  override fun toString(): String = this.name.lowercase(Locale.getDefault())
}

enum class DataType {
  INTEGER, FLOAT, BOOLEAN, STRING, ENUM, COLOR, DATETIME
}

enum class DataUnit(val value: String?) {
  CELSIUS("°C"), FAHRENHEIT("°F"), DEGREE("°"), LITER("L"), GALON("gal"), VOLTS("V"), WATT("W"), AMPERE("A"), PERCENT("%"),
  METER("m"), FEET("ft"), PASCAL("Pa"), PSI("psi"), COUNT("#"), SECOND("s"), BYTES("B"), NONE(null)
}
