package com.mqgateway.utils


import kotlin.Unit
import kotlin.jvm.functions.Function0
import org.jetbrains.annotations.NotNull
import com.mqgateway.homie.mqtt.MqttClient
import com.mqgateway.homie.mqtt.MqttClientFactory

class MqttClientFactoryStub implements MqttClientFactory {

	MqttClientStub mqttClient = new MqttClientStub()

	@Override
	MqttClient create(@NotNull String clientId, @NotNull Function0<Unit> connectedListener, @NotNull Function0<Unit> disconnectedListener) {
		return mqttClient
	}
}