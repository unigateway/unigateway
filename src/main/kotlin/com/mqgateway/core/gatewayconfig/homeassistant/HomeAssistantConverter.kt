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
      val entityName = device.config[DEVICE_CONFIG_HA_ENTITY_NAME] ?: ""
      val basicProperties = HomeAssistantComponentBasicProperties(haDevice, unigatewayId, device.id, entityName)

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
          listOf(HomeAssistantLight(basicProperties, stateTopic, commandTopic, true, RelayDevice.STATE_ON, RelayDevice.STATE_OFF))
        } else {
          val deviceClass = device.config[DEVICE_CONFIG_HA_DEVICE_CLASS]?.let { HomeAssistantSwitch.DeviceClass.fromValue(it) }
            ?: HomeAssistantSwitch.DeviceClass.SWITCH
          listOf(
            HomeAssistantSwitch(
              basicProperties, stateTopic, commandTopic, true, RelayDevice.STATE_ON, RelayDevice.STATE_OFF, deviceClass
            )
          )
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
                deviceClass = HomeAssistantSensor.DeviceClass.ENUM,
                stateTopic = homieStateTopic
              )
            )
          }
          else -> {
            listOf(
              HomeAssistantBinarySensor(
                basicProperties = basicProperties,
                stateTopic = homieStateTopic,
                payloadOn = SwitchButtonDevice.PRESSED_STATE_VALUE,
                payloadOff = SwitchButtonDevice.RELEASED_STATE_VALUE,
                deviceClass = HomeAssistantBinarySensor.DeviceClass.NONE
              )
            )
          }
        }
      }
      DeviceType.REED_SWITCH -> {
        listOf(
          HomeAssistantBinarySensor(
            basicProperties = basicProperties,
            stateTopic = homieStateTopic(unigatewayId, device.id, DevicePropertyType.STATE),
            payloadOn = ReedSwitchDevice.OPEN_STATE_VALUE,
            payloadOff = ReedSwitchDevice.CLOSED_STATE_VALUE,
            deviceClass = device.config[DEVICE_CONFIG_HA_DEVICE_CLASS]?.let { HomeAssistantBinarySensor.DeviceClass.fromValue(it) }
              ?: HomeAssistantBinarySensor.DeviceClass.OPENING
          )
        )
      }
      DeviceType.MOTION_DETECTOR -> {
        listOf(
          HomeAssistantBinarySensor(
            basicProperties = basicProperties,
            stateTopic = homieStateTopic(unigatewayId, device.id, DevicePropertyType.STATE),
            payloadOn = MotionSensorDevice.MOVE_START_STATE_VALUE,
            payloadOff = MotionSensorDevice.MOVE_STOP_STATE_VALUE,
            deviceClass = HomeAssistantBinarySensor.DeviceClass.MOTION
          )
        )
      }
      DeviceType.EMULATED_SWITCH -> {
        val stateTopic = homieStateTopic(unigatewayId, device.id, DevicePropertyType.STATE)
        val commandTopic = homieCommandTopic(unigatewayId, device.id, DevicePropertyType.STATE)
        listOf(
          HomeAssistantSwitch(
            basicProperties = basicProperties,
            stateTopic = stateTopic,
            commandTopic = commandTopic,
            retain = false,
            payloadOn = EmulatedSwitchButtonDevice.PRESSED_STATE_VALUE,
            payloadOff = EmulatedSwitchButtonDevice.RELEASED_STATE_VALUE,
            deviceClass = HomeAssistantSwitch.DeviceClass.SWITCH
          )
        )
      }
      DeviceType.SHUTTER -> listOf(
        HomeAssistantCover(
          basicProperties = basicProperties,
          stateTopic = homieStateTopic(unigatewayId, device.id, DevicePropertyType.STATE),
          commandTopic = homieCommandTopic(unigatewayId, device.id, DevicePropertyType.STATE),
          positionTopic = homieStateTopic(unigatewayId, device.id, DevicePropertyType.POSITION),
          setPositionTopic = homieCommandTopic(unigatewayId, device.id, DevicePropertyType.POSITION),
          deviceClass = DeviceClass.SHUTTER,
          payloadOpen = ShutterDevice.Command.OPEN.name,
          payloadClose = ShutterDevice.Command.CLOSE.name,
          payloadStop = ShutterDevice.Command.STOP.name,
          positionOpen = ShutterDevice.POSITION_OPEN,
          positionClosed = ShutterDevice.POSITION_CLOSED,
          stateOpen = ShutterDevice.State.OPEN.name,
          stateClosed = ShutterDevice.State.CLOSED.name,
          stateOpening = ShutterDevice.State.OPENING.name,
          stateClosing = ShutterDevice.State.CLOSING.name,
          stateStopped = null,
          retain = false
        )
      )
      DeviceType.TIMER_SWITCH -> listOf()
      DeviceType.GATE -> listOf(
        HomeAssistantCover(
          basicProperties = basicProperties,
          stateTopic = homieStateTopic(unigatewayId, device.id, DevicePropertyType.STATE),
          commandTopic = homieCommandTopic(unigatewayId, device.id, DevicePropertyType.STATE),
          positionTopic = null,
          setPositionTopic = null,
          deviceClass = if (device.config[DEVICE_CONFIG_HA_DEVICE_CLASS].equals(DeviceClass.GATE.name, true)) {
            DeviceClass.GATE
          } else {
            DeviceClass.GARAGE
          },
          payloadOpen = GateDevice.Command.OPEN.name,
          payloadClose = GateDevice.Command.CLOSE.name,
          payloadStop = GateDevice.Command.STOP.name,
          positionOpen = null,
          positionClosed = null,
          stateOpen = GateDevice.State.OPEN.name,
          stateClosed = GateDevice.State.CLOSED.name,
          stateOpening = GateDevice.State.OPENING.name,
          stateClosing = GateDevice.State.CLOSING.name,
          stateStopped = null,
          retain = false
        )
      )
      DeviceType.TEMPERATURE -> listOf(
        HomeAssistantSensor(
          basicProperties = basicProperties,
          availabilityTopic = null,
          payloadAvailable = null,
          payloadNotAvailable = null,
          deviceClass = HomeAssistantSensor.DeviceClass.TEMPERATURE,
          stateTopic = homieStateTopic(unigatewayId, device.id, DevicePropertyType.TEMPERATURE),
          unitOfMeasurement = device.getProperty(DevicePropertyType.TEMPERATURE).unit.value
        )
      )
      DeviceType.HUMIDITY -> listOf(
        HomeAssistantSensor(
          basicProperties = basicProperties,
          availabilityTopic = null,
          payloadAvailable = null,
          payloadNotAvailable = null,
          deviceClass = HomeAssistantSensor.DeviceClass.HUMIDITY,
          stateTopic = homieStateTopic(unigatewayId, device.id, DevicePropertyType.HUMIDITY),
          unitOfMeasurement = device.getProperty(DevicePropertyType.HUMIDITY).unit.value
        )
      )
      DeviceType.LIGHT -> {
        val stateTopic = homieStateTopic(unigatewayId, device.id, DevicePropertyType.STATE)
        val commandTopic = homieCommandTopic(unigatewayId, device.id, DevicePropertyType.STATE)
        listOf(HomeAssistantLight(basicProperties, stateTopic, commandTopic, true, RelayDevice.STATE_ON, RelayDevice.STATE_OFF))
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
        basicProperties = HomeAssistantComponentBasicProperties(rootHaDevice, unigatewayId, "${unigatewayId}_CPU_TEMPERATURE", "CPU temperature"),
        availabilityTopic = availabilityTopic,
        payloadAvailable = availabilityOnline,
        payloadNotAvailable = availabilityOffline,
        deviceClass = HomeAssistantSensor.DeviceClass.TEMPERATURE,
        stateTopic = homieStateTopic(unigatewayId, unigatewayId, DevicePropertyType.TEMPERATURE),
        unitOfMeasurement = unigatewayDevice.getProperty(DevicePropertyType.TEMPERATURE).unit.value
      ),
      HomeAssistantSensor(
        basicProperties = HomeAssistantComponentBasicProperties(rootHaDevice, unigatewayId, "${unigatewayId}_MEMORY_FREE", "Free memory"),
        availabilityTopic = availabilityTopic,
        payloadAvailable = availabilityOnline,
        payloadNotAvailable = availabilityOffline,
        deviceClass = HomeAssistantSensor.DeviceClass.DATA_SIZE,
        stateTopic = homieStateTopic(unigatewayId, unigatewayId, DevicePropertyType.MEMORY),
        unitOfMeasurement = unigatewayDevice.getProperty(DevicePropertyType.MEMORY).unit.value
      ),
      HomeAssistantSensor(
        basicProperties = HomeAssistantComponentBasicProperties(rootHaDevice, unigatewayId, "${unigatewayId}_UPTIME", "Uptime"),
        availabilityTopic = availabilityTopic,
        payloadAvailable = availabilityOnline,
        payloadNotAvailable = availabilityOffline,
        deviceClass = HomeAssistantSensor.DeviceClass.NONE,
        stateTopic = homieStateTopic(unigatewayId, unigatewayId, DevicePropertyType.UPTIME),
        unitOfMeasurement = unigatewayDevice.getProperty(DevicePropertyType.UPTIME).unit.value
      ),
      HomeAssistantSensor(
        basicProperties = HomeAssistantComponentBasicProperties(rootHaDevice, unigatewayId, "${unigatewayId}_IP_ADDRESS", "IP address"),
        availabilityTopic = availabilityTopic,
        payloadAvailable = availabilityOnline,
        payloadNotAvailable = availabilityOffline,
        deviceClass = HomeAssistantSensor.DeviceClass.NONE,
        stateTopic = homieStateTopic(unigatewayId, unigatewayId, DevicePropertyType.IP_ADDRESS),
        unitOfMeasurement = unigatewayDevice.getProperty(DevicePropertyType.IP_ADDRESS).unit.value
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
    const val DEVICE_CONFIG_HA_ENTITY_NAME: String = "haEntityName"
  }
}
