package com.appifyhub.monolith.service.messaging

import assertk.all
import assertk.assertThat
import assertk.assertions.hasClass
import assertk.assertions.isDataClassEqualTo
import assertk.assertions.isEqualTo
import assertk.assertions.isFailure
import assertk.assertions.isSuccess
import assertk.assertions.messageContains
import com.appifyhub.monolith.TestAppifyHubApplication
import com.appifyhub.monolith.domain.common.Settable
import com.appifyhub.monolith.domain.messaging.Message
import com.appifyhub.monolith.domain.messaging.MessageTemplate
import com.appifyhub.monolith.domain.messaging.Variable.PROJECT_NAME
import com.appifyhub.monolith.domain.messaging.Variable.USER_NAME
import com.appifyhub.monolith.service.messaging.MessageTemplateService.Inputs
import com.appifyhub.monolith.util.Stubber
import com.appifyhub.monolith.util.Stubs
import com.appifyhub.monolith.util.TimeProviderFake
import com.appifyhub.monolith.util.ext.truncateTo
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

  @Test fun `adding template fails with invalid project ID`() {
    assertThat { service.addTemplate(Stubs.messageTemplateCreator.copy(projectId = -1)) }
      .isFailure()
      .all {
        hasClass(ResponseStatusException::class)
        messageContains("Project ID")
      }
  }

  @Test fun `adding template fails with invalid template name`() {
    assertThat { service.addTemplate(Stubs.messageTemplateCreator.copy(name = "\n\t")) }
      .isFailure()
      .all {
        hasClass(ResponseStatusException::class)
        messageContains("Template Name")
      }
  }

  @Test fun `adding template fails with invalid language tag`() {
    assertThat { service.addTemplate(Stubs.messageTemplateCreator.copy(languageTag = "und")) }
      .isFailure()
      .all {
        hasClass(ResponseStatusException::class)
        messageContains("Language Tag")
      }
  }

  @Test fun `adding template fails with invalid title`() {
    assertThat { service.addTemplate(Stubs.messageTemplateCreator.copy(title = "\n\t")) }
      .isFailure()
      .all {
        hasClass(ResponseStatusException::class)
        messageContains("Template Title")
      }
  }

  @Test fun `adding template fails with invalid content`() {
    assertThat { service.addTemplate(Stubs.messageTemplateCreator.copy(content = "\n\t")) }
      .isFailure()
      .all {
        hasClass(ResponseStatusException::class)
        messageContains("Template Content")
      }
  }

  @DirtiesContext(methodMode = MethodMode.AFTER_METHOD)
  @Test fun `adding template fails with an already matching name-language combination`() {
    service.addTemplate(Stubs.messageTemplateCreator)
    assertThat { service.addTemplate(Stubs.messageTemplateCreator) }
      .isFailure()
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
          id = 2,
          createdAt = timeProvider.currentDate,
          updatedAt = timeProvider.currentDate,
        )
      )
  }

  @Test fun `fetching template by ID fails with invalid ID`() {
    assertThat { service.fetchTemplateById(-1) }
      .isFailure()
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
    assertThat { service.fetchTemplatesByName(-1, "name") }
      .isFailure()
      .all {
        hasClass(ResponseStatusException::class)
        messageContains("Project ID")
      }
  }

  @Test fun `fetching template by name fails with invalid name`() {
    assertThat { service.fetchTemplatesByName(1, "") }
      .isFailure()
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
    assertThat { service.fetchTemplatesByProjectId(-1) }
      .isFailure()
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
    assertThat { service.fetchTemplatesByNameAndLanguage(-1, "name", "lang") }
      .isFailure()
      .all {
        hasClass(ResponseStatusException::class)
        messageContains("Project ID")
      }
  }

  @Test fun `fetching template by name and language fails with invalid name`() {
    assertThat { service.fetchTemplatesByNameAndLanguage(1, "", "lang") }
      .isFailure()
      .all {
        hasClass(ResponseStatusException::class)
        messageContains("Template Name")
      }
  }

  @Test fun `fetching template by name and language fails with invalid language tag`() {
    assertThat { service.fetchTemplatesByNameAndLanguage(1, "name", "asoiasjdoaisjdo") }
      .isFailure()
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
        .map { it.cleanDates() }
    )
      .isEqualTo(listOf(template.cleanDates()))
  }

  @Test fun `updating template fails with invalid template ID`() {
    assertThat { service.updateTemplate(Stubs.messageTemplateUpdater.copy(id = -1)) }
      .isFailure()
      .all {
        hasClass(ResponseStatusException::class)
        messageContains("Template ID")
      }
  }

  @Test fun `updating template fails with invalid template name`() {
    assertThat { service.updateTemplate(Stubs.messageTemplateUpdater.copy(name = Settable("\n\t"))) }
      .isFailure()
      .all {
        hasClass(ResponseStatusException::class)
        messageContains("Template Name")
      }
  }

  @Test fun `updating template fails with invalid language tag`() {
    assertThat { service.updateTemplate(Stubs.messageTemplateUpdater.copy(languageTag = Settable("und"))) }
      .isFailure()
      .all {
        hasClass(ResponseStatusException::class)
        messageContains("Language Tag")
      }
  }

  @Test fun `updating template fails with invalid title`() {
    assertThat { service.updateTemplate(Stubs.messageTemplateUpdater.copy(title = Settable("\n\t"))) }
      .isFailure()
      .all {
        hasClass(ResponseStatusException::class)
        messageContains("Template Title")
      }
  }

  @Test fun `updating template fails with invalid content`() {
    assertThat { service.updateTemplate(Stubs.messageTemplateUpdater.copy(content = Settable("\n\t"))) }
      .isFailure()
      .all {
        hasClass(ResponseStatusException::class)
        messageContains("Template Content")
      }
  }

  @Test fun `updating template fails with missing template`() {
    assertThat { service.updateTemplate(Stubs.messageTemplateUpdater) }
      .isFailure()
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
        ).cleanDates()
      )
  }

  @Test fun `deleting template fails with invalid template ID`() {
    assertThat { service.deleteTemplateById(-1) }
      .isFailure()
      .all {
        hasClass(ResponseStatusException::class)
        messageContains("Template ID")
      }
  }

  @DirtiesContext(methodMode = MethodMode.AFTER_METHOD)
  @Test fun `deleting template works`() {
    val template1 = service.addTemplate(Stubs.messageTemplateCreator)
    val template2 = service.addTemplate(Stubs.messageTemplateCreator.copy(name = "another"))

    assertThat { service.deleteTemplateById(template1.id) }
      .isSuccess()
      .all {
        assertThat { service.fetchTemplateById(template1.id) }
          .isFailure()
          .hasClass(NoSuchElementException::class)
        assertThat(service.fetchTemplateById(template2.id))
          .transform { it.id }
          .isEqualTo(template2.id)
      }
  }

  @Test fun `deleting all project templates fails with invalid project ID`() {
    assertThat { service.deleteAllTemplatesByProjectId(-1) }
      .isFailure()
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

    assertThat { service.deleteAllTemplatesByProjectId(project1.id) }
      .isSuccess()
      .all {
        assertThat { service.fetchTemplateById(template11.id) }
          .isFailure()
          .hasClass(NoSuchElementException::class)
        assertThat { service.fetchTemplateById(template12.id) }
          .isFailure()
          .hasClass(NoSuchElementException::class)
        assertThat(service.fetchTemplateById(template21.id))
          .transform { it.id }
          .isEqualTo(template21.id)
      }
  }

  @Test fun `deleting all project templates by name fails with invalid project ID`() {
    assertThat { service.deleteAllTemplatesByName(-1, "name") }
      .isFailure()
      .all {
        hasClass(ResponseStatusException::class)
        messageContains("Project ID")
      }
  }

  @Test fun `deleting all project templates by name fails with invalid name`() {
    assertThat { service.deleteAllTemplatesByName(1, "\n\t") }
      .isFailure()
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

    assertThat { service.deleteAllTemplatesByName(project.id, name = Stubs.messageTemplateCreator.name) }
      .isSuccess()
      .all {
        assertThat { service.fetchTemplateById(template1Us.id) }
          .isFailure()
          .hasClass(NoSuchElementException::class)
        assertThat { service.fetchTemplateById(template1Uk.id) }
          .isFailure()
          .hasClass(NoSuchElementException::class)
        assertThat(service.fetchTemplateById(template2.id))
          .transform { it.id }
          .isEqualTo(template2.id)
      }
  }

  @Test fun `getting variables fails with blank content`() {
    assertThat { service.detectVariables(" \n\t ") }
      .isFailure()
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
    assertThat { service.materializeById(-1, Inputs()) }
      .isFailure()
      .all {
        hasClass(ResponseStatusException::class)
        messageContains("Template ID")
      }
  }

  @Test fun `materializing template by ID fails with missing template`() {
    assertThat { service.materializeById(1, Inputs()) }
      .isFailure()
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
        )
      )
  }

  @DirtiesContext(methodMode = MethodMode.AFTER_METHOD)
  @Test fun `materializing template by ID works (with replacements)`() {
    val project = stubber.projects.new(name = "Templated Project")
    val creator = Stubs.messageTemplateCreator.copy(
      projectId = project.id,
      content = "{{${PROJECT_NAME.code}}} works",
    )
    val template = service.addTemplate(creator)
    val inputs = Inputs(projectId = project.id)

    assertThat(service.materializeById(template.id, inputs).cleanDates())
      .isDataClassEqualTo(
        Message(
          template = template.cleanDates(),
          materialized = "${project.name} works",
        )
      )
  }

  @Test fun `materializing template by name fails with invalid project ID`() {
    assertThat { service.materializeByName(-1, "name", Inputs()) }
      .isFailure()
      .all {
        hasClass(ResponseStatusException::class)
        messageContains("Project ID")
      }
  }

  @Test fun `materializing template by name fails with invalid template name`() {
    assertThat { service.materializeByName(1, "\n\t", Inputs()) }
      .isFailure()
      .all {
        hasClass(ResponseStatusException::class)
        messageContains("Template Name")
      }
  }

  @DirtiesContext(methodMode = MethodMode.AFTER_METHOD)
  @Test fun `materializing template by name fails with no templates`() {
    val project = stubber.projects.new()

    assertThat { service.materializeByName(project.id, "name", Inputs()) }
      .isFailure()
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
      )
    )
    val template = service.addTemplate(
      Stubs.messageTemplateCreator.copy(
        projectId = project.id,
        languageTag = Locale.GERMANY.toLanguageTag(),
        content = "{{${USER_NAME.code}}} works",
      )
    )

    assertThat(service.materializeByName(project.id, template.name, inputs).cleanDates())
      .isDataClassEqualTo(
        Message(
          template = template.cleanDates(),
          materialized = "${user.name} works",
        )
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
      )
    )
    val template = service.addTemplate(
      Stubs.messageTemplateCreator.copy(
        projectId = project.id,
        languageTag = Locale.GERMANY.toLanguageTag(),
        content = "{{${PROJECT_NAME.code}}} works",
      )
    )

    assertThat(service.materializeByName(project.id, template.name, inputs).cleanDates())
      .isDataClassEqualTo(
        Message(
          template = template.cleanDates(),
          materialized = "${project.name} works",
        )
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
