package com.mqgateway.core.gatewayconfig.homeassistant

import com.mqgateway.core.device.DevicePropertyType
import com.mqgateway.core.device.DeviceType
import com.mqgateway.core.device.emulatedswitch.EmulatedSwitchButtonDevice
import com.mqgateway.core.device.gate.SingleButtonsGateDevice
import com.mqgateway.core.device.motiondetector.MotionSensorDevice
import com.mqgateway.core.device.reedswitch.ReedSwitchDevice
import com.mqgateway.core.device.relay.RelayDevice
import com.mqgateway.core.device.shutter.ShutterDevice
import com.mqgateway.core.device.switchbutton.SwitchButtonDevice
import com.mqgateway.core.gatewayconfig.DeviceConfiguration
import com.mqgateway.core.gatewayconfig.GatewayConfiguration
import com.mqgateway.core.gatewayconfig.homeassistant.HomeAssistantComponentType.LIGHT
import com.mqgateway.core.gatewayconfig.homeassistant.HomeAssistantCover.DeviceClass
import com.mqgateway.core.gatewayconfig.homeassistant.HomeAssistantTrigger.TriggerType.BUTTON_LONG_PRESS
import com.mqgateway.core.gatewayconfig.homeassistant.HomeAssistantTrigger.TriggerType.BUTTON_LONG_RELEASE
import com.mqgateway.core.gatewayconfig.homeassistant.HomeAssistantTrigger.TriggerType.BUTTON_SHORT_PRESS
import com.mqgateway.core.gatewayconfig.homeassistant.HomeAssistantTrigger.TriggerType.BUTTON_SHORT_RELEASE
import com.mqgateway.homie.HOMIE_PREFIX
import mu.KotlinLogging

private val LOGGER = KotlinLogging.logger {}

// TODO it needs adjustment to get ha configuration from device. Maybe the device should have support for HA and specific properties to set
//  when configuring. Maybe device can implement some interface?
class HomeAssistantConverter(private val gatewayFirmwareVersion: String) {

  fun convert(gatewayConfiguration: GatewayConfiguration): List<HomeAssistantComponent> {
    LOGGER.info { "Converting Gateway configuration to HomeAssistant auto-discovery config" }
    val devices = gatewayConfiguration.devices

    val mqGatewayCoreComponents = convertMqGatewayRootDeviceToHaSensors(gatewayConfiguration)

    return mqGatewayCoreComponents + devices.flatMap { device ->
      val haDevice = HomeAssistantDevice(
        identifiers = listOf("${gatewayConfiguration.name}_${device.id}"),
        name = device.name,
        manufacturer = "Aetas",
        viaDevice = gatewayConfiguration.name,
        firmwareVersion = gatewayFirmwareVersion,
        model = "MqGateway ${device.type.name}"
      )
      val basicProperties = HomeAssistantComponentBasicProperties(haDevice, gatewayConfiguration.name, device.id)

      return@flatMap toHomeAssistantComponents(device, haDevice, basicProperties, gatewayConfiguration)
    }
  }

  private fun toHomeAssistantComponents(
    device: DeviceConfiguration,
    haDevice: HomeAssistantDevice,
    basicProperties: HomeAssistantComponentBasicProperties,
    gatewayConfiguration: GatewayConfiguration
  ): List<HomeAssistantComponent> {

    val components = when (device.type) {
      DeviceType.RELAY -> {
        val stateTopic = homieStateTopic(gatewayConfiguration, device.id, DevicePropertyType.STATE)
        val commandTopic = homieCommandTopic(gatewayConfiguration, device, DevicePropertyType.STATE)
        if (device.config[DEVICE_CONFIG_HA_COMPONENT].equals(LIGHT.value, true)) {
          listOf(HomeAssistantLight(basicProperties, device.name, stateTopic, commandTopic, true, RelayDevice.STATE_ON, RelayDevice.STATE_OFF))
        } else {
          listOf(HomeAssistantSwitch(basicProperties, device.name, stateTopic, commandTopic, true, RelayDevice.STATE_ON, RelayDevice.STATE_OFF))
        }
      }
      DeviceType.SWITCH_BUTTON -> {
        val homieStateTopic = homieStateTopic(gatewayConfiguration, device.id, DevicePropertyType.STATE)
        when {
          device.config[DEVICE_CONFIG_HA_COMPONENT].equals(HomeAssistantComponentType.TRIGGER.value, true) -> {
            listOf(
              HomeAssistantTrigger(
                HomeAssistantComponentBasicProperties(haDevice, gatewayConfiguration.name, "${device.id}_PRESS"),
                homieStateTopic,
                SwitchButtonDevice.PRESSED_STATE_VALUE,
                BUTTON_SHORT_PRESS,
                "button"
              ),
              HomeAssistantTrigger(
                HomeAssistantComponentBasicProperties(haDevice, gatewayConfiguration.name, "${device.id}_RELEASE"),
                homieStateTopic,
                SwitchButtonDevice.RELEASED_STATE_VALUE,
                BUTTON_SHORT_RELEASE,
                "button"
              ),
              HomeAssistantTrigger(
                HomeAssistantComponentBasicProperties(haDevice, gatewayConfiguration.name, "${device.id}_LONG_PRESS"),
                homieStateTopic,
                SwitchButtonDevice.LONG_PRESSED_STATE_VALUE,
                BUTTON_LONG_PRESS,
                "button"
              ),
              HomeAssistantTrigger(
                HomeAssistantComponentBasicProperties(haDevice, gatewayConfiguration.name, "${device.id}_LONG_RELEASE"),
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
            homieStateTopic(gatewayConfiguration, device.id, DevicePropertyType.STATE),
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
            homieStateTopic(gatewayConfiguration, device.id, DevicePropertyType.STATE),
            MotionSensorDevice.MOVE_START_STATE_VALUE,
            MotionSensorDevice.MOVE_STOP_STATE_VALUE,
            HomeAssistantBinarySensor.DeviceClass.MOTION
          )
        )
      }
      DeviceType.EMULATED_SWITCH -> {
        val stateTopic = homieStateTopic(gatewayConfiguration, device.id, DevicePropertyType.STATE)
        val commandTopic = homieCommandTopic(gatewayConfiguration, device, DevicePropertyType.STATE)
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
      DeviceType.SHUTTER -> listOf(
        HomeAssistantCover(
          basicProperties,
          device.name,
          homieStateTopic(gatewayConfiguration, device.id, DevicePropertyType.STATE),
          homieCommandTopic(gatewayConfiguration, device, DevicePropertyType.STATE),
          homieStateTopic(gatewayConfiguration, device.id, DevicePropertyType.POSITION),
          homieCommandTopic(gatewayConfiguration, device, DevicePropertyType.POSITION),
          DeviceClass.SHUTTER,
          ShutterDevice.Command.OPEN.name,
          ShutterDevice.Command.CLOSE.name,
          ShutterDevice.Command.STOP.name,
          ShutterDevice.POSITION_OPEN,
          ShutterDevice.POSITION_CLOSED,
          ShutterDevice.State.OPEN.name,
          ShutterDevice.State.CLOSED.name,
          ShutterDevice.State.OPENING.name,
          ShutterDevice.State.CLOSING.name,
          null,
          false
        )
      )
      DeviceType.TIMER_SWITCH -> listOf()
      DeviceType.GATE -> listOf(
        HomeAssistantCover(
          basicProperties,
          device.name,
          homieStateTopic(gatewayConfiguration, device.id, DevicePropertyType.STATE),
          homieCommandTopic(gatewayConfiguration, device, DevicePropertyType.STATE),
          null,
          null,
          if (device.config[DEVICE_CONFIG_HA_DEVICE_CLASS].equals(DeviceClass.GATE.name, true)) DeviceClass.GATE else DeviceClass.GARAGE,
          SingleButtonsGateDevice.Command.OPEN.name,
          SingleButtonsGateDevice.Command.CLOSE.name,
          SingleButtonsGateDevice.Command.STOP.name,
          null,
          null,
          SingleButtonsGateDevice.State.OPEN.name,
          SingleButtonsGateDevice.State.CLOSED.name,
          SingleButtonsGateDevice.State.OPENING.name,
          SingleButtonsGateDevice.State.CLOSING.name,
          null,
          false
        )
      )
      DeviceType.UNIGATEWAY -> throw IllegalArgumentException("MqGateway should not be configured as a device")
    }

    LOGGER.debug { "Device ${device.id} (${device.type}) converted to HA components types: ${components.map { it.componentType }}" }
    LOGGER.trace { "Device $device converted to HA components: $components" }

    return components
  }

  private fun convertMqGatewayRootDeviceToHaSensors(gatewayConfiguration: GatewayConfiguration): List<HomeAssistantSensor> {
    val rootHaDevice = HomeAssistantDevice(
      identifiers = listOf(gatewayConfiguration.name),
      name = gatewayConfiguration.name,
      manufacturer = "Aetas",
      firmwareVersion = gatewayFirmwareVersion,
      model = "MqGateway"
    )
    val availabilityTopic = "$HOMIE_PREFIX/${gatewayConfiguration.name}/\$state"
    val availabilityOnline = "ready"
    val availabilityOffline = "lost"
    return listOf(
      HomeAssistantSensor(
        HomeAssistantComponentBasicProperties(rootHaDevice, gatewayConfiguration.name, "${gatewayConfiguration.name}_CPU_TEMPERATURE"),
        "CPU temperature",
        availabilityTopic,
        availabilityOnline,
        availabilityOffline,
        HomeAssistantSensor.DeviceClass.TEMPERATURE,
        homieStateTopic(gatewayConfiguration, gatewayConfiguration.name, DevicePropertyType.TEMPERATURE),
        null // todo DeviceType.UNIGATEWAY.property(DevicePropertyType.TEMPERATURE).unit.value
      ),
      HomeAssistantSensor(
        HomeAssistantComponentBasicProperties(rootHaDevice, gatewayConfiguration.name, "${gatewayConfiguration.name}_MEMORY_FREE"),
        "Free memory",
        availabilityTopic,
        availabilityOnline,
        availabilityOffline,
        HomeAssistantSensor.DeviceClass.NONE,
        homieStateTopic(gatewayConfiguration, gatewayConfiguration.name, DevicePropertyType.MEMORY),
        null // todo DeviceType.UNIGATEWAY.property(DevicePropertyType.MEMORY).unit.value
      ),
      HomeAssistantSensor(
        HomeAssistantComponentBasicProperties(rootHaDevice, gatewayConfiguration.name, "${gatewayConfiguration.name}_UPTIME"),
        "Uptime",
        availabilityTopic,
        availabilityOnline,
        availabilityOffline,
        HomeAssistantSensor.DeviceClass.NONE,
        homieStateTopic(gatewayConfiguration, gatewayConfiguration.name, DevicePropertyType.UPTIME),
        null // todo DeviceType.UNIGATEWAY.property(DevicePropertyType.UPTIME).unit.value
      ),
      HomeAssistantSensor(
        HomeAssistantComponentBasicProperties(rootHaDevice, gatewayConfiguration.name, "${gatewayConfiguration.name}_IP_ADDRESS"),
        "IP address",
        availabilityTopic,
        availabilityOnline,
        availabilityOffline,
        HomeAssistantSensor.DeviceClass.NONE,
        homieStateTopic(gatewayConfiguration, gatewayConfiguration.name, DevicePropertyType.IP_ADDRESS),
        null // todo DeviceType.UNIGATEWAY.property(DevicePropertyType.IP_ADDRESS).unit.value
      )
    )
  }

  private fun homieStateTopic(gatewayConfiguration: GatewayConfiguration, deviceId: String, propertyType: DevicePropertyType): String {
    return "$HOMIE_PREFIX/${gatewayConfiguration.name}/$deviceId/$propertyType"
  }

  private fun homieCommandTopic(gatewayConfiguration: GatewayConfiguration, device: DeviceConfiguration, propertyType: DevicePropertyType): String {
    return homieStateTopic(gatewayConfiguration, device.id, propertyType) + "/set"
  }

  companion object {
    const val DEVICE_CONFIG_HA_COMPONENT: String = "haComponent"
    const val DEVICE_CONFIG_HA_DEVICE_CLASS: String = "haDeviceClass"
  }
}
