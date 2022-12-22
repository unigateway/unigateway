package com.mqgateway.core.gatewayconfig.homeassistant

import com.mqgateway.core.device.Device
import com.mqgateway.core.device.DevicePropertyType
import com.mqgateway.core.device.DeviceRegistry
import com.mqgateway.core.device.DeviceType
import com.mqgateway.core.device.emulatedswitch.EmulatedSwitchButtonDevice
import com.mqgateway.core.device.gate.GateDevice
import com.mqgateway.core.device.motiondetector.MotionSensorDevice
import com.mqgateway.core.device.reedswitch.ReedSwitchDevice
import com.mqgateway.core.device.relay.RelayDevice
import com.mqgateway.core.device.shutter.ShutterDevice
import com.mqgateway.core.device.switchbutton.SwitchButtonDevice
import com.mqgateway.core.gatewayconfig.homeassistant.HomeAssistantComponentType.LIGHT
import com.mqgateway.core.gatewayconfig.homeassistant.HomeAssistantCover.DeviceClass
import com.mqgateway.core.gatewayconfig.homeassistant.HomeAssistantTrigger.TriggerType.BUTTON_LONG_PRESS
import com.mqgateway.core.gatewayconfig.homeassistant.HomeAssistantTrigger.TriggerType.BUTTON_LONG_RELEASE
import com.mqgateway.core.gatewayconfig.homeassistant.HomeAssistantTrigger.TriggerType.BUTTON_SHORT_PRESS
import com.mqgateway.core.gatewayconfig.homeassistant.HomeAssistantTrigger.TriggerType.BUTTON_SHORT_RELEASE
import com.mqgateway.homie.HOMIE_PREFIX
import mu.KotlinLogging

private val LOGGER = KotlinLogging.logger {}

class HomeAssistantConverter(private val gatewayFirmwareVersion: String) {

  fun convert(deviceRegistry: DeviceRegistry): List<HomeAssistantComponent> {
    LOGGER.info { "Converting Gateway configuration to HomeAssistant auto-discovery config" }
    val devices = deviceRegistry.devices.filter { it.type != DeviceType.UNIGATEWAY }

    val uniGatewayDevice = deviceRegistry.getUniGatewayDevice()
    val mqGatewayCoreComponents = convertMqGatewayRootDeviceToHaSensors(uniGatewayDevice)

    val unigatewayId = uniGatewayDevice.id
    return mqGatewayCoreComponents + devices.flatMap { device ->
      val haDevice = HomeAssistantDevice(
        identifiers = listOf("${unigatewayId}_${device.id}"),
        name = device.name,
        manufacturer = "Aetas",
        viaDevice = unigatewayId,
        firmwareVersion = gatewayFirmwareVersion,
        model = "UniGateway ${device.type.name}"
      )
      val basicProperties = HomeAssistantComponentBasicProperties(haDevice, unigatewayId, device.id)

      return@flatMap toHomeAssistantComponents(device, haDevice, basicProperties, unigatewayId)
    }
  }

  private fun toHomeAssistantComponents(
    device: Device,
    haDevice: HomeAssistantDevice,
    basicProperties: HomeAssistantComponentBasicProperties,
    unigatewayId: String
  ): List<HomeAssistantComponent> {

    val components = when (device.type) {
      DeviceType.RELAY -> {
        val stateTopic = homieStateTopic(unigatewayId, device.id, DevicePropertyType.STATE)
        val commandTopic = homieCommandTopic(unigatewayId, device.id, DevicePropertyType.STATE)
        if (device.config[DEVICE_CONFIG_HA_COMPONENT].equals(LIGHT.value, true)) {
          listOf(HomeAssistantLight(basicProperties, device.name, stateTopic, commandTopic, true, RelayDevice.STATE_ON, RelayDevice.STATE_OFF))
        } else {
          listOf(HomeAssistantSwitch(basicProperties, device.name, stateTopic, commandTopic, true, RelayDevice.STATE_ON, RelayDevice.STATE_OFF))
        }
      }
      DeviceType.SWITCH_BUTTON -> {
        val homieStateTopic = homieStateTopic(unigatewayId, device.id, DevicePropertyType.STATE)
        when {
          device.config[DEVICE_CONFIG_HA_COMPONENT].equals(HomeAssistantComponentType.TRIGGER.value, true) -> {
            listOf(
              HomeAssistantTrigger(
                HomeAssistantComponentBasicProperties(haDevice, unigatewayId, "${device.id}_PRESS"),
                homieStateTopic,
                SwitchButtonDevice.PRESSED_STATE_VALUE,
                BUTTON_SHORT_PRESS,
                "button"
              ),
              HomeAssistantTrigger(
                HomeAssistantComponentBasicProperties(haDevice, unigatewayId, "${device.id}_RELEASE"),
                homieStateTopic,
                SwitchButtonDevice.RELEASED_STATE_VALUE,
                BUTTON_SHORT_RELEASE,
                "button"
              ),
              HomeAssistantTrigger(
                HomeAssistantComponentBasicProperties(haDevice, unigatewayId, "${device.id}_LONG_PRESS"),
                homieStateTopic,
                SwitchButtonDevice.LONG_PRESSED_STATE_VALUE,
                BUTTON_LONG_PRESS,
                "button"
              ),
              HomeAssistantTrigger(
                HomeAssistantComponentBasicProperties(haDevice, unigatewayId, "${device.id}_LONG_RELEASE"),
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
            homieStateTopic(unigatewayId, device.id, DevicePropertyType.STATE),
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
            homieStateTopic(unigatewayId, device.id, DevicePropertyType.STATE),
            MotionSensorDevice.MOVE_START_STATE_VALUE,
            MotionSensorDevice.MOVE_STOP_STATE_VALUE,
            HomeAssistantBinarySensor.DeviceClass.MOTION
          )
        )
      }
      DeviceType.EMULATED_SWITCH -> {
        val stateTopic = homieStateTopic(unigatewayId, device.id, DevicePropertyType.STATE)
        val commandTopic = homieCommandTopic(unigatewayId, device.id, DevicePropertyType.STATE)
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
          homieStateTopic(unigatewayId, device.id, DevicePropertyType.STATE),
          homieCommandTopic(unigatewayId, device.id, DevicePropertyType.STATE),
          homieStateTopic(unigatewayId, device.id, DevicePropertyType.POSITION),
          homieCommandTopic(unigatewayId, device.id, DevicePropertyType.POSITION),
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
          homieStateTopic(unigatewayId, device.id, DevicePropertyType.STATE),
          homieCommandTopic(unigatewayId, device.id, DevicePropertyType.STATE),
          null,
          null,
          if (device.config[DEVICE_CONFIG_HA_DEVICE_CLASS].equals(DeviceClass.GATE.name, true)) DeviceClass.GATE else DeviceClass.GARAGE,
          GateDevice.Command.OPEN.name,
          GateDevice.Command.CLOSE.name,
          GateDevice.Command.STOP.name,
          null,
          null,
          GateDevice.State.OPEN.name,
          GateDevice.State.CLOSED.name,
          GateDevice.State.OPENING.name,
          GateDevice.State.CLOSING.name,
          null,
          false
        )
      )
      DeviceType.TEMPERATURE -> listOf(
        HomeAssistantSensor(
          basicProperties,
          device.name,
          null,
          null,
          null,
          HomeAssistantSensor.DeviceClass.TEMPERATURE,
          homieStateTopic(unigatewayId, device.id, DevicePropertyType.TEMPERATURE),
          device.getProperty(DevicePropertyType.TEMPERATURE).unit.value
        )
      )
      DeviceType.HUMIDITY -> listOf(
        HomeAssistantSensor(
          basicProperties,
          device.name,
          null,
          null,
          null,
          HomeAssistantSensor.DeviceClass.HUMIDITY,
          homieStateTopic(unigatewayId, device.id, DevicePropertyType.HUMIDITY),
          device.getProperty(DevicePropertyType.HUMIDITY).unit.value
        )
      )
      DeviceType.LIGHT -> {
        val stateTopic = homieStateTopic(unigatewayId, device.id, DevicePropertyType.STATE)
        val commandTopic = homieCommandTopic(unigatewayId, device.id, DevicePropertyType.STATE)
        listOf(HomeAssistantLight(basicProperties, device.name, stateTopic, commandTopic, true, RelayDevice.STATE_ON, RelayDevice.STATE_OFF))
      }
      DeviceType.UNIGATEWAY -> throw IllegalArgumentException("MqGateway should not be configured as a device")
    }

    LOGGER.debug { "Device ${device.id} (${device.type}) converted to HA components types: ${components.map { it.componentType }}" }
    LOGGER.trace { "Device $device converted to HA components: $components" }

    return components
  }

  private fun convertMqGatewayRootDeviceToHaSensors(unigatewayDevice: Device): List<HomeAssistantSensor> {
    val unigatewayId = unigatewayDevice.id
    val rootHaDevice = HomeAssistantDevice(
      identifiers = listOf(unigatewayId),
      name = unigatewayDevice.name,
      manufacturer = "Aetas",
      firmwareVersion = gatewayFirmwareVersion,
      model = "UniGateway"
    )
    val availabilityTopic = "$HOMIE_PREFIX/$unigatewayId/\$state"
    val availabilityOnline = "ready"
    val availabilityOffline = "lost"
    return listOf(
      HomeAssistantSensor(
        HomeAssistantComponentBasicProperties(rootHaDevice, unigatewayId, "${unigatewayId}_CPU_TEMPERATURE"),
        "CPU temperature",
        availabilityTopic,
        availabilityOnline,
        availabilityOffline,
        HomeAssistantSensor.DeviceClass.TEMPERATURE,
        homieStateTopic(unigatewayId, unigatewayId, DevicePropertyType.TEMPERATURE),
        unigatewayDevice.getProperty(DevicePropertyType.TEMPERATURE).unit.value
      ),
      HomeAssistantSensor(
        HomeAssistantComponentBasicProperties(rootHaDevice, unigatewayId, "${unigatewayId}_MEMORY_FREE"),
        "Free memory",
        availabilityTopic,
        availabilityOnline,
        availabilityOffline,
        HomeAssistantSensor.DeviceClass.NONE,
        homieStateTopic(unigatewayId, unigatewayId, DevicePropertyType.MEMORY),
        unigatewayDevice.getProperty(DevicePropertyType.MEMORY).unit.value
      ),
      HomeAssistantSensor(
        HomeAssistantComponentBasicProperties(rootHaDevice, unigatewayId, "${unigatewayId}_UPTIME"),
        "Uptime",
        availabilityTopic,
        availabilityOnline,
        availabilityOffline,
        HomeAssistantSensor.DeviceClass.NONE,
        homieStateTopic(unigatewayId, unigatewayId, DevicePropertyType.UPTIME),
        unigatewayDevice.getProperty(DevicePropertyType.UPTIME).unit.value
      ),
      HomeAssistantSensor(
        HomeAssistantComponentBasicProperties(rootHaDevice, unigatewayId, "${unigatewayId}_IP_ADDRESS"),
        "IP address",
        availabilityTopic,
        availabilityOnline,
        availabilityOffline,
        HomeAssistantSensor.DeviceClass.NONE,
        homieStateTopic(unigatewayId, unigatewayId, DevicePropertyType.IP_ADDRESS),
        unigatewayDevice.getProperty(DevicePropertyType.IP_ADDRESS).unit.value
      )
    )
  }

  private fun homieStateTopic(unigatewayId: String, deviceId: String, propertyType: DevicePropertyType): String {
    return "$HOMIE_PREFIX/$unigatewayId/$deviceId/$propertyType"
  }

  private fun homieCommandTopic(unigatewayId: String, deviceId: String, propertyType: DevicePropertyType): String {
    return homieStateTopic(unigatewayId, deviceId, propertyType) + "/set"
  }

  companion object {
    const val DEVICE_CONFIG_HA_COMPONENT: String = "haComponent"
    const val DEVICE_CONFIG_HA_DEVICE_CLASS: String = "haDeviceClass"
  }
}
