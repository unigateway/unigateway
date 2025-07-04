package com.mqgateway.configuration

import com.mqgateway.core.gatewayconfig.connector.HardwareConnectorFactory
import com.mqgateway.core.gatewayconfig.validation.EmptyGatewayValidator
import com.mqgateway.core.gatewayconfig.validation.GatewayValidator
import com.mqgateway.core.io.provider.HardwareConnector
import com.mqgateway.core.io.provider.HardwareInputOutputProvider
import kotlin.reflect.KClass

interface HardwareInterfaceFactory<T : HardwareConnector> {
  fun hardwareInputOutputProvider(): HardwareInputOutputProvider<T>

  fun hardwareConnectorFactory(): HardwareConnectorFactory<T>

  fun connectorClass(): KClass<T>

  fun configurationValidator(): GatewayValidator {
    return EmptyGatewayValidator()
  }
}
