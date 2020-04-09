package com.mqgateway.core.gatewayconfig

import kotlinx.serialization.Serializable

@Serializable
class SystemConfiguration(val networkAdapter: String = "eth0", val platform: SystemPlatform, val components: ComponentsConfiguration)

enum class SystemPlatform {
  NANOPI, RASPBERRYPI, SIMULATED
}

@Serializable
data class ComponentsConfiguration(val mcp23017: Mcp23017Configuration)

@Serializable
data class Mcp23017Configuration(val ports: List<String> = listOf("0x20","0x21","0x22","0x23"))

