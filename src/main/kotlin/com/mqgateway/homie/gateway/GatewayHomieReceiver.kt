package com.mqgateway.homie.gateway

import mu.KotlinLogging
import com.mqgateway.core.device.DeviceRegistry
import com.mqgateway.homie.HomieMqttTopic
import com.mqgateway.homie.HomieReceiver

private val LOGGER = KotlinLogging.logger {}

class GatewayHomieReceiver(private val gatewayDeviceRegistry: DeviceRegistry) : HomieReceiver {

  override fun propertySet(mqttTopic: String, payload: String) {
    LOGGER.debug { "Setting property command received from MQTT ($mqttTopic $payload)" }
    val homieTopic = HomieMqttTopic.fromString(mqttTopic)
    val gatewayDevice = gatewayDeviceRegistry.getById(homieTopic.nodeId!!) ?: throw DeviceNotFoundException(homieTopic.nodeId)
    LOGGER.trace { "Device found in registry (${gatewayDevice.id})" }

    gatewayDevice.changeState(homieTopic.propertyId!!, payload)
  }
}

class DeviceNotFoundException(deviceId: String): Exception("Device with $deviceId not found in device registry")

