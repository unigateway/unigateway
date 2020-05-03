package com.mqgateway

import com.mqgateway.core.device.DeviceFactory
import com.mqgateway.core.device.DeviceRegistry
import com.mqgateway.core.gatewayconfig.ConfigLoader
import com.mqgateway.core.mcpexpander.McpExpandersFactory
import com.mqgateway.core.mcpexpander.Pi4JExpanderPinProvider
import com.mqgateway.core.onewire.OneWireBus
import com.mqgateway.core.pi4j.Pi4jConfigurer
import com.mqgateway.homie.gateway.GatewayHomieReceiver
import com.mqgateway.homie.gateway.GatewayHomieUpdateListener
import com.mqgateway.homie.gateway.HomieDeviceFactory
import com.mqgateway.homie.mqtt.HiveMqttClientFactory
import com.pi4j.io.gpio.GpioController
import com.pi4j.io.gpio.GpioFactory
import com.pi4j.io.i2c.I2CBus
import java.util.Properties
import mu.KotlinLogging

private val LOGGER = KotlinLogging.logger {}

private const val DEFAULT_GATEWAY_CONFIG_PATH = "gateway.yaml"

fun main(args: Array<String>) {
  LOGGER.info { "HomieGateway started. Initialization..." }

  LOGGER.debug { "Loading application properties" }
  val properties = Properties()
  properties.load(object {}.javaClass.getResourceAsStream("/application.properties").bufferedReader())

  LOGGER.debug { "Loading gateway configuration" }
  val gatewayConfigPath = if (args.isNotEmpty()) args[0] else DEFAULT_GATEWAY_CONFIG_PATH
  val gateway = ConfigLoader.load(gatewayConfigPath)

  LOGGER.debug { "Set up Pi4j components" }
  Pi4jConfigurer.setup(gateway.system.platform)
  val gpio: GpioController = GpioFactory.getInstance()
  val mcpPorts: List<Int> = gateway.system.components.mcp23017.ports.map { it.toInt(16) }
  val mcpExpanders = McpExpandersFactory.create(I2CBus.BUS_0, mcpPorts)

  LOGGER.debug { "Registering devices" }
  val oneWireBus = OneWireBus()
  val deviceFactory = DeviceFactory(Pi4JExpanderPinProvider(gpio, mcpExpanders), oneWireBus)
  val deviceRegistry = DeviceRegistry(deviceFactory.createAll(gateway))
  oneWireBus.start()

  LOGGER.debug { "Preparing Homie configuration with MQTT connection" }
  val mqttClientFactory = HiveMqttClientFactory(gateway.mqttHostname)
  val homieDevice = HomieDeviceFactory(mqttClientFactory, properties.getProperty("version")).toHomieDevice(gateway)
  val homieReceiver = GatewayHomieReceiver(deviceRegistry)
  homieDevice.connect(homieReceiver)
  deviceRegistry.addUpdateListener(GatewayHomieUpdateListener(homieDevice))

  LOGGER.debug { "Devices initialization" }
  deviceRegistry.initializeDevices()

  LOGGER.info { "Initialization finished successfully. Running normally." }
}
