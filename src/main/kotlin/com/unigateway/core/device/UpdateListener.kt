package com.unigateway.core.device

fun interface UpdateListener {
  fun valueUpdated(deviceId: String, propertyId: String, newValue: String)
}
