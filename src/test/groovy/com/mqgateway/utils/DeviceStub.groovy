package com.mqgateway.utils


import org.jetbrains.annotations.NotNull
import com.mqgateway.core.device.Device
import com.mqgateway.core.gatewayconfig.DeviceType

class DeviceStub extends Device {

	private Map<String, String> properties = [:]

	boolean initialized = false

	DeviceStub(@NotNull String id) {
		super(id, DeviceType.RELAY)
	}

	@Override
	protected void initDevice() {
		initialized = true
	}

	@Override
	void changeState(@NotNull String propertyId, @NotNull String newValue) {
		properties.put(propertyId, newValue)
	}

	String getValueForProperty(String propertyId) {
		return properties[propertyId]
	}
}