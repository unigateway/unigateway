package com.mqgateway

import com.pi4j.io.gpio.GpioController
import com.pi4j.io.gpio.GpioFactory
import com.pi4j.io.i2c.I2CBus
import mu.KotlinLogging
import com.mqgateway.core.device.DeviceFactory
import com.mqgateway.core.device.DeviceRegistry
import com.mqgateway.core.gatewayconfig.ConfigLoader
import com.mqgateway.core.mcpexpander.McpExpandersFactory
import com.mqgateway.core.mcpexpander.Pi4JExpanderPinProvider
import com.mqgateway.core.pi4j.Pi4jConfigurer
import com.mqgateway.homie.gateway.GatewayHomieReceiver
import com.mqgateway.homie.gateway.GatewayHomieUpdateListener
import com.mqgateway.homie.gateway.HomieDeviceFactory
import com.mqgateway.homie.mqtt.HiveMqttClientFactory
import java.util.Properties


private val LOGGER = KotlinLogging.logger {}

private const val DEFAULT_GATEWAY_CONFIG_PATH = "gateway.yml"


fun main(args: Array<String>) {
  LOGGER.info { "HomieGateway started. Initialization..." }

  LOGGER.debug { "Loading application properties" }
  val properties = Properties()
  properties.load(object{}.javaClass.getResourceAsStream("/application.properties").bufferedReader())

  LOGGER.debug { "Loading gateway configuration" }
  val gatewayConfigPath = if (args.isNotEmpty()) args[0] else DEFAULT_GATEWAY_CONFIG_PATH
  val gateway = ConfigLoader.load(gatewayConfigPath)

  LOGGER.debug { "Set up Pi4j components" }
  Pi4jConfigurer.setup(gateway.system.platform)
  val gpio: GpioController = GpioFactory.getInstance()
  val mcpPorts: List<Int> = gateway.system.components.mcp23017.ports.map { it.toInt(16) }
  val mcpExpanders = McpExpandersFactory.create(I2CBus.BUS_0, mcpPorts)

  LOGGER.debug { "Registering devices" }
  val deviceFactory = DeviceFactory(Pi4JExpanderPinProvider(gpio, mcpExpanders))
  val deviceRegistry = DeviceRegistry(deviceFactory.createAll(gateway))

  LOGGER.debug { "Preparing Homie configuration with MQTT connection" }
  val mqttClientFactory = HiveMqttClientFactory(gateway.mqttHostname)
  val homieDevice = HomieDeviceFactory(mqttClientFactory, properties.getProperty("version")).toHomieDevice(gateway)
  val homieReceiver = GatewayHomieReceiver(deviceRegistry)
  homieDevice.connect(homieReceiver)
  deviceRegistry.addUpdateListener(GatewayHomieUpdateListener(homieDevice))


  LOGGER.debug { "Devices initialization" }
  deviceRegistry.initailizeDevices()
  
  LOGGER.info { "Initialization finished successfully. Running normally." }

}

// TODO Telegraf on every MqGateway PI
// TODO Grafana Loki + Promtail for log aggregation
// TODO Configuration changes with Ansible+Docker
// TODO OTA update with Ansible+Docker

// TODO OTA update by looking at specific URL, downloading new jar file and restarting app
// TODO accept configuration file (yaml) by HTTP and restart application after accepting it
