package com.mqgateway.core.device.unigateway

import com.mqgateway.core.device.Device
import com.mqgateway.core.gatewayconfig.DevicePropertyType.IP_ADDRESS
import com.mqgateway.core.gatewayconfig.DevicePropertyType.MEMORY
import com.mqgateway.core.gatewayconfig.DevicePropertyType.TEMPERATURE
import com.mqgateway.core.gatewayconfig.DevicePropertyType.UPTIME
import com.mqgateway.core.gatewayconfig.DeviceType
import com.mqgateway.core.utils.SystemInfoProvider
import java.time.Duration
import kotlin.concurrent.fixedRateTimer

class UniGatewayDevice(id: String, private val periodBetweenUpdates: Duration, private val systemInfoProvider: SystemInfoProvider) :
  Device(id, DeviceType.UNIGATEWAY) {

  override fun initDevice() {
    super.initDevice()

    fixedRateTimer(name = "unigateway-$id-timer", daemon = true, period = periodBetweenUpdates.toMillis()) {
      notifyNow()
    }
  }

  private fun notifyNow() {
    notify(TEMPERATURE, systemInfoProvider.getCpuTemperature())
    notify(MEMORY, systemInfoProvider.getMemoryFree())
    notify(UPTIME, systemInfoProvider.getUptime().toMillis() / 1000)
    notify(IP_ADDRESS, systemInfoProvider.getIPAddresses())
  }
}
