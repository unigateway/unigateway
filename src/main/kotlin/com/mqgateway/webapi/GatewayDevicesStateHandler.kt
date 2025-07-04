package com.mqgateway.webapi

import com.mqgateway.core.device.UpdateListener
import io.micronaut.serde.annotation.Serdeable

class GatewayDevicesStateHandler : UpdateListener {
  private val devicesState: MutableMap<String, MutableMap<String, String>> = mutableMapOf()

  override fun valueUpdated(
    deviceId: String,
    propertyId: String,
    newValue: String,
  ) {
    devicesState.getOrPut(deviceId) { mutableMapOf(Pair(propertyId, newValue)) }[propertyId] = newValue
  }

  fun devicesState(): List<DeviceState> =
    devicesState.map { DeviceState(it.key, it.value.map { property -> DevicePropertyState(property.key, property.value) }) }
}

@Serdeable
data class DeviceState(val deviceId: String, val properties: List<DevicePropertyState>)

@Serdeable
data class DevicePropertyState(val propertyId: String, var value: String)
