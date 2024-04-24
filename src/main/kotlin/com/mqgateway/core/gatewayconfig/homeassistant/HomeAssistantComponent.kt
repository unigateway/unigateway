package com.mqgateway.core.gatewayconfig.homeassistant

import com.fasterxml.jackson.annotation.JsonIgnore
import com.fasterxml.jackson.annotation.JsonInclude
import com.fasterxml.jackson.annotation.JsonProperty
import com.fasterxml.jackson.annotation.JsonUnwrapped

abstract class HomeAssistantComponent(
  @JsonIgnore val componentType: HomeAssistantComponentType,
  @field:JsonUnwrapped val properties: HomeAssistantComponentBasicProperties
) {
  protected fun uniqueId() = properties.uniqueId()
}

@JsonInclude(JsonInclude.Include.NON_NULL)
data class HomeAssistantComponentBasicProperties(
  @field:JsonProperty("device") val device: HomeAssistantDevice? = null,
  @JsonIgnore val nodeId: String,
  @field:JsonProperty("object_id") val objectId: String,
  @field:JsonProperty("name") val name: String? = null,
) {
  fun uniqueId() = "${nodeId}_$objectId"
}

data class HomeAssistantDevice(
  @field:JsonProperty("identifiers") val identifiers: List<String>,
  @field:JsonProperty("manufacturer") val manufacturer: String? = null,
  @field:JsonProperty("model") val model: String? = null,
  @field:JsonProperty("name") val name: String? = null,
  @field:JsonProperty("sw_version") val firmwareVersion: String? = null,
  @field:JsonProperty("via_device") val viaDevice: String? = null
)

enum class HomeAssistantComponentType(val value: String) {
  LIGHT("light"),
  SWITCH("switch"),
  BINARY_SENSOR("binary_sensor"),
  SENSOR("sensor"),
  COVER("cover"),
  TRIGGER("device_automation"),
}

data class HomeAssistantLight(
  @JsonIgnore val basicProperties: HomeAssistantComponentBasicProperties,
  @field:JsonProperty("state_topic") val stateTopic: String,
  @field:JsonProperty("command_topic") val commandTopic: String,
  @field:JsonProperty("retain") val retain: Boolean,
  @field:JsonProperty("payload_on") val payloadOn: String,
  @field:JsonProperty("payload_off") val payloadOff: String
) : HomeAssistantComponent(HomeAssistantComponentType.LIGHT, basicProperties) {
  @field:JsonProperty("unique_id") val uniqueId: String = uniqueId()
}

data class HomeAssistantSwitch(
  @JsonIgnore val basicProperties: HomeAssistantComponentBasicProperties,
  @field:JsonProperty("state_topic") val stateTopic: String,
  @field:JsonProperty("command_topic") val commandTopic: String,
  @field:JsonProperty("retain") val retain: Boolean,
  @field:JsonProperty("payload_on") val payloadOn: String,
  @field:JsonProperty("payload_off") val payloadOff: String,
  @field:JsonIgnore val deviceClass: DeviceClass
) : HomeAssistantComponent(HomeAssistantComponentType.SWITCH, basicProperties) {
  @field:JsonProperty("unique_id") val uniqueId: String = uniqueId()
  @field:JsonProperty("device_class") val deviceClassOutput: String? = if (deviceClass != DeviceClass.NONE) deviceClass.value else null

  enum class DeviceClass(val value: String) {
    NONE("None"),
    OUTLET("outlet"),
    SWITCH("switch");

    companion object {
      fun fromValue(value: String): DeviceClass? {
        return DeviceClass.values().find { it.value == value }
      }
    }
  }
}

data class HomeAssistantBinarySensor(
  @JsonIgnore val basicProperties: HomeAssistantComponentBasicProperties,
  @field:JsonProperty("state_topic") val stateTopic: String,
  @field:JsonProperty("payload_on") val payloadOn: String,
  @field:JsonProperty("payload_off") val payloadOff: String,
  @field:JsonIgnore val deviceClass: DeviceClass
) : HomeAssistantComponent(HomeAssistantComponentType.BINARY_SENSOR, basicProperties) {

  @field:JsonProperty("unique_id") val uniqueId: String = "${properties.nodeId}_${properties.objectId}"
  @field:JsonProperty("device_class") val deviceClassOutput: String? = if (deviceClass != DeviceClass.NONE) deviceClass.value else null

  enum class DeviceClass(val value: String) {
    NONE("None"),
    BATTERY("battery"),
    BATTERY_CHARGING("battery_charging"),
    CARBON_MONOXIDE("carbon_monoxide"),
    COLD("cold"),
    CONNECTIVITY("connectivity"),
    DOOR("door"),
    GARAGE_DOOR("garage_door"),
    GAS("gas"),
    HEAT("heat"),
    LIGHT("light"),
    LOCK("lock"),
    MOISTURE("moisture"),
    MOTION("motion"),
    MOVING("moving"),
    OCCUPANCY("occupancy"),
    OPENING("opening"),
    PLUG("plug"),
    POWER("power"),
    PRESENCE("presence"),
    PROBLEM("problem"),
    RUNNING("running"),
    SAFETY("safety"),
    SMOKE("smoke"),
    SOUND("sound"),
    TAMPER("tamper"),
    UPDATE("update"),
    VIBRATION("vibration"),
    WINDOW("window");

    companion object {
      fun fromValue(value: String): DeviceClass? {
        return DeviceClass.values().find { it.value == value }
      }
    }
  }
}

data class HomeAssistantSensor(
  @JsonIgnore val basicProperties: HomeAssistantComponentBasicProperties,
  @field:JsonProperty("availability_topic") val availabilityTopic: String? = null,
  @field:JsonProperty("payload_available") val payloadAvailable: String? = null,
  @field:JsonProperty("payload_not_available") val payloadNotAvailable: String? = null,
  @field:JsonIgnore val deviceClass: DeviceClass,
  @field:JsonProperty("state_topic") val stateTopic: String,
  @field:JsonProperty("unit_of_measurement") val unitOfMeasurement: String? = null
) : HomeAssistantComponent(HomeAssistantComponentType.SENSOR, basicProperties) {

  @field:JsonProperty("unique_id") val uniqueId: String = "${properties.nodeId}_${properties.objectId}"
  @field:JsonProperty("device_class") val deviceClassOutput: String? = if (deviceClass != DeviceClass.NONE) deviceClass.value else null

  enum class DeviceClass(val value: String) {
    NONE("None"),
    APPARENT_POWER("apparent_power"),
    AQI("aqi"),
    ATMOSPHERIC_PRESSURE("atmospheric_pressure"),
    BATTERY("battery"),
    CARBON_DIOXIDE("carbon_dioxide"),
    CARBON_MONOXIDE("carbon_monoxide"),
    CURRENT("current"),
    DATA_RATE("data_rate"),
    DATA_SIZE("data_size"),
    DATE("date"),
    DISTANCE("distance"),
    DURATION("duration"),
    ENERGY("energy"),
    ENERGY_STORAGE("energy_storage"),
    ENUM("enum"),
    FREQUENCY("frequency"),
    GAS("gas"),
    HUMIDITY("humidity"),
    ILLUMINANCE("illuminance"),
    IRRADIANCE("irradiance"),
    MOISTURE("moisture"),
    MONETARY("monetary"),
    NITROGEN_DIOXIDE("nitrogen_dioxide"),
    NITROGEN_MONOXIDE("nitrogen_monoxide"),
    NITROUS_OXIDE("nitrous_oxide"),
    OZONE("ozone"),
    PM1("pm1"),
    PM25("pm25"),
    PM10("pm10"),
    POWER_FACTOR("power_factor"),
    POWER("power"),
    PRECIPITATION("precipitation"),
    PRECIPITATION_INTENSITY("precipitation_intensity"),
    PRESSURE("pressure"),
    REACTIVE_POWER("reactive_power"),
    SIGNAL_STRENGTH("signal_strength"),
    SOUND_PRESSURE("sound_pressure"),
    SPEED("speed"),
    SULPHUR_DIOXIDE("sulphur_dioxide"),
    TEMPERATURE("temperature"),
    TIMESTAMP("timestamp"),
    VOLATILE_ORGANIC_COMPOUNDS("volatile_organic_compounds"),
    VOLATILE_ORGANIC_COMPOUNDS_PARTS("volatile_organic_compounds_parts"),
    VOLTAGE("voltage"),
    VOLUME("volume"),
    VOLUME_STORAGE("volume_storage"),
    WATER("water"),
    WEIGHT("weight"),
    WIND_SPEED("wind_speed");

    override fun toString(): String {
      return value
    }
  }
}

data class HomeAssistantTrigger(
  @JsonIgnore val basicProperties: HomeAssistantComponentBasicProperties,
  @field:JsonProperty("topic") val topic: String,
  @field:JsonProperty("payload") val payload: String,
  @field:JsonProperty("type") val type: TriggerType,
  @field:JsonProperty("subtype") val subtype: String
) : HomeAssistantComponent(HomeAssistantComponentType.TRIGGER, basicProperties) {

  @JsonProperty("automation_type")
  val automationType = "trigger"

  enum class TriggerType {
    BUTTON_SHORT_PRESS,
    BUTTON_SHORT_RELEASE,
    BUTTON_LONG_PRESS,
    BUTTON_LONG_RELEASE,
    BUTTON_DOUBLE_PRESS,
    BUTTON_TRIPLE_PRESS,
    BUTTON_QUADRUPLE_PRESS,
  }
}

data class HomeAssistantCover(
  @JsonIgnore val basicProperties: HomeAssistantComponentBasicProperties,
  @field:JsonProperty("state_topic") val stateTopic: String?,
  @field:JsonProperty("command_topic") val commandTopic: String,
  @field:JsonProperty("position_topic") val positionTopic: String?,
  @field:JsonProperty("set_position_topic") val setPositionTopic: String?,
  @field:JsonIgnore val deviceClass: DeviceClass,
  @field:JsonProperty("payload_open") val payloadOpen: String,
  @field:JsonProperty("payload_close") val payloadClose: String,
  @field:JsonProperty("payload_stop") val payloadStop: String,
  @field:JsonProperty("position_open") val positionOpen: Int? = 100,
  @field:JsonProperty("position_closed") val positionClosed: Int? = 0,
  @field:JsonProperty("state_open") val stateOpen: String?,
  @field:JsonProperty("state_closed") val stateClosed: String?,
  @field:JsonProperty("state_opening") val stateOpening: String?,
  @field:JsonProperty("state_closing") val stateClosing: String?,
  @field:JsonProperty("state_stopped") val stateStopped: String?,
  @field:JsonProperty("retain") val retain: Boolean

) : HomeAssistantComponent(HomeAssistantComponentType.COVER, basicProperties) {

  @field:JsonProperty("unique_id") val uniqueId: String = uniqueId()
  @field:JsonProperty("device_class") val deviceClassOutput: String? = if (deviceClass != DeviceClass.NONE) deviceClass.name else null

  enum class DeviceClass {
    NONE,
    AWNING,
    BLIND,
    CURTAIN,
    DAMPER,
    DOOR,
    GARAGE,
    GATE,
    SHADE,
    SHUTTER,
    WINDOW
  }
}
