package com.unigateway.core.device.unigateway

import com.unigateway.core.device.DataType.FLOAT
import com.unigateway.core.device.DataType.INTEGER
import com.unigateway.core.device.DataType.STRING
import com.unigateway.core.device.DataUnit.BYTES
import com.unigateway.core.device.DataUnit.CELSIUS
import com.unigateway.core.device.DataUnit.SECOND
import com.unigateway.core.device.Device
import com.unigateway.core.device.DeviceProperty
import com.unigateway.core.device.DevicePropertyType.IP_ADDRESS
import com.unigateway.core.device.DevicePropertyType.MEMORY
import com.unigateway.core.device.DevicePropertyType.TEMPERATURE
import com.unigateway.core.device.DevicePropertyType.UPTIME
import com.unigateway.core.device.DeviceType
import com.unigateway.core.utils.SystemInfoProvider
import java.time.Duration
import kotlin.concurrent.fixedRateTimer

class UniGatewayDevice(
  id: String,
  name: String,
  private val periodBetweenUpdates: Duration,
  private val systemInfoProvider: SystemInfoProvider,
  config: Map<String, String> = emptyMap()
) :
  Device(
    id, name, DeviceType.UNIGATEWAY,
    setOf(
      DeviceProperty(TEMPERATURE, FLOAT, null, retained = true, unit = CELSIUS),
      DeviceProperty(MEMORY, INTEGER, null, retained = true, unit = BYTES),
      DeviceProperty(UPTIME, INTEGER, null, retained = true, unit = SECOND),
      DeviceProperty(IP_ADDRESS, STRING, null, retained = true)
    ),
    config
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
