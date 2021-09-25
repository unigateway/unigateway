package com.mqgateway.configuration

import com.mqgateway.core.device.DeviceRegistry
import com.mqgateway.core.gatewayconfig.Gateway
import com.mqgateway.homie.HomieDevice
import com.mqgateway.homie.gateway.GatewayHomieReceiver
import com.mqgateway.homie.gateway.GatewayHomieUpdateListener
import com.mqgateway.homie.gateway.HomieDeviceFactory
import com.mqgateway.homie.mqtt.HiveMqttClientFactory
import com.mqgateway.homie.mqtt.MqttClientFactory
import io.micronaut.context.annotation.Factory
import javax.inject.Singleton

@Factory
class HomieFactory {

  @Singleton
  fun mqttClientFactory(gateway: Gateway): MqttClientFactory = HiveMqttClientFactory(gateway.mqttHostname)

  @Singleton
  fun homieDevice(
    mqttClientFactory: MqttClientFactory,
    homieReceiver: GatewayHomieReceiver,
    mqttConnectedListeners: List<HomieDevice.MqttConnectedListener>,
    gatewayApplicationProperties: GatewayApplicationProperties,
    gatewaySystemProperties: GatewaySystemProperties,
    gateway: Gateway
  ): HomieDevice {

    val homieDevice = HomieDeviceFactory(mqttClientFactory, homieReceiver, gatewayApplicationProperties.appVersion)
      .toHomieDevice(gateway, gatewaySystemProperties.networkAdapter)

    mqttConnectedListeners.forEach { listener ->
      homieDevice.addMqttConnectedListener(listener)
    }

    return homieDevice
  }

  @Singleton
  fun homieReceiver(deviceRegistry: DeviceRegistry) = GatewayHomieReceiver(deviceRegistry)

  @Singleton
  fun gatewayHomieUpdateListener(
    homieDevice: HomieDevice
  ): GatewayHomieUpdateListener {

    return GatewayHomieUpdateListener(homieDevice)
  }
}
