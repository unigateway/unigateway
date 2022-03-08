package com.mqgateway.core.hardware.simulated

import kotlinx.serialization.ExperimentalSerializationApi
import kotlinx.serialization.InternalSerializationApi
import kotlinx.serialization.KSerializer
import kotlinx.serialization.descriptors.PolymorphicKind
import kotlinx.serialization.descriptors.SerialDescriptor
import kotlinx.serialization.descriptors.buildSerialDescriptor
import kotlinx.serialization.encoding.Decoder
import kotlinx.serialization.encoding.Encoder

@ExperimentalSerializationApi
@InternalSerializationApi
class SimulatedConnectorSerializer : KSerializer<SimulatedConnector> {
  override fun deserialize(decoder: Decoder): SimulatedConnector {
    val pin = decoder.decodeInt()
    val initialValue = decoder.decodeString().ifBlank { null }

    return SimulatedConnector(pin, initialValue)
  }

  override val descriptor: SerialDescriptor
    get() = buildSerialDescriptor("io.unigateway.core.hardware.simulated", PolymorphicKind.OPEN)

  override fun serialize(encoder: Encoder, value: SimulatedConnector) {
    encoder.encodeInt(value.pin)
    encoder.encodeString(value.initialValue ?: "")
  }
}
