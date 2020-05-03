package com.mqgateway.core.device

import com.mqgateway.core.gatewayconfig.DevicePropertyType
import com.mqgateway.core.gatewayconfig.DeviceType
import com.mqgateway.core.onewire.OneWireBusDeviceConnectionEvent
import com.mqgateway.core.onewire.device.OneWireBusDevice
import mu.KotlinLogging

private val LOGGER = KotlinLogging.logger {}

abstract class OneWireDevice(id: String, type: DeviceType, val oneWireBusDevice: OneWireBusDevice) : Device(id, type) {

  override fun initDevice() {
    super.initDevice()
    if (!oneWireBusDevice.isAvailable()) {
      LOGGER.warn { "OneWire device $id is not available. Initializing anyway - it may be connected later." }
    }
    oneWireBusDevice.addDeviceConnectionListener { event ->
      if (event.type == OneWireBusDeviceConnectionEvent.ConnectionEventType.CONNECTED) {
        notify(DevicePropertyType.STATE.toString(), STATUS_CONNECTED_VALUE)
      } else {
        notify(DevicePropertyType.STATE.toString(), STATUS_DISCONNECTED_VALUE)
      }
    }
  }

  companion object {
    const val CONFIG_ONE_WIRE_ADDRESS_KEY = "oneWireAddress"
    const val STATUS_CONNECTED_VALUE = "CONNECTED"
    const val STATUS_DISCONNECTED_VALUE = "DISCONNECTED"
  }
}
