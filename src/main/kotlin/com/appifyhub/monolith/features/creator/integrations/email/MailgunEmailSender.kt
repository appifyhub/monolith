package com.appifyhub.monolith.features.creator.integrations.email

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

private const val API_BASE = "https://api.mailgun.net/v3"
private const val ENDPOINT = "messages"
private const val FORM_KEY_FROM = "from"
private const val FORM_KEY_TO = "to"
private const val FORM_KEY_TITLE = "subject"
private const val FORM_KEY_TEXT = "text"
private const val FORM_KEY_HTML = "html"

@Component
class MailgunEmailSender(
  restTemplateBuilder: RestTemplateBuilder,
) : EmailSender {

  private val restTemplate = restTemplateBuilder.build()

  override val type = EmailSender.Type.MAILGUN

  override fun send(
    project: Project,
    toEmail: String,
    title: String,
    body: String,
    isHtml: Boolean,
  ) {
    if (project.mailgunConfig == null) throwUnauthorized { "Mailgun not configured" }

    restTemplate.postForEntity<String>(
      url = URI.create("$API_BASE/${project.mailgunConfig.domain}/$ENDPOINT"),
      request = HttpEntity(
        // form body
        LinkedMultiValueMap<String, String>().apply {
          add(FORM_KEY_FROM, "${project.mailgunConfig.senderName} <${project.mailgunConfig.senderEmail}>")
          add(FORM_KEY_TO, toEmail)
          add(FORM_KEY_TITLE, title)
          add(if (isHtml) FORM_KEY_HTML else FORM_KEY_TEXT, body)
        },
        // required headers
        HttpHeaders().apply {
          acceptCharset = listOf(Charset.defaultCharset())
          contentType = MediaType.APPLICATION_FORM_URLENCODED
          setBasicAuth(Base64Utils.encodeToString(project.mailgunConfig.apiKey.encodeToByteArray()))
        }
      ),
    )
  }

}
