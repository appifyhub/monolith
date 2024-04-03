package com.appifyhub.monolith.features.init.domain.model

import io.micrometer.core.instrument.MeterRegistry
import org.springframework.boot.actuate.autoconfigure.metrics.MeterRegistryCustomizer
import org.springframework.context.annotation.Bean
import org.springframework.stereotype.Component

@Component
class ApplicationMetrics(
  private val initializationConfig: InitializationConfig,
) {

  @Bean fun metricsCommonTags() = MeterRegistryCustomizer { registry: MeterRegistry ->
    registry.config().commonTags("application", initializationConfig.projectName)
  }

}
