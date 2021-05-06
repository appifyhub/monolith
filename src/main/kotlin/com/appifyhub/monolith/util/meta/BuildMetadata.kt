package com.appifyhub.monolith.util.meta

import org.springframework.beans.factory.annotation.Value
import org.springframework.context.annotation.Configuration
import org.springframework.context.annotation.PropertySource

@Configuration
@PropertySource("classpath:generated.properties")
class BuildMetadata {

  @Value("\${version}")
  lateinit var version: String

  @Value("\${quality}")
  lateinit var quality: String

}
