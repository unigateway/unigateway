package com.mqgateway.homie

import static com.mqgateway.homie.HomieProperty.DataType.INTEGER
import static com.mqgateway.homie.HomieProperty.Unit.NONE

import com.mqgateway.homie.mqtt.MqttClient
import com.mqgateway.homie.mqtt.MqttClientStateException
import com.mqgateway.homie.mqtt.MqttMessage
import com.mqgateway.utils.MqttClientStub
import kotlin.NotImplementedError
import org.jetbrains.annotations.NotNull
import spock.lang.Specification

class HomiePropertyTest extends Specification {

	MqttClientStub mqttClientStub = new MqttClientStub()

	void setup() {
		mqttClientStub.connect(new MqttMessage("", "", 1, true), true)
	}

	def "should read initial property value from MQTT when property is retained and value already exists"() {
		given:
		HomieProperty homieProperty = new HomieProperty("testDevId1", "nodeId1", "myUniqueProperty", "myUniqueProperty", INTEGER, "1:100", true, true, NONE)
		HomieReceiverStub homieReceiverStub = new HomieReceiverStub()
		mqttClientStub.publishSync(new MqttMessage("homie/testDevId1/nodeId1/myUniqueProperty", "41", 1, true))

		when:
		homieProperty.setup$unigateway(mqttClientStub, homieReceiverStub)
    homieProperty.initializeValue$unigateway(homieReceiverStub)

		then:
		homieReceiverStub.initializedProperties["myUniqueProperty"] == "41"
	}

  def "should not rethrow exception when MQTT message could not be sent - it has to be ignored to not terminate the thread"() {
    given:
    HomieProperty homieProperty = new HomieProperty("testDevId1", "nodeId1", "myUniqueProperty", "myUniqueProperty", INTEGER, "1:100", true, true, NONE)
    HomieReceiverStub homieReceiverStub = new HomieReceiverStub()
    MqttClient mqttClientMock = Mock(MqttClient)
    mqttClientMock.publishSync(_) >> { throw new MqttClientStateException("Faked exception", new RuntimeException("Faked exception")) }

    when:
    homieProperty.setup$unigateway(mqttClientMock, homieReceiverStub)
    homieProperty.onChange("someNewValue")

    then:
    noExceptionThrown()
  }
}

class HomieReceiverStub implements HomieReceiver {

	Map<String, String> initializedProperties = new HashMap<>()

	@Override
	void initProperty(@NotNull String nodeId, @NotNull String propertyId, @NotNull String value) {
		initializedProperties.put(propertyId, value)
	}

	@Override
	void propertySet(@NotNull String mqttTopic, @NotNull String payload) {
		throw new NotImplementedError()
	}
}
