package com.mqgateway.core.gatewayconfig

import com.mqgateway.core.io.provider.Connector
import com.mqgateway.core.io.provider.MySensorsConnector
import kotlinx.serialization.ExperimentalSerializationApi
import kotlinx.serialization.cbor.Cbor
import kotlinx.serialization.decodeFromByteArray
import kotlinx.serialization.encodeToByteArray
import kotlinx.serialization.modules.SerializersModule
import kotlinx.serialization.modules.plus
import kotlinx.serialization.modules.polymorphic
import kotlinx.serialization.modules.subclass

@ExperimentalSerializationApi
class FastConfigurationSerializer(
  private val customConnectorSerializerModule: SerializersModule
) {
  private val baseConnectorModule = SerializersModule {
    polymorphic(Connector::class) {
      subclass(MySensorsConnector::class)
    }
  }
  private val format = Cbor {
    serializersModule = baseConnectorModule + customConnectorSerializerModule
  }

  fun encode(gatewayConfiguration: GatewayConfiguration): ByteArray {
    return format.encodeToByteArray(gatewayConfiguration)
  }

  fun decode(bytes: ByteArray): GatewayConfiguration {
    return format.decodeFromByteArray(bytes)
  }
}
