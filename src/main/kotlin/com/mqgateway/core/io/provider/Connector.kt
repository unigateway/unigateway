package com.mqgateway.core.io.provider

/**
 * Configuration used by InputOutputProviders to instantiate input/output interface (i.e. BinaryInput, BinaryOutput etc.)
 */
sealed class Connector

/**
 * Configuration required by the hardware interface to instantiate concrete hardware input/output interface (i.e. BinaryInput, BinaryOutput etc.)
 */
abstract class HardwareConnector : Connector()

data class MySensorsConnector(
  val nodeId: Int
) : Connector()
