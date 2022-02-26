package com.mqgateway.utils

import com.mqgateway.core.gatewayconfig.DeviceConfiguration
import com.mqgateway.core.gatewayconfig.GatewayConfiguration

class TestGatewayFactory {

  static GatewayConfiguration gateway(List<DeviceConfiguration> devices) {
    new GatewayConfiguration("1.0", "gtwName", devices)
  }
}
