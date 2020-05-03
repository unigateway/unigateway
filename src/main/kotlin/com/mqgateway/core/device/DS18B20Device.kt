package com.mqgateway.core.device

import com.mqgateway.core.gatewayconfig.DevicePropertyType
import com.mqgateway.core.gatewayconfig.DeviceType
import com.mqgateway.core.onewire.device.OneWireBusDevice
import mu.KotlinLogging

private val LOGGER = KotlinLogging.logger {}

class DS18B20Device(id: String, oneWireBusDevice: OneWireBusDevice) : OneWireDevice(id, DeviceType.DS18B20, oneWireBusDevice) {

  override fun initDevice() {
    super.initDevice()
    oneWireBusDevice.addValueReceivedListener { event ->
      notify(DevicePropertyType.TEMPERATURE.toString(), event.newValue)
      LOGGER.info { "Device($id) temperature changed to ${event.newValue}" }
    }
  }
}
