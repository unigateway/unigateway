package com.mqgateway.core.gatewayconfig

import com.mqgateway.core.io.provider.Connector
import com.mqgateway.core.io.provider.MySensorsConnector
import com.mqgateway.core.mysensors.InternalType
import com.mqgateway.core.mysensors.PresentationType
import com.mqgateway.core.mysensors.SetReqType
import com.mqgateway.core.mysensors.StreamType
import com.mqgateway.core.mysensors.Type
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
    polymorphic(Type::class) {
      subclass(PresentationType::class)
      subclass(SetReqType::class)
      subclass(InternalType::class)
      subclass(StreamType::class)
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
