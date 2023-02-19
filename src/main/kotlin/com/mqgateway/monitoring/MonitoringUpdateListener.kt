package com.mqgateway.monitoring

import com.mqgateway.core.device.DevicePropertyType.MEMORY
import com.mqgateway.core.device.DevicePropertyType.TEMPERATURE
import com.mqgateway.core.device.DevicePropertyType.UPTIME
import com.mqgateway.core.device.UpdateListener
import com.mqgateway.core.gatewayconfig.GatewayConfiguration
import io.micrometer.core.instrument.MeterRegistry
import io.micrometer.core.instrument.TimeGauge
import jakarta.inject.Singleton
import java.util.concurrent.TimeUnit
import java.util.concurrent.atomic.AtomicInteger

@Singleton
class MonitoringUpdateListener(private val meterRegistry: MeterRegistry, private val gatewayConfiguration: GatewayConfiguration) : UpdateListener {

  private var uptimeInSeconds: AtomicInteger = AtomicInteger()
  private var freeMemory: Float? = null
  private var cpuTemperature: Float? = null

  init {
    TimeGauge.builder("unigateway.uptime", uptimeInSeconds, TimeUnit.SECONDS, { it.toDouble() }).register(meterRegistry)
  }

  override fun valueUpdated(deviceId: String, propertyId: String, newValue: String) {
    meterRegistry.counter("unigateway.device.value.update", "device.id", deviceId, "property.id", propertyId).increment()

    if (deviceId == gatewayConfiguration.id) {
      when (propertyId) {
        TEMPERATURE.toString().lowercase() -> {
          cpuTemperature = newValue.toFloat()
          meterRegistry.gauge("unigateway.cpu.temp", cpuTemperature!!)
        }
        MEMORY.toString().lowercase() -> {
          freeMemory = newValue.toFloat()
          meterRegistry.gauge("unigateway.memory.free", freeMemory!!)
        }
        UPTIME.toString().lowercase() -> {
          uptimeInSeconds.set(newValue.toInt())
        }
      }
    }
  }
}
