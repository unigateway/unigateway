package com.mqgateway.utils

import org.jetbrains.annotations.NotNull
import com.mqgateway.core.device.Device
import com.mqgateway.core.device.DeviceType

class DeviceStub extends Device {

	private Map<String, String> properties = [:]

	boolean initialized = false

	DeviceStub(String id, DeviceType deviceType = DeviceType.RELAY) {
		super(id, "Name of ${id}", deviceType, [], [:])
	}

	@Override
	protected void initDevice() {
		initialized = true
	}

	@Override
	void change(@NotNull String propertyId, @NotNull String newValue) {
		properties.put(propertyId, newValue)
	}

	String getValueForProperty(String propertyId) {
		return properties[propertyId]
	}

  boolean equals(o) {
    if (this.is(o)) return true
    if (getClass() != o.class) return false

    DeviceStub that = (DeviceStub) o

    if (initialized != that.initialized) return false
    if (properties != that.properties) return false

    return true
  }

  int hashCode() {
    int result
    result = (properties != null ? properties.hashCode() : 0)
    result = 31 * result + (initialized ? 1 : 0)
    return result
  }

  @Override
  String toString() {
    return new StringJoiner(", ", DeviceStub.class.getSimpleName() + "[", "]")
      .add("properties=" + properties)
      .add("initialized=" + initialized)
      .toString();
  }
}
