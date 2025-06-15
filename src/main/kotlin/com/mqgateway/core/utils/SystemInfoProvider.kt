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

class OshiSystemInfoProvider : SystemInfoProvider {
  private val systemInfo: SystemInfo = SystemInfo()

  override fun getCpuTemperature(): Float {
    return systemInfo.hardware.sensors.cpuTemperature.toFloat()
  }

  override fun getMemoryFree(): Long {
    return systemInfo.hardware.memory.available
  }

  override fun getUptime(): Duration {
    return Duration.ofSeconds(systemInfo.operatingSystem.systemUptime)
  }

  override fun getIPAddresses(): String {
    return systemInfo.hardware.networkIFs
      .flatMap { it.iPv4addr.toList() }
      .joinToString()
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
