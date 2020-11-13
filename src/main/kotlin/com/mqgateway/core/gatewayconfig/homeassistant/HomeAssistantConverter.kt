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

class HomeAssistantConverter(private val gatewayFirmwareVersion: String) {

  fun convert(gateway: Gateway): List<HomeAssistantComponent> {
    LOGGER.info { "Converting Gateway configuration to HomeAssistant auto-discovery config" }
    val devices = gateway.rooms
      .flatMap { it.points }
      .flatMap { it.devices }

    val mqGatewayCoreComponents = convertMqGatewayRootDeviceToHaSensors(gateway)

    return mqGatewayCoreComponents + devices.flatMap { device ->
      val haDevice = HomeAssistantDevice(
        identifiers = listOf("${gateway.name}_${device.id}"),
        name = device.name,
        manufacturer = "Aetas",
        viaDevice = gateway.name,
        firmwareVersion = gatewayFirmwareVersion,
        model = "MqGateway ${device.type.name}"
      )
      val basicProperties = HomeAssistantComponentBasicProperties(haDevice, gateway.name, device.id)

      val components = when (device.type) {
        DeviceType.RELAY -> {
          val stateTopic = homieStateTopic(gateway, device.id, DevicePropertyType.STATE)
          val commandTopic = homieCommandTopic(gateway, device, DevicePropertyType.STATE)
          if (device.config[DEVICE_CONFIG_HA_COMPONENT].equals(LIGHT.value, true)) {
            listOf(HomeAssistantLight(basicProperties, device.name, stateTopic, commandTopic, true, RelayDevice.STATE_ON, RelayDevice.STATE_OFF))
          } else {
            listOf(HomeAssistantSwitch(basicProperties, device.name, stateTopic, commandTopic, true, RelayDevice.STATE_ON, RelayDevice.STATE_OFF))
          }
        }
        DeviceType.SWITCH_BUTTON -> {
          val homieStateTopic = homieStateTopic(gateway, device.id, DevicePropertyType.STATE)
          when {
            device.config[DEVICE_CONFIG_HA_COMPONENT].equals(HomeAssistantComponentType.TRIGGER.value, true) -> {
              listOf(
                HomeAssistantTrigger(
                      HomeAssistantComponentBasicProperties(haDevice, gateway.name, "${device.id}_PRESS"),
                      homieStateTopic,
                      SwitchButtonDevice.PRESSED_STATE_VALUE,
                      BUTTON_SHORT_PRESS,
                      "button"
                ),
                HomeAssistantTrigger(
                      HomeAssistantComponentBasicProperties(haDevice, gateway.name, "${device.id}_RELEASE"),
                      homieStateTopic,
                      SwitchButtonDevice.RELEASED_STATE_VALUE,
                      BUTTON_SHORT_RELEASE,
                      "button"
                ),
                HomeAssistantTrigger(
                      HomeAssistantComponentBasicProperties(haDevice, gateway.name, "${device.id}_LONG_PRESS"),
                      homieStateTopic,
                      SwitchButtonDevice.LONG_PRESSED_STATE_VALUE,
                      BUTTON_LONG_PRESS,
                      "button"
                ),
                HomeAssistantTrigger(
                      HomeAssistantComponentBasicProperties(haDevice, gateway.name, "${device.id}_LONG_RELEASE"),
                      homieStateTopic,
                      SwitchButtonDevice.LONG_RELEASED_STATE_VALUE,
                      BUTTON_LONG_RELEASE,
                      "button"
                )
              )
            }
            device.config[DEVICE_CONFIG_HA_COMPONENT].equals(HomeAssistantComponentType.SENSOR.value, true) -> {
              listOf(
                HomeAssistantSensor(
                      basicProperties = basicProperties,
                      name = device.name,
                      stateTopic = homieStateTopic,
                      deviceClass = HomeAssistantSensor.DeviceClass.NONE
                )
              )
            }
            else -> {
              listOf(
                HomeAssistantBinarySensor(
                      basicProperties,
                      device.name,
                      homieStateTopic,
                      SwitchButtonDevice.PRESSED_STATE_VALUE,
                      SwitchButtonDevice.RELEASED_STATE_VALUE,
                      HomeAssistantBinarySensor.DeviceClass.NONE
                )
              )
            }
          }
        }
        DeviceType.REED_SWITCH -> {
          listOf(
            HomeAssistantBinarySensor(
              basicProperties,
              device.name,
              homieStateTopic(gateway, device.id, DevicePropertyType.STATE),
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
              device.name,
              homieStateTopic(gateway, device.id, DevicePropertyType.STATE),
              MotionSensorDevice.MOVE_START_STATE_VALUE,
              MotionSensorDevice.MOVE_STOP_STATE_VALUE,
              HomeAssistantBinarySensor.DeviceClass.MOTION
            )
          )
        }
        DeviceType.EMULATED_SWITCH -> {
          val stateTopic = homieStateTopic(gateway, device.id, DevicePropertyType.STATE)
          val commandTopic = homieCommandTopic(gateway, device, DevicePropertyType.STATE)
          listOf(
            HomeAssistantSwitch(
              basicProperties,
              device.name,
              stateTopic,
              commandTopic,
              false,
              EmulatedSwitchButtonDevice.PRESSED_STATE_VALUE,
              EmulatedSwitchButtonDevice.RELEASED_STATE_VALUE
            )
          )
        }
        DeviceType.BME280 -> {
          val availabilityTopic = homieStateTopic(gateway, device.id, DevicePropertyType.STATE)

          listOf(
            HomeAssistantSensor(
              HomeAssistantComponentBasicProperties(haDevice, gateway.name, "${device.id}_TEMPERATURE"),
              device.name,
              availabilityTopic,
              PeriodicSerialInputDevice.AVAILABILITY_ONLINE_STATE,
              PeriodicSerialInputDevice.AVAILABILITY_OFFLINE_STATE,
              HomeAssistantSensor.DeviceClass.TEMPERATURE,
              homieStateTopic(gateway, device.id, DevicePropertyType.TEMPERATURE),
              device.type.property(DevicePropertyType.TEMPERATURE).unit.value
            ),
            HomeAssistantSensor(
              HomeAssistantComponentBasicProperties(haDevice, gateway.name, "${device.id}_HUMIDITY"),
              device.name,
              availabilityTopic,
              PeriodicSerialInputDevice.AVAILABILITY_ONLINE_STATE,
              PeriodicSerialInputDevice.AVAILABILITY_OFFLINE_STATE,
              HomeAssistantSensor.DeviceClass.HUMIDITY,
              homieStateTopic(gateway, device.id, DevicePropertyType.HUMIDITY),
              device.type.property(DevicePropertyType.HUMIDITY).unit.value
            ),
            HomeAssistantSensor(
              HomeAssistantComponentBasicProperties(haDevice, gateway.name, "${device.id}_PRESSURE"),
              device.name,
              availabilityTopic,
              PeriodicSerialInputDevice.AVAILABILITY_ONLINE_STATE,
              PeriodicSerialInputDevice.AVAILABILITY_OFFLINE_STATE,
              HomeAssistantSensor.DeviceClass.PRESSURE,
              homieStateTopic(gateway, device.id, DevicePropertyType.PRESSURE),
              device.type.property(DevicePropertyType.PRESSURE).unit.value
            ),
            HomeAssistantSensor(
              HomeAssistantComponentBasicProperties(haDevice, gateway.name, "${device.id}_LAST_PING"),
              device.name,
              availabilityTopic,
              PeriodicSerialInputDevice.AVAILABILITY_ONLINE_STATE,
              PeriodicSerialInputDevice.AVAILABILITY_OFFLINE_STATE,
              HomeAssistantSensor.DeviceClass.TIMESTAMP,
              homieStateTopic(gateway, device.id, DevicePropertyType.LAST_PING),
              device.type.property(DevicePropertyType.LAST_PING).unit.value
            )
          )
        }
        DeviceType.DHT22 -> {
          val availabilityTopic = homieStateTopic(gateway, device.id, DevicePropertyType.STATE)
          listOf(
            HomeAssistantSensor(
              HomeAssistantComponentBasicProperties(haDevice, gateway.name, "${device.id}_TEMPERATURE"),
              device.name,
              availabilityTopic,
              PeriodicSerialInputDevice.AVAILABILITY_ONLINE_STATE,
              PeriodicSerialInputDevice.AVAILABILITY_OFFLINE_STATE,
              HomeAssistantSensor.DeviceClass.TEMPERATURE,
              homieStateTopic(gateway, device.id, DevicePropertyType.TEMPERATURE),
              device.type.property(DevicePropertyType.TEMPERATURE).unit.value
            ),
            HomeAssistantSensor(
              HomeAssistantComponentBasicProperties(haDevice, gateway.name, "${device.id}_HUMIDITY"),
              device.name,
              availabilityTopic,
              PeriodicSerialInputDevice.AVAILABILITY_ONLINE_STATE,
              PeriodicSerialInputDevice.AVAILABILITY_OFFLINE_STATE,
              HomeAssistantSensor.DeviceClass.HUMIDITY,
              homieStateTopic(gateway, device.id, DevicePropertyType.HUMIDITY),
              device.type.property(DevicePropertyType.HUMIDITY).unit.value
            ),
            HomeAssistantSensor(
              HomeAssistantComponentBasicProperties(haDevice, gateway.name, "${device.id}_LAST_PING"),
              device.name,
              availabilityTopic,
              PeriodicSerialInputDevice.AVAILABILITY_ONLINE_STATE,
              PeriodicSerialInputDevice.AVAILABILITY_OFFLINE_STATE,
              HomeAssistantSensor.DeviceClass.TIMESTAMP,
              homieStateTopic(gateway, device.id, DevicePropertyType.LAST_PING),
              device.type.property(DevicePropertyType.LAST_PING).unit.value
            )
          )
        }
        DeviceType.SCT013 -> {
          val availabilityTopic = homieStateTopic(gateway, device.id, DevicePropertyType.STATE)
          listOf(
            HomeAssistantSensor(
              HomeAssistantComponentBasicProperties(haDevice, gateway.name, "${device.id}_POWER"),
              device.name,
              availabilityTopic,
              PeriodicSerialInputDevice.AVAILABILITY_ONLINE_STATE,
              PeriodicSerialInputDevice.AVAILABILITY_OFFLINE_STATE,
              HomeAssistantSensor.DeviceClass.POWER,
              homieStateTopic(gateway, device.id, DevicePropertyType.POWER),
              device.type.property(DevicePropertyType.POWER).unit.value
            ),
            HomeAssistantSensor(
              HomeAssistantComponentBasicProperties(haDevice, gateway.name, "${device.id}_LAST_PING"),
              device.name,
              availabilityTopic,
              PeriodicSerialInputDevice.AVAILABILITY_ONLINE_STATE,
              PeriodicSerialInputDevice.AVAILABILITY_OFFLINE_STATE,
              HomeAssistantSensor.DeviceClass.TIMESTAMP,
              homieStateTopic(gateway, device.id, DevicePropertyType.LAST_PING),
              device.type.property(DevicePropertyType.LAST_PING).unit.value
            )
          )
        }
        DeviceType.SHUTTER -> listOf(
          HomeAssistantCover(
            basicProperties,
            device.name,
            homieStateTopic(gateway, device.id, DevicePropertyType.STATE),
            homieCommandTopic(gateway, device, DevicePropertyType.STATE),
            homieStateTopic(gateway, device.id, DevicePropertyType.POSITION),
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
        DeviceType.MQGATEWAY -> throw IllegalArgumentException("MqGateway should not be configured as a device")
      }

      LOGGER.debug { "Device ${device.id} (${device.type}) converted to HA components types: ${components.map { it.componentType }}" }
      LOGGER.trace { "Device $device converted to HA components: $components" }
      return@flatMap components
    }
  }

  private fun convertMqGatewayRootDeviceToHaSensors(gateway: Gateway): List<HomeAssistantSensor> {
    val rootHaDevice = HomeAssistantDevice(
      identifiers = listOf(gateway.name),
      name = gateway.name,
      manufacturer = "Aetas",
      firmwareVersion = gatewayFirmwareVersion,
      model = "MqGateway"
    )
    val availabilityTopic = "$HOMIE_PREFIX/${gateway.name}/\$state"
    val availabilityOnline = "ready"
    val availabilityOffline = "lost"
    return listOf(
      HomeAssistantSensor(
        HomeAssistantComponentBasicProperties(rootHaDevice, gateway.name, "${gateway.name}_CPU_TEMPERATURE"),
        gateway.name,
        availabilityTopic,
        availabilityOnline,
        availabilityOffline,
        HomeAssistantSensor.DeviceClass.TEMPERATURE,
        homieStateTopic(gateway, gateway.name, DevicePropertyType.TEMPERATURE),
        DeviceType.MQGATEWAY.property(DevicePropertyType.TEMPERATURE).unit.value
      ),
      HomeAssistantSensor(
        HomeAssistantComponentBasicProperties(rootHaDevice, gateway.name, "${gateway.name}_MEMORY_FREE"),
        gateway.name,
        availabilityTopic,
        availabilityOnline,
        availabilityOffline,
        HomeAssistantSensor.DeviceClass.NONE,
        homieStateTopic(gateway, gateway.name, DevicePropertyType.MEMORY),
        DeviceType.MQGATEWAY.property(DevicePropertyType.MEMORY).unit.value
      ),
      HomeAssistantSensor(
        HomeAssistantComponentBasicProperties(rootHaDevice, gateway.name, "${gateway.name}_UPTIME"),
        gateway.name,
        availabilityTopic,
        availabilityOnline,
        availabilityOffline,
        HomeAssistantSensor.DeviceClass.NONE,
        homieStateTopic(gateway, gateway.name, DevicePropertyType.UPTIME),
        DeviceType.MQGATEWAY.property(DevicePropertyType.UPTIME).unit.value
      ),
      HomeAssistantSensor(
        HomeAssistantComponentBasicProperties(rootHaDevice, gateway.name, "${gateway.name}_IP_ADDRESS"),
        gateway.name,
        availabilityTopic,
        availabilityOnline,
        availabilityOffline,
        HomeAssistantSensor.DeviceClass.NONE,
        homieStateTopic(gateway, gateway.name, DevicePropertyType.IP_ADDRESS),
        DeviceType.MQGATEWAY.property(DevicePropertyType.IP_ADDRESS).unit.value
      )
    )
  }

  private fun homieStateTopic(gateway: Gateway, deviceId: String, propertyType: DevicePropertyType): String {
    return "$HOMIE_PREFIX/${gateway.name}/$deviceId/$propertyType"
  }

  private fun homieCommandTopic(gateway: Gateway, device: DeviceConfig, propertyType: DevicePropertyType): String {
    return homieStateTopic(gateway, device.id, propertyType) + "/set"
  }

  companion object {
    const val DEVICE_CONFIG_HA_COMPONENT: String = "haComponent"
  }
}
