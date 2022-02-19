package com.mqgateway.configuration

import com.mqgateway.core.hardware.MqExpanderPinProvider
import com.mqgateway.core.hardware.MqMcpExpanders
import com.mqgateway.core.hardware.pi4j.Pi4JExpanderPinProvider
import com.mqgateway.core.hardware.pi4j.Pi4JGpioController
import com.mqgateway.core.hardware.pi4j.Pi4JMcpExpanders
import com.mqgateway.core.pi4j.Pi4jConfigurer
import com.mqgateway.core.utils.Pi4JSystemInfoProvider
import com.mqgateway.core.utils.SystemInfoProvider
import com.pi4j.io.gpio.GpioFactory
import com.pi4j.io.i2c.I2CBus
import io.micronaut.context.annotation.Factory
import io.micronaut.context.annotation.Requires
import javax.inject.Singleton

@Factory
@Requires(property = "gateway.system.platform", notEquals = "MQGATEWAY") // TODO this class needs to be removed completely
internal class Pi4JHardwareFactory {

  @Singleton
  fun mcpExpanders(): MqMcpExpanders {
    val mcpPorts: List<Int> = listOf("20", "21", "22", "23", "24", "25", "26", "27").map { it.toInt(16) } // TODO needs to be removed
    return Pi4JMcpExpanders(I2CBus.BUS_0, mcpPorts)
  }

  @Singleton
  fun expanderPinProvider(gatewaySystemProperties: GatewaySystemProperties, mcpExpanders: MqMcpExpanders): MqExpanderPinProvider {
    Pi4jConfigurer.setup()
    val gpio = Pi4JGpioController(GpioFactory.getInstance())
    return Pi4JExpanderPinProvider(gpio, mcpExpanders)
  }

  @Singleton
  fun systemInfoProvider(): SystemInfoProvider {
    return Pi4JSystemInfoProvider()
  }
}
