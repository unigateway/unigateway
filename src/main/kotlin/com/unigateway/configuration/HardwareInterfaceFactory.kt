package com.unigateway.configuration

import com.unigateway.core.gatewayconfig.connector.HardwareConnectorFactory
import com.unigateway.core.gatewayconfig.validation.EmptyGatewayValidator
import com.unigateway.core.gatewayconfig.validation.GatewayValidator
import com.unigateway.core.io.provider.HardwareConnector
import com.unigateway.core.io.provider.HardwareInputOutputProvider
import kotlin.reflect.KClass

interface HardwareInterfaceFactory<T : HardwareConnector> {
  fun hardwareInputOutputProvider(): HardwareInputOutputProvider<T>
  fun hardwareConnectorFactory(): HardwareConnectorFactory<T>
  fun connectorClass(): KClass<T>

  fun configurationValidator(): GatewayValidator {
    return EmptyGatewayValidator()
  }
}
