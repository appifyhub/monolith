package com.appifyhub.monolith.init

import org.springframework.beans.factory.annotation.Value
import org.springframework.stereotype.Component

@Component
class AdminProjectConfig {

  @Value("\${app.adminProject.owner.name}")
  lateinit var ownerName: String

  @Value("\${app.adminProject.owner.secret}")
  lateinit var ownerSecret: String

  @Value("\${app.adminProject.owner.email}")
  lateinit var ownerEmail: String

  @Value("\${app.adminProject.properties.name}")
  lateinit var projectName: String

}
