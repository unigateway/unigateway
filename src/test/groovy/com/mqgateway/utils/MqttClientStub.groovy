package com.mqgateway.utils

import com.mqgateway.homie.mqtt.MqttClient
import com.mqgateway.homie.mqtt.MqttMessage
import java.time.Instant
import kotlin.Unit
import kotlin.jvm.functions.Function1
import org.jetbrains.annotations.NotNull

class MqttClientStub implements MqttClient {

	boolean connected = false
	MqttMessage willMessage
	boolean cleanSession

	Instant connectionTime
	Instant disconnectionTime

	List<MqttMessage> publishedMessages = []
	Map<String, Function1<? super MqttMessage, Unit>> subscriptions = [:]


	@Override
	void connect(@NotNull MqttMessage willMessage, boolean cleanSession) {
		this.connected = true
		this.willMessage = willMessage
		this.cleanSession = cleanSession
		this.connectionTime = Instant.now()
	}

	@Override
	void publishSync(@NotNull MqttMessage mqttMessage) {
		if (!connected) throw new IllegalStateException("Trying to send MQTT message without connection")
		publishedMessages.add(mqttMessage)
	}

	@Override
	void subscribeAsync(@NotNull String topicFilter, @NotNull Function1<? super MqttMessage, Unit> callback) {
		if (!connected) throw new IllegalStateException("Trying to subscribe without connection")
		subscriptions.put(topicFilter, callback)
	}

	@Override
	void publishAsync(@NotNull MqttMessage mqttMessage) {
		if (!connected) throw new IllegalStateException("Trying to send MQTT message without connection")
		publishSync(mqttMessage)
	}

	@Override
	void disconnect() {
		connected = false
		disconnectionTime = Instant.now()
	}

	@Override
	String read(@NotNull String topic) {
		return null
	}
}