package com.mqgateway.configuration

import com.mqgateway.core.device.DeviceRegistry
import com.mqgateway.homie.HomieDevice
import com.mqgateway.homie.MqttStatusIndicator
import com.mqgateway.homie.gateway.GatewayHomieReceiver
import com.mqgateway.homie.gateway.GatewayHomieUpdateListener
import com.mqgateway.homie.gateway.HomieDeviceFactory
import com.mqgateway.homie.mqtt.HiveMqttClientFactory
import com.mqgateway.homie.mqtt.MqttClientFactory
import io.micronaut.context.annotation.Factory
import io.micronaut.context.annotation.Requires
import jakarta.inject.Singleton

@Factory
@Requires(property = "gateway.mqtt.enabled", value = "true")
class HomieFactory {

  @Singleton
  fun mqttClientFactory(mqttProperties: MqttProperties) =
    HiveMqttClientFactory(mqttProperties.hostname, mqttProperties.port, mqttProperties.username, mqttProperties.password)

  @Singleton
  fun homieListenersRegisteredEventHandler(homieDevice: HomieDevice): HomieListenersRegisteredEventListener {
    return HomieListenersRegisteredEventListener(homieDevice)
  }

  @Singleton
  fun homieApplicationShutdownEventHandler(homieDevice: HomieDevice): HomieApplicationShutdownEventListener {
    return HomieApplicationShutdownEventListener(homieDevice)
  }

  @Singleton
  fun homieDevice(
    mqttClientFactory: MqttClientFactory,
    homieReceiver: GatewayHomieReceiver,
    mqttConnectionListeners: List<HomieDevice.MqttConnectionListener>,
    gatewayApplicationProperties: GatewayApplicationProperties,
    gatewaySystemProperties: GatewaySystemProperties,
    deviceRegistry: DeviceRegistry
  ): HomieDevice {

    val homieDevice = HomieDeviceFactory(mqttClientFactory, homieReceiver, gatewayApplicationProperties.appVersion)
      .toHomieDevice(deviceRegistry, gatewaySystemProperties.networkAdapter)

    mqttConnectionListeners.forEach { listener ->
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

  @Singleton
  fun mqttStatusIndicator(): MqttStatusIndicator {
    return MqttStatusIndicator()
  }
}
