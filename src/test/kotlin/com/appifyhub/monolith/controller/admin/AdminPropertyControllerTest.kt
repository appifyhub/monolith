package com.appifyhub.monolith.controller.admin

import assertk.all
import assertk.assertThat
import assertk.assertions.hasSize
import assertk.assertions.isDataClassEqualTo
import assertk.assertions.isEmpty
import assertk.assertions.isEqualTo
import assertk.assertions.isFailure
import com.appifyhub.monolith.TestAppifyHubApplication
import com.appifyhub.monolith.controller.admin.AdminPropertyController.Endpoints.CONFIGURATIONS
import com.appifyhub.monolith.controller.admin.AdminPropertyController.Endpoints.PROPERTIES
import com.appifyhub.monolith.controller.admin.AdminPropertyController.Endpoints.PROPERTY
import com.appifyhub.monolith.domain.admin.property.Property
import com.appifyhub.monolith.domain.admin.property.PropertyCategory
import com.appifyhub.monolith.domain.admin.property.PropertyConfiguration
import com.appifyhub.monolith.domain.admin.property.PropertyConfiguration.PROJECT_DESCRIPTION
import com.appifyhub.monolith.domain.admin.property.PropertyConfiguration.PROJECT_USERS_MAX
import com.appifyhub.monolith.domain.admin.property.PropertyType
import com.appifyhub.monolith.domain.user.User.Authority.OWNER
import com.appifyhub.monolith.network.admin.property.PropertyConfigurationResponse
import com.appifyhub.monolith.network.admin.property.PropertyResponse
import com.appifyhub.monolith.network.admin.property.ops.MultiplePropertiesSaveRequest
import com.appifyhub.monolith.network.admin.property.ops.PropertyDto
import com.appifyhub.monolith.network.admin.property.ops.PropertyFilterQueryParams
import com.appifyhub.monolith.network.admin.property.ops.PropertySaveRequest
import com.appifyhub.monolith.network.common.MessageResponse
import com.appifyhub.monolith.network.mapper.toNetwork
import com.appifyhub.monolith.network.user.DateTimeMapper
import com.appifyhub.monolith.service.admin.PropertyService
import com.appifyhub.monolith.util.AuthTestHelper
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
class AdminPropertyControllerTest {

  @Autowired lateinit var timeProvider: TimeProviderFake
  @Autowired lateinit var restTemplate: TestRestTemplate
  @Autowired lateinit var authHelper: AuthTestHelper
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
    val project = authHelper.adminProject

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
    val project = authHelper.adminProject
    val token = authHelper.newRealJwt(OWNER).token.tokenValue

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
      transform { response -> response.body!!.map { PropertyConfiguration.find(it.name)!! }.sorted() }
        .isEqualTo(PropertyConfiguration.filter().sorted())
    }
  }

  @Test fun `get configurations succeeds with filter`() {
    val project = authHelper.adminProject
    val token = authHelper.newRealJwt(OWNER).token.tokenValue
    val filter = PropertyFilterQueryParams(
      type = PropertyType.INTEGER.name,
      category = PropertyCategory.OPERATIONAL.name,
      name_contains = PROJECT_USERS_MAX.name,
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
        isEqualTo(listOf(PROJECT_USERS_MAX.toNetwork()))
      }
    }
  }

  @Test fun `get property fails when unauthorized`() {
    val project = authHelper.adminProject

    assertThat(
      restTemplate.exchange<PropertyResponse>(
        url = "$baseUrl$PROPERTY",
        method = HttpMethod.GET,
        requestEntity = bearerBlankRequest("invalid"),
        uriVariables = mapOf(
          "projectId" to project.id,
          "propertyName" to PROJECT_USERS_MAX.name,
        ),
      )
    ).all {
      transform { it.statusCode }.isEqualTo(HttpStatus.UNAUTHORIZED)
    }
  }

  @Test fun `get property succeeds`() {
    val project = authHelper.adminProject
    val prop = propertyService.saveProperty<Int>(project.id, PROJECT_USERS_MAX.name, "20")
    val token = authHelper.newRealJwt(OWNER).token.tokenValue

    assertThat(
      restTemplate.exchange<PropertyResponse>(
        url = "$baseUrl$PROPERTY",
        method = HttpMethod.GET,
        requestEntity = bearerBlankRequest(token),
        uriVariables = mapOf(
          "projectId" to project.id,
          "propertyName" to PROJECT_USERS_MAX.name,
        ),
      )
    ).all {
      transform { it.statusCode }.isEqualTo(HttpStatus.OK)
      transform { it.body!! }.isDataClassEqualTo(prop.toNetwork())
    }
  }

  @Test fun `get properties fails when unauthorized`() {
    val project = authHelper.adminProject

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
    val project = authHelper.adminProject
    val props = propertyService.saveProperties(
      projectId = project.id,
      propNames = listOf(PROJECT_USERS_MAX.name, PROJECT_DESCRIPTION.name),
      propRawValues = listOf("20", "desc"),
    )
    val token = authHelper.newRealJwt(OWNER).token.tokenValue

    assertThat(
      restTemplate.exchange<List<PropertyResponse>>(
        url = "$baseUrl$PROPERTIES?names={name1}&names={name2}",
        method = HttpMethod.GET,
        requestEntity = bearerBlankRequest(token),
        uriVariables = mapOf(
          "projectId" to project.id,
          "name1" to PROJECT_USERS_MAX.name,
          "name2" to PROJECT_DESCRIPTION.name,
        ),
      )
    ).all {
      transform { it.statusCode }.isEqualTo(HttpStatus.OK)
      transform { it.body!! }.isEqualTo(props.map(Property<*>::toNetwork))
    }
  }

  @Test fun `get properties succeeds with no filter`() {
    val project = authHelper.adminProject
    val props = propertyService.saveProperties(
      projectId = project.id,
      propNames = listOf(PROJECT_USERS_MAX.name, PROJECT_DESCRIPTION.name),
      propRawValues = listOf("20", "desc"),
    )
    val token = authHelper.newRealJwt(OWNER).token.tokenValue

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
    val project = authHelper.adminProject
    val props = propertyService.saveProperties(
      projectId = project.id,
      propNames = listOf(PROJECT_USERS_MAX.name),
      propRawValues = listOf("20"),
    )
    val token = authHelper.newRealJwt(OWNER).token.tokenValue
    val filter = PropertyFilterQueryParams(
      type = PropertyType.INTEGER.name,
      category = PropertyCategory.OPERATIONAL.name,
      name_contains = PROJECT_USERS_MAX.name,
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
    val project = authHelper.adminProject

    assertThat(
      restTemplate.exchange<PropertyResponse>(
        url = "$baseUrl$PROPERTY",
        method = HttpMethod.POST,
        requestEntity = bearerBlankRequest("invalid"),
        uriVariables = mapOf(
          "projectId" to project.id,
          "propertyName" to PROJECT_USERS_MAX.name,
        ),
      )
    ).all {
      transform { it.statusCode }.isEqualTo(HttpStatus.UNAUTHORIZED)
    }
  }

  @Test fun `save property succeeds`() {
    val project = authHelper.adminProject
    val token = authHelper.newRealJwt(OWNER).token.tokenValue

    assertThat(
      restTemplate.exchange<PropertyResponse>(
        url = "$baseUrl$PROPERTY",
        method = HttpMethod.POST,
        requestEntity = bearerBodyRequest(PropertySaveRequest("20"), token),
        uriVariables = mapOf(
          "projectId" to project.id,
          "propertyName" to PROJECT_USERS_MAX.name,
        ),
      )
    ).all {
      transform { it.statusCode }.isEqualTo(HttpStatus.OK)
      transform { it.body!! }.isDataClassEqualTo(
        PropertyResponse(
          config = PROJECT_USERS_MAX.toNetwork(),
          rawValue = "20",
          updatedAt = DateTimeMapper.formatAsDateTime(timeProvider.currentDate)
        )
      )
    }
  }

  @Test fun `save properties fails when unauthorized`() {
    val project = authHelper.adminProject

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
    val project = authHelper.adminProject
    val token = authHelper.newRealJwt(OWNER).token.tokenValue
    val values = MultiplePropertiesSaveRequest(
      properties = listOf(
        PropertyDto(PROJECT_USERS_MAX.name, "20"),
        PropertyDto(PROJECT_DESCRIPTION.name, "desc"),
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
            config = PROJECT_USERS_MAX.toNetwork(),
            rawValue = "20",
            updatedAt = DateTimeMapper.formatAsDateTime(timeProvider.currentDate)
          ),
          PropertyResponse(
            config = PROJECT_DESCRIPTION.toNetwork(),
            rawValue = "desc",
            updatedAt = DateTimeMapper.formatAsDateTime(timeProvider.currentDate)
          ),
        )
      )
    }
  }

  @Test fun `clear property fails when unauthorized`() {
    val project = authHelper.adminProject

    assertThat(
      restTemplate.exchange<MessageResponse>(
        url = "$baseUrl$PROPERTY",
        method = HttpMethod.DELETE,
        requestEntity = bearerBlankRequest("invalid"),
        uriVariables = mapOf(
          "projectId" to project.id,
          "propertyName" to PROJECT_USERS_MAX.name,
        ),
      )
    ).all {
      transform { it.statusCode }.isEqualTo(HttpStatus.UNAUTHORIZED)
    }
  }

  @Test fun `clear property succeeds`() {
    val project = authHelper.adminProject
    val token = authHelper.newRealJwt(OWNER).token.tokenValue
    propertyService.saveProperty<Int>(project.id, PROJECT_USERS_MAX.name, "20")

    assertThat(
      restTemplate.exchange<MessageResponse>(
        url = "$baseUrl$PROPERTY",
        method = HttpMethod.DELETE,
        requestEntity = bearerBlankRequest(token),
        uriVariables = mapOf(
          "projectId" to project.id,
          "propertyName" to PROJECT_USERS_MAX.name,
        ),
      )
    ).all {
      transform { it.statusCode }.isEqualTo(HttpStatus.OK)
      transform { it.body!! }.isDataClassEqualTo(MessageResponse.DONE)

      assertThat { propertyService.fetchProperty<Int>(project.id, PROJECT_USERS_MAX.name) }
        .isFailure()
    }
  }

  @Test fun `clear properties fails when unauthorized`() {
    val project = authHelper.adminProject

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
    val project = authHelper.adminProject
    val token = authHelper.newRealJwt(OWNER).token.tokenValue
    propertyService.saveProperties(
      projectId = project.id,
      propNames = listOf(PROJECT_USERS_MAX.name, PROJECT_DESCRIPTION.name),
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
        propertyService.fetchProperties(project.id, listOf(PROJECT_USERS_MAX.name, PROJECT_DESCRIPTION.name))
      ).isEmpty()
    }
  }

  @Test fun `clear properties succeeds with filter`() {
    val project = authHelper.adminProject
    val token = authHelper.newRealJwt(OWNER).token.tokenValue
    propertyService.saveProperties(
      projectId = project.id,
      propNames = listOf(PROJECT_USERS_MAX.name, PROJECT_DESCRIPTION.name),
      propRawValues = listOf("20", "desc"),
    )
    val filter = PropertyFilterQueryParams(name_contains = "PROJECT")

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
        propertyService.fetchProperties(project.id, listOf(PROJECT_USERS_MAX.name, PROJECT_DESCRIPTION.name))
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
