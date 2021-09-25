package com.mqgateway.frontend

import io.micronaut.context.env.Environment
import io.micronaut.http.annotation.Controller
import io.micronaut.http.annotation.Get
import io.micronaut.http.server.types.files.StreamedFile
import java.util.Optional

@Controller("/ui")
class FrontendClientForwardController(private val environment: Environment) {

  /**
   * Forwards any unmapped paths (except those containing a period) to the client {@code index.html}.
   * @return forward to client {@code index.html}.
   */
  @Get("/{path:[^\\.]*}")
  fun forward(path: String): Optional<StreamedFile> {
    return environment.getResource("classpath:webapp/index.html").map { StreamedFile(it) }
  }
}
