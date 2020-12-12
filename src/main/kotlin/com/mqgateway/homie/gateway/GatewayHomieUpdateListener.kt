package com.mqgateway.homie.gateway

import com.mqgateway.core.device.UpdateListener
import com.mqgateway.homie.HomieDevice
import mu.KotlinLogging

private val LOGGER = KotlinLogging.logger {}

class GatewayHomieUpdateListener(private val homieDevice: HomieDevice) : UpdateListener {

  override fun valueUpdated(deviceId: String, propertyId: String, newValue: String) {
    LOGGER.debug { "Notified about $deviceId.$propertyId updated to $newValue" }
    val homieNode = homieDevice.nodes[deviceId] ?: throw UnknownHomiePropertyException(deviceId, propertyId)
    val homieProperty = homieNode.properties[propertyId] ?: throw UnknownHomiePropertyException(deviceId, propertyId)
    homieProperty.onChange(newValue)
  }
}

class UnknownHomiePropertyException(deviceId: String, propertyId: String) :
  Exception("deviceId($deviceId), propertyId($propertyId)")
