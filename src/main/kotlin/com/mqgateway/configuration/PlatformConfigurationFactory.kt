package com.mqgateway.configuration

interface PlatformConfigurationFactory {
  fun create(configMap: Map<String, Any>): GatewaySystemProperties.PlatformConfiguration
  fun supports(platform: String): Boolean
}
