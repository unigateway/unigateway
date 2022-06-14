package com.mqgateway.homie

import com.mqgateway.core.device.UpdateListenersInitializedEvent
import io.micronaut.context.event.ApplicationEventListener
import io.micronaut.runtime.event.ApplicationShutdownEvent
import java.util.EventObject

class HomieLifecycleManager(private val homieDevice: HomieDevice) : ApplicationEventListener<EventObject> {

  override fun onApplicationEvent(event: EventObject) {
    when (event) {
      is UpdateListenersInitializedEvent -> homieDevice.connect()
      is ApplicationShutdownEvent -> homieDevice.disconnect()
    }
  }

  override fun supports(event: EventObject?): Boolean {
    return event is UpdateListenersInitializedEvent || event is ApplicationShutdownEvent
  }
}
