package com.mqgateway.core.gatewayconfig.homeassistant

import com.fasterxml.jackson.annotation.JsonIgnore
import com.fasterxml.jackson.annotation.JsonProperty
import com.fasterxml.jackson.annotation.JsonUnwrapped

abstract class HomeAssistantComponent(
  @JsonIgnore val componentType: HomeAssistantComponentType,
  @field:JsonUnwrapped val properties: HomeAssistantComponentBasicProperties
)

data class HomeAssistantComponentBasicProperties(
  @field:JsonProperty("name") val name: String,
  @JsonIgnore val nodeId: String,
  @JsonIgnore val objectId: String
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
) : HomeAssistantComponent(HomeAssistantComponentType.LIGHT, basicProperties)

data class HomeAssistantSwitch(
  @JsonIgnore val basicProperties: HomeAssistantComponentBasicProperties,
  @field:JsonProperty("state_topic") val stateTopic: String,
  @field:JsonProperty("command_topic") val commandTopic: String,
  @field:JsonProperty("retain") val retain: Boolean,
  @field:JsonProperty("payload_on") val payloadOn: String,
  @field:JsonProperty("payload_off") val payloadOff: String
) : HomeAssistantComponent(HomeAssistantComponentType.SWITCH, basicProperties)

data class HomeAssistantBinarySensor(
  @JsonIgnore val basicProperties: HomeAssistantComponentBasicProperties,
  @field:JsonProperty("state_topic") val stateTopic: String,
  @field:JsonProperty("payload_on") val payloadOn: String,
  @field:JsonProperty("payload_off") val payloadOff: String,
  @field:JsonProperty("device_class") val deviceClass: DeviceClass
) : HomeAssistantComponent(HomeAssistantComponentType.BINARY_SENSOR, basicProperties) {
  enum class DeviceClass(val value: String) {
    MOTION("motion"),
    OPENING("opening"),
    GAS("gas"),
  }
}

data class HomeAssistantSensor(
  @JsonIgnore val basicProperties: HomeAssistantComponentBasicProperties,
  @field:JsonProperty("availability_topic") val availabilityTopic: String,
  @field:JsonProperty("payload_available") val payloadAvailable: String,
  @field:JsonProperty("payload_not_available") val payloadNotAvailable: String,
  @field:JsonProperty("device_class") val deviceClass: DeviceClass,
  @field:JsonProperty("state_topic") val stateTopic: String,
  @field:JsonProperty("unit_of_measurement") val unitOfMeasurement: String
) : HomeAssistantComponent(HomeAssistantComponentType.SENSOR, basicProperties) {
  enum class DeviceClass(val value: String) {
    None("None"),
    BATTERY("battery"),
    HUMIDITY("humidity"),
    ILLUMINANCE("illuminance"),
    SIGNAL_STRENGTH("signal_strength"),
    TEMPERATURE("temperature"),
    POWER("power"),
    PRESSURE("pressure"),
    TIMESTAMP("timestamp"),
    CURRENT("current"),
    ENERGY("energy"),
    POWER_FACTOR("power_factor"),
    VOLTAGE("voltage"),
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
  @field:JsonProperty("device_class") val deviceClass: DeviceClass,
  @field:JsonProperty("payload_open") val payloadOpen: String,
  @field:JsonProperty("payload_close") val payloadClose: String,
  @field:JsonProperty("payload_stop") val payloadStop: String,
  @field:JsonProperty("position_open") val positionOpen: Int? = 100,
  @field:JsonProperty("position_closed") val positionClosed: Int? = 0,
  @field:JsonProperty("state_open") val stateOpen: String?,
  @field:JsonProperty("state_closed") val stateClosed: String?,
  @field:JsonProperty("retain") val retain: Boolean

) : HomeAssistantComponent(HomeAssistantComponentType.COVER, basicProperties) {
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
