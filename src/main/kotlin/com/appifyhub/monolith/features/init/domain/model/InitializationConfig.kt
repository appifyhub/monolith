package com.appifyhub.monolith.features.init.domain.model

import org.springframework.beans.factory.annotation.Value
import org.springframework.stereotype.Component

@Component
class InitializationConfig {

  // Owner data

  @Value("\${app.creator.owner.name}")
  lateinit var ownerName: String

  @Value("\${app.creator.owner.secret}")
  lateinit var ownerSignature: String

  @Value("\${app.creator.owner.email}")
  lateinit var ownerEmail: String

  // Project data

  @Value("\${app.creator.properties.name}")
  lateinit var projectName: String

  // Integrations data

  @Value("\${app.creator.integrations.mailgun.api-key}")
  lateinit var mailgunApiKey: String

  @Value("\${app.creator.integrations.mailgun.domain}")
  lateinit var mailgunDomain: String

  @Value("\${app.creator.integrations.mailgun.sender-name}")
  lateinit var mailgunSenderName: String

  @Value("\${app.creator.integrations.mailgun.sender-email}")
  lateinit var mailgunSenderEmail: String

  @Value("\${app.creator.integrations.twilio.account-sid}")
  lateinit var twilioAccountSid: String

  @Value("\${app.creator.integrations.twilio.auth-token}")
  lateinit var twilioAuthToken: String

  @Value("\${app.creator.integrations.twilio.messaging-service-id}")
  lateinit var twilioMessagingServiceId: String

  @Value("\${app.creator.integrations.twilio.max-price-per-message}")
  lateinit var twilioMaxPricePerMessage: String

  @Value("\${app.creator.integrations.twilio.max-retry-attempts}")
  lateinit var twilioMaxRetryAttempts: String

  @Value("\${app.creator.integrations.twilio.default-sender-name}")
  lateinit var twilioDefaultSenderName: String

  @Value("\${app.creator.integrations.twilio.default-sender-number}")
  lateinit var twilioDefaultSenderNumber: String

  @Value("\${app.creator.integrations.firebase.project-name}")
  lateinit var firebaseProjectName: String

  @Value("\${app.creator.integrations.firebase.service-account-key-base64}")
  lateinit var firebaseServiceAccountKeyBase64: String

}
