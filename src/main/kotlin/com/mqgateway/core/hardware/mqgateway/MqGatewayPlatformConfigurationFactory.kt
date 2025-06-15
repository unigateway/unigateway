package com.mqgateway.core.hardware.mqgateway

class MqGatewayPlatformConfigurationFactory {
  @Suppress("UNCHECKED_CAST")
  fun create(configMap: Map<String, *>): MqGatewayPlatformConfiguration {
    val expanderConfigMap = configMap[EXPANDER_KEY] as Map<String, Any>?
    val expander =
      MqGatewayPlatformConfiguration.ExpanderConfiguration(expanderConfigMap?.get(EXPANDER_ENABLED_KEY)?.toString()?.toBoolean() ?: false)
    val componentsConfigMap = configMap[COMPONENTS_KEY] as Map<String, Map<String, Any>>?
    val mcp23017Ports = (componentsConfigMap?.get(COMPONENTS_MCP23017_KEY)?.get(COMPONENTS_MCP23017_PORTS_KEY) as List<String>?)?.map { it.toInt(16) }
    val components =
      MqGatewayPlatformConfiguration.ComponentsConfiguration(
        MqGatewayPlatformConfiguration.ComponentsConfiguration.Mcp23017Configuration(
          expander,
          mcp23017Ports,
        ),
      )
    val defaultDebounceMs: Long? = configMap[DEFAULT_DEBOUNCE_MS_KEY]?.toString()?.toLong()
    return if (defaultDebounceMs == null) {
      MqGatewayPlatformConfiguration(expander, components)
    } else {
      MqGatewayPlatformConfiguration(expander, components, defaultDebounceMs)
    }
  }

  companion object {
    private const val EXPANDER_KEY = "expander"
    private const val EXPANDER_ENABLED_KEY = "enabled"
    private const val COMPONENTS_KEY = "components"
    private const val COMPONENTS_MCP23017_KEY = "mcp23017"
    private const val COMPONENTS_MCP23017_PORTS_KEY = "ports"
    private const val DEFAULT_DEBOUNCE_MS_KEY = "default-debounce-ms"
  }
}
