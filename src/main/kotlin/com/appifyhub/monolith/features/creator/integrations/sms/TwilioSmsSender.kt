package com.appifyhub.monolith.features.creator.integrations.sms

import com.appifyhub.monolith.features.creator.domain.model.Project
import com.appifyhub.monolith.util.extension.throwUnauthorized
import org.springframework.boot.web.client.RestTemplateBuilder
import org.springframework.http.HttpEntity
import org.springframework.http.HttpHeaders
import org.springframework.http.MediaType
import org.springframework.stereotype.Component
import org.springframework.util.Base64Utils
import org.springframework.util.LinkedMultiValueMap
import org.springframework.web.client.postForEntity
import java.net.URI
import java.nio.charset.Charset

private const val API_BASE = "https://api.twilio.com/2010-04-01/Accounts"
private const val ENDPOINT = "Messages.json"
private const val FORM_KEY_TO = "To"
private const val FORM_KEY_FROM = "From"
private const val FORM_KEY_MESSAGING_SERVICE_ID = "MessagingServiceId"
private const val FORM_KEY_BODY = "Body"
private const val FORM_KEY_MAX_PRICE = "MaxPrice"
private const val FORM_KEY_MAX_ATTEMPTS = "Attempt"

@Component
class TwilioSmsSender(
  restTemplateBuilder: RestTemplateBuilder,
) : SmsSender {

  private val restTemplate = restTemplateBuilder.build()

  override val type = SmsSender.Type.TWILIO

  override fun send(project: Project, toNumber: String, body: String) {
    if (project.twilioConfig == null) throwUnauthorized { "Twilio not configured" }

    restTemplate.postForEntity<String>(
      url = URI.create("$API_BASE/${project.twilioConfig.accountSid}/$ENDPOINT"),
      request = HttpEntity(
        // form body
        LinkedMultiValueMap<String, String>().apply {
          add(FORM_KEY_TO, toNumber)
          add(FORM_KEY_FROM, project.twilioConfig.defaultSender)
          add(FORM_KEY_MESSAGING_SERVICE_ID, project.twilioConfig.messagingServiceId)
          add(FORM_KEY_BODY, body)
          add(FORM_KEY_MAX_PRICE, project.twilioConfig.maxPricePerMessage.toString())
          add(FORM_KEY_MAX_ATTEMPTS, project.twilioConfig.maxRetryAttempts.toString())
        },
        // required headers
        HttpHeaders().apply {
          acceptCharset = listOf(Charset.defaultCharset())
          contentType = MediaType.APPLICATION_FORM_URLENCODED
          setBasicAuth(Base64Utils.encodeToString(project.twilioConfig.userAuth.encodeToByteArray()))
        },
      ),
    )
  }

}
