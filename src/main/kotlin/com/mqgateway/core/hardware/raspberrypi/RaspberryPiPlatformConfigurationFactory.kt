package com.mqgateway.core.hardware.raspberrypi

class RaspberryPiPlatformConfigurationFactory {

  fun create(configMap: Map<String, *>): RaspberryPiPlatformConfiguration {
    val defaultDebounceMs: Int? = configMap[DEFAULT_DEBOUNCE_MS_KEY]?.toString()?.toInt()
    val defaultPullUpDown: PullUpDown? = configMap[DEFAULT_PULL_UP_DOWN_KEY]?.toString()?.let { PullUpDown.valueOf(it) }
    return RaspberryPiPlatformConfiguration(defaultDebounceMs ?: DEFAULT_DEBOUNCE_MS, defaultPullUpDown ?: DEFAULT_PULL_UP_DOWN)
  }

  companion object {
    private const val DEFAULT_DEBOUNCE_MS_KEY = "default-debounce-ms"
    private const val DEFAULT_DEBOUNCE_MS = 50
    private const val DEFAULT_PULL_UP_DOWN_KEY = "default-pull-up-down"
    private val DEFAULT_PULL_UP_DOWN = PullUpDown.PULL_UP
  }
}
