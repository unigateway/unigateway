package com.mqgateway.configuration

import com.fasterxml.jackson.annotation.JsonInclude
import com.fasterxml.jackson.databind.ObjectMapper
import com.fasterxml.jackson.dataformat.yaml.YAMLFactory
import com.fasterxml.jackson.module.kotlin.KotlinModule
import com.mqgateway.core.device.DeviceRegistry
import com.mqgateway.core.device.factory.DeviceFactoryProvider
import com.mqgateway.core.gatewayconfig.ConfigLoader
import com.mqgateway.core.gatewayconfig.FastConfigurationSerializer
import com.mqgateway.core.gatewayconfig.DeviceConfiguration
import com.mqgateway.core.gatewayconfig.DeviceType
import com.mqgateway.core.gatewayconfig.GatewayConfiguration
import com.mqgateway.core.gatewayconfig.connector.ConnectorFactory
import com.mqgateway.core.gatewayconfig.connector.HardwareConnectorFactory
import com.mqgateway.core.gatewayconfig.connector.MySensorsConnectorFactory
import com.mqgateway.core.gatewayconfig.parser.ConfigurationJacksonModule
import com.mqgateway.core.gatewayconfig.parser.YamlParser
import com.mqgateway.core.gatewayconfig.rest.GatewayConfigurationService
import com.mqgateway.core.gatewayconfig.validation.ConfigValidator
import com.mqgateway.core.gatewayconfig.validation.GatewayValidator
import com.mqgateway.core.io.provider.HardwareConnector
import com.mqgateway.core.io.provider.HardwareInputOutputProvider
import com.mqgateway.core.io.provider.InputOutputProvider
import com.mqgateway.core.io.provider.MySensorsInputOutputProvider
import com.mqgateway.core.utils.SystemInfoProvider
import com.mqgateway.core.utils.TimersScheduler
import io.micronaut.context.annotation.Factory
import jakarta.inject.Named
import jakarta.inject.Singleton
import kotlinx.serialization.modules.SerializersModule

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
  fun deviceRegistry(
    gatewayConfiguration: GatewayConfiguration,
    deviceFactoryProvider: DeviceFactoryProvider
  ): DeviceRegistry {
    // todo move to some class (how to call it? xD) to test it
    //  todo here should be also logic that first we create all basic devices, and then all devices with internalDevice
    val mqGatewayDeviceConfiguration = DeviceConfiguration(gatewayConfiguration.name, gatewayConfiguration.name, DeviceType.MQGATEWAY)
    val gatewayDevice = deviceFactoryProvider.getFactory(DeviceType.MQGATEWAY).create(mqGatewayDeviceConfiguration)

    val configuredDevices = gatewayConfiguration.devices.map {
      deviceFactoryProvider
        .getFactory(it.type)
        .create(it)
    }.toSet()

    return DeviceRegistry(setOf(gatewayDevice) + configuredDevices)
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
}
