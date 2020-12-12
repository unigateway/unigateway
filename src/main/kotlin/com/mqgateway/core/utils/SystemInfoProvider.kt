package com.mqgateway.core.utils

import com.pi4j.system.NetworkInfo
import com.pi4j.system.SystemInfo
import java.lang.management.ManagementFactory
import java.time.Duration

interface SystemInfoProvider {
  fun getCpuTemperature(): Float
  fun getMemoryFree(): Long
  fun getUptime(): Duration
  fun getIPAddresses(): String
}

class Pi4JSystemInfoProvider : SystemInfoProvider {
  override fun getCpuTemperature(): Float = SystemInfo.getCpuTemperature() / 1000
  override fun getMemoryFree(): Long = SystemInfo.getMemoryFree()
  override fun getUptime(): Duration = Duration.ofMillis(ManagementFactory.getRuntimeMXBean().uptime)
  override fun getIPAddresses(): String = NetworkInfo.getIPAddresses().joinToString(", ")
}

class SimulatedSystemInfoProvider : SystemInfoProvider {
  private var cpuTemperature: Float = 30f
    set(value) {
      field = value
    }

  private var memoryFree: Long = 1000000000
    set(value) {
      field = value
    }

  private var uptime: Duration = Duration.ofDays(1)
    set(value) {
      field = value
    }

  private var ipAddress: String = "192.168.1.70"
    get() = field

  override fun getCpuTemperature(): Float = cpuTemperature

  override fun getMemoryFree(): Long = memoryFree

  override fun getUptime(): Duration = uptime

  override fun getIPAddresses(): String = ipAddress
}
