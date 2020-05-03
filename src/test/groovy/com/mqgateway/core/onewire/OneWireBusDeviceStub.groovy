package com.mqgateway.core.onewire

import com.mqgateway.core.onewire.device.OneWireBusDevice
import org.jetbrains.annotations.NotNull

class OneWireBusDeviceStub extends OneWireBusDevice {

	private Closure<String> returning = { return "some value" }

	void setReturnValue(Closure<String> closure) {
		returning = closure
	}


	OneWireBusDeviceStub(@NotNull String address) {
		super(address)
	}

	String deviceValueFileName$mqgateway_main() {
		return null
	}


	String deviceValueFileName$mqgateway() {
		return deviceValueFileName$mqgateway_main()
	}

	@Override
	String readValue(String masterDirPath) {
		return returning.call()
	}
}