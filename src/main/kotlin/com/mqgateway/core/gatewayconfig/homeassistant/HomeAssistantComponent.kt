package com.mqgateway.core.gatewayconfig.homeassistant

import com.fasterxml.jackson.annotation.JsonIgnore
import com.fasterxml.jackson.annotation.JsonProperty
import com.fasterxml.jackson.annotation.JsonUnwrapped

abstract class HomeAssistantComponent(
  @JsonIgnore val componentType: HomeAssistantComponentType,
  @JsonUnwrapped val properties: HomeAssistantComponentBasicProperties) {
}

data class HomeAssistantComponentBasicProperties(
  @JsonProperty("name") val name: String,
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
  @JsonProperty("state_topic") val stateTopic: String,
  @JsonProperty("command_topic") val commandTopic: String,
  @JsonProperty("retain") val retain: Boolean,
  @JsonProperty("payload_on") val payloadOn: String,
  @JsonProperty("payload_off") val payloadOff: String
) : HomeAssistantComponent(HomeAssistantComponentType.LIGHT, basicProperties)

data class HomeAssistantSwitch(
  @JsonIgnore val basicProperties: HomeAssistantComponentBasicProperties,
  @JsonProperty("state_topic") val stateTopic: String,
  @JsonProperty("command_topic") val commandTopic: String,
  @JsonProperty("retain") val retain: Boolean,
  @JsonProperty("payload_on") val payloadOn: String,
  @JsonProperty("payload_off") val payloadOff: String
) : HomeAssistantComponent(HomeAssistantComponentType.SWITCH, basicProperties)

data class HomeAssistantBinarySensor(
  @JsonIgnore val basicProperties: HomeAssistantComponentBasicProperties,
  @JsonProperty("state_topic") val stateTopic: String,
  @JsonProperty("payload_on") val payloadOn: String,
  @JsonProperty("payload_off") val payloadOff: String,
  @JsonProperty("device_class") val deviceClass: DeviceClass
) : HomeAssistantComponent(HomeAssistantComponentType.BINARY_SENSOR, basicProperties) {
  enum class DeviceClass(val value: String) {
    MOTION("motion"),
    OPENING("opening"),
    GAS("gas"),
  }
}

data class HomeAssistantSensor(
  @JsonIgnore val basicProperties: HomeAssistantComponentBasicProperties,
  @JsonProperty("availability_topic") val availabilityTopic: String,
  @JsonProperty("payload_available") val payloadAvailable: String,
  @JsonProperty("payload_not_available") val payloadNotAvailable: String,
  @JsonProperty("device_class") val deviceClass: DeviceClass,
  @JsonProperty("state_topic") val stateTopic: String,
  @JsonProperty("unit_of_measurement") val unitOfMeasurement: String
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
  @JsonProperty("topic") val topic: String,
  @JsonProperty("payload") val payload: String,
  @JsonProperty("type") val type: TriggerType,
  @JsonProperty("subtype") val subtype: String,
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
  @JsonProperty("state_topic") val stateTopic: String,
  @JsonProperty("command_topic") val commandTopic: String,
  @JsonProperty("device_class") val deviceClass: DeviceClass,
  @JsonProperty("payload_open") val payloadOpen: String,
  @JsonProperty("payload_close") val payloadClose: String,
  @JsonProperty("payload_stop") val payloadStop: String,
  @JsonProperty("position_open") val positionOpen: Int = 100,
  @JsonProperty("position_closed") val positionClosed: Int = 0,
  @JsonProperty("state_open") val stateOpen: String,
  @JsonProperty("state_closed") val stateClosed: String,
  @JsonProperty("state_opening") val stateOpening: String,
  @JsonProperty("state_closing") val stateClosing: String,
  @JsonProperty("retain") val retain: Boolean,

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

