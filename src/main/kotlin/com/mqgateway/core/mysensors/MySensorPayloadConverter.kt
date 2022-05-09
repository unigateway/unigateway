package com.mqgateway.core.mysensors

import com.mqgateway.core.io.BinaryState

object MySensorPayloadConverter {

  fun parseFloat(payload: String): Float {
    return payload.toFloat()
  }

  fun parseBinary(payload: String): BinaryState {
    return when (payload) {
      "ON" -> BinaryState.HIGH
      "OFF" -> BinaryState.LOW
      else -> throw Exception("Binary payload: '$payload' cannot be parsed to binary state")
    }
  }

  fun serializeFloat(float: Float): String {
    return float.toString()
  }

  fun serializeBinary(binaryState: BinaryState): String {
    return when (binaryState) {
      BinaryState.HIGH -> "ON"
      BinaryState.LOW -> "OFF"
    }
  }
}
