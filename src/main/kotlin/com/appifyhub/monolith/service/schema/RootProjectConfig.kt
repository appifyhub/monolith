package com.appifyhub.monolith.service.schema

import org.springframework.beans.factory.annotation.Value
import org.springframework.stereotype.Component

@Component
class RootProjectConfig {

  @Value("\${app.seed.rootProject.name}")
  lateinit var rootProjectName: String

  @Value("\${app.seed.rootProject.owner.name}")
  lateinit var rootOwnerName: String

  @Value("\${app.seed.rootProject.owner.signature}")
  lateinit var rootOwnerSignature: String

  @Value("\${app.seed.rootProject.owner.email}")
  lateinit var rootOwnerEmail: String

}
