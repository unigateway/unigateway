package com.mqgateway.configuration

import com.mqgateway.core.device.UpdateListenersRegisteredEvent
import com.mqgateway.homie.HomieDevice
import io.micronaut.context.event.ApplicationEventListener
import io.micronaut.runtime.event.ApplicationShutdownEvent
import java.util.EventObject

class HomieListenersRegisteredEventListener(private val homieDevice: HomieDevice) : ApplicationEventListener<EventObject> {
  override fun onApplicationEvent(event: EventObject) {
    homieDevice.connect()
  }

  override fun supports(event: EventObject): Boolean {
    return event is UpdateListenersRegisteredEvent
  }
}

class HomieApplicationShutdownEventListener(private val homieDevice: HomieDevice) : ApplicationEventListener<EventObject> {
  override fun onApplicationEvent(event: EventObject) {
    homieDevice.disconnect()
  }

  override fun supports(event: EventObject): Boolean {
    return event is ApplicationShutdownEvent
  }
}
