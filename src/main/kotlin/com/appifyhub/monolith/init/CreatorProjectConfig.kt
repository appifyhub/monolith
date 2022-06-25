package com.appifyhub.monolith.init

import org.springframework.beans.factory.annotation.Value
import org.springframework.stereotype.Component

@Component
class CreatorProjectConfig {

  @Value("\${app.creator.owner.name}")
  lateinit var ownerName: String

  @Value("\${app.creator.owner.secret}")
  lateinit var ownerSignature: String

  @Value("\${app.creator.owner.email}")
  lateinit var ownerEmail: String

  @Value("\${app.creator.properties.name}")
  lateinit var projectName: String

  @Value("\${app.creator.integrations.mailgun.api-key}")
  lateinit var mailgunApiKey: String

  @Value("\${app.creator.integrations.mailgun.domain}")
  lateinit var mailgunDomain: String

  @Value("\${app.creator.integrations.mailgun.sender-name}")
  lateinit var mailgunSenderName: String

  @Value("\${app.creator.integrations.mailgun.sender-email}")
  lateinit var mailgunSenderEmail: String

}
