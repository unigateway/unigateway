package com.mqgateway.utils

import com.mqgateway.homie.mqtt.MqttClient
import com.mqgateway.homie.mqtt.MqttMessage
import java.time.Instant
import kotlin.Unit
import kotlin.jvm.functions.Function1
import org.jetbrains.annotations.NotNull

class MqttClientStub implements MqttClient {

	MqttClientStub(List<TestMqttConnectionListener> connectionListeners = []) {
		this.connectionListeners = connectionListeners
	}
	boolean connected = false
	MqttMessage willMessage
	boolean cleanSession

	Instant connectionTime
	Instant disconnectionTime

	List<MqttMessage> publishedMessages = []
	Map<String, Function1<? super MqttMessage, Unit>> subscriptions = [:]

	List<TestMqttConnectionListener> connectionListeners


	@Override
	void connect(@NotNull MqttMessage willMessage, boolean cleanSession) {
		this.connected = true
		this.willMessage = willMessage
		this.cleanSession = cleanSession
		this.connectionTime = Instant.now()
		connectionListeners.forEach { it.onConnected() }
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
		connectionListeners.forEach { it.onDisconnected() }
	}

	@Override
	String read(@NotNull String topic) {
		return publishedMessages.find {it.retain && it.topic == topic}?.payload
	}
}

interface TestMqttConnectionListener {
	def onConnected()
	def onDisconnected()
}