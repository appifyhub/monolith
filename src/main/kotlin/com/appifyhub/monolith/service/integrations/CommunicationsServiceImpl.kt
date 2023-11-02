package com.appifyhub.monolith.service.integrations

import com.appifyhub.monolith.domain.creator.Project
import com.appifyhub.monolith.domain.creator.Project.UserIdType
import com.appifyhub.monolith.domain.messaging.Message
import com.appifyhub.monolith.domain.messaging.PushDevice
import com.appifyhub.monolith.domain.user.User
import com.appifyhub.monolith.domain.user.User.ContactType
import com.appifyhub.monolith.domain.user.UserId
import com.appifyhub.monolith.service.creator.CreatorService
import com.appifyhub.monolith.service.integrations.CommunicationsService.Type
import com.appifyhub.monolith.service.integrations.email.EmailSender
import com.appifyhub.monolith.service.integrations.push.PushSender
import com.appifyhub.monolith.service.integrations.sms.SmsSender
import com.appifyhub.monolith.service.messaging.MessageTemplateService
import com.appifyhub.monolith.service.messaging.MessageTemplateService.Inputs
import com.appifyhub.monolith.service.messaging.PushDeviceService
import com.appifyhub.monolith.service.user.UserService
import com.appifyhub.monolith.util.ext.requireValid
import com.appifyhub.monolith.util.ext.throwNotFound
import com.appifyhub.monolith.validation.impl.Normalizers
import org.slf4j.LoggerFactory
import org.springframework.stereotype.Service

@Service
class CommunicationsServiceImpl(
  private val creatorService: CreatorService,
  private val userService: UserService,
  private val templateService: MessageTemplateService,
  private val pushDeviceService: PushDeviceService,
  private val emailSenders: List<EmailSender>,
  private val smsSenders: List<SmsSender>,
  private val pushSenders: List<PushSender>,
) : CommunicationsService {

  private val log = LoggerFactory.getLogger(this::class.java)

  override fun sendTo(projectId: Long, userId: UserId, overrideUser: User?, templateId: Long, type: Type) {
    log.debug("Sending $type template $templateId to $userId")

    val normalizedProjectId = Normalizers.ProjectId.run(projectId).requireValid { "Project ID" }
    val normalizedUserId = Normalizers.UserId.run(userId).requireValid { "User ID" }
    val normalizedTemplateId = Normalizers.MessageTemplateId.run(templateId).requireValid { "Template ID" }

    val project = creatorService.fetchProjectById(normalizedProjectId)
    val user = userService.fetchUserByUserId(normalizedUserId)
    val inputs = Inputs(userId = user.id, projectId = project.id, overrideUser = overrideUser)
    val message = templateService.materializeById(normalizedTemplateId, inputs)

    execute(type, project, user, message)
  }

  override fun sendTo(projectId: Long, userId: UserId, overrideUser: User?, templateName: String, type: Type) {
    log.debug("Sending $type template $templateName to $userId")

    val normalizedProjectId = Normalizers.ProjectId.run(projectId).requireValid { "Project ID" }
    val normalizedUserId = Normalizers.UserId.run(userId).requireValid { "User ID" }
    val normalizedTemplateName = Normalizers.MessageTemplateName.run(templateName).requireValid { "Template Name" }

    val project = creatorService.fetchProjectById(normalizedProjectId)
    val user = userService.fetchUserByUserId(normalizedUserId)
    val inputs = Inputs(userId = user.id, projectId = project.id, overrideUser = overrideUser)
    val message = templateService.materializeByName(project.id, normalizedTemplateName, inputs)

    execute(type, project, user, message)
  }

  private fun execute(type: Type, project: Project, user: User, message: Message) = when (type) {
    Type.EMAIL -> resolveEmailSender(project).send(
      project = project,
      toEmail = resolveEmail(project, user),
      title = message.template.title,
      body = message.materialized,
      isHtml = message.template.isHtml,
    )

    Type.SMS -> resolveSmsSender(project).send(
      project = project,
      toNumber = resolvePhoneNumber(project, user),
      body = message.materialized,
    )

    Type.PUSH -> resolvePushSender(project).let { sender ->
      resolvePushDeviceIds(user).forEach { deviceId ->
        sender.send(
          project = project,
          receiverToken = deviceId,
          notification = PushSender.Notification(
            title = message.template.title,
            body = message.materialized,
            imageUrl = null,
          ),
          data = null,
        )
      }
    }
  }

  private fun resolveEmailSender(project: Project): EmailSender = when {
    project.mailgunConfig != null -> emailSenders.first { it.type == EmailSender.Type.MAILGUN }
    else -> emailSenders.first { it.type == EmailSender.Type.LOG }
  }

  private fun resolveSmsSender(project: Project): SmsSender = when {
    project.twilioConfig != null -> smsSenders.first { it.type == SmsSender.Type.TWILIO }
    else -> smsSenders.first { it.type == SmsSender.Type.LOG }
  }

  private fun resolvePushSender(project: Project): PushSender = when {
    project.firebaseConfig != null -> pushSenders.first { it.type == PushSender.Type.FIREBASE }
    else -> pushSenders.first { it.type == PushSender.Type.LOG }
  }

  private fun resolveEmail(project: Project, user: User): String = when {
    project.userIdType == UserIdType.EMAIL -> user.id.userId
    user.contactType == ContactType.EMAIL -> requireNotNull(user.contact)
    else -> throwNotFound { "Email not found" }
  }

  private fun resolvePhoneNumber(project: Project, user: User): String = when {
    project.userIdType == UserIdType.PHONE -> user.id.userId
    user.contactType == ContactType.PHONE -> requireNotNull(user.contact)
    else -> throwNotFound { "Phone number not found" }
  }

  private fun resolvePushDeviceIds(user: User): List<String> =
    pushDeviceService.fetchAllDevicesByUser(user)
      .map(PushDevice::deviceId)
      .takeIf(List<String>::isNotEmpty)
      ?: throwNotFound { "Push devices not found" }

}
