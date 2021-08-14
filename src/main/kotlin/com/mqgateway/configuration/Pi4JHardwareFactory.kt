package com.mqgateway.configuration

import com.mqgateway.core.hardware.MqExpanderPinProvider
import com.mqgateway.core.hardware.MqMcpExpanders
import com.mqgateway.core.hardware.MqSerial
import com.mqgateway.core.hardware.pi4j.Pi4JExpanderPinProvider
import com.mqgateway.core.hardware.pi4j.Pi4JGpioController
import com.mqgateway.core.hardware.pi4j.Pi4JMcpExpanders
import com.mqgateway.core.hardware.pi4j.Pi4JSerial
import com.mqgateway.core.pi4j.Pi4jConfigurer
import com.mqgateway.core.utils.Pi4JSystemInfoProvider
import com.mqgateway.core.utils.SystemInfoProvider
import com.pi4j.io.gpio.GpioFactory
import com.pi4j.io.i2c.I2CBus
import com.pi4j.io.serial.SerialFactory
import io.micronaut.context.annotation.Factory
import io.micronaut.context.annotation.Requires
import mu.KotlinLogging
import java.nio.file.Paths
import javax.inject.Singleton
import kotlin.io.path.ExperimentalPathApi
import kotlin.io.path.notExists

private val LOGGER = KotlinLogging.logger {}

@Factory
@Requires(property = "gateway.system.platform", notEquals = "SIMULATED")
internal class Pi4JHardwareFactory {

  @Singleton
  fun mcpExpanders(gatewaySystemProperties: GatewaySystemProperties): MqMcpExpanders {
    val mcpPorts: List<Int> = gatewaySystemProperties.components.mcp23017.getPorts().map { it.toInt(16) }
    return Pi4JMcpExpanders(I2CBus.BUS_0, mcpPorts)
  }

  @Singleton
  fun expanderPinProvider(gatewaySystemProperties: GatewaySystemProperties, mcpExpanders: MqMcpExpanders): MqExpanderPinProvider {
    Pi4jConfigurer.setup(gatewaySystemProperties.platform)
    val gpio = Pi4JGpioController(GpioFactory.getInstance())
    return Pi4JExpanderPinProvider(gpio, mcpExpanders)
  }

  @Singleton
  fun systemInfoProvider(): SystemInfoProvider {
    return Pi4JSystemInfoProvider()
  }

  @ExperimentalPathApi
  @Singleton
  @Requires(property = "gateway.system.components.mysensors.enabled", value = "true")
  fun mySensorsSerial(gatewaySystemProperties: GatewaySystemProperties): MqSerial {
    val serialDevice: String = gatewaySystemProperties.components.mySensors.serialDevice
    while (Paths.get(serialDevice).notExists()) {
      LOGGER.warn {
        """
          Serial device '$serialDevice' has not been found. MySensors Gateway has probably not been started yet or it's misconfigured.
            You can disable MySensors by setting environment variable GATEWAY_SYSTEM_COMPONENTS_MYSENSORS_ENABLED=false.
            Waiting until MySensors create PTY on '$serialDevice'..."
        """.trimIndent().trim()
      }
      Thread.sleep(5000)
    }

    val serial = Pi4JSerial(SerialFactory.createInstance())
    serial.open(serialDevice, 115200)
    return serial
  }
}
