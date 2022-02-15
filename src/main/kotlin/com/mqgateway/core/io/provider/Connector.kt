package com.mqgateway.core.io.provider

/**
 * Configuration used by InputOutputProviders to instantiate input/output interface (i.e. BinaryInput, BinaryOutput etc.)
 */
sealed interface Connector

/**
 * Configuration required by the hardware interface to instantiate concrete hardware input/output interface (i.e. BinaryInput, BinaryOutput etc.)
 */
interface HardwareConnector : Connector

data class MySensorsConnector(
  val nodeId: Int
) : Connector
