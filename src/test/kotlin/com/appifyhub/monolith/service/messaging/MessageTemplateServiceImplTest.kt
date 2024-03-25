package com.appifyhub.monolith.service.messaging

import assertk.all
import assertk.assertAll
import assertk.assertFailure
import assertk.assertThat
import assertk.assertions.hasClass
import assertk.assertions.hasSize
import assertk.assertions.isDataClassEqualTo
import assertk.assertions.isEqualTo
import assertk.assertions.messageContains
import com.appifyhub.monolith.TestAppifyHubApplication
import com.appifyhub.monolith.domain.common.Settable
import com.appifyhub.monolith.domain.creator.messaging.Message
import com.appifyhub.monolith.domain.creator.messaging.MessageTemplate
import com.appifyhub.monolith.domain.creator.messaging.Variable
import com.appifyhub.monolith.domain.creator.messaging.Variable.PROJECT_NAME
import com.appifyhub.monolith.domain.creator.messaging.Variable.USER_NAME
import com.appifyhub.monolith.service.messaging.MessageTemplateDefaults.ProjectCreated
import com.appifyhub.monolith.service.messaging.MessageTemplateDefaults.UserAuthResetCompleted
import com.appifyhub.monolith.service.messaging.MessageTemplateDefaults.UserCreated
import com.appifyhub.monolith.service.messaging.MessageTemplateService.Inputs
import com.appifyhub.monolith.util.Stubber
import com.appifyhub.monolith.util.Stubs
import com.appifyhub.monolith.util.TimeProviderFake
import com.appifyhub.monolith.util.extension.truncateTo
import java.time.temporal.ChronoUnit
import java.util.Locale
import org.junit.jupiter.api.AfterEach
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.extension.ExtendWith
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.test.annotation.DirtiesContext
import org.springframework.test.annotation.DirtiesContext.MethodMode
import org.springframework.test.context.ActiveProfiles
import org.springframework.test.context.junit.jupiter.SpringExtension
import org.springframework.web.server.ResponseStatusException

@ExtendWith(SpringExtension::class)
@ActiveProfiles(TestAppifyHubApplication.PROFILE)
@SpringBootTest(classes = [TestAppifyHubApplication::class])
class MessageTemplateServiceImplTest {

  @Autowired lateinit var service: MessageTemplateService
  @Autowired lateinit var timeProvider: TimeProviderFake
  @Autowired lateinit var stubber: Stubber

  @BeforeEach fun setup() {
    timeProvider.staticTime = { 0 }
  }

  @AfterEach fun teardown() {
    timeProvider.staticTime = { null }
  }

  @Test fun `initializing defaults works`() {
    assertAll {
      val creatorProjectId = stubber.projects.creator().id

      assertThat(service.deleteAllTemplatesByProjectId(creatorProjectId))
        .isEqualTo(Unit)

      assertThat(service.initializeDefaults())
        .isEqualTo(Unit)

      assertThat(service.fetchTemplatesByName(projectId = creatorProjectId, name = ProjectCreated.NAME))
        .hasSize(1)
      assertThat(service.fetchTemplatesByName(projectId = creatorProjectId, name = UserCreated.NAME))
        .hasSize(1)
      assertThat(service.fetchTemplatesByName(projectId = creatorProjectId, name = UserAuthResetCompleted.NAME))
        .hasSize(1)
    }
  }

  @Test fun `adding template fails with invalid project ID`() {
    assertFailure { service.addTemplate(Stubs.messageTemplateCreator.copy(projectId = -1)) }
      .all {
        hasClass(ResponseStatusException::class)
        messageContains("Project ID")
      }
  }

  @Test fun `adding template fails with invalid template name`() {
    assertFailure { service.addTemplate(Stubs.messageTemplateCreator.copy(name = "\n\t")) }
      .all {
        hasClass(ResponseStatusException::class)
        messageContains("Template Name")
      }
  }

  @Test fun `adding template fails with invalid language tag`() {
    assertFailure { service.addTemplate(Stubs.messageTemplateCreator.copy(languageTag = "und")) }
      .all {
        hasClass(ResponseStatusException::class)
        messageContains("Language Tag")
      }
  }

  @Test fun `adding template fails with invalid title`() {
    assertFailure { service.addTemplate(Stubs.messageTemplateCreator.copy(title = "\n\t")) }
      .all {
        hasClass(ResponseStatusException::class)
        messageContains("Template Title")
      }
  }

  @Test fun `adding template fails with invalid content`() {
    assertFailure { service.addTemplate(Stubs.messageTemplateCreator.copy(content = "\n\t")) }
      .all {
        hasClass(ResponseStatusException::class)
        messageContains("Template Content")
      }
  }

  @DirtiesContext(methodMode = MethodMode.AFTER_METHOD)
  @Test fun `adding template fails with an already matching name-language combination`() {
    service.addTemplate(Stubs.messageTemplateCreator)
    assertFailure { service.addTemplate(Stubs.messageTemplateCreator) }
      .all {
        hasClass(ResponseStatusException::class)
        messageContains("Duplicate")
      }
  }

  @DirtiesContext(methodMode = MethodMode.AFTER_METHOD)
  @Test fun `adding template works with valid data`() {
    assertThat(service.addTemplate(Stubs.messageTemplateCreator))
      .isDataClassEqualTo(
        Stubs.messageTemplate.copy(
          id = 5, // there are some default templates
          createdAt = timeProvider.currentDate,
          updatedAt = timeProvider.currentDate,
        ),
      )
  }

  @Test fun `fetching template by ID fails with invalid ID`() {
    assertFailure { service.fetchTemplateById(-1) }
      .all {
        hasClass(ResponseStatusException::class)
        messageContains("Template ID")
      }
  }

  @DirtiesContext(methodMode = MethodMode.AFTER_METHOD)
  @Test fun `fetching template by ID works`() {
    val template = service.addTemplate(Stubs.messageTemplateCreator)

    assertThat(service.fetchTemplateById(template.id).cleanDates())
      .isDataClassEqualTo(template.cleanDates())
  }

  @Test fun `fetching template by name fails with invalid project ID`() {
    assertFailure { service.fetchTemplatesByName(-1, "name") }
      .all {
        hasClass(ResponseStatusException::class)
        messageContains("Project ID")
      }
  }

  @Test fun `fetching template by name fails with invalid name`() {
    assertFailure { service.fetchTemplatesByName(1, "") }
      .all {
        hasClass(ResponseStatusException::class)
        messageContains("Template Name")
      }
  }

  @DirtiesContext(methodMode = MethodMode.AFTER_METHOD)
  @Test fun `fetching template by name works`() {
    val project = stubber.projects.new()
    val template = service.addTemplate(Stubs.messageTemplateCreator.copy(projectId = project.id))

    assertThat(service.fetchTemplatesByName(project.id, template.name).map { it.cleanDates() })
      .isEqualTo(listOf(template.cleanDates()))
  }

  @Test fun `fetching template by project ID fails with invalid project ID`() {
    assertFailure { service.fetchTemplatesByProjectId(-1) }
      .all {
        hasClass(ResponseStatusException::class)
        messageContains("Project ID")
      }
  }

  @DirtiesContext(methodMode = MethodMode.AFTER_METHOD)
  @Test fun `fetching template by project ID works`() {
    val project = stubber.projects.new()
    val template = service.addTemplate(Stubs.messageTemplateCreator.copy(projectId = project.id))

    assertThat(service.fetchTemplatesByProjectId(project.id).map { it.cleanDates() })
      .isEqualTo(listOf(template.cleanDates()))
  }

  @Test fun `fetching template by name and language fails with invalid project ID`() {
    assertFailure { service.fetchTemplatesByNameAndLanguage(-1, "name", "lang") }
      .all {
        hasClass(ResponseStatusException::class)
        messageContains("Project ID")
      }
  }

  @Test fun `fetching template by name and language fails with invalid name`() {
    assertFailure { service.fetchTemplatesByNameAndLanguage(1, "", "lang") }
      .all {
        hasClass(ResponseStatusException::class)
        messageContains("Template Name")
      }
  }

  @Test fun `fetching template by name and language fails with invalid language tag`() {
    assertFailure { service.fetchTemplatesByNameAndLanguage(1, "name", "asoiasjdoaisjdo") }
      .all {
        hasClass(ResponseStatusException::class)
        messageContains("Language Tag")
      }
  }

  @DirtiesContext(methodMode = MethodMode.AFTER_METHOD)
  @Test fun `fetching template by name and language works`() {
    val project = stubber.projects.new()
    val template = service.addTemplate(Stubs.messageTemplateCreator.copy(projectId = project.id))

    assertThat(
      service.fetchTemplatesByNameAndLanguage(project.id, template.name, template.languageTag)
        .map { it.cleanDates() },
    )
      .isEqualTo(listOf(template.cleanDates()))
  }

  @Test fun `updating template fails with invalid template ID`() {
    assertFailure { service.updateTemplate(Stubs.messageTemplateUpdater.copy(id = -1)) }
      .all {
        hasClass(ResponseStatusException::class)
        messageContains("Template ID")
      }
  }

  @Test fun `updating template fails with invalid template name`() {
    assertFailure { service.updateTemplate(Stubs.messageTemplateUpdater.copy(name = Settable("\n\t"))) }
      .all {
        hasClass(ResponseStatusException::class)
        messageContains("Template Name")
      }
  }

  @Test fun `updating template fails with invalid language tag`() {
    assertFailure { service.updateTemplate(Stubs.messageTemplateUpdater.copy(languageTag = Settable("und"))) }
      .all {
        hasClass(ResponseStatusException::class)
        messageContains("Language Tag")
      }
  }

  @Test fun `updating template fails with invalid title`() {
    assertFailure { service.updateTemplate(Stubs.messageTemplateUpdater.copy(title = Settable("\n\t"))) }
      .all {
        hasClass(ResponseStatusException::class)
        messageContains("Template Title")
      }
  }

  @Test fun `updating template fails with invalid content`() {
    assertFailure { service.updateTemplate(Stubs.messageTemplateUpdater.copy(content = Settable("\n\t"))) }
      .all {
        hasClass(ResponseStatusException::class)
        messageContains("Template Content")
      }
  }

  @Test fun `updating template fails with missing template`() {
    assertFailure { service.updateTemplate(Stubs.messageTemplateUpdater) }
      .all {
        hasClass(NoSuchElementException::class)
      }
  }

  @DirtiesContext(methodMode = MethodMode.AFTER_METHOD)
  @Test fun `updating template works with valid data`() {
    val template = service.addTemplate(Stubs.messageTemplateCreator)
    val updater = Stubs.messageTemplateUpdater.copy(id = template.id)

    assertThat(service.updateTemplate(updater).cleanDates())
      .isDataClassEqualTo(
        Stubs.messageTemplateUpdated.copy(
          id = template.id,
          createdAt = template.createdAt,
          updatedAt = timeProvider.currentDate,
        ).cleanDates(),
      )
  }

  @Test fun `deleting template fails with invalid template ID`() {
    assertFailure { service.deleteTemplateById(-1) }
      .all {
        hasClass(ResponseStatusException::class)
        messageContains("Template ID")
      }
  }

  @DirtiesContext(methodMode = MethodMode.AFTER_METHOD)
  @Test fun `deleting template works`() {
    val template1 = service.addTemplate(Stubs.messageTemplateCreator)
    val template2 = service.addTemplate(Stubs.messageTemplateCreator.copy(name = "another"))

    assertAll {
      assertThat(service.deleteTemplateById(template1.id))
        .isEqualTo(Unit)
      assertFailure { service.fetchTemplateById(template1.id) }
        .hasClass(NoSuchElementException::class)
      assertThat(service.fetchTemplateById(template2.id))
        .transform { it.id }
        .isEqualTo(template2.id)
    }
  }

  @Test fun `deleting all project templates fails with invalid project ID`() {
    assertFailure { service.deleteAllTemplatesByProjectId(-1) }
      .all {
        hasClass(ResponseStatusException::class)
        messageContains("Project ID")
      }
  }

  @DirtiesContext(methodMode = MethodMode.AFTER_METHOD)
  @Test fun `deleting all project templates works`() {
    val project1 = stubber.projects.new()
    val project2 = stubber.projects.new()
    val template11 = service.addTemplate(Stubs.messageTemplateCreator.copy(projectId = project1.id))
    val template12 = service.addTemplate(Stubs.messageTemplateCreator.copy(projectId = project1.id, name = "another"))
    val template21 = service.addTemplate(Stubs.messageTemplateCreator.copy(projectId = project2.id))

    assertAll {
      assertThat(service.deleteAllTemplatesByProjectId(project1.id))
        .isEqualTo(Unit)
      assertFailure { service.fetchTemplateById(template11.id) }
        .hasClass(NoSuchElementException::class)
      assertFailure { service.fetchTemplateById(template12.id) }
        .hasClass(NoSuchElementException::class)
      assertThat(service.fetchTemplateById(template21.id))
        .transform { it.id }
        .isEqualTo(template21.id)
    }
  }

  @Test fun `deleting all project templates by name fails with invalid project ID`() {
    assertFailure { service.deleteAllTemplatesByName(-1, "name") }
      .all {
        hasClass(ResponseStatusException::class)
        messageContains("Project ID")
      }
  }

  @Test fun `deleting all project templates by name fails with invalid name`() {
    assertFailure { service.deleteAllTemplatesByName(1, "\n\t") }
      .all {
        hasClass(ResponseStatusException::class)
        messageContains("Template Name")
      }
  }

  @DirtiesContext(methodMode = MethodMode.AFTER_METHOD)
  @Test fun `deleting all project templates by name works`() {
    val project = stubber.projects.new()
    val us = Locale.US.toLanguageTag()
    val uk = Locale.UK.toLanguageTag()
    val template1Us = service.addTemplate(Stubs.messageTemplateCreator.copy(projectId = project.id, languageTag = us))
    val template1Uk = service.addTemplate(Stubs.messageTemplateCreator.copy(projectId = project.id, languageTag = uk))
    val template2 = service.addTemplate(Stubs.messageTemplateCreator.copy(projectId = project.id, name = "another"))

    assertAll {
      assertThat(service.deleteAllTemplatesByName(project.id, name = Stubs.messageTemplateCreator.name))
        .isEqualTo(Unit)
      assertFailure { service.fetchTemplateById(template1Us.id) }
        .hasClass(NoSuchElementException::class)
      assertFailure { service.fetchTemplateById(template1Uk.id) }
        .hasClass(NoSuchElementException::class)
      assertThat(service.fetchTemplateById(template2.id))
        .transform { it.id }
        .isEqualTo(template2.id)
    }
  }

  @Test fun `getting variables fails with blank content`() {
    assertFailure { service.detectVariables(" \n\t ") }
      .all {
        hasClass(ResponseStatusException::class)
        messageContains("Template Content")
      }
  }

  @Test fun `getting variables works (none detected)`() {
    assertThat(service.detectVariables("no variables in here"))
      .isEqualTo(emptySet())
  }

  @Test fun `getting variables works (valid ones detected)`() {
    assertThat(service.detectVariables("{{user.name}} + {{project.name}} + {{invalid.code}}"))
      .isEqualTo(sortedSetOf(USER_NAME, PROJECT_NAME))
  }

  @Test fun `materializing template by ID fails with invalid template ID`() {
    assertFailure { service.materializeById(-1, Inputs()) }
      .all {
        hasClass(ResponseStatusException::class)
        messageContains("Template ID")
      }
  }

  @Test fun `materializing template by ID fails with missing template`() {
    assertFailure { service.materializeById(1, Inputs()) }
      .all {
        hasClass(NoSuchElementException::class)
      }
  }

  @DirtiesContext(methodMode = MethodMode.AFTER_METHOD)
  @Test fun `materializing template by ID works (no replacements)`() {
    val project = stubber.projects.new()
    val creator = Stubs.messageTemplateCreator.copy(
      projectId = project.id,
      content = "Static content",
    )
    val template = service.addTemplate(creator)

    assertThat(service.materializeById(template.id, Inputs()).cleanDates())
      .isDataClassEqualTo(
        Message(
          template = template.cleanDates(),
          materialized = creator.content,
        ),
      )
  }

  @DirtiesContext(methodMode = MethodMode.AFTER_METHOD)
  @Test fun `materializing template by ID works (with replacements)`() {
    val project = stubber.projects.new(name = "Templated Project")
    val user = stubber.users(project).default(autoVerified = false)
    val content = Variable.values().joinToString(" # ") { "{{${it.code}}}" }

    val creator = Stubs.messageTemplateCreator.copy(
      projectId = project.id,
      content = content,
    )

    val template = service.addTemplate(creator)
    val inputs = Inputs(projectId = project.id, userId = user.id)
    val expectedContent = listOf(
      user.name,
      project.name,
      user.verificationToken,
      user.signature,
    ).joinToString(" # ")

    assertThat(service.materializeById(template.id, inputs).cleanDates())
      .isDataClassEqualTo(
        Message(
          template = template.cleanDates(),
          materialized = expectedContent,
        ),
      )
  }

  @Test fun `materializing template by name fails with invalid project ID`() {
    assertFailure { service.materializeByName(-1, "name", Inputs()) }
      .all {
        hasClass(ResponseStatusException::class)
        messageContains("Project ID")
      }
  }

  @Test fun `materializing template by name fails with invalid template name`() {
    assertFailure { service.materializeByName(1, "\n\t", Inputs()) }
      .all {
        hasClass(ResponseStatusException::class)
        messageContains("Template Name")
      }
  }

  @DirtiesContext(methodMode = MethodMode.AFTER_METHOD)
  @Test fun `materializing template by name fails with no templates`() {
    val project = stubber.projects.new()

    assertFailure { service.materializeByName(project.id, "name", Inputs()) }
      .all {
        hasClass(ResponseStatusException::class)
        messageContains("No matching templates")
      }
  }

  @DirtiesContext(methodMode = MethodMode.AFTER_METHOD)
  @Test fun `materializing template by name works (matching user's language)`() {
    val project = stubber.projects.new(name = "Templated Project")
    val user = stubber.users(project).default(language = Locale.GERMANY.toLanguageTag())
    val inputs = Inputs(userId = user.id)
    service.addTemplate(
      Stubs.messageTemplateCreator.copy(
        projectId = project.id,
        content = "Static content",
      ),
    )
    val template = service.addTemplate(
      Stubs.messageTemplateCreator.copy(
        projectId = project.id,
        languageTag = Locale.GERMANY.toLanguageTag(),
        content = "{{${USER_NAME.code}}} works",
      ),
    )

    assertThat(service.materializeByName(project.id, template.name, inputs).cleanDates())
      .isDataClassEqualTo(
        Message(
          template = template.cleanDates(),
          materialized = "${user.name} works",
        ),
      )
  }

  @DirtiesContext(methodMode = MethodMode.AFTER_METHOD)
  @Test fun `materializing template by name works (matching project's language)`() {
    val project = stubber.projects.new(name = "Templated Project", language = Locale.GERMANY.toLanguageTag())
    val inputs = Inputs(projectId = project.id)
    service.addTemplate(
      Stubs.messageTemplateCreator.copy(
        projectId = project.id,
        content = "Static content",
      ),
    )
    val template = service.addTemplate(
      Stubs.messageTemplateCreator.copy(
        projectId = project.id,
        languageTag = Locale.GERMANY.toLanguageTag(),
        content = "{{${PROJECT_NAME.code}}} works",
      ),
    )

    assertThat(service.materializeByName(project.id, template.name, inputs).cleanDates())
      .isDataClassEqualTo(
        Message(
          template = template.cleanDates(),
          materialized = "${project.name} works",
        ),
      )
  }

  // Helpers

  private fun Message.cleanDates() = copy(
    template = template.cleanDates(),
  )

  private fun MessageTemplate.cleanDates() = copy(
    createdAt = createdAt.truncateTo(ChronoUnit.SECONDS),
    updatedAt = updatedAt.truncateTo(ChronoUnit.SECONDS),
  )

}
