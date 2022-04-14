package com.mqgateway.configuration

import com.fasterxml.jackson.annotation.JsonInclude
import com.fasterxml.jackson.databind.ObjectMapper
import com.fasterxml.jackson.dataformat.yaml.YAMLFactory
import com.fasterxml.jackson.module.kotlin.KotlinModule
import com.mqgateway.core.device.DeviceFactoryProvider
import com.mqgateway.core.device.DeviceRegistry
import com.mqgateway.core.gatewayconfig.ConfigLoader
import com.mqgateway.core.gatewayconfig.DeviceRegistryFactory
import com.mqgateway.core.gatewayconfig.FastConfigurationSerializer
import com.mqgateway.core.gatewayconfig.GatewayConfiguration
import com.mqgateway.core.gatewayconfig.connector.ConnectorFactory
import com.mqgateway.core.gatewayconfig.connector.HardwareConnectorFactory
import com.mqgateway.core.gatewayconfig.connector.MySensorsConnectorFactory
import com.mqgateway.core.gatewayconfig.parser.ConfigurationJacksonModule
import com.mqgateway.core.gatewayconfig.parser.YamlParser
import com.mqgateway.core.gatewayconfig.rest.GatewayConfigurationService
import com.mqgateway.core.gatewayconfig.validation.ConfigValidator
import com.mqgateway.core.gatewayconfig.validation.GateAdditionalConfigValidator
import com.mqgateway.core.gatewayconfig.validation.JsonSchemaValidator
import com.mqgateway.core.gatewayconfig.validation.ReferenceDeviceValidator
import com.mqgateway.core.gatewayconfig.validation.ShutterAdditionalConfigValidator
import com.mqgateway.core.gatewayconfig.validation.UniqueDeviceIdsValidator
import com.mqgateway.core.io.provider.HardwareConnector
import com.mqgateway.core.io.provider.HardwareInputOutputProvider
import com.mqgateway.core.io.provider.InputOutputProvider
import com.mqgateway.core.io.provider.MySensorsInputOutputProvider
import com.mqgateway.core.utils.OshiSystemInfoProvider
import com.mqgateway.core.utils.SystemInfoProvider
import com.mqgateway.core.utils.TimersScheduler
import io.micronaut.context.annotation.Factory
import jakarta.inject.Named
import jakarta.inject.Singleton
import kotlinx.serialization.ExperimentalSerializationApi
import kotlinx.serialization.modules.SerializersModule

@ExperimentalSerializationApi
@Factory
internal class ComponentsFactory {

  @Singleton
  fun configValidator(
    @Named("yamlObjectMapper") yamlObjectMapper: ObjectMapper,
    gatewaySystemProperties: GatewaySystemProperties,
  ): JsonSchemaValidator {
    return JsonSchemaValidator(yamlObjectMapper, gatewaySystemProperties)
  }

  @Singleton
  fun configValidator(
    jsonSchemaValidator: JsonSchemaValidator,
    hardwareInterfaceFactory: HardwareInterfaceFactory<*>
  ): ConfigValidator {
    val validators = listOf(
      GateAdditionalConfigValidator(),
      ReferenceDeviceValidator(),
      ShutterAdditionalConfigValidator(),
      UniqueDeviceIdsValidator()
    )
    return ConfigValidator(jsonSchemaValidator, validators + hardwareInterfaceFactory.configurationValidator())
  }

  @Singleton
  fun yamlParser(@Named("yamlObjectMapper") yamlObjectMapper: ObjectMapper): YamlParser {
    return YamlParser(yamlObjectMapper)
  }

  @Singleton
  fun fastConfigurationSerializer(serializersModule: SerializersModule): FastConfigurationSerializer {
    return FastConfigurationSerializer(serializersModule)
  }

  @Singleton
  fun gatewayConfigLoader(
    yamlParser: YamlParser,
    fastConfigurationSerializer: FastConfigurationSerializer,
    configValidator: ConfigValidator
  ): ConfigLoader {
    return ConfigLoader(yamlParser, fastConfigurationSerializer, configValidator)
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
  fun deviceFactoryProvider(
    inputOutputProvider: InputOutputProvider<*>,
    timersScheduler: TimersScheduler,
    systemInfoProvider: SystemInfoProvider
  ): DeviceFactoryProvider {
    return DeviceFactoryProvider(inputOutputProvider, timersScheduler, systemInfoProvider)
  }

  @Singleton
  fun deviceRegistryFactory(
    deviceFactoryProvider: DeviceFactoryProvider
  ): DeviceRegistryFactory {
    return DeviceRegistryFactory(deviceFactoryProvider)
  }

  @Singleton
  fun deviceRegistry(
    deviceRegistryFactory: DeviceRegistryFactory,
    gatewayConfiguration: GatewayConfiguration
  ): DeviceRegistry {
    return deviceRegistryFactory.create(gatewayConfiguration)
  }

  @Singleton
  fun mySensorsConnectorFactory(): MySensorsConnectorFactory {
    return MySensorsConnectorFactory()
  }

  @Singleton
  fun mySensorsInputOutputProvider(): MySensorsInputOutputProvider {
    return MySensorsInputOutputProvider()
  }

  @Singleton
  fun <T : HardwareConnector> connectorFactory(
    mySensorsConnectorFactory: MySensorsConnectorFactory,
    hardwareConnectorFactory: HardwareConnectorFactory<T>
  ): ConnectorFactory<T> {
    return ConnectorFactory(mySensorsConnectorFactory, hardwareConnectorFactory)
  }

  @Singleton
  fun <T : HardwareConnector> inputOutputProvider(
    hardwareInputOutputProvider: HardwareInputOutputProvider<T>,
    mySensorsInputOutputProvider: MySensorsInputOutputProvider
  ): InputOutputProvider<T> {
    return InputOutputProvider(hardwareInputOutputProvider, mySensorsInputOutputProvider)
  }

  @Singleton
  fun systemInfoProvider(): SystemInfoProvider {
    return OshiSystemInfoProvider()
  }
}
