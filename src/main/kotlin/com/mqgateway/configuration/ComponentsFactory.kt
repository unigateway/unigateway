package com.mqgateway.configuration

import com.fasterxml.jackson.databind.ObjectMapper
import com.fasterxml.jackson.dataformat.yaml.YAMLFactory
import com.fasterxml.jackson.module.kotlin.KotlinModule
import com.mqgateway.core.device.DeviceFactory
import com.mqgateway.core.device.DeviceRegistry
import com.mqgateway.core.gatewayconfig.ConfigLoader
import com.mqgateway.core.gatewayconfig.Gateway
import com.mqgateway.core.gatewayconfig.parser.YamlParser
import com.mqgateway.core.gatewayconfig.validation.ConfigValidator
import com.mqgateway.core.gatewayconfig.validation.GatewayValidator
import com.mqgateway.core.hardware.MqExpanderPinProvider
import com.mqgateway.core.hardware.MqSerial
import com.mqgateway.core.utils.SerialConnection
import com.mqgateway.core.utils.SystemInfoProvider
import com.mqgateway.core.utils.TimersScheduler
import com.mqgateway.homie.HomieDevice
import com.mqgateway.homie.gateway.GatewayHomieReceiver
import com.mqgateway.homie.gateway.HomieDeviceFactory
import com.mqgateway.homie.mqtt.HiveMqttClientFactory
import com.mqgateway.homie.mqtt.MqttClientFactory
import io.micronaut.context.annotation.Factory
import io.micronaut.context.annotation.Requires
import javax.inject.Named
import javax.inject.Singleton

@Factory
internal class ComponentsFactory {

  @Singleton
  fun gatewayConfigLoader(@Named("yamlObjectMapper") yamlObjectMapper: ObjectMapper, gatewayValidators: List<GatewayValidator>): ConfigLoader {
    val yamlParser = YamlParser(yamlObjectMapper)
    val configValidator = ConfigValidator(yamlObjectMapper, gatewayValidators)
    return ConfigLoader(yamlParser, configValidator)
  }

  @Singleton
  @Named("yamlObjectMapper")
  fun yamlObjectMapper(): ObjectMapper {
    val mapper = ObjectMapper(YAMLFactory())
    mapper.registerModule(KotlinModule())
    return mapper
  }

  @Singleton
  fun gatewayConfiguration(
    gatewayApplicationProperties: GatewayApplicationProperties,
    gatewayConfigLoader: ConfigLoader
  ): Gateway {
    return gatewayConfigLoader.load(gatewayApplicationProperties.configPath)
  }

  @Singleton
  fun deviceRegistry(
    systemInfoProvider: SystemInfoProvider,
    expanderPinProvider: MqExpanderPinProvider,
    gateway: Gateway,
    timersScheduler: TimersScheduler,
    serialConnection: SerialConnection?
  ): DeviceRegistry {
    val deviceFactory = DeviceFactory(expanderPinProvider, timersScheduler, serialConnection, systemInfoProvider)
    return DeviceRegistry(deviceFactory.createAll(gateway))
  }

  @Singleton
  fun mqttClientFactory(gateway: Gateway): MqttClientFactory = HiveMqttClientFactory(gateway.mqttHostname)

  @Singleton
  fun homieDevice(
    mqttClientFactory: MqttClientFactory,
    homieReceiver: GatewayHomieReceiver,
    gatewayApplicationProperties: GatewayApplicationProperties,
    gatewaySystemProperties: GatewaySystemProperties,
    gateway: Gateway
  ): HomieDevice {

    return HomieDeviceFactory(mqttClientFactory, homieReceiver, gatewayApplicationProperties.appVersion)
      .toHomieDevice(gateway, gatewaySystemProperties.networkAdapter)
  }

  @Singleton
  fun homieReceiver(deviceRegistry: DeviceRegistry) = GatewayHomieReceiver(deviceRegistry)

  @Singleton
  @Requires(property = "gateway.system.components.serial.enabled", value = "true")
  fun serialConnection(serial: MqSerial): SerialConnection {
    val serialConnection = SerialConnection(serial)
    serialConnection.init()
    return serialConnection
  }
}
