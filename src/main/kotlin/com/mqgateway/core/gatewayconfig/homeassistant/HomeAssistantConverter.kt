package com.mqgateway.core.gatewayconfig.homeassistant

import com.mqgateway.core.device.EmulatedSwitchButtonDevice
import com.mqgateway.core.device.MotionSensorDevice
import com.mqgateway.core.device.ReedSwitchDevice
import com.mqgateway.core.device.RelayDevice
import com.mqgateway.core.device.ShutterDevice
import com.mqgateway.core.device.SwitchButtonDevice
import com.mqgateway.core.device.serial.PeriodicSerialInputDevice
import com.mqgateway.core.gatewayconfig.DeviceConfig
import com.mqgateway.core.gatewayconfig.DevicePropertyType
import com.mqgateway.core.gatewayconfig.DeviceType
import com.mqgateway.core.gatewayconfig.Gateway
import com.mqgateway.core.gatewayconfig.homeassistant.HomeAssistantComponentType.LIGHT
import com.mqgateway.core.gatewayconfig.homeassistant.HomeAssistantTrigger.TriggerType.BUTTON_LONG_PRESS
import com.mqgateway.core.gatewayconfig.homeassistant.HomeAssistantTrigger.TriggerType.BUTTON_LONG_RELEASE
import com.mqgateway.core.gatewayconfig.homeassistant.HomeAssistantTrigger.TriggerType.BUTTON_SHORT_PRESS
import com.mqgateway.core.gatewayconfig.homeassistant.HomeAssistantTrigger.TriggerType.BUTTON_SHORT_RELEASE
import com.mqgateway.homie.HOMIE_PREFIX
import mu.KotlinLogging

private val LOGGER = KotlinLogging.logger {}

class HomeAssistantConverter {

  fun convert(gateway: Gateway): List<HomeAssistantComponent> {
    LOGGER.info { "Converting Gateway configuration to HomeAssistant auto-discovery config" }
    val devices = gateway.rooms
      .flatMap { it.points }
      .flatMap { it.devices }

    return devices.flatMap { device ->
      val basicProperties = HomeAssistantComponentBasicProperties(device.name, gateway.name, device.id)

      val components = when (device.type) {
        DeviceType.RELAY -> {
          val stateTopic = homieStateTopic(gateway, device, DevicePropertyType.STATE)
          val commandTopic = homieCommandTopic(gateway, device, DevicePropertyType.STATE)
          if (device.config[DEVICE_CONFIG_HA_COMPONENT].equals(LIGHT.value, true)) {
            listOf(HomeAssistantLight(basicProperties, stateTopic, commandTopic, true, RelayDevice.STATE_ON, RelayDevice.STATE_OFF))
          } else {
            listOf(HomeAssistantSwitch(basicProperties, stateTopic, commandTopic, true, RelayDevice.STATE_ON, RelayDevice.STATE_OFF))
          }
        }
        DeviceType.SWITCH_BUTTON -> {
          val homieStateTopic = homieStateTopic(gateway, device, DevicePropertyType.STATE)
          listOf(
            HomeAssistantTrigger(
              HomeAssistantComponentBasicProperties(device.name, gateway.name, "${device.id}_PRESS"),
              homieStateTopic,
              SwitchButtonDevice.PRESSED_STATE_VALUE,
              BUTTON_SHORT_PRESS,
              "button"
            ),
            HomeAssistantTrigger(
              HomeAssistantComponentBasicProperties(device.name, gateway.name, "${device.id}_RELEASE"),
              homieStateTopic,
              SwitchButtonDevice.RELEASED_STATE_VALUE,
              BUTTON_SHORT_RELEASE,
              "button"
            ),
            HomeAssistantTrigger(
              HomeAssistantComponentBasicProperties(device.name, gateway.name, "${device.id}_LONG_PRESS"),
              homieStateTopic,
              SwitchButtonDevice.LONG_PRESSED_STATE_VALUE,
              BUTTON_LONG_PRESS,
              "button"
            ),
            HomeAssistantTrigger(
              HomeAssistantComponentBasicProperties(device.name, gateway.name, "${device.id}_LONG_RELEASE"),
              homieStateTopic,
              SwitchButtonDevice.LONG_RELEASED_STATE_VALUE,
              BUTTON_LONG_RELEASE,
              "button"
            )
          )
        }
        DeviceType.REED_SWITCH -> {
          listOf(
            HomeAssistantBinarySensor(
              basicProperties,
              homieStateTopic(gateway, device, DevicePropertyType.STATE),
              ReedSwitchDevice.OPEN_STATE_VALUE,
              ReedSwitchDevice.CLOSED_STATE_VALUE,
              HomeAssistantBinarySensor.DeviceClass.OPENING
            )
          )
        }
        DeviceType.MOTION_DETECTOR -> {
          listOf(
            HomeAssistantBinarySensor(
              basicProperties,
              homieStateTopic(gateway, device, DevicePropertyType.STATE),
              MotionSensorDevice.MOVE_START_STATE_VALUE,
              MotionSensorDevice.MOVE_STOP_STATE_VALUE,
              HomeAssistantBinarySensor.DeviceClass.MOTION
            )
          )
        }
        DeviceType.EMULATED_SWITCH -> {
          val stateTopic = homieStateTopic(gateway, device, DevicePropertyType.STATE)
          val commandTopic = homieCommandTopic(gateway, device, DevicePropertyType.STATE)
          listOf(
            HomeAssistantSwitch(
              basicProperties,
              stateTopic,
              commandTopic,
              false,
              EmulatedSwitchButtonDevice.PRESSED_STATE_VALUE,
              EmulatedSwitchButtonDevice.RELEASED_STATE_VALUE
            )
          )
        }
        DeviceType.BME280 -> {
          val availabilityTopic = homieStateTopic(gateway, device, DevicePropertyType.STATE)

          listOf(
            HomeAssistantSensor(
              HomeAssistantComponentBasicProperties(device.name, gateway.name, "${device.id}_TEMPERATURE"),
              availabilityTopic,
              PeriodicSerialInputDevice.AVAILABILITY_ONLINE_STATE,
              PeriodicSerialInputDevice.AVAILABILITY_OFFLINE_STATE,
              HomeAssistantSensor.DeviceClass.TEMPERATURE,
              homieStateTopic(gateway, device, DevicePropertyType.TEMPERATURE),
              device.type.property(DevicePropertyType.TEMPERATURE).unit.value
            ),
            HomeAssistantSensor(
              HomeAssistantComponentBasicProperties(device.name, gateway.name, "${device.id}_HUMIDITY"),
              availabilityTopic,
              PeriodicSerialInputDevice.AVAILABILITY_ONLINE_STATE,
              PeriodicSerialInputDevice.AVAILABILITY_OFFLINE_STATE,
              HomeAssistantSensor.DeviceClass.HUMIDITY,
              homieStateTopic(gateway, device, DevicePropertyType.HUMIDITY),
              device.type.property(DevicePropertyType.HUMIDITY).unit.value
            ),
            HomeAssistantSensor(
              HomeAssistantComponentBasicProperties(device.name, gateway.name, "${device.id}_PRESSURE"),
              availabilityTopic,
              PeriodicSerialInputDevice.AVAILABILITY_ONLINE_STATE,
              PeriodicSerialInputDevice.AVAILABILITY_OFFLINE_STATE,
              HomeAssistantSensor.DeviceClass.PRESSURE,
              homieStateTopic(gateway, device, DevicePropertyType.PRESSURE),
              device.type.property(DevicePropertyType.PRESSURE).unit.value
            ),
            HomeAssistantSensor(
              HomeAssistantComponentBasicProperties(device.name, gateway.name, "${device.id}_LAST_PING"),
              availabilityTopic,
              PeriodicSerialInputDevice.AVAILABILITY_ONLINE_STATE,
              PeriodicSerialInputDevice.AVAILABILITY_OFFLINE_STATE,
              HomeAssistantSensor.DeviceClass.TIMESTAMP,
              homieStateTopic(gateway, device, DevicePropertyType.LAST_PING),
              device.type.property(DevicePropertyType.LAST_PING).unit.value
            )
          )
        }
        DeviceType.DHT22 -> {
          val availabilityTopic = homieStateTopic(gateway, device, DevicePropertyType.STATE)
          listOf(
            HomeAssistantSensor(
              HomeAssistantComponentBasicProperties(device.name, gateway.name, "${device.id}_TEMPERATURE"),
              availabilityTopic,
              PeriodicSerialInputDevice.AVAILABILITY_ONLINE_STATE,
              PeriodicSerialInputDevice.AVAILABILITY_OFFLINE_STATE,
              HomeAssistantSensor.DeviceClass.TEMPERATURE,
              homieStateTopic(gateway, device, DevicePropertyType.TEMPERATURE),
              device.type.property(DevicePropertyType.TEMPERATURE).unit.value
            ),
            HomeAssistantSensor(
              HomeAssistantComponentBasicProperties(device.name, gateway.name, "${device.id}_HUMIDITY"),
              availabilityTopic,
              PeriodicSerialInputDevice.AVAILABILITY_ONLINE_STATE,
              PeriodicSerialInputDevice.AVAILABILITY_OFFLINE_STATE,
              HomeAssistantSensor.DeviceClass.HUMIDITY,
              homieStateTopic(gateway, device, DevicePropertyType.HUMIDITY),
              device.type.property(DevicePropertyType.HUMIDITY).unit.value
            ),
            HomeAssistantSensor(
              HomeAssistantComponentBasicProperties(device.name, gateway.name, "${device.id}_LAST_PING"),
              availabilityTopic,
              PeriodicSerialInputDevice.AVAILABILITY_ONLINE_STATE,
              PeriodicSerialInputDevice.AVAILABILITY_OFFLINE_STATE,
              HomeAssistantSensor.DeviceClass.TIMESTAMP,
              homieStateTopic(gateway, device, DevicePropertyType.LAST_PING),
              device.type.property(DevicePropertyType.LAST_PING).unit.value
            )
          )
        }
        DeviceType.SCT013 -> {
          val availabilityTopic = homieStateTopic(gateway, device, DevicePropertyType.STATE)
          listOf(
            HomeAssistantSensor(
              HomeAssistantComponentBasicProperties(device.name, gateway.name, "${device.id}_POWER"),
              availabilityTopic,
              PeriodicSerialInputDevice.AVAILABILITY_ONLINE_STATE,
              PeriodicSerialInputDevice.AVAILABILITY_OFFLINE_STATE,
              HomeAssistantSensor.DeviceClass.POWER,
              homieStateTopic(gateway, device, DevicePropertyType.POWER),
              device.type.property(DevicePropertyType.POWER).unit.value
            ),
            HomeAssistantSensor(
              HomeAssistantComponentBasicProperties(device.name, gateway.name, "${device.id}_LAST_PING"),
              availabilityTopic,
              PeriodicSerialInputDevice.AVAILABILITY_ONLINE_STATE,
              PeriodicSerialInputDevice.AVAILABILITY_OFFLINE_STATE,
              HomeAssistantSensor.DeviceClass.TIMESTAMP,
              homieStateTopic(gateway, device, DevicePropertyType.LAST_PING),
              device.type.property(DevicePropertyType.LAST_PING).unit.value
            )
          )
        }
        DeviceType.SHUTTER -> listOf(
          HomeAssistantCover(
            basicProperties,
            homieStateTopic(gateway, device, DevicePropertyType.STATE),
            homieCommandTopic(gateway, device, DevicePropertyType.STATE),
            homieStateTopic(gateway, device, DevicePropertyType.POSITION),
            homieCommandTopic(gateway, device, DevicePropertyType.POSITION),
            HomeAssistantCover.DeviceClass.SHUTTER,
            ShutterDevice.Command.OPEN.name,
            ShutterDevice.Command.CLOSE.name,
            ShutterDevice.Command.STOP.name,
            ShutterDevice.POSITION_OPEN,
            ShutterDevice.POSITION_CLOSED,
            null,
            null,
            false
          )
        )
        DeviceType.TIMER_SWITCH -> listOf()
      }

      LOGGER.debug { "Device ${device.id} (${device.type}) converted to HA components types: ${components.map { it.componentType }}" }
      LOGGER.trace { "Device $device converted to HA components: $components" }
      return@flatMap components
    }
  }

  private fun homieStateTopic(gateway: Gateway, device: DeviceConfig, propertyType: DevicePropertyType): String {
    return "$HOMIE_PREFIX/${gateway.name}/${device.id}/$propertyType"
  }

  private fun homieCommandTopic(gateway: Gateway, device: DeviceConfig, propertyType: DevicePropertyType): String {
    return homieStateTopic(gateway, device, propertyType) + "/set"
  }

  companion object {
    const val DEVICE_CONFIG_HA_COMPONENT: String = "haComponent"
  }
}
