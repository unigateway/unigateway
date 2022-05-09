package com.mqgateway.configuration

import io.micronaut.context.annotation.ConfigurationInject
import io.micronaut.context.annotation.ConfigurationProperties
import io.micronaut.core.convert.format.MapFormat
import javax.validation.constraints.NotBlank
import javax.validation.constraints.NotNull

@ConfigurationProperties("gateway.system")
data class GatewaySystemProperties @ConfigurationInject constructor(
  @NotBlank val networkAdapter: String,
  @NotNull val platform: String,
  @MapFormat(transformation = MapFormat.MapTransformation.NESTED) val platformConfig: Map<String, Any>?,
  @NotNull val mqttHostname: String,
  @NotNull val mySensors: MySensors
) {
  @ConfigurationProperties("mysensors")
  data class MySensors @ConfigurationInject constructor(
    val enabled: Boolean = false,
    val portDescriptor: String,
    val baudRate: Int = 9600
  )
}
