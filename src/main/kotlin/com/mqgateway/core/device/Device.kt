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
   * Set device property to the specific value on the initialization
   * This should be done before initDevice()
   */
  open fun initProperty(propertyId: String, value: String) {
    LOGGER.info { "Initializing of property '$id.$propertyId' not implemented." }
    // To be implemented by devices extending this class if needed
  }

  /**
   * Starts device specific initialization by calling initDevice()
   * WARNING: Add listeners before calling this method
   */
  @JvmOverloads
  fun init(listenersExpected: Boolean = true) {
    // TODO do not let to initialize twice (check initialized) - this might be problematic if complex device initialize simple,referred device
    LOGGER.info { "Initializing device type='$type' id='$id'" }
    if (listenersExpected && updateListeners.isEmpty()) {
      LOGGER.error { "No update listener registered for device id='$id'" }
    }
    initDevice()
    initialized = true
    LOGGER.trace { "Initializing device(id=$id) finished" }
  }

  /**
   * Device specific initialization
   * This initialization should happen after initProperty()
   */
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
