package com.mqgateway.core.gatewayconfig.rest

import com.mqgateway.core.gatewayconfig.Gateway
import io.micronaut.http.HttpResponse
import io.micronaut.http.MediaType
import io.micronaut.http.annotation.Body
import io.micronaut.http.annotation.Controller
import io.micronaut.http.annotation.Get
import io.micronaut.http.annotation.Put

@Controller("/configuration/gateway", consumes = [MediaType.APPLICATION_YAML, MediaType.APPLICATION_JSON])
class GatewayConfigurationRestController(private val configurationService: GatewayConfigurationService, private val gatewayConfiguration: Gateway) {

  @Get
  fun currentConfiguration() = gatewayConfiguration

  @Put
  fun replaceConfiguration(@Body newConfigurationPayload: String): HttpResponse<GatewayConfigurationReplacementResult> {

    val result = configurationService.replaceConfiguration(newConfigurationPayload)
    return if (result.success) {
      HttpResponse.ok(result)
    } else {
      HttpResponse.badRequest(result)
    }
  }
}
