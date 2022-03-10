package com.mqgateway.configuration

import com.mqgateway.core.gatewayconfig.connector.HardwareConnectorFactory
import com.mqgateway.core.io.provider.HardwareConnector
import com.mqgateway.core.io.provider.HardwareInputOutputProvider
import kotlin.reflect.KClass

interface HardwareInterfaceFactory<T : HardwareConnector> {
  fun hardwareInputOutputProvider(platformConfiguration: Map<String, *>): HardwareInputOutputProvider<T>
  fun hardwareConnectorFactory(): HardwareConnectorFactory<T>
  fun connectorClass(): KClass<T>
}