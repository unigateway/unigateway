package com.mqgateway.webapi

import io.micronaut.cache.annotation.Cacheable
import io.micronaut.context.annotation.Property
import io.micronaut.core.annotation.Introspected
import io.micronaut.http.HttpHeaders
import io.micronaut.http.HttpRequest
import io.micronaut.http.client.HttpClient
import io.micronaut.http.client.annotation.Client
import javax.inject.Singleton

@Singleton
open class UpdateChecker(
  @param:Client("\${gateway.update.host}") private val updateHttpClient: HttpClient,
  @param:Property(name = "gateway.update.path") private val updatePath: String
) {

  @Cacheable(cacheNames = ["mq-gateway-latest-release"])
  open fun getLatestVersionInfo(): ReleaseInfo {
    val request = HttpRequest.GET<ReleaseInfo>(updatePath)
      .header(HttpHeaders.USER_AGENT, "Micronaut HTTP Client")
      .header(HttpHeaders.ACCEPT, "application/vnd.github.v3+json, application/json")
    val response = updateHttpClient.toBlocking().exchange(request, ReleaseInfo::class.java)
    return response.body()!!
  }
}

@Introspected
data class ReleaseInfo(val name: String, val tag_name: String, val html_url: String)