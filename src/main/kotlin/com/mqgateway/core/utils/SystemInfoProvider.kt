package com.mqgateway.core.utils

import oshi.SystemInfo
import java.time.Duration

interface SystemInfoProvider {
  fun getCpuTemperature(): Float
  fun getMemoryFree(): Long
  fun getUptime(): Duration
  fun getIPAddresses(): String
  fun getSummary(): String
}

class OshiSystemInfoProvider: SystemInfoProvider {
  private val systemInfo: SystemInfo = SystemInfo()

  override fun getCpuTemperature(): Float {
    return systemInfo.hardware.sensors.cpuTemperature.toFloat() // todo change to double?
  }

  override fun getMemoryFree(): Long {
    return systemInfo.hardware.memory.available
  }

  override fun getUptime(): Duration {
    return Duration.ofSeconds(systemInfo.operatingSystem.systemUptime)
  }

  override fun getIPAddresses(): String {
    // todo systemInfo.hardware.networkIFs
    return ""
  }

  override fun getSummary(): String {
    return """
      System info
      CPU temperature: ${getCpuTemperature()} 
      Free memory: ${getMemoryFree()}
      Uptime: ${getUptime()}
      IP address: ${getIPAddresses()}
    """.trimIndent()
  }
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

  override fun getSummary(): String {
    TODO("Not yet implemented")
  }
}
