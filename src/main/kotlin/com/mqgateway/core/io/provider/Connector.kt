package com.mqgateway.core.io.provider

import kotlinx.serialization.KSerializer
import kotlinx.serialization.Serializable
import kotlinx.serialization.descriptors.SerialDescriptor
import kotlinx.serialization.encoding.Decoder
import kotlinx.serialization.encoding.Encoder

/**
 * Configuration used by InputOutputProviders to instantiate input/output interface (i.e. BinaryInput, BinaryOutput etc.)
 */
sealed interface Connector

/**
 * Configuration required by the hardware interface to instantiate concrete hardware input/output interface (i.e. BinaryInput, BinaryOutput etc.)
 */
interface HardwareConnector : Connector

@Serializable
data class MySensorsConnector(
  val nodeId: Int
) : Connector

class HardwareConnectorSerializer<T : HardwareConnector>(private val connectorSerializer: KSerializer<T>) : KSerializer<T> {
  override fun deserialize(decoder: Decoder): T {
    return connectorSerializer.deserialize(decoder)
  }

  override val descriptor: SerialDescriptor
    get() = connectorSerializer.descriptor

  override fun serialize(encoder: Encoder, value: T) {
    connectorSerializer.serialize(encoder, value)
  }
}
