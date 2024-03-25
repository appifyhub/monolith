package com.appifyhub.monolith.controller.user

import assertk.all
import assertk.assertFailure
import assertk.assertThat
import assertk.assertions.isDataClassEqualTo
import assertk.assertions.isEqualTo
import assertk.assertions.isInstanceOf
import assertk.assertions.isNull
import com.appifyhub.monolith.TestAppifyHubApplication
import com.appifyhub.monolith.controller.common.Endpoints.PROJECT_SIGNUP
import com.appifyhub.monolith.controller.common.Endpoints.PROJECT_USER_SEARCH
import com.appifyhub.monolith.controller.common.Endpoints.UNIVERSAL_USER
import com.appifyhub.monolith.controller.common.Endpoints.UNIVERSAL_USER_AUTHORITY
import com.appifyhub.monolith.controller.common.Endpoints.UNIVERSAL_USER_DATA
import com.appifyhub.monolith.controller.common.Endpoints.UNIVERSAL_USER_SIGNATURE
import com.appifyhub.monolith.controller.common.Endpoints.UNIVERSAL_USER_SIGNATURE_RESET
import com.appifyhub.monolith.controller.common.Endpoints.UNIVERSAL_USER_VERIFY
import com.appifyhub.monolith.domain.creator.Project.Status.REVIEW
import com.appifyhub.monolith.domain.user.User
import com.appifyhub.monolith.domain.user.User.Authority
import com.appifyhub.monolith.network.common.SimpleResponse
import com.appifyhub.monolith.network.user.DateTimeMapper
import com.appifyhub.monolith.network.user.UserResponse
import com.appifyhub.monolith.network.user.ops.UserUpdateAuthorityRequest
import com.appifyhub.monolith.features.auth.domain.service.AuthService
import com.appifyhub.monolith.util.Stubber
import com.appifyhub.monolith.util.Stubs
import com.appifyhub.monolith.util.TimeProviderFake
import com.appifyhub.monolith.util.TimeProviderSystem
import com.appifyhub.monolith.util.bearerBodyRequest
import com.appifyhub.monolith.util.bearerEmptyRequest
import com.appifyhub.monolith.util.bodyRequest
import com.appifyhub.monolith.util.emptyRequest
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
import org.springframework.test.annotation.DirtiesContext.ClassMode.BEFORE_EACH_TEST_METHOD
import org.springframework.test.context.ActiveProfiles
import org.springframework.test.context.junit.jupiter.SpringExtension

@ExtendWith(SpringExtension::class)
@ActiveProfiles(TestAppifyHubApplication.PROFILE)
@DirtiesContext(classMode = BEFORE_EACH_TEST_METHOD)
@SpringBootTest(
  classes = [TestAppifyHubApplication::class],
  webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT,
)
class UserControllerTest {

  @Autowired lateinit var timeProvider: TimeProviderFake
  @Autowired lateinit var restTemplate: TestRestTemplate
  @Autowired lateinit var stubber: Stubber
  @Autowired lateinit var authService: AuthService

  @LocalServerPort var port: Int = 0
  private val baseUrl: String by lazy { "http://localhost:$port" }

  @BeforeEach fun setup() {
    timeProvider.staticTime = { TimeProviderSystem().currentMillis }
  }

  @AfterEach fun teardown() {
    timeProvider.staticTime = { null }
  }

  // region Add User

  @Test fun `add user fails when project non-functional`() {
    val project = stubber.projects.new(status = REVIEW)

    assertThat(
      restTemplate.exchange<SimpleResponse>(
        url = "$baseUrl$PROJECT_SIGNUP",
        method = HttpMethod.POST,
        requestEntity = bodyRequest(Stubs.userSignupRequest),
        uriVariables = mapOf("projectId" to project.id),
      ),
    ).all {
      transform { it.statusCode }.isEqualTo(HttpStatus.PRECONDITION_REQUIRED)
    }
  }

  @Test fun `add user succeeds`() {
    val project = stubber.projects.new(activateNow = true)

    assertThat(
      restTemplate.exchange<UserResponse>(
        url = "$baseUrl$PROJECT_SIGNUP",
        method = HttpMethod.POST,
        requestEntity = bodyRequest(Stubs.userSignupRequest),
        uriVariables = mapOf("projectId" to project.id),
      ),
    ).all {
      transform { it.statusCode }.isEqualTo(HttpStatus.OK)
      transform { it.body!! }.isDataClassEqualTo(
        Stubs.userResponse.copy(
          userId = Stubs.userId.userId,
          projectId = project.id,
          universalId = Stubs.userId.copy(projectId = project.id).toUniversalFormat(),
          authority = Authority.DEFAULT.name,
          createdAt = DateTimeMapper.formatAsDateTime(timeProvider.currentDate),
          updatedAt = DateTimeMapper.formatAsDateTime(timeProvider.currentDate),
        ),
      )
    }
  }

  // endregion

  // region Get User

  @Test fun `get user fails when unauthorized`() {
    val project = stubber.projects.new()
    val universalId = stubber.users(project).default().id.toUniversalFormat()

    assertThat(
      restTemplate.exchange<SimpleResponse>(
        url = "$baseUrl$UNIVERSAL_USER",
        method = HttpMethod.GET,
        requestEntity = bearerEmptyRequest("invalid"),
        uriVariables = mapOf("universalId" to universalId),
      ),
    ).all {
      transform { it.statusCode }.isEqualTo(HttpStatus.UNAUTHORIZED)
    }
  }

  @Test fun `get user fails when project non-functional`() {
    val project = stubber.projects.new(status = REVIEW)
    val user = stubber.users(project).default()
    val token = stubber.tokens(user).real().token.tokenValue

    assertThat(
      restTemplate.exchange<SimpleResponse>(
        url = "$baseUrl$UNIVERSAL_USER",
        method = HttpMethod.GET,
        requestEntity = bearerEmptyRequest(token),
        uriVariables = mapOf("universalId" to user.id.toUniversalFormat()),
      ),
    ).all {
      transform { it.statusCode }.isEqualTo(HttpStatus.PRECONDITION_REQUIRED)
    }
  }

  @Test fun `get user succeeds with valid authorization`() {
    val project = stubber.projects.new(activateNow = true)
    val self = stubber.users(project).default()
    val universalId = self.id.toUniversalFormat()
    val token = stubber.tokens(self).real().token.tokenValue

    assertThat(
      restTemplate.exchange<UserResponse>(
        url = "$baseUrl$UNIVERSAL_USER",
        method = HttpMethod.GET,
        requestEntity = bearerEmptyRequest(token),
        uriVariables = mapOf("universalId" to universalId),
      ),
    ).all {
      transform { it.statusCode }.isEqualTo(HttpStatus.OK)
      transform { it.body!! }.isDataClassEqualTo(
        Stubs.userResponse.copy(
          userId = self.id.userId,
          projectId = project.id,
          universalId = universalId,
          type = self.type.name,
          authority = self.authority.name,
          birthday = DateTimeMapper.formatAsDate(self.birthday!!),
          createdAt = DateTimeMapper.formatAsDateTime(timeProvider.currentDate),
          updatedAt = DateTimeMapper.formatAsDateTime(timeProvider.currentDate),
        ),
      )
    }
  }

  // endregion

  // region Search User

  @Test fun `search user fails when unauthorized`() {
    val project = stubber.projects.new()

    assertThat(
      restTemplate.exchange<SimpleResponse>(
        url = "$baseUrl$PROJECT_USER_SEARCH?user_name={userName}",
        method = HttpMethod.GET,
        requestEntity = bearerEmptyRequest("invalid"),
        uriVariables = mapOf(
          "projectId" to project.id,
          "userName" to "whatever",
        ),
      ),
    ).all {
      transform { it.statusCode }.isEqualTo(HttpStatus.UNAUTHORIZED)
    }
  }

  @Test fun `search user fails when project non-functional`() {
    val project = stubber.projects.new(status = REVIEW)
    val admin = stubber.users(project).admin()
    val token = stubber.tokens(admin).real().token.tokenValue

    assertThat(
      restTemplate.exchange<SimpleResponse>(
        url = "$baseUrl$PROJECT_USER_SEARCH?user_name={userName}",
        method = HttpMethod.GET,
        requestEntity = bearerEmptyRequest(token),
        uriVariables = mapOf(
          "projectId" to project.id,
          "userName" to "whatever",
        ),
      ),
    ).all {
      transform { it.statusCode }.isEqualTo(HttpStatus.PRECONDITION_REQUIRED)
    }
  }

  @Test fun `search user fails when no query is provided`() {
    val project = stubber.projects.new(activateNow = true)
    val admin = stubber.users(project).admin()
    val token = stubber.tokens(admin).real().token.tokenValue

    assertThat(
      restTemplate.exchange<SimpleResponse>(
        url = "$baseUrl$PROJECT_USER_SEARCH",
        method = HttpMethod.GET,
        requestEntity = bearerEmptyRequest(token),
        uriVariables = mapOf("projectId" to project.id),
      ),
    ).all {
      transform { it.statusCode }.isEqualTo(HttpStatus.NOT_FOUND)
    }
  }

  @Test fun `search user succeeds with valid authorization and user name`() {
    val project = stubber.projects.new(activateNow = true)
    val admin = stubber.users(project).admin()
    val token = stubber.tokens(admin).real().token.tokenValue

    assertThat(
      restTemplate.exchange<List<UserResponse>>(
        url = "$baseUrl$PROJECT_USER_SEARCH?user_name={userName}",
        method = HttpMethod.GET,
        requestEntity = bearerEmptyRequest(token),
        uriVariables = mapOf(
          "projectId" to project.id,
          "userName" to admin.name,
        ),
      ),
    ).all {
      transform { it.statusCode }.isEqualTo(HttpStatus.OK)
      transform { it.body!!.first() }.isDataClassEqualTo(
        Stubs.userResponse.copy(
          userId = admin.id.userId,
          projectId = project.id,
          universalId = admin.id.toUniversalFormat(),
          type = admin.type.name,
          authority = admin.authority.name,
          birthday = DateTimeMapper.formatAsDate(admin.birthday!!),
          createdAt = DateTimeMapper.formatAsDateTime(timeProvider.currentDate),
          updatedAt = DateTimeMapper.formatAsDateTime(timeProvider.currentDate),
        ),
      )
    }
  }

  @Test fun `search user succeeds with valid authorization and user contact`() {
    val project = stubber.projects.new(activateNow = true)
    val admin = stubber.users(project).admin()
    val token = stubber.tokens(admin).real().token.tokenValue

    assertThat(
      restTemplate.exchange<List<UserResponse>>(
        url = "$baseUrl$PROJECT_USER_SEARCH?user_contact={userContact}",
        method = HttpMethod.GET,
        requestEntity = bearerEmptyRequest(token),
        uriVariables = mapOf(
          "projectId" to project.id,
          "userContact" to admin.contact,
        ),
      ),
    ).all {
      transform { it.statusCode }.isEqualTo(HttpStatus.OK)
      transform { it.body!!.first() }.isDataClassEqualTo(
        Stubs.userResponse.copy(
          userId = admin.id.userId,
          projectId = project.id,
          universalId = admin.id.toUniversalFormat(),
          type = admin.type.name,
          authority = admin.authority.name,
          birthday = DateTimeMapper.formatAsDate(admin.birthday!!),
          createdAt = DateTimeMapper.formatAsDateTime(timeProvider.currentDate),
          updatedAt = DateTimeMapper.formatAsDateTime(timeProvider.currentDate),
        ),
      )
    }
  }

  // endregion

  // region Update user: Authority

  @Test fun `update user authority fails when unauthorized`() {
    val project = stubber.projects.new()
    val universalId = stubber.users(project).default().id.toUniversalFormat()
    val request = UserUpdateAuthorityRequest(Authority.MODERATOR.name)

    assertThat(
      restTemplate.exchange<SimpleResponse>(
        url = "$baseUrl$UNIVERSAL_USER_AUTHORITY",
        method = HttpMethod.PUT,
        requestEntity = bearerBodyRequest(request, "invalid"),
        uriVariables = mapOf("universalId" to universalId),
      ),
    ).all {
      transform { it.statusCode }.isEqualTo(HttpStatus.UNAUTHORIZED)
    }
  }

  @Test fun `update user authority fails when project non-functional`() {
    val project = stubber.projects.new(status = REVIEW)
    val user = stubber.users(project).default()
    val token = stubber.tokens(user).real().token.tokenValue
    val request = UserUpdateAuthorityRequest(Authority.MODERATOR.name)

    assertThat(
      restTemplate.exchange<SimpleResponse>(
        url = "$baseUrl$UNIVERSAL_USER_AUTHORITY",
        method = HttpMethod.PUT,
        requestEntity = bearerBodyRequest(request, token),
        uriVariables = mapOf("universalId" to user.id.toUniversalFormat()),
      ),
    ).all {
      transform { it.statusCode }.isEqualTo(HttpStatus.PRECONDITION_REQUIRED)
    }
  }

  @Test fun `update user authority succeeds with valid authorization`() {
    val project = stubber.projects.new(activateNow = true)
    val owner = stubber.users(project).owner()
    val targetUser = stubber.users(project).default()
    val token = stubber.tokens(owner).real().token.tokenValue
    val request = UserUpdateAuthorityRequest(Authority.MODERATOR.name)

    assertThat(
      restTemplate.exchange<UserResponse>(
        url = "$baseUrl$UNIVERSAL_USER_AUTHORITY",
        method = HttpMethod.PUT,
        requestEntity = bearerBodyRequest(request, token),
        uriVariables = mapOf("universalId" to targetUser.id.toUniversalFormat()),
      ),
    ).all {
      transform { it.statusCode }.isEqualTo(HttpStatus.OK)
      transform { it.body!! }.isDataClassEqualTo(
        Stubs.userResponse.copy(
          userId = targetUser.id.userId,
          projectId = project.id,
          universalId = targetUser.id.toUniversalFormat(),
          type = targetUser.type.name,
          authority = request.authority,
          birthday = DateTimeMapper.formatAsDate(targetUser.birthday!!),
          createdAt = DateTimeMapper.formatAsDateTime(timeProvider.currentDate),
          updatedAt = DateTimeMapper.formatAsDateTime(timeProvider.currentDate),
        ),
      )
    }
  }

  // endregion

  // region Update user: Data

  @Test fun `update user data fails when unauthorized`() {
    val project = stubber.projects.new()
    val universalId = stubber.users(project).default().id.toUniversalFormat()

    assertThat(
      restTemplate.exchange<SimpleResponse>(
        url = "$baseUrl$UNIVERSAL_USER_DATA",
        method = HttpMethod.PUT,
        requestEntity = bearerBodyRequest(Stubs.userUpdateDataRequest, "invalid"),
        uriVariables = mapOf("universalId" to universalId),
      ),
    ).all {
      transform { it.statusCode }.isEqualTo(HttpStatus.UNAUTHORIZED)
    }
  }

  @Test fun `update user data fails when project non-functional`() {
    val project = stubber.projects.new(status = REVIEW)
    val user = stubber.users(project).default()
    val token = stubber.tokens(user).real().token.tokenValue

    assertThat(
      restTemplate.exchange<SimpleResponse>(
        url = "$baseUrl$UNIVERSAL_USER_DATA",
        method = HttpMethod.PUT,
        requestEntity = bearerBodyRequest(Stubs.userUpdateDataRequest, token),
        uriVariables = mapOf("universalId" to user.id.toUniversalFormat()),
      ),
    ).all {
      transform { it.statusCode }.isEqualTo(HttpStatus.PRECONDITION_REQUIRED)
    }
  }

  @Test fun `update user data succeeds with valid authorization`() {
    val project = stubber.projects.new(activateNow = true)
    val owner = stubber.users(project).owner()
    val targetUser = stubber.users(project).default()
    val token = stubber.tokens(owner).real().token.tokenValue

    assertThat(
      restTemplate.exchange<UserResponse>(
        url = "$baseUrl$UNIVERSAL_USER_DATA",
        method = HttpMethod.PUT,
        requestEntity = bearerBodyRequest(Stubs.userUpdateDataRequest, token),
        uriVariables = mapOf("universalId" to targetUser.id.toUniversalFormat()),
      ),
    ).all {
      transform { it.statusCode }.isEqualTo(HttpStatus.OK)
      transform { it.body!! }.isDataClassEqualTo(
        Stubs.userResponseUpdated.copy(
          userId = targetUser.id.userId,
          projectId = project.id,
          universalId = targetUser.id.toUniversalFormat(),
          authority = targetUser.authority.name,
          createdAt = DateTimeMapper.formatAsDateTime(timeProvider.currentDate),
          updatedAt = DateTimeMapper.formatAsDateTime(timeProvider.currentDate),
        ),
      )
    }
  }

  // endregion

  // region Update user: Signature

  @Test fun `update user signature fails when unauthorized`() {
    val project = stubber.projects.new()
    val universalId = stubber.users(project).default().id.toUniversalFormat()

    assertThat(
      restTemplate.exchange<SimpleResponse>(
        url = "$baseUrl$UNIVERSAL_USER_SIGNATURE?logout=false",
        method = HttpMethod.PUT,
        requestEntity = bearerBodyRequest(Stubs.userUpdateSignatureRequest, "invalid"),
        uriVariables = mapOf("universalId" to universalId),
      ),
    ).all {
      transform { it.statusCode }.isEqualTo(HttpStatus.UNAUTHORIZED)
    }
  }

  @Test fun `update user signature fails when project non-functional`() {
    val project = stubber.projects.new(status = REVIEW)
    val user = stubber.users(project).default()
    val token = stubber.tokens(user).real().token.tokenValue

    assertThat(
      restTemplate.exchange<SimpleResponse>(
        url = "$baseUrl$UNIVERSAL_USER_SIGNATURE?logout=false",
        method = HttpMethod.PUT,
        requestEntity = bearerBodyRequest(Stubs.userUpdateSignatureRequest, token),
        uriVariables = mapOf("universalId" to user.id.toUniversalFormat()),
      ),
    ).all {
      transform { it.statusCode }.isEqualTo(HttpStatus.PRECONDITION_REQUIRED)
    }
  }

  @Test fun `update user signature succeeds with valid authorization (no logout)`() {
    val project = stubber.projects.new(activateNow = true)
    val targetUser = stubber.users(project).default()
    val token = stubber.tokens(targetUser).real()

    assertThat(
      restTemplate.exchange<UserResponse>(
        url = "$baseUrl$UNIVERSAL_USER_SIGNATURE?logout=false",
        method = HttpMethod.PUT,
        requestEntity = bearerBodyRequest(Stubs.userUpdateSignatureRequest, token.token.tokenValue),
        uriVariables = mapOf("universalId" to targetUser.id.toUniversalFormat()),
      ),
    ).all {
      // verify response
      transform { it.statusCode }.isEqualTo(HttpStatus.OK)
      transform { it.body!! }.isDataClassEqualTo(
        Stubs.userResponse.copy(
          userId = targetUser.id.userId,
          projectId = project.id,
          universalId = targetUser.id.toUniversalFormat(),
          type = targetUser.type.name,
          authority = targetUser.authority.name,
          birthday = DateTimeMapper.formatAsDate(targetUser.birthday!!),
          createdAt = DateTimeMapper.formatAsDateTime(timeProvider.currentDate),
          updatedAt = DateTimeMapper.formatAsDateTime(timeProvider.currentDate),
        ),
      )
      // check if changing worked
      assertFailure {
        authService.resolveUser(
          targetUser.id.toUniversalFormat(),
          Stubs.userUpdateSignatureRequest.rawSignatureOld,
        )
      }
      assertThat(
        authService.resolveUser(
          targetUser.id.toUniversalFormat(),
          Stubs.userUpdateSignatureRequest.rawSignatureNew,
        ),
      ).isInstanceOf(User::class)
      // check if logout param was respected
      assertThat(authService.refreshAuth(token, ipAddress = null))
        .isInstanceOf(String::class)
    }
  }

  @Test fun `update user signature succeeds with valid authorization (with logout)`() {
    val project = stubber.projects.new(activateNow = true)
    val targetUser = stubber.users(project).default()
    val token = stubber.tokens(targetUser).real()

    assertThat(
      restTemplate.exchange<UserResponse>(
        url = "$baseUrl$UNIVERSAL_USER_SIGNATURE?logout=true",
        method = HttpMethod.PUT,
        requestEntity = bearerBodyRequest(Stubs.userUpdateSignatureRequest, token.token.tokenValue),
        uriVariables = mapOf("universalId" to targetUser.id.toUniversalFormat()),
      ),
    ).all {
      // verify response
      transform { it.statusCode }.isEqualTo(HttpStatus.OK)
      transform { it.body!! }.isDataClassEqualTo(
        Stubs.userResponse.copy(
          userId = targetUser.id.userId,
          projectId = project.id,
          universalId = targetUser.id.toUniversalFormat(),
          type = targetUser.type.name,
          authority = targetUser.authority.name,
          birthday = DateTimeMapper.formatAsDate(targetUser.birthday!!),
          createdAt = DateTimeMapper.formatAsDateTime(timeProvider.currentDate),
          updatedAt = DateTimeMapper.formatAsDateTime(timeProvider.currentDate),
        ),
      )
      // check if changing worked
      assertFailure {
        authService.resolveUser(
          targetUser.id.toUniversalFormat(),
          Stubs.userUpdateSignatureRequest.rawSignatureOld,
        )
      }
      assertThat(
        authService.resolveUser(
          targetUser.id.toUniversalFormat(),
          Stubs.userUpdateSignatureRequest.rawSignatureNew,
        ),
      ).isInstanceOf(User::class)
      // check if logout param was respected
      assertFailure {
        authService.refreshAuth(token, ipAddress = null)
      }
    }
  }

  // endregion

  // region Token Verification

  @Test fun `token verification fails when project non-functional`() {
    val project = stubber.projects.new(status = REVIEW)
    val user = stubber.users(project).default(autoVerified = false)

    assertThat(
      restTemplate.exchange<SimpleResponse>(
        url = "$baseUrl$UNIVERSAL_USER_VERIFY",
        method = HttpMethod.PUT,
        requestEntity = emptyRequest(),
        uriVariables = mapOf(
          "universalId" to user.id.toUniversalFormat(),
          "verificationToken" to user.verificationToken,
        ),
      ),
    ).all {
      transform { it.statusCode }.isEqualTo(HttpStatus.PRECONDITION_REQUIRED)
    }
  }

  @Test fun `token verification succeeds with valid token`() {
    val project = stubber.projects.new(activateNow = true)
    val user = stubber.users(project).default(autoVerified = false)

    assertThat(
      restTemplate.exchange<SimpleResponse>(
        url = "$baseUrl$UNIVERSAL_USER_VERIFY",
        method = HttpMethod.PUT,
        requestEntity = emptyRequest(),
        uriVariables = mapOf(
          "universalId" to user.id.toUniversalFormat(),
          "verificationToken" to user.verificationToken,
        ),
      ),
    ).all {
      transform { it.statusCode }.isEqualTo(HttpStatus.OK)
      transform { it.body!! }.isDataClassEqualTo(SimpleResponse.DONE)
      assertThat(stubber.users(project).default().verificationToken).isNull()
    }
  }

  // endregion

  // region Signature reset

  @Test fun `reset signature fails when project non-functional`() {
    val project = stubber.projects.new(status = REVIEW)
    val user = stubber.users(project).default()

    assertThat(
      restTemplate.exchange<SimpleResponse>(
        url = "$baseUrl$UNIVERSAL_USER_SIGNATURE_RESET",
        method = HttpMethod.PUT,
        requestEntity = emptyRequest(),
        uriVariables = mapOf("universalId" to user.id.toUniversalFormat()),
      ),
    ).all {
      transform { it.statusCode }.isEqualTo(HttpStatus.PRECONDITION_REQUIRED)
    }
  }

  @Test fun `reset signature succeeds with valid authorization`() {
    val project = stubber.projects.new(activateNow = true)
    val user = stubber.users(project).default()
    val token = stubber.tokens(user).real()

    assertThat(
      restTemplate.exchange<SimpleResponse>(
        url = "$baseUrl$UNIVERSAL_USER_SIGNATURE_RESET",
        method = HttpMethod.PUT,
        requestEntity = emptyRequest(),
        uriVariables = mapOf("universalId" to user.id.toUniversalFormat()),
      ),
    ).all {
      // verify response
      transform { it.statusCode }.isEqualTo(HttpStatus.OK)
      transform { it.body!! }.isDataClassEqualTo(SimpleResponse.DONE)
      // check if changing worked
      assertFailure {
        authService.resolveUser(
          user.id.toUniversalFormat(),
          Stubs.userCreator.rawSignature,
        )
      }
      // check if tokens are invalid
      assertFailure {
        authService.refreshAuth(token, ipAddress = null)
      }
    }
  }

  // endregion

  // region Delete User

  @Test fun `delete user fails when unauthorized`() {
    val project = stubber.projects.new()
    val universalId = stubber.users(project).default().id.toUniversalFormat()

    assertThat(
      restTemplate.exchange<SimpleResponse>(
        url = "$baseUrl$UNIVERSAL_USER",
        method = HttpMethod.DELETE,
        requestEntity = bearerEmptyRequest("invalid"),
        uriVariables = mapOf("universalId" to universalId),
      ),
    ).all {
      transform { it.statusCode }.isEqualTo(HttpStatus.UNAUTHORIZED)
    }
  }

  @Test fun `delete user fails when project non-functional`() {
    val project = stubber.projects.new(status = REVIEW)
    val user = stubber.users(project).default()
    val token = stubber.tokens(user).real().token.tokenValue

    assertThat(
      restTemplate.exchange<SimpleResponse>(
        url = "$baseUrl$UNIVERSAL_USER",
        method = HttpMethod.DELETE,
        requestEntity = bearerEmptyRequest(token),
        uriVariables = mapOf("universalId" to user.id.toUniversalFormat()),
      ),
    ).all {
      transform { it.statusCode }.isEqualTo(HttpStatus.PRECONDITION_REQUIRED)
    }
  }

  @Test fun `delete user succeeds with valid authorization`() {
    val project = stubber.projects.new(activateNow = true)
    val self = stubber.users(project).default()
    val universalId = self.id.toUniversalFormat()
    val token = stubber.tokens(self).real().token.tokenValue

    assertThat(
      restTemplate.exchange<SimpleResponse>(
        url = "$baseUrl$UNIVERSAL_USER",
        method = HttpMethod.DELETE,
        requestEntity = bearerEmptyRequest(token),
        uriVariables = mapOf("universalId" to universalId),
      ),
    ).all {
      transform { it.statusCode }.isEqualTo(HttpStatus.OK)
      transform { it.body!! }.isDataClassEqualTo(SimpleResponse.DONE)
      assertFailure {
        authService.resolveUser(self.id.toUniversalFormat(), Stubs.userCreator.rawSignature)
      }
    }
  }

}
