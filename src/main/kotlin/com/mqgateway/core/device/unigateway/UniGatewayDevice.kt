package com.mqgateway.core.device.unigateway

import com.mqgateway.core.device.DataType.FLOAT
import com.mqgateway.core.device.DataType.INTEGER
import com.mqgateway.core.device.DataType.STRING
import com.mqgateway.core.device.DataUnit.BYTES
import com.mqgateway.core.device.DataUnit.CELSIUS
import com.mqgateway.core.device.DataUnit.SECOND
import com.mqgateway.core.device.Device
import com.mqgateway.core.device.DeviceProperty
import com.mqgateway.core.device.DevicePropertyType.IP_ADDRESS
import com.mqgateway.core.device.DevicePropertyType.MEMORY
import com.mqgateway.core.device.DevicePropertyType.TEMPERATURE
import com.mqgateway.core.device.DevicePropertyType.UPTIME
import com.mqgateway.core.device.DeviceType
import com.mqgateway.core.utils.SystemInfoProvider
import java.time.Duration
import kotlin.concurrent.fixedRateTimer

class UniGatewayDevice(id: String, name: String, private val periodBetweenUpdates: Duration, private val systemInfoProvider: SystemInfoProvider) :
  Device(
    id, name, DeviceType.UNIGATEWAY,
    setOf(
      DeviceProperty(TEMPERATURE, FLOAT, null, retained = true, unit = CELSIUS),
      DeviceProperty(MEMORY, INTEGER, null, retained = true, unit = BYTES),
      DeviceProperty(UPTIME, INTEGER, null, retained = true, unit = SECOND),
      DeviceProperty(IP_ADDRESS, STRING, null, retained = true)
    )
  ) {

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
