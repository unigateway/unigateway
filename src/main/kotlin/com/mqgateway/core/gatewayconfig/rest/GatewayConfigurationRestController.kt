package com.mqgateway.core.gatewayconfig.rest

import io.micronaut.http.HttpResponse
import io.micronaut.http.MediaType
import io.micronaut.http.annotation.Body
import io.micronaut.http.annotation.Controller
import io.micronaut.http.annotation.Get
import io.micronaut.http.annotation.Produces
import io.micronaut.http.annotation.Put

@Controller("/configuration/gateway", consumes = [MediaType.APPLICATION_YAML])
class GatewayConfigurationRestController(private val configurationService: GatewayConfigurationService) {

  @Produces(MediaType.TEXT_PLAIN)
  @Get
  fun currentConfiguration() = configurationService.readConfigurationFromFile()

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
