package com.unigateway.configuration

import com.fasterxml.jackson.annotation.JsonInclude
import com.fasterxml.jackson.databind.ObjectMapper
import com.fasterxml.jackson.dataformat.yaml.YAMLFactory
import com.fasterxml.jackson.module.kotlin.KotlinModule
import com.unigateway.core.device.DeviceFactoryProvider
import com.unigateway.core.device.DeviceRegistry
import com.unigateway.core.gatewayconfig.ConfigLoader
import com.unigateway.core.gatewayconfig.DeviceRegistryFactory
import com.unigateway.core.gatewayconfig.FastConfigurationSerializer
import com.unigateway.core.gatewayconfig.GatewayConfiguration
import com.unigateway.core.gatewayconfig.connector.ConnectorFactory
import com.unigateway.core.gatewayconfig.connector.HardwareConnectorFactory
import com.unigateway.core.gatewayconfig.connector.MySensorsConnectorFactory
import com.unigateway.core.gatewayconfig.parser.ConfigurationJacksonModule
import com.unigateway.core.gatewayconfig.parser.YamlParser
import com.unigateway.core.gatewayconfig.rest.GatewayConfigurationService
import com.unigateway.core.gatewayconfig.validation.ConfigValidator
import com.unigateway.core.gatewayconfig.validation.GateAdditionalConfigValidator
import com.unigateway.core.gatewayconfig.validation.JsonSchemaValidator
import com.unigateway.core.gatewayconfig.validation.ReferenceDeviceValidator
import com.unigateway.core.gatewayconfig.validation.ShutterAdditionalConfigValidator
import com.unigateway.core.gatewayconfig.validation.UniqueDeviceIdsValidator
import com.unigateway.core.io.JCommSerial
import com.unigateway.core.io.provider.DefaultMySensorsInputOutputProvider
import com.unigateway.core.io.provider.DisabledMySensorsInputOutputProvider
import com.unigateway.core.io.provider.HardwareConnector
import com.unigateway.core.io.provider.HardwareInputOutputProvider
import com.unigateway.core.io.provider.InputOutputProvider
import com.unigateway.core.io.provider.MySensorsInputOutputProvider
import com.unigateway.core.mysensors.MySensorMessageSerializer
import com.unigateway.core.mysensors.MySensorsSerialConnection
import com.unigateway.core.utils.OshiSystemInfoProvider
import com.unigateway.core.utils.SystemInfoProvider
import com.unigateway.core.utils.TimersScheduler
import io.micronaut.context.annotation.Factory
import io.micronaut.context.annotation.Requires
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
  @Requires(property = "gateway.system.mysensors.enabled", value = "true")
  fun mySensorsSerialConnection(gatewaySystemProperties: GatewaySystemProperties): MySensorsSerialConnection {
    val serial = JCommSerial()

    serial.open(gatewaySystemProperties.mySensors.portDescriptor, gatewaySystemProperties.mySensors.baudRate)
    return MySensorsSerialConnection(serial, MySensorMessageSerializer())
  }

  @Singleton
  @Requires(property = "gateway.system.mysensors.enabled", value = "true")
  fun mySensorsInputOutputProvider(mySensorsSerialConnection: MySensorsSerialConnection): MySensorsInputOutputProvider {
    return DefaultMySensorsInputOutputProvider(mySensorsSerialConnection)
  }

  @Singleton
  @Requires(property = "gateway.system.mysensors.enabled", value = "false")
  fun mySensorsInputOutputProvider(): MySensorsInputOutputProvider {
    return DisabledMySensorsInputOutputProvider()
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
