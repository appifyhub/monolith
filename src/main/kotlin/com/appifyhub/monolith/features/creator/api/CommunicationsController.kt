package com.appifyhub.monolith.features.creator.api

import com.appifyhub.monolith.features.common.api.Endpoints
import com.appifyhub.monolith.features.user.domain.model.UserId
import com.appifyhub.monolith.features.auth.domain.access.AccessManager
import com.appifyhub.monolith.features.auth.domain.access.AccessManager.Feature
import com.appifyhub.monolith.features.auth.domain.access.AccessManager.Privilege
import com.appifyhub.monolith.features.creator.domain.service.CommunicationsService
import com.appifyhub.monolith.features.creator.domain.service.CommunicationsService.Type
import com.appifyhub.monolith.features.common.api.model.SimpleResponse
import com.appifyhub.monolith.features.user.api.model.MessageSendRequest
import com.appifyhub.monolith.util.extension.throwPreconditionFailed
import org.slf4j.LoggerFactory
import org.springframework.security.core.Authentication
import org.springframework.web.bind.annotation.PathVariable
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RestController

@RestController
class CommunicationsController(
  private val communicationsService: CommunicationsService,
  private val accessManager: AccessManager,
) {

  private val log = LoggerFactory.getLogger(this::class.java)

  @PostMapping(Endpoints.MESSAGING_SEND)
  fun sendMessage(
    authentication: Authentication,
    @PathVariable projectId: Long,
    @PathVariable universalId: String,
    @RequestBody messageSendRequest: MessageSendRequest,
  ): SimpleResponse {
    log.debug("[POST] sending a message $messageSendRequest to user $universalId in project $projectId")

    if (messageSendRequest.templateId == null && messageSendRequest.templateName == null) {
      throwPreconditionFailed { "Template name or template ID not found" }
    }

    accessManager.requireProjectFunctional(projectId)

    val type = Type.find(messageSendRequest.type)
    val feature = when (type) {
      Type.EMAIL -> Feature.EMAILS
      Type.SMS -> Feature.SMS
      Type.PUSH -> Feature.PUSH
    }
    accessManager.requireProjectFeaturesFunctional(projectId, feature)

    val userId = UserId.fromUniversalFormat(universalId)
    val user = accessManager.requestUserAccess(authentication, userId, Privilege.MESSAGE_TEMPLATE_SEND)

    if (messageSendRequest.templateId != null) {
      communicationsService.sendTo(projectId, user.id, null, messageSendRequest.templateId, type)
    } else if (messageSendRequest.templateName != null) {
      communicationsService.sendTo(projectId, user.id, null, messageSendRequest.templateName, type)
    }

    return SimpleResponse.DONE
  }

}
