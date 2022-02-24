package com.mqgateway.configuration

import com.fasterxml.jackson.annotation.JsonInclude
import com.fasterxml.jackson.databind.ObjectMapper
import com.fasterxml.jackson.dataformat.yaml.YAMLFactory
import com.fasterxml.jackson.module.kotlin.KotlinModule
import com.mqgateway.core.device.DeviceFactory
import com.mqgateway.core.device.DeviceRegistry
import com.mqgateway.core.gatewayconfig.ConfigLoader
import com.mqgateway.core.gatewayconfig.GatewayConfiguration
import com.mqgateway.core.gatewayconfig.connector.ConnectorFactory
import com.mqgateway.core.gatewayconfig.parser.ConfigurationJacksonModule
import com.mqgateway.core.gatewayconfig.parser.YamlParser
import com.mqgateway.core.gatewayconfig.rest.GatewayConfigurationService
import com.mqgateway.core.gatewayconfig.validation.ConfigValidator
import com.mqgateway.core.gatewayconfig.validation.GatewayValidator
import com.mqgateway.core.hardware.MqExpanderPinProvider
import com.mqgateway.core.utils.SystemInfoProvider
import com.mqgateway.core.utils.TimersScheduler
import io.micronaut.context.annotation.Factory
import jakarta.inject.Named
import jakarta.inject.Singleton

@Factory
internal class ComponentsFactory {

  @Singleton
  fun configValidator(
    @Named("yamlObjectMapper") yamlObjectMapper: ObjectMapper,
    gatewaySystemProperties: GatewaySystemProperties,
    gatewayValidators: List<GatewayValidator>
  ): ConfigValidator {
    return ConfigValidator(yamlObjectMapper, gatewaySystemProperties, gatewayValidators)
  }

  @Singleton
  fun gatewayConfigLoader(
    @Named("yamlObjectMapper") yamlObjectMapper: ObjectMapper,
    configValidator: ConfigValidator
  ): ConfigLoader {
    val yamlParser = YamlParser(yamlObjectMapper)
    return ConfigLoader(yamlParser, configValidator)
  }

  @Singleton
  @Named("yamlObjectMapper")
  fun yamlObjectMapper(connectorFactory: ConnectorFactory<*>): ObjectMapper {
    val mapper = ObjectMapper(YAMLFactory())
    mapper.registerModule(KotlinModule())
    mapper.setSerializationInclusion(JsonInclude.Include.NON_EMPTY)

    mapper.registerModule(ConfigurationJacksonModule(connectorFactory))

    return mapper
  }

  @Singleton
  fun gatewayConfiguration(
    gatewayApplicationProperties: GatewayApplicationProperties,
    gatewayConfigLoader: ConfigLoader
  ): GatewayConfiguration {
    return gatewayConfigLoader.load(gatewayApplicationProperties.configPath)
  }

  @Singleton
  fun gatewayConfigurationService(
    configValidator: ConfigValidator,
    gatewayApplicationProperties: GatewayApplicationProperties,
    @Named("yamlObjectMapper") yamlObjectMapper: ObjectMapper
  ): GatewayConfigurationService {
    return GatewayConfigurationService(configValidator, gatewayApplicationProperties.configPath, yamlObjectMapper)
  }

  @Singleton
  fun deviceFactory(
    expanderPinProvider: MqExpanderPinProvider,
    timersScheduler: TimersScheduler,
    systemInfoProvider: SystemInfoProvider
  ): DeviceFactory {
    return DeviceFactory(expanderPinProvider, timersScheduler, systemInfoProvider)
  }

  @Singleton
  fun deviceRegistry(
    gatewayConfiguration: GatewayConfiguration,
    deviceFactory: DeviceFactory
  ): DeviceRegistry {
    return DeviceRegistry(deviceFactory.createAll(gatewayConfiguration))
  }
}
