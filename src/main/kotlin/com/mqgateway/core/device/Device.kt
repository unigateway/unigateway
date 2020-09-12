package com.mqgateway.core.device

import com.mqgateway.core.gatewayconfig.DevicePropertyType
import com.mqgateway.core.gatewayconfig.DeviceType
import mu.KotlinLogging

private val LOGGER = KotlinLogging.logger {}

/**
 * Add listeners before init
 */
abstract class Device(val id: String, val type: DeviceType) {

  private val updateListeners: MutableList<UpdateListener> = mutableListOf()
  private var initialized: Boolean = false

  /**
   * Add listeners before calling this method
   */
  fun init() {
    LOGGER.info { "Initializing device type='$type' id='$id'" }
    if (updateListeners.isEmpty()) {
      LOGGER.warn { "No update listener registered for device id='$id'" }
    }
    initDevice()
    initialized = true
    LOGGER.trace { "Initializing device(id=$id) finished" }
  }

  protected open fun initDevice() {
    // To be implemented by devices extending this class if needed
  }

  fun notify(propertyId: DevicePropertyType, newValue: Number) {
    notify(propertyId, newValue.toString())
  }

  fun notify(propertyId: DevicePropertyType, newValue: String) {
    LOGGER.trace { "Notifying listeners about property value change ($id.$propertyId = $newValue)" }
    updateListeners.forEach {
      it.valueUpdated(id, propertyId.toString(), newValue)
    }
  }

  fun addListener(updateListener: UpdateListener) {
    updateListeners.add(updateListener)
  }

  open fun change(propertyId: String, newValue: String) {
    throw UnsupportedStateChangeException(id, propertyId)
  }
}

class UnsupportedStateChangeException(deviceId: String, propertyId: String) : Exception("deviceId=$deviceId, propertyId=$propertyId")
