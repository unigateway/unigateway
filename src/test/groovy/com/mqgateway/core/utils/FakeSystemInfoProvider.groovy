package com.mqgateway.core.utils

import java.time.Duration

class FakeSystemInfoProvider implements SystemInfoProvider {

	float cpuTemperature
	long memoryFree
	Duration uptime = Duration.ofSeconds(0)
	String ipAddresses = ""

	@Override
	float getCpuTemperature() {
		return cpuTemperature
	}

	@Override
	long getMemoryFree() {
		return memoryFree
	}

	@Override
	Duration getUptime() {
		return uptime
	}

	@Override
	String getIPAddresses() {
		return ipAddresses
	}
}