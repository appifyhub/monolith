package com.appifyhub.monolith.service.messaging

import com.appifyhub.monolith.domain.common.mapValueNonNull
import com.appifyhub.monolith.domain.creator.Project
import com.appifyhub.monolith.domain.messaging.Message
import com.appifyhub.monolith.domain.messaging.MessageTemplate
import com.appifyhub.monolith.domain.messaging.ops.MessageTemplateCreator
import com.appifyhub.monolith.domain.messaging.Variable
import com.appifyhub.monolith.domain.messaging.Variable.PROJECT_NAME
import com.appifyhub.monolith.domain.messaging.Variable.USER_NAME
import com.appifyhub.monolith.domain.messaging.ops.MessageTemplateUpdater
import com.appifyhub.monolith.domain.user.User
import com.appifyhub.monolith.repository.messaging.MessageTemplateRepository
import com.appifyhub.monolith.service.creator.CreatorService
import com.appifyhub.monolith.service.messaging.MessageTemplateService.Inputs
import com.appifyhub.monolith.service.user.UserService
import com.appifyhub.monolith.util.ext.requireValid
import com.appifyhub.monolith.util.ext.throwNormalization
import com.appifyhub.monolith.util.ext.throwNotFound
import com.appifyhub.monolith.util.ext.throwPreconditionFailed
import com.appifyhub.monolith.validation.impl.Normalizers
import org.slf4j.LoggerFactory
import org.springframework.stereotype.Component

private const val DEFAULT_VALUE = "******"
private const val VAR_PREFIX = "{{"
private const val VAR_SUFFIX = "}}"

@Component
class MessageTemplateServiceImpl(
  private val repository: MessageTemplateRepository,
  private val userService: UserService,
  private val creatorService: CreatorService,
) : MessageTemplateService {

  private data class ResolvedInputs(
    val user: User? = null,
    val project: Project? = null,
  )

  private val log = LoggerFactory.getLogger(this::class.java)

  // Storage-related

  override fun addTemplate(creator: MessageTemplateCreator): MessageTemplate {
    log.debug("Adding template using $creator")

    val normalizedProjectId = Normalizers.ProjectId.run(creator.projectId).requireValid { "Project ID" }
    val normalizedName = Normalizers.MessageTemplateName.run(creator.name).requireValid { "Template Name" }
    val normalizedTitle = Normalizers.MessageTemplate.run(creator.title).requireValid { "Template Title" }
    val normalizedContent = Normalizers.MessageTemplate.run(creator.content).requireValid { "Template Content" }
    val normalizedLanguage = Normalizers.LanguageTag.run(creator.languageTag).requireValid { "Language Tag" }
      .also { if (it == null) throwNormalization { "Language Tag" } } // mandatory in templates

    val normalizedCreator = MessageTemplateCreator(
      projectId = normalizedProjectId,
      name = normalizedName,
      languageTag = requireNotNull(normalizedLanguage),
      title = normalizedTitle,
      content = normalizedContent,
      isHtml = creator.isHtml,
    )

    // only one entry is allowed per name/language combination
    val isDuplicateEntry = repository.fetchTemplatesByNameAndLanguage(
      projectId = normalizedCreator.projectId,
      name = normalizedCreator.name,
      languageTag = normalizedCreator.languageTag,
    ).isNotEmpty()
    if (isDuplicateEntry) throwPreconditionFailed { "Duplicate entry for ${creator.name}/${creator.languageTag}" }

    return repository.addTemplate(normalizedCreator)
  }

  override fun fetchTemplateById(id: Long): MessageTemplate {
    log.debug("Fetching template by ID $id")

    val normalizedTemplateId = Normalizers.MessageTemplateId.run(id).requireValid { "Template ID" }

    return repository.fetchTemplateById(normalizedTemplateId)
  }

  override fun fetchTemplatesByName(projectId: Long, name: String): List<MessageTemplate> {
    log.debug("Fetching templates by name $name in project $projectId")
    return fetchAllByName(projectId, name)
  }

  override fun fetchTemplatesByProjectId(projectId: Long): List<MessageTemplate> {
    log.debug("Fetching templates in project $projectId")

    val normalizedProjectId = Normalizers.ProjectId.run(projectId).requireValid { "Project ID" }

    return repository.fetchTemplatesByProjectId(normalizedProjectId)
  }

  override fun fetchTemplatesByNameAndLanguage(
    projectId: Long,
    name: String,
    languageTag: String,
  ): List<MessageTemplate> {
    log.debug("Fetching templates by name $name and language $languageTag in project $projectId")

    val normalizedProjectId = Normalizers.ProjectId.run(projectId).requireValid { "Project ID" }
    val normalizedTemplateName = Normalizers.MessageTemplateName.run(name).requireValid { "Template Name" }
    val normalizedLanguageTag = Normalizers.LanguageTag.run(languageTag).requireValid { "Language Tag" }
      .also { if (it == null) throwNormalization { "Language Tag" } } // mandatory in templates

    return repository.fetchTemplatesByNameAndLanguage(
      normalizedProjectId,
      normalizedTemplateName,
      requireNotNull(normalizedLanguageTag),
    )
  }

  override fun updateTemplate(updater: MessageTemplateUpdater): MessageTemplate {
    log.debug("Updating template using $updater")

    val normalizedTemplateId = Normalizers.MessageTemplateId.run(updater.id).requireValid { "Template ID" }
    val normalizedName = updater.name?.mapValueNonNull {
      Normalizers.MessageTemplateName.run(it).requireValid { "Template Name" }
    }
    val normalizedLanguage = updater.languageTag?.mapValueNonNull { languageTag ->
      Normalizers.LanguageTag.run(languageTag).requireValid { "Language Tag" }
        .also { if (it == null) throwNormalization { "Language Tag" } } // mandatory in templates
        .let { requireNotNull(it) }
    }
    val normalizedTitle = updater.title?.mapValueNonNull {
      Normalizers.MessageTemplate.run(it).requireValid { "Template Title" }
    }
    val normalizedContent = updater.content?.mapValueNonNull {
      Normalizers.MessageTemplate.run(it).requireValid { "Template Content" }
    }

    val normalizedUpdater = MessageTemplateUpdater(
      id = normalizedTemplateId,
      name = normalizedName,
      languageTag = normalizedLanguage,
      title = normalizedTitle,
      content = normalizedContent,
      isHtml = updater.isHtml,
    )

    return repository.updateTemplate(normalizedUpdater)
  }

  override fun deleteTemplateById(id: Long) {
    log.debug("Deleting template $id")

    val normalizedTemplateId = Normalizers.MessageTemplateId.run(id).requireValid { "Template ID" }

    return repository.deleteTemplateById(normalizedTemplateId)
  }

  override fun deleteAllTemplatesByProjectId(projectId: Long) {
    log.debug("Deleting all templates in project $projectId")

    val normalizedProjectId = Normalizers.ProjectId.run(projectId).requireValid { "Project ID" }

    return repository.deleteAllTemplatesByProjectId(normalizedProjectId)
  }

  override fun deleteAllTemplatesByName(projectId: Long, name: String) {
    log.debug("Deleting all templates by name $name in project $projectId")

    val normalizedProjectId = Normalizers.ProjectId.run(projectId).requireValid { "Project ID" }
    val normalizedTemplateName = Normalizers.MessageTemplateName.run(name).requireValid { "Template Name" }

    return repository.deleteAllTemplatesByName(normalizedProjectId, normalizedTemplateName)
  }

  // Operational

  override fun detectVariables(content: String): Set<Variable> {
    log.debug("Finding variable codes in $content")

    val normalizedContent = Normalizers.MessageTemplate.run(content).requireValid { "Template Content" }

    return Variable.values()
      .filter { normalizedContent.contains(it.wrap()) }
      .toSortedSet()
  }

  override fun materializeById(templateId: Long, inputs: Inputs): Message {
    log.debug("Materializing template $templateId with $inputs")

    val normalizedTemplateId = Normalizers.MessageTemplateId.run(templateId).requireValid { "Template ID" }
    val template = repository.fetchTemplateById(normalizedTemplateId)
    val resolvedInputs = inputs.fetch()
    val materialized = template.content.replaceInputs(resolvedInputs)

    return Message(template, materialized)
  }

  override fun materializeByName(projectId: Long, templateName: String, inputs: Inputs): Message {
    log.debug("Materializing template by name $templateName in project $projectId with $inputs")

    val matchingTemplates = fetchAllByName(projectId, templateName)
    val resolvedInputs = inputs.fetch()
    val template = when {
      matchingTemplates.isEmpty() -> throwNotFound { "No matching templates" }
      else -> // language selection priority: (1) user, (2) project, (3) last updated
        matchingTemplates.firstOrNull { it.languageTag == resolvedInputs.user?.languageTag }
          ?: matchingTemplates.firstOrNull { it.languageTag == resolvedInputs.project?.languageTag }
          ?: matchingTemplates.maxByOrNull { it.updatedAt }!!
    }
    val materialized = template.content.replaceInputs(resolvedInputs)

    return Message(template, materialized)
  }

  // Helpers

  private fun fetchAllByName(projectId: Long, name: String): List<MessageTemplate> {
    val normalizedProjectId = Normalizers.ProjectId.run(projectId).requireValid { "Project ID" }
    val normalizedTemplateName = Normalizers.MessageTemplateName.run(name).requireValid { "Template Name" }

    return repository.fetchTemplatesByName(normalizedProjectId, normalizedTemplateName)
  }

  private fun Inputs.fetch(): ResolvedInputs {
    log.debug("Resolving inputs $this")

    return ResolvedInputs(
      user = userId?.let {
        val normalized = Normalizers.UserId.run(userId).requireValid { "User ID" }
        userService.fetchUserByUserId(normalized)
      },
      project = projectId?.let {
        val normalized = Normalizers.ProjectId.run(projectId).requireValid { "Project ID" }
        creatorService.fetchProjectById(normalized)
      }
    )
  }

  private fun String.replaceInputs(resolvedInputs: ResolvedInputs): String {
    var result = this
    forEachVariable { type, wrapped ->
      val value = when (type) {
        USER_NAME -> resolvedInputs.user?.name
        PROJECT_NAME -> resolvedInputs.project?.name
      } ?: DEFAULT_VALUE
      result = result.replace(wrapped, value)
    }
    return result
  }

  private inline fun forEachVariable(
    action: (variable: Variable, wrapped: String) -> Unit,
  ) = Variable.values().forEach { action(it, it.wrap()) }

  private fun Variable.wrap() = "$VAR_PREFIX$code$VAR_SUFFIX"

}
