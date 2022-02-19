package com.mqgateway.core.hardware.mqgateway

import com.mqgateway.configuration.PlatformConfigurationFactory

class MqGatewayPlatformConfigurationFactory : PlatformConfigurationFactory {
  override fun create(configMap: Map<String, Any>): MqGatewayPlatformConfiguration {
    val expanderConfigMap = configMap[EXPANDER_KEY] as Map<String, Any>?
    val expander = MqGatewayPlatformConfiguration.ExpanderConfiguration(expanderConfigMap?.get(EXPANDER_ENABLED_KEY) as Boolean? ?: false)
    val componentsConfigMap = configMap[COMPONENTS_KEY] as Map<String, Map<String, Any>>?
    val mcp23017Ports = componentsConfigMap?.get(COMPONENTS_MCP23017_KEY)?.get(COMPONENTS_MCP23017_PORTS_KEY) as List<String>?
    val components = MqGatewayPlatformConfiguration.ComponentsConfiguration(
      MqGatewayPlatformConfiguration.ComponentsConfiguration.Mcp23017Configuration(
        expander, mcp23017Ports
      )
    )
    return MqGatewayPlatformConfiguration(expander, components)
  }

  companion object {
    private const val EXPANDER_KEY = "expander"
    private const val EXPANDER_ENABLED_KEY = "enabled"
    private const val COMPONENTS_KEY = "components"
    private const val COMPONENTS_MCP23017_KEY = "mcp23017"
    private const val COMPONENTS_MCP23017_PORTS_KEY = "ports"
  }

  override fun supports(platform: String) = platform.equals("MQGATEWAY", true)
}
