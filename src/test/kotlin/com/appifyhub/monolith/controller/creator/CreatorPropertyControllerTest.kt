package com.appifyhub.monolith.controller.creator

import assertk.all
import assertk.assertThat
import assertk.assertions.hasSize
import assertk.assertions.isDataClassEqualTo
import assertk.assertions.isEmpty
import assertk.assertions.isEqualTo
import assertk.assertions.isFailure
import com.appifyhub.monolith.TestAppifyHubApplication
import com.appifyhub.monolith.controller.creator.CreatorPropertyController.Endpoints.CONFIGURATIONS
import com.appifyhub.monolith.controller.creator.CreatorPropertyController.Endpoints.PROPERTIES
import com.appifyhub.monolith.controller.creator.CreatorPropertyController.Endpoints.PROPERTY
import com.appifyhub.monolith.domain.creator.property.ProjectProperty
import com.appifyhub.monolith.domain.creator.property.ProjectProperty.DESCRIPTION
import com.appifyhub.monolith.domain.creator.property.ProjectProperty.LOGO_URL
import com.appifyhub.monolith.domain.creator.property.ProjectProperty.MAX_USERS
import com.appifyhub.monolith.domain.creator.property.ProjectProperty.WEBSITE_URL
import com.appifyhub.monolith.domain.creator.property.Property
import com.appifyhub.monolith.domain.creator.property.PropertyCategory
import com.appifyhub.monolith.domain.creator.property.PropertyType
import com.appifyhub.monolith.domain.user.User.Authority.OWNER
import com.appifyhub.monolith.network.creator.property.PropertyConfigurationResponse
import com.appifyhub.monolith.network.creator.property.PropertyResponse
import com.appifyhub.monolith.network.creator.property.ops.MultiplePropertiesSaveRequest
import com.appifyhub.monolith.network.creator.property.ops.PropertyValueDto
import com.appifyhub.monolith.network.creator.property.ops.PropertyFilterQueryParams
import com.appifyhub.monolith.network.creator.property.ops.PropertySaveRequest
import com.appifyhub.monolith.network.common.MessageResponse
import com.appifyhub.monolith.network.mapper.toNetwork
import com.appifyhub.monolith.network.user.DateTimeMapper
import com.appifyhub.monolith.service.creator.PropertyService
import com.appifyhub.monolith.util.Stubber
import com.appifyhub.monolith.util.TimeProviderFake
import com.appifyhub.monolith.util.TimeProviderSystem
import com.appifyhub.monolith.util.bearerBlankRequest
import com.appifyhub.monolith.util.bearerBodyRequest
import org.junit.jupiter.api.AfterEach
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.extension.ExtendWith
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.boot.test.web.client.TestRestTemplate
import org.springframework.boot.test.web.client.exchange
import org.springframework.boot.web.server.LocalServerPort
import org.springframework.http.HttpMethod
import org.springframework.http.HttpStatus
import org.springframework.test.annotation.DirtiesContext
import org.springframework.test.context.ActiveProfiles
import org.springframework.test.context.junit.jupiter.SpringExtension

@ExtendWith(SpringExtension::class)
@ActiveProfiles(TestAppifyHubApplication.PROFILE)
@DirtiesContext(classMode = DirtiesContext.ClassMode.BEFORE_EACH_TEST_METHOD)
@SpringBootTest(
  classes = [TestAppifyHubApplication::class],
  webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT,
)
@Suppress("SpringJavaInjectionPointsAutowiringInspection") // some weird thing with restTemplate
class CreatorPropertyControllerTest {

  @Autowired lateinit var timeProvider: TimeProviderFake
  @Autowired lateinit var restTemplate: TestRestTemplate
  @Autowired lateinit var stubber: Stubber
  @Autowired lateinit var propertyService: PropertyService

  @LocalServerPort var port: Int = 0
  private val baseUrl: String by lazy { "http://localhost:$port" }

  @BeforeEach fun setup() {
    timeProvider.staticTime = { TimeProviderSystem().currentMillis }
  }

  @AfterEach fun teardown() {
    timeProvider.staticTime = { null }
  }

  @Test fun `get configurations fails when unauthorized`() {
    val project = stubber.projects.creator()

    assertThat(
      restTemplate.exchange<List<PropertyConfigurationResponse>>(
        url = "$baseUrl$CONFIGURATIONS",
        method = HttpMethod.GET,
        requestEntity = bearerBlankRequest("invalid"),
        uriVariables = mapOf(
          "projectId" to project.id,
        ),
      )
    ).all {
      transform { it.statusCode }.isEqualTo(HttpStatus.UNAUTHORIZED)
    }
  }

  @Test fun `get configurations succeeds with no filter`() {
    val project = stubber.projects.new()
    val token = stubber.tokens(project).real(OWNER).token.tokenValue

    assertThat(
      restTemplate.exchange<List<PropertyConfigurationResponse>>(
        url = "$baseUrl$CONFIGURATIONS",
        method = HttpMethod.GET,
        requestEntity = bearerBlankRequest(token),
        uriVariables = mapOf(
          "projectId" to project.id,
        ),
      )
    ).all {
      transform { it.statusCode }.isEqualTo(HttpStatus.OK)
      transform { response -> response.body!!.map { ProjectProperty.find(it.name)!! }.sorted() }
        .isEqualTo(ProjectProperty.filter().sorted())
    }
  }

  @Test fun `get configurations succeeds with filter`() {
    val project = stubber.projects.new()
    val token = stubber.tokens(project).real(OWNER).token.tokenValue
    val filter = PropertyFilterQueryParams(
      type = PropertyType.INTEGER.name,
      category = PropertyCategory.OPERATIONAL.name,
      name_contains = MAX_USERS.name,
    )

    assertThat(
      restTemplate.exchange<List<PropertyConfigurationResponse>>(
        url = "$baseUrl$CONFIGURATIONS?${filter.buildQueryTemplate()}",
        method = HttpMethod.GET,
        requestEntity = bearerBlankRequest(token),
        uriVariables = filter.buildQueryMap() + mapOf(
          "projectId" to project.id,
        ),
      )
    ).all {
      transform { it.statusCode }.isEqualTo(HttpStatus.OK)
      transform { it.body!! }.all {
        hasSize(1)
        isEqualTo(listOf(MAX_USERS.toNetwork()))
      }
    }
  }

  @Test fun `get property fails when unauthorized`() {
    val project = stubber.projects.new()

    assertThat(
      restTemplate.exchange<PropertyResponse>(
        url = "$baseUrl$PROPERTY",
        method = HttpMethod.GET,
        requestEntity = bearerBlankRequest("invalid"),
        uriVariables = mapOf(
          "projectId" to project.id,
          "propertyName" to MAX_USERS.name,
        ),
      )
    ).all {
      transform { it.statusCode }.isEqualTo(HttpStatus.UNAUTHORIZED)
    }
  }

  @Test fun `get property succeeds`() {
    val project = stubber.projects.new()
    val token = stubber.tokens(project).real(OWNER).token.tokenValue
    val prop = propertyService.saveProperty<Int>(project.id, MAX_USERS.name, "20")

    assertThat(
      restTemplate.exchange<PropertyResponse>(
        url = "$baseUrl$PROPERTY",
        method = HttpMethod.GET,
        requestEntity = bearerBlankRequest(token),
        uriVariables = mapOf(
          "projectId" to project.id,
          "propertyName" to MAX_USERS.name,
        ),
      )
    ).all {
      transform { it.statusCode }.isEqualTo(HttpStatus.OK)
      transform { it.body!! }.isDataClassEqualTo(prop.toNetwork())
    }
  }

  @Test fun `get properties fails when unauthorized`() {
    val project = stubber.projects.new()

    assertThat(
      restTemplate.exchange<List<PropertyResponse>>(
        url = "$baseUrl$PROPERTIES",
        method = HttpMethod.GET,
        requestEntity = bearerBlankRequest("invalid"),
        uriVariables = mapOf(
          "projectId" to project.id,
        ),
      )
    ).all {
      transform { it.statusCode }.isEqualTo(HttpStatus.UNAUTHORIZED)
    }
  }

  @Test fun `get properties succeeds with names`() {
    val project = stubber.projects.new()
    val token = stubber.tokens(project).real(OWNER).token.tokenValue
    val props = propertyService.saveProperties(
      projectId = project.id,
      propNames = listOf(MAX_USERS.name, DESCRIPTION.name),
      propRawValues = listOf("20", "desc"),
    )

    assertThat(
      restTemplate.exchange<List<PropertyResponse>>(
        url = "$baseUrl$PROPERTIES?names={name1}&names={name2}",
        method = HttpMethod.GET,
        requestEntity = bearerBlankRequest(token),
        uriVariables = mapOf(
          "projectId" to project.id,
          "name1" to MAX_USERS.name,
          "name2" to DESCRIPTION.name,
        ),
      )
    ).all {
      transform { it.statusCode }.isEqualTo(HttpStatus.OK)
      transform { it.body!! }.isEqualTo(props.map(Property<*>::toNetwork))
    }
  }

  @Test fun `get properties succeeds with no filter`() {
    val project = stubber.projects.new()
    val token = stubber.tokens(project).real(OWNER).token.tokenValue
    val props = propertyService.saveProperties(
      projectId = project.id,
      propNames = listOf(MAX_USERS.name, DESCRIPTION.name),
      propRawValues = listOf("20", "desc"),
    )

    assertThat(
      restTemplate.exchange<List<PropertyResponse>>(
        url = "$baseUrl$PROPERTIES",
        method = HttpMethod.GET,
        requestEntity = bearerBlankRequest(token),
        uriVariables = mapOf(
          "projectId" to project.id,
        ),
      )
    ).all {
      transform { it.statusCode }.isEqualTo(HttpStatus.OK)
      transform { it.body!! }.isEqualTo(props.map(Property<*>::toNetwork))
    }
  }

  @Test fun `get properties succeeds with filter`() {
    val project = stubber.projects.new()
    val token = stubber.tokens(project).real(OWNER).token.tokenValue
    val props = propertyService.saveProperties(
      projectId = project.id,
      propNames = listOf(MAX_USERS.name),
      propRawValues = listOf("20"),
    )
    val filter = PropertyFilterQueryParams(
      type = PropertyType.INTEGER.name,
      category = PropertyCategory.OPERATIONAL.name,
      name_contains = MAX_USERS.name,
    )

    assertThat(
      restTemplate.exchange<List<PropertyResponse>>(
        url = "$baseUrl$PROPERTIES?${filter.buildQueryTemplate()}",
        method = HttpMethod.GET,
        requestEntity = bearerBlankRequest(token),
        uriVariables = filter.buildQueryMap() + mapOf(
          "projectId" to project.id,
        ),
      )
    ).all {
      transform { it.statusCode }.isEqualTo(HttpStatus.OK)
      transform { it.body!! }.isEqualTo(props.map(Property<*>::toNetwork))
    }
  }

  @Test fun `save property fails when unauthorized`() {
    val project = stubber.projects.new()

    assertThat(
      restTemplate.exchange<PropertyResponse>(
        url = "$baseUrl$PROPERTY",
        method = HttpMethod.POST,
        requestEntity = bearerBlankRequest("invalid"),
        uriVariables = mapOf(
          "projectId" to project.id,
          "propertyName" to MAX_USERS.name,
        ),
      )
    ).all {
      transform { it.statusCode }.isEqualTo(HttpStatus.UNAUTHORIZED)
    }
  }

  @Test fun `save property succeeds`() {
    val project = stubber.projects.new()
    val token = stubber.tokens(project).real(OWNER).token.tokenValue

    assertThat(
      restTemplate.exchange<PropertyResponse>(
        url = "$baseUrl$PROPERTY",
        method = HttpMethod.POST,
        requestEntity = bearerBodyRequest(PropertySaveRequest("20"), token),
        uriVariables = mapOf(
          "projectId" to project.id,
          "propertyName" to MAX_USERS.name,
        ),
      )
    ).all {
      transform { it.statusCode }.isEqualTo(HttpStatus.OK)
      transform { it.body!! }.isDataClassEqualTo(
        PropertyResponse(
          config = MAX_USERS.toNetwork(),
          rawValue = "20",
          updatedAt = DateTimeMapper.formatAsDateTime(timeProvider.currentDate)
        )
      )
    }
  }

  @Test fun `save properties fails when unauthorized`() {
    val project = stubber.projects.new()

    assertThat(
      restTemplate.exchange<List<PropertyResponse>>(
        url = "$baseUrl$PROPERTIES",
        method = HttpMethod.POST,
        requestEntity = bearerBlankRequest("invalid"),
        uriVariables = mapOf(
          "projectId" to project.id,
        ),
      )
    ).all {
      transform { it.statusCode }.isEqualTo(HttpStatus.UNAUTHORIZED)
    }
  }

  @Test fun `save properties succeeds`() {
    val project = stubber.projects.new()
    val token = stubber.tokens(project).real(OWNER).token.tokenValue
    val values = MultiplePropertiesSaveRequest(
      properties = listOf(
        PropertyValueDto(MAX_USERS.name, "20"),
        PropertyValueDto(DESCRIPTION.name, "desc"),
      )
    )

    assertThat(
      restTemplate.exchange<List<PropertyResponse>>(
        url = "$baseUrl$PROPERTIES",
        method = HttpMethod.POST,
        requestEntity = bearerBodyRequest(values, token),
        uriVariables = mapOf(
          "projectId" to project.id,
        ),
      )
    ).all {
      transform { it.statusCode }.isEqualTo(HttpStatus.OK)
      transform { it.body!! }.isEqualTo(
        listOf(
          PropertyResponse(
            config = MAX_USERS.toNetwork(),
            rawValue = "20",
            updatedAt = DateTimeMapper.formatAsDateTime(timeProvider.currentDate)
          ),
          PropertyResponse(
            config = DESCRIPTION.toNetwork(),
            rawValue = "desc",
            updatedAt = DateTimeMapper.formatAsDateTime(timeProvider.currentDate)
          ),
        )
      )
    }
  }

  @Test fun `clear property fails when unauthorized`() {
    val project = stubber.projects.new()

    assertThat(
      restTemplate.exchange<MessageResponse>(
        url = "$baseUrl$PROPERTY",
        method = HttpMethod.DELETE,
        requestEntity = bearerBlankRequest("invalid"),
        uriVariables = mapOf(
          "projectId" to project.id,
          "propertyName" to MAX_USERS.name,
        ),
      )
    ).all {
      transform { it.statusCode }.isEqualTo(HttpStatus.UNAUTHORIZED)
    }
  }

  @Test fun `clear property succeeds`() {
    val project = stubber.projects.new()
    val token = stubber.tokens(project).real(OWNER).token.tokenValue
    propertyService.saveProperty<Int>(project.id, MAX_USERS.name, "20")

    assertThat(
      restTemplate.exchange<MessageResponse>(
        url = "$baseUrl$PROPERTY",
        method = HttpMethod.DELETE,
        requestEntity = bearerBlankRequest(token),
        uriVariables = mapOf(
          "projectId" to project.id,
          "propertyName" to MAX_USERS.name,
        ),
      )
    ).all {
      transform { it.statusCode }.isEqualTo(HttpStatus.OK)
      transform { it.body!! }.isDataClassEqualTo(MessageResponse.DONE)

      assertThat { propertyService.fetchProperty<Int>(project.id, MAX_USERS.name) }
        .isFailure()
    }
  }

  @Test fun `clear properties fails when unauthorized`() {
    val project = stubber.projects.new()

    assertThat(
      restTemplate.exchange<MessageResponse>(
        url = "$baseUrl$PROPERTIES",
        method = HttpMethod.DELETE,
        requestEntity = bearerBlankRequest("invalid"),
        uriVariables = mapOf(
          "projectId" to project.id,
        ),
      )
    ).all {
      transform { it.statusCode }.isEqualTo(HttpStatus.UNAUTHORIZED)
    }
  }

  @Test fun `clear properties succeeds with no filter`() {
    val project = stubber.projects.new()
    val token = stubber.tokens(project).real(OWNER).token.tokenValue
    propertyService.saveProperties(
      projectId = project.id,
      propNames = listOf(MAX_USERS.name, DESCRIPTION.name),
      propRawValues = listOf("20", "desc"),
    )

    assertThat(
      restTemplate.exchange<MessageResponse>(
        url = "$baseUrl$PROPERTIES",
        method = HttpMethod.DELETE,
        requestEntity = bearerBlankRequest(token),
        uriVariables = mapOf(
          "projectId" to project.id,
        ),
      )
    ).all {
      transform { it.statusCode }.isEqualTo(HttpStatus.OK)
      transform { it.body!! }.isDataClassEqualTo(MessageResponse.DONE)

      assertThat(
        propertyService.fetchProperties(project.id, listOf(MAX_USERS.name, DESCRIPTION.name))
      ).isEmpty()
    }
  }

  @Test fun `clear properties succeeds with filter`() {
    val project = stubber.projects.new()
    val token = stubber.tokens(project).real(OWNER).token.tokenValue
    propertyService.saveProperties(
      projectId = project.id,
      propNames = listOf(LOGO_URL.name, WEBSITE_URL.name),
      propRawValues = listOf("https://photo.com/1.png", "https://website.com"),
    )
    val filter = PropertyFilterQueryParams(name_contains = "URL")

    assertThat(
      restTemplate.exchange<MessageResponse>(
        url = "$baseUrl$PROPERTIES?${filter.buildQueryTemplate()}",
        method = HttpMethod.DELETE,
        requestEntity = bearerBlankRequest(token),
        uriVariables = filter.buildQueryMap() + mapOf(
          "projectId" to project.id,
        ),
      )
    ).all {
      transform { it.statusCode }.isEqualTo(HttpStatus.OK)
      transform { it.body!! }.isDataClassEqualTo(MessageResponse.DONE)

      assertThat(
        propertyService.fetchProperties(project.id, listOf(LOGO_URL.name, WEBSITE_URL.name))
      ).isEmpty()
    }
  }

  // region Helpers

  private fun PropertyFilterQueryParams.buildQueryMap(): Map<String, Any> {
    val propertyNames = javaClass.declaredFields.map { it.name }
    val result = mutableMapOf<String, Any>()
    propertyNames.forEach { fieldName ->
      javaClass.getDeclaredField(fieldName)
        .apply { isAccessible = true }
        .get(this)?.let { result[fieldName] = it }
    }
    return result
  }

  // gives something like "type={type}&category={category}" (for templating)
  private fun PropertyFilterQueryParams.buildQueryTemplate(): String =
    buildQueryMap().keys.joinToString("&") { "$it={$it}" }

  // endregion

}
