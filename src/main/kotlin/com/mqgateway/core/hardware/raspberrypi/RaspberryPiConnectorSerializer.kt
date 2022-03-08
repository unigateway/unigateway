package com.mqgateway.core.hardware.raspberrypi

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
class RaspberryPiConnectorSerializer : KSerializer<RaspberryPiConnector> {
  override fun deserialize(decoder: Decoder): RaspberryPiConnector {
    val pin = decoder.decodeInt()
    val debounceMs = decoder.decodeInt().takeIf { it != -1 }

    return RaspberryPiConnector(pin, debounceMs)
  }

  override val descriptor: SerialDescriptor
    get() = buildSerialDescriptor("io.unigateway.core.hardware.raspberrypi", PolymorphicKind.OPEN)

  override fun serialize(encoder: Encoder, value: RaspberryPiConnector) {
    encoder.encodeInt(value.pin)
    encoder.encodeInt(value.debounceMs ?: -1)
  }
}
