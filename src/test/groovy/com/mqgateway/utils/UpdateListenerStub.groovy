package com.mqgateway.utils


import org.jetbrains.annotations.NotNull
import com.mqgateway.core.device.UpdateListener

class UpdateListenerStub implements UpdateListener {

	List<Update> receivedUpdates = []

	@Override
	void valueUpdated(@NotNull String deviceId, @NotNull String propertyId, @NotNull String newValue) {
		receivedUpdates.add(new Update(deviceId, propertyId, newValue))
	}

	List<Update> updatesByPropertyId(@NotNull String propertyId) {
		receivedUpdates.findAll {it.propertyId == propertyId}
	}

	static final class Update {
		String deviceId
		String propertyId
		String newValue

		Update(String deviceId, String propertyId, String newValue) {
			this.deviceId = deviceId
			this.propertyId = propertyId
			this.newValue = newValue
		}

		boolean equals(o) {
			if (this.is(o)) return true
			if (getClass() != o.class) return false

			Update update = (Update) o

			if (deviceId != update.deviceId) return false
			if (newValue != update.newValue) return false
			if (propertyId != update.propertyId) return false

			return true
		}

		int hashCode() {
			int result
			result = (deviceId != null ? deviceId.hashCode() : 0)
			result = 31 * result + (propertyId != null ? propertyId.hashCode() : 0)
			result = 31 * result + (newValue != null ? newValue.hashCode() : 0)
			return result
		}

		@Override
		String toString() {
			return new StringJoiner(", ", Update.class.getSimpleName() + "[", "]")
				.add("deviceId='" + deviceId + "'")
				.add("propertyId='" + propertyId + "'")
				.add("newValue='" + newValue + "'")
				.toString()
		}
	}
}