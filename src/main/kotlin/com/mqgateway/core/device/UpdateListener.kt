package com.mqgateway.core.device

fun interface UpdateListener {
  fun valueUpdated(deviceId: String, propertyId: String, newValue: String)
}
