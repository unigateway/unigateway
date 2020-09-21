package com.mqgateway.utils


import kotlin.Unit
import kotlin.jvm.functions.Function1
import org.jetbrains.annotations.NotNull
import com.mqgateway.homie.mqtt.MqttClient
import com.mqgateway.homie.mqtt.MqttMessage

class MqttClientStub implements MqttClient {

	boolean connected = false
	MqttMessage willMessage
	boolean cleanSession

	List<MqttMessage> publishedMessages = []
	Map<String, Function1<? super MqttMessage, Unit>> subscriptions = [:]


	@Override
	void connect(@NotNull MqttMessage willMessage, boolean cleanSession) {
		this.connected = true
		this.willMessage = willMessage
		this.cleanSession = cleanSession
	}

	@Override
	void publishSync(@NotNull MqttMessage mqttMessage) {
		publishedMessages.add(mqttMessage)
	}

	@Override
	void subscribeAsync(@NotNull String topicFilter, @NotNull Function1<? super MqttMessage, Unit> callback) {
		subscriptions.put(topicFilter, callback)
	}

	@Override
	void publishAsync(@NotNull MqttMessage mqttMessage) {
		publishSync(mqttMessage)
	}

	@Override
	void disconnect() {
		connected = false
	}
}