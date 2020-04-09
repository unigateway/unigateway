package com.mqgateway.core.device

interface UpdateListener {
  fun valueUpdated(deviceId: String, propertyId: String, newValue: String)
}