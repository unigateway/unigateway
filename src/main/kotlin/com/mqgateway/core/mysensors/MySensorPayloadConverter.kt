package com.mqgateway.core.mysensors

import com.mqgateway.core.io.BinaryState

object MySensorPayloadConverter {
  @JvmStatic
  fun parseFloat(payload: String): Float {
    return payload.toFloat()
  }

  @JvmStatic
  fun parseBinary(payload: String): BinaryState {
    return when (payload) {
      "1" -> BinaryState.HIGH
      "0" -> BinaryState.LOW
      else -> throw Exception("Binary payload: '$payload' cannot be parsed to binary state")
    }
  }

  @JvmStatic
  fun serializeFloat(float: Float): String {
    return float.toString()
  }

  @JvmStatic
  fun serializeBinary(binaryState: BinaryState): String {
    return when (binaryState) {
      BinaryState.HIGH -> "1"
      BinaryState.LOW -> "0"
    }
  }
}
