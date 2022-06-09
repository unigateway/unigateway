package com.unigateway.core.gatewayconfig

import com.unigateway.core.io.provider.Connector
import com.unigateway.core.io.provider.MySensorsConnector
import com.unigateway.core.mysensors.InternalType
import com.unigateway.core.mysensors.PresentationType
import com.unigateway.core.mysensors.SetReqType
import com.unigateway.core.mysensors.StreamType
import com.unigateway.core.mysensors.Type
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
