package com.appifyhub.monolith.features.creator.domain.service

import assertk.all
import assertk.assertFailure
import assertk.assertThat
import assertk.assertions.hasClass
import assertk.assertions.hasSize
import assertk.assertions.isDataClassEqualTo
import assertk.assertions.isEqualTo
import assertk.assertions.messageContains
import com.appifyhub.monolith.TestAppifyHubApplication
import com.appifyhub.monolith.domain.user.User.ContactType
import com.appifyhub.monolith.domain.user.UserId
import com.appifyhub.monolith.features.creator.domain.model.Project
import com.appifyhub.monolith.features.creator.domain.model.messaging.MessageTemplateCreator
import com.appifyhub.monolith.features.creator.domain.model.messaging.Variable
import com.appifyhub.monolith.features.creator.domain.service.CommunicationsService.Type
import com.appifyhub.monolith.features.creator.integrations.email.LogEmailSender
import com.appifyhub.monolith.features.creator.integrations.email.LogEmailSender.SentEmail
import com.appifyhub.monolith.features.creator.integrations.push.LogPushSender
import com.appifyhub.monolith.features.creator.integrations.push.LogPushSender.SentPush
import com.appifyhub.monolith.features.creator.integrations.sms.LogSmsSender
import com.appifyhub.monolith.features.creator.integrations.sms.LogSmsSender.SentSms
import com.appifyhub.monolith.service.messaging.PushDeviceService
import com.appifyhub.monolith.util.Stubber
import com.appifyhub.monolith.util.Stubs
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.extension.ExtendWith
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.test.annotation.DirtiesContext
import org.springframework.test.annotation.DirtiesContext.ClassMode.BEFORE_EACH_TEST_METHOD
import org.springframework.test.context.ActiveProfiles
import org.springframework.test.context.junit.jupiter.SpringExtension
import org.springframework.web.server.ResponseStatusException
import java.util.Locale

@ExtendWith(SpringExtension::class)
@ActiveProfiles(TestAppifyHubApplication.PROFILE)
@SpringBootTest(classes = [TestAppifyHubApplication::class])
@DirtiesContext(classMode = BEFORE_EACH_TEST_METHOD)
class CommunicationsServiceImplTest {

  @Autowired lateinit var service: CommunicationsService
  @Autowired lateinit var templateService: MessageTemplateService
  @Autowired lateinit var pushDeviceService: PushDeviceService
  @Autowired lateinit var emailSender: LogEmailSender // always configured
  @Autowired lateinit var smsSender: LogSmsSender // always configured
  @Autowired lateinit var pushSender: LogPushSender // always configured
  @Autowired lateinit var stubber: Stubber

  @Test fun `sending anything with template ID fails with invalid project ID`() {
    assertFailure {
      service.sendTo(
        projectId = -1,
        userId = Stubs.userId,
        templateId = Stubs.messageTemplate.id,
        type = Type.SMS,
      )
    }
      .all {
        hasClass(ResponseStatusException::class)
        messageContains("Project ID")
      }
  }

  @Test fun `sending anything with template ID fails with invalid user ID`() {
    assertFailure {
      service.sendTo(
        projectId = Stubs.project.id,
        userId = UserId("", -1),
        templateId = Stubs.messageTemplate.id,
        type = Type.EMAIL,
      )
    }
      .all {
        hasClass(ResponseStatusException::class)
        messageContains("User ID")
      }
  }

  @Test fun `sending anything with template ID fails with invalid template ID`() {
    assertFailure {
      service.sendTo(
        projectId = Stubs.project.id,
        userId = Stubs.userId,
        templateId = -1,
        type = Type.SMS,
      )
    }
      .all {
        hasClass(ResponseStatusException::class)
        messageContains("Template ID")
      }
  }

  @Test fun `sending anything with template name fails with invalid project ID`() {
    assertFailure {
      service.sendTo(
        projectId = -1,
        userId = Stubs.userId,
        templateName = Stubs.messageTemplate.name,
        type = Type.EMAIL,
      )
    }
      .all {
        hasClass(ResponseStatusException::class)
        messageContains("Project ID")
      }
  }

  @Test fun `sending anything with template name fails with invalid user ID`() {
    assertFailure {
      service.sendTo(
        projectId = Stubs.project.id,
        userId = UserId("", -1),
        templateName = Stubs.messageTemplate.name,
        type = Type.SMS,
      )
    }
      .all {
        hasClass(ResponseStatusException::class)
        messageContains("User ID")
      }
  }

  @Test fun `sending anything with template name fails with invalid template ID`() {
    assertFailure {
      service.sendTo(
        projectId = Stubs.project.id,
        userId = Stubs.userId,
        templateName = "",
        type = Type.EMAIL,
      )
    }
      .all {
        hasClass(ResponseStatusException::class)
        messageContains("Template Name")
      }
  }

  @Test fun `sending email with template ID works with valid data (email as user ID)`() {
    val owner = stubber.creators.default()
    val project = stubber.projects.new(owner = owner, userIdType = Project.UserIdType.EMAIL, name = "Best")
    val user = stubber.users(project).default(idSuffix = "@domain.com")
    val template = templateService.addTemplate(
      MessageTemplateCreator(
        projectId = project.id,
        name = "template",
        languageTag = Locale.US.toLanguageTag(),
        title = "Welcome",
        content = "Welcome {{${Variable.USER_NAME.code}}} to {{${Variable.PROJECT_NAME.code}}}",
        isHtml = true,
      ),
    )

    assertThat(
      service.sendTo(
        projectId = project.id,
        userId = user.id,
        templateId = template.id,
        type = Type.EMAIL,
      ),
    ).isEqualTo(Unit)

    assertThat(emailSender)
      .all {
        transform { it.history }.hasSize(1)
        transform { it.history.first() }.isDataClassEqualTo(
          SentEmail(
            projectId = project.id,
            toEmail = "username_default@domain.com",
            title = "Welcome",
            body = "Welcome User's Name to Best",
            isHtml = true,
          ),
        )
      }
  }

  @Test fun `sending email with template name works with valid data (email as contact)`() {
    val owner = stubber.creators.default()
    val project = stubber.projects.new(owner = owner, name = "Best")
    val user = stubber.users(project).default(contactType = ContactType.EMAIL, contact = "user@domain.com")
    val template = templateService.addTemplate(
      MessageTemplateCreator(
        projectId = project.id,
        name = "template",
        languageTag = Locale.US.toLanguageTag(),
        title = "Welcome",
        content = "Welcome {{${Variable.USER_NAME.code}}} to {{${Variable.PROJECT_NAME.code}}}",
        isHtml = true,
      ),
    )

    assertThat(
      service.sendTo(
        projectId = project.id,
        userId = user.id,
        templateName = template.name,
        type = Type.EMAIL,
      ),
    ).isEqualTo(Unit)

    assertThat(emailSender)
      .all {
        transform { it.history }.hasSize(1)
        transform { it.history.first() }.isDataClassEqualTo(
          SentEmail(
            projectId = project.id,
            toEmail = "user@domain.com",
            title = "Welcome",
            body = "Welcome User's Name to Best",
            isHtml = true,
          ),
        )
      }
  }

  @Test fun `sending sms with template ID works with valid data (phone as user ID)`() {
    val owner = stubber.creators.default()
    val project = stubber.projects.new(owner = owner, userIdType = Project.UserIdType.PHONE, name = "Best")
    val user = stubber.users(project).default(idReplace = "+491746000000")
    val template = templateService.addTemplate(
      MessageTemplateCreator(
        projectId = project.id,
        name = "template",
        languageTag = Locale.US.toLanguageTag(),
        title = "Welcome",
        content = "Welcome {{${Variable.USER_NAME.code}}} to {{${Variable.PROJECT_NAME.code}}}",
        isHtml = false,
      ),
    )

    assertThat(
      service.sendTo(
        projectId = project.id,
        userId = user.id,
        templateId = template.id,
        type = Type.SMS,
      ),
    ).isEqualTo(Unit)

    assertThat(smsSender)
      .all {
        transform { it.history }.hasSize(1)
        transform { it.history.first() }.isDataClassEqualTo(
          SentSms(
            projectId = project.id,
            toNumber = "+491746000000",
            body = "Welcome User's Name to Best",
          ),
        )
      }
  }

  @Test fun `sending sms with template name works with valid data (email as contact)`() {
    val owner = stubber.creators.default()
    val project = stubber.projects.new(owner = owner, name = "Best")
    val user = stubber.users(project).default(contactType = ContactType.PHONE, contact = "+491746000000")
    val template = templateService.addTemplate(
      MessageTemplateCreator(
        projectId = project.id,
        name = "template",
        languageTag = Locale.US.toLanguageTag(),
        title = "Welcome",
        content = "Welcome {{${Variable.USER_NAME.code}}} to {{${Variable.PROJECT_NAME.code}}}",
        isHtml = true,
      ),
    )

    assertThat(
      service.sendTo(
        projectId = project.id,
        userId = user.id,
        templateName = template.name,
        type = Type.SMS,
      ),
    ).isEqualTo(Unit)

    assertThat(smsSender)
      .all {
        transform { it.history }.hasSize(1)
        transform { it.history.first() }.isDataClassEqualTo(
          SentSms(
            projectId = project.id,
            toNumber = "+491746000000",
            body = "Welcome User's Name to Best",
          ),
        )
      }
  }

  @Test fun `sending push with template ID works with valid data`() {
    val owner = stubber.creators.default()
    val project = stubber.projects.new(owner = owner, name = "Best")
    val user = stubber.users(project).default()
    val template = templateService.addTemplate(
      MessageTemplateCreator(
        projectId = project.id,
        name = "template",
        languageTag = Locale.US.toLanguageTag(),
        title = "Welcome",
        content = "Welcome {{${Variable.USER_NAME.code}}} to {{${Variable.PROJECT_NAME.code}}}",
        isHtml = false,
      ),
    )
    val pushDevice = pushDeviceService.addDevice(Stubs.pushDevice.copy(owner = user))

    assertThat(
      service.sendTo(
        projectId = project.id,
        userId = user.id,
        templateId = template.id,
        type = Type.PUSH,
      ),
    ).isEqualTo(Unit)

    assertThat(pushSender)
      .all {
        transform { it.history }.hasSize(1)
        transform { it.history.first() }.isDataClassEqualTo(
          SentPush(
            projectId = project.id,
            receiverToken = pushDevice.deviceId,
            body = "Welcome User's Name to Best",
            title = template.title,
            imageUrl = null,
            data = null,
          ),
        )
      }
  }

  @Test fun `sending push with template name works with valid data`() {
    val owner = stubber.creators.default()
    val project = stubber.projects.new(owner = owner, name = "Best")
    val user = stubber.users(project).default()
    val template = templateService.addTemplate(
      MessageTemplateCreator(
        projectId = project.id,
        name = "template",
        languageTag = Locale.US.toLanguageTag(),
        title = "Welcome",
        content = "Welcome {{${Variable.USER_NAME.code}}} to {{${Variable.PROJECT_NAME.code}}}",
        isHtml = false,
      ),
    )
    val pushDevice = pushDeviceService.addDevice(Stubs.pushDevice.copy(owner = user))

    assertThat(
      service.sendTo(
        projectId = project.id,
        userId = user.id,
        templateName = template.name,
        type = Type.PUSH,
      ),
    ).isEqualTo(Unit)

    assertThat(pushSender)
      .all {
        transform { it.history }.hasSize(1)
        transform { it.history.first() }.isDataClassEqualTo(
          SentPush(
            projectId = project.id,
            receiverToken = pushDevice.deviceId,
            body = "Welcome User's Name to Best",
            title = template.title,
            imageUrl = null,
            data = null,
          ),
        )
      }
  }

}
