package com.appifyhub.monolith.controller.creator

import com.appifyhub.monolith.controller.common.Endpoints
import com.appifyhub.monolith.domain.creator.messaging.MessageTemplate
import com.appifyhub.monolith.domain.creator.messaging.Variable
import com.appifyhub.monolith.domain.user.UserId
import com.appifyhub.monolith.features.auth.domain.access.AccessManager
import com.appifyhub.monolith.features.auth.domain.access.AccessManager.Privilege
import com.appifyhub.monolith.network.common.SimpleResponse
import com.appifyhub.monolith.network.creator.messaging.MessageResponse
import com.appifyhub.monolith.network.creator.messaging.MessageTemplateResponse
import com.appifyhub.monolith.network.creator.messaging.VariableResponse
import com.appifyhub.monolith.network.creator.messaging.ops.DetectVariablesRequest
import com.appifyhub.monolith.network.creator.messaging.ops.MessageInputsRequest
import com.appifyhub.monolith.network.creator.messaging.ops.MessageTemplateCreateRequest
import com.appifyhub.monolith.network.creator.messaging.ops.MessageTemplateUpdateRequest
import com.appifyhub.monolith.network.mapper.toDomain
import com.appifyhub.monolith.network.mapper.toNetwork
import com.appifyhub.monolith.service.messaging.MessageTemplateService
import com.appifyhub.monolith.service.messaging.MessageTemplateService.Inputs
import com.appifyhub.monolith.util.extension.throwNormalization
import org.slf4j.LoggerFactory
import org.springframework.security.core.Authentication
import org.springframework.web.bind.annotation.DeleteMapping
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PathVariable
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.PutMapping
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RequestParam
import org.springframework.web.bind.annotation.RestController

@RestController
class MessageTemplateController(
  private val templateService: MessageTemplateService,
  private val accessManager: AccessManager,
) {

  private val log = LoggerFactory.getLogger(this::class.java)

  @PostMapping(Endpoints.TEMPLATES)
  fun addTemplate(
    authentication: Authentication,
    @PathVariable projectId: Long,
    @RequestBody templateRequest: MessageTemplateCreateRequest,
  ): MessageTemplateResponse {
    log.debug("[POST] adding template $templateRequest in project $projectId")

    accessManager.requireProjectFunctional(projectId)
    accessManager.requestCreator(authentication, matchesId = null, requireVerified = true)
    accessManager.requestProjectAccess(authentication, projectId, Privilege.MESSAGE_TEMPLATE_WRITE)

    return templateService.addTemplate(templateRequest.toDomain(projectId)).toNetwork()
  }

  @GetMapping(Endpoints.TEMPLATE)
  fun fetchTemplateById(
    authentication: Authentication,
    @PathVariable projectId: Long,
    @PathVariable templateId: Long,
  ): MessageTemplateResponse {
    log.debug("[GET] fetching template $templateId in project $projectId")

    accessManager.requireProjectFunctional(projectId)
    accessManager.requestCreator(authentication, matchesId = null, requireVerified = true)
    accessManager.requestProjectAccess(authentication, projectId, Privilege.MESSAGE_TEMPLATE_READ)

    return templateService.fetchTemplateById(templateId).toNetwork()
  }

  @GetMapping(Endpoints.TEMPLATE_SEARCH)
  fun searchTemplates(
    authentication: Authentication,
    @PathVariable projectId: Long,
    @RequestParam("name", required = false) templateName: String? = null,
    @RequestParam("language_tag", required = false) templateLanguageTag: String? = null,
  ): List<MessageTemplateResponse> {
    log.debug("[GET] fetching templates by name $templateName/$templateLanguageTag in project $projectId")

    accessManager.requireProjectFunctional(projectId)
    accessManager.requestCreator(authentication, matchesId = null, requireVerified = true)
    accessManager.requestProjectAccess(authentication, projectId, Privilege.MESSAGE_TEMPLATE_READ)

    return when {
      templateName != null && templateLanguageTag != null ->
        templateService.fetchTemplatesByNameAndLanguage(projectId, templateName, templateLanguageTag)

      templateName != null -> templateService.fetchTemplatesByName(projectId, templateName)
      else -> templateService.fetchTemplatesByProjectId(projectId)
    }.map(MessageTemplate::toNetwork)
  }

  @PutMapping(Endpoints.TEMPLATE)
  fun updateTemplate(
    authentication: Authentication,
    @PathVariable projectId: Long,
    @PathVariable templateId: Long,
    @RequestBody request: MessageTemplateUpdateRequest,
  ): MessageTemplateResponse {
    log.debug("[PUT] updating template $templateId using $request in project $projectId")

    accessManager.requireProjectFunctional(projectId)
    accessManager.requestCreator(authentication, matchesId = null, requireVerified = true)
    accessManager.requestProjectAccess(authentication, projectId, Privilege.MESSAGE_TEMPLATE_WRITE)

    return templateService.updateTemplate(request.toDomain(templateId)).toNetwork()
  }

  @DeleteMapping(Endpoints.TEMPLATE)
  fun deleteTemplateById(
    authentication: Authentication,
    @PathVariable projectId: Long,
    @PathVariable templateId: Long,
  ): SimpleResponse {
    log.debug("[DELETE] deleting template $templateId in project $projectId")

    accessManager.requireProjectFunctional(projectId)
    accessManager.requestCreator(authentication, matchesId = null, requireVerified = true)
    accessManager.requestProjectAccess(authentication, projectId, Privilege.MESSAGE_TEMPLATE_WRITE)

    templateService.deleteTemplateById(templateId)

    return SimpleResponse.DONE
  }

  @DeleteMapping(Endpoints.TEMPLATE_SEARCH)
  fun deleteTemplates(
    authentication: Authentication,
    @PathVariable projectId: Long,
    @RequestParam("name", required = false) templateName: String? = null,
  ): SimpleResponse {
    log.debug("[DELETE] deleting templates by name $templateName in project $projectId")

    accessManager.requireProjectFunctional(projectId)
    accessManager.requestCreator(authentication, matchesId = null, requireVerified = true)
    accessManager.requestProjectAccess(authentication, projectId, Privilege.MESSAGE_TEMPLATE_WRITE)

    when {
      templateName != null -> templateService.deleteAllTemplatesByName(projectId, templateName)
      else -> templateService.deleteAllTemplatesByProjectId(projectId)
    }

    return SimpleResponse.DONE
  }

  @GetMapping(Endpoints.TEMPLATE_VARIABLES)
  fun getDefinedVariables(
    authentication: Authentication,
    @PathVariable projectId: Long,
  ): List<VariableResponse> {
    log.debug("[GET] fetching defined variables in project $projectId")

    accessManager.requireProjectFunctional(projectId)
    accessManager.requestCreator(authentication, matchesId = null, requireVerified = true)
    accessManager.requestProjectAccess(authentication, projectId, Privilege.MESSAGE_TEMPLATE_READ)

    return Variable.values().toSortedSet().map(Variable::toNetwork)
  }

  @PostMapping(Endpoints.TEMPLATE_VARIABLES)
  fun detectVariables(
    authentication: Authentication,
    @PathVariable projectId: Long,
    @RequestBody contentRequest: DetectVariablesRequest,
  ): List<VariableResponse> {
    log.debug("[POST] detecting variables within '$contentRequest' in project $projectId")

    accessManager.requireProjectFunctional(projectId)
    accessManager.requestCreator(authentication, matchesId = null, requireVerified = true)
    accessManager.requestProjectAccess(authentication, projectId, Privilege.MESSAGE_TEMPLATE_READ)

    val detected = templateService.detectVariables(contentRequest.content)
    return detected.map(Variable::toNetwork)
  }

  @PostMapping(Endpoints.TEMPLATE_MATERIALIZE)
  fun materialize(
    authentication: Authentication,
    @PathVariable projectId: Long,
    @RequestParam("id") templateId: Long? = null,
    @RequestParam("name") templateName: String? = null,
    @RequestBody inputsRequest: MessageInputsRequest? = null,
  ): MessageResponse {
    log.debug("[POST] materialize template $templateId/$templateName in project $projectId using $inputsRequest")

    accessManager.requireProjectFunctional(projectId)
    accessManager.requestCreator(authentication, matchesId = null, requireVerified = true)
    accessManager.requestProjectAccess(authentication, projectId, Privilege.MESSAGE_TEMPLATE_READ)

    // additional requirements based on inputs
    inputsRequest?.universalUserId?.let {
      accessManager.requestUserAccess(authentication, UserId.fromUniversalFormat(it), Privilege.USER_READ_DATA)
    }
    inputsRequest?.projectId?.let {
      accessManager.requestProjectAccess(authentication, it, Privilege.PROJECT_READ)
    }

    val inputs = inputsRequest?.toDomain() ?: Inputs()
    val message = when {
      templateId != null -> templateService.materializeById(templateId, inputs)
      templateName != null -> templateService.materializeByName(projectId, templateName, inputs)
      else -> throwNormalization { "One of [Template ID, Template Name] are required" }
    }

    return message.toNetwork()
  }

}
