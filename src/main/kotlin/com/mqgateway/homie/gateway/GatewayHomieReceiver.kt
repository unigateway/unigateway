package com.mqgateway.homie.gateway

import com.mqgateway.core.device.DeviceRegistry
import com.mqgateway.homie.HomieMqttTopic
import com.mqgateway.homie.HomieReceiver
import mu.KotlinLogging

private val LOGGER = KotlinLogging.logger {}

class GatewayHomieReceiver(private val gatewayDeviceRegistry: DeviceRegistry) : HomieReceiver {

  override fun initProperty(nodeId: String, propertyId: String, value: String) {
    LOGGER.debug { "Initializing property ($propertyId $value)" }
    val gatewayDevice = gatewayDeviceRegistry.getById(nodeId) ?: throw DeviceNotFoundException(nodeId)
    LOGGER.trace { "Device found in registry (${gatewayDevice.id})" }

    gatewayDevice.initProperty(propertyId, value)
  }

  override fun propertySet(mqttTopic: String, payload: String) {
    LOGGER.debug { "Setting property command received from MQTT ($mqttTopic $payload)" }
    val homieTopic = HomieMqttTopic.fromString(mqttTopic)
    val gatewayDevice = gatewayDeviceRegistry.getById(homieTopic.nodeId!!) ?: throw DeviceNotFoundException(homieTopic.nodeId)
    LOGGER.trace { "Device found in registry (${gatewayDevice.id})" }

    gatewayDevice.change(homieTopic.propertyId!!, payload)
  }
}

class DeviceNotFoundException(deviceId: String) : Exception("Device with id '$deviceId' not found in device registry")
