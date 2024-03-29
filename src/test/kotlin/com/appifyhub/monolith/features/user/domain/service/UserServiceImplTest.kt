package com.appifyhub.monolith.features.user.domain.service

import assertk.all
import assertk.assertAll
import assertk.assertFailure
import assertk.assertThat
import assertk.assertions.hasClass
import assertk.assertions.isDataClassEqualTo
import assertk.assertions.isEmpty
import assertk.assertions.isEqualTo
import assertk.assertions.isGreaterThan
import assertk.assertions.isInstanceOf
import assertk.assertions.messageContains
import com.appifyhub.monolith.TestAppifyHubApplication
import com.appifyhub.monolith.features.common.domain.model.Settable
import com.appifyhub.monolith.eventbus.UserAuthResetCompleted
import com.appifyhub.monolith.eventbus.UserCreated
import com.appifyhub.monolith.features.creator.domain.model.Project.UserIdType
import com.appifyhub.monolith.features.creator.repository.SignatureGenerator
import com.appifyhub.monolith.features.user.domain.model.User
import com.appifyhub.monolith.features.user.domain.model.User.Authority
import com.appifyhub.monolith.features.user.domain.model.User.ContactType
import com.appifyhub.monolith.features.user.domain.model.User.Type
import com.appifyhub.monolith.features.user.domain.model.UserCreator
import com.appifyhub.monolith.features.user.domain.model.UserId
import com.appifyhub.monolith.features.user.domain.model.UserUpdater
import com.appifyhub.monolith.features.user.repository.util.TokenGenerator
import com.appifyhub.monolith.features.user.repository.util.UserIdGenerator
import com.appifyhub.monolith.util.EventBusFake
import com.appifyhub.monolith.util.Stubber
import com.appifyhub.monolith.util.Stubs
import com.appifyhub.monolith.util.TimeProviderFake
import com.appifyhub.monolith.util.extension.truncateTo
import org.junit.jupiter.api.AfterEach
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.extension.ExtendWith
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.dao.EmptyResultDataAccessException
import org.springframework.security.crypto.password.PasswordEncoder
import org.springframework.test.annotation.DirtiesContext
import org.springframework.test.annotation.DirtiesContext.MethodMode
import org.springframework.test.context.ActiveProfiles
import org.springframework.test.context.junit.jupiter.SpringExtension
import org.springframework.web.server.ResponseStatusException
import java.time.temporal.ChronoUnit
import java.util.Date
import java.util.Locale

@ExtendWith(SpringExtension::class)
@ActiveProfiles(TestAppifyHubApplication.PROFILE)
@SpringBootTest(classes = [TestAppifyHubApplication::class])
class UserServiceImplTest {

  @Autowired lateinit var service: UserService
  @Autowired lateinit var signupCodeService: SignupCodeService
  @Autowired lateinit var passwordEncoder: PasswordEncoder
  @Autowired lateinit var timeProvider: TimeProviderFake
  @Autowired lateinit var eventBus: EventBusFake
  @Autowired lateinit var stubber: Stubber

  @BeforeEach fun setup() {
    // ensure valid birthday from the stub
    val fiftyYearsMillis: Long = ChronoUnit.YEARS.duration.multipliedBy(50).toMillis()
    timeProvider.staticTime = { Stubs.userCreator.birthday!!.time + fiftyYearsMillis }
  }

  @AfterEach fun teardown() {
    timeProvider.staticTime = { null }
    UserIdGenerator.interceptor = { null }
    TokenGenerator.emailInterceptor = { null }
    TokenGenerator.phoneInterceptor = { null }
    SignatureGenerator.interceptor = { null }
  }

  // Adding

  @Test fun `adding user fails with invalid username`() {
    val project = stubber.projects.new(userIdType = UserIdType.USERNAME)
    val creator = Stubs.userCreator.copy(userId = " ", projectId = project.id)

    assertFailure { service.addUser(creator) }
      .all {
        hasClass(ResponseStatusException::class)
        messageContains("Username ID")
      }
  }

  @Test fun `adding user fails with invalid email`() {
    val project = stubber.projects.new(userIdType = UserIdType.EMAIL)
    val creator = Stubs.userCreator.copy(userId = "invalid", projectId = project.id)

    assertFailure { service.addUser(creator) }
      .all {
        hasClass(ResponseStatusException::class)
        messageContains("Email ID")
      }
  }

  @Test fun `adding user fails with invalid phone`() {
    val project = stubber.projects.new(userIdType = UserIdType.PHONE)
    val creator = Stubs.userCreator.copy(userId = "invalid", projectId = project.id)

    assertFailure { service.addUser(creator) }
      .all {
        hasClass(ResponseStatusException::class)
        messageContains("Phone ID")
      }
  }

  @Test fun `adding user fails with invalid custom user ID`() {
    val project = stubber.projects.new(userIdType = UserIdType.CUSTOM)
    val creator = Stubs.userCreator.copy(userId = " ", projectId = project.id)

    assertFailure { service.addUser(creator) }
      .all {
        hasClass(ResponseStatusException::class)
        messageContains("User ID")
      }
  }

  @Test fun `adding user fails with invalid raw signature`() {
    val project = stubber.projects.new(userIdType = Stubs.project.userIdType)
    val creator = Stubs.userCreator.copy(rawSignature = " ", projectId = project.id)

    assertFailure { service.addUser(creator) }
      .all {
        hasClass(ResponseStatusException::class)
        messageContains("Signature")
      }
  }

  @Test fun `adding user fails with invalid name`() {
    val project = stubber.projects.new(userIdType = Stubs.project.userIdType)
    val creator = Stubs.userCreator.copy(name = " ", projectId = project.id)

    assertFailure { service.addUser(creator) }
      .all {
        hasClass(ResponseStatusException::class)
        messageContains("Name")
      }
  }

  @Test fun `adding user fails with invalid contact email`() {
    val project = stubber.projects.new(userIdType = Stubs.project.userIdType)
    val creator = Stubs.userCreator.copy(contactType = ContactType.EMAIL, contact = "invalid", projectId = project.id)

    assertFailure { service.addUser(creator) }
      .all {
        hasClass(ResponseStatusException::class)
        messageContains("Contact Email")
      }
  }

  @Test fun `adding user fails with invalid contact phone`() {
    val project = stubber.projects.new(userIdType = Stubs.project.userIdType)
    val creator = Stubs.userCreator.copy(contactType = ContactType.PHONE, contact = "invalid", projectId = project.id)

    assertFailure { service.addUser(creator) }
      .all {
        hasClass(ResponseStatusException::class)
        messageContains("Contact Phone")
      }
  }

  @Test fun `adding user fails with invalid custom contact`() {
    val project = stubber.projects.new(userIdType = Stubs.project.userIdType)
    val creator = Stubs.userCreator.copy(contactType = ContactType.CUSTOM, contact = " ", projectId = project.id)

    assertFailure { service.addUser(creator) }
      .all {
        hasClass(ResponseStatusException::class)
        messageContains("Contact")
      }
  }

  @Test fun `adding user fails with invalid birthday`() {
    val project = stubber.projects.new(userIdType = Stubs.project.userIdType)
    val fiveYearsMillis: Long = ChronoUnit.YEARS.duration.multipliedBy(5).toMillis()
    val birthday = Date(timeProvider.currentMillis - fiveYearsMillis)
    val creator = Stubs.userCreator.copy(birthday = birthday, projectId = project.id)

    assertFailure { service.addUser(creator) }
      .all {
        hasClass(ResponseStatusException::class)
        messageContains("Birthday")
      }
  }

  @Test fun `adding user fails with invalid organization`() {
    val project = stubber.projects.new(userIdType = Stubs.project.userIdType)
    val creator = Stubs.userCreator.copy(company = Stubs.company.copy(countryCode = "D"), projectId = project.id)

    assertFailure { service.addUser(creator) }
      .all {
        hasClass(ResponseStatusException::class)
        messageContains("Company")
      }
  }

  @Test fun `adding user fails with invalid signup code (format)`() {
    val project = stubber.projects.new(userIdType = Stubs.project.userIdType, requiresSignupCodes = true)
    val creator = Stubs.userCreator.copy(signupCode = "invalid", projectId = project.id)

    assertFailure { service.addUser(creator) }
      .all {
        hasClass(ResponseStatusException::class)
        messageContains("Signup Code")
      }
  }

  @Test fun `adding user fails with invalid signup code (already used)`() {
    val project = stubber.projects.new(userIdType = Stubs.project.userIdType, requiresSignupCodes = true)
    val signupCodeOwner = stubber.users(project).default()
    val signupCode = signupCodeService.createCode(signupCodeOwner.id)
      .let { signupCodeService.markCodeUsed(it.code, project.id) }
    val creator = Stubs.userCreator.copy(signupCode = signupCode.code, projectId = project.id)

    assertFailure { service.addUser(creator) }
      .all {
        hasClass(ResponseStatusException::class)
        messageContains("Signup code already used")
      }
  }

  @Test fun `adding user fails with max users reached`() {
    val project = stubber.projects.new(userIdType = UserIdType.USERNAME, maxUsers = 2)
    stubber.users(project).default(idSuffix = "_1")
    stubber.users(project).default(idSuffix = "_2")
    val creator = Stubs.userCreator.copy(projectId = project.id)

    assertFailure { service.addUser(creator) }
      .all {
        hasClass(ResponseStatusException::class)
        messageContains("Maximum users")
      }
  }

  @DirtiesContext(methodMode = MethodMode.BEFORE_METHOD)
  @Test fun `adding user works with random ID`() {
    val project = stubber.projects.new(userIdType = UserIdType.RANDOM)
    val creator = Stubs.userCreator.copy(userId = null, projectId = project.id)
    stubGenerators()

    assertThat(service.addUser(creator))
      .isDataClassEqualTo(
        Stubs.user.copy(
          id = UserId(userId = UserIdGenerator.nextId, project.id),
          verificationToken = TokenGenerator.nextEmailToken,
          createdAt = timeProvider.currentDate,
          updatedAt = timeProvider.currentDate,
        ),
      )
  }

  @DirtiesContext(methodMode = MethodMode.BEFORE_METHOD)
  @Test fun `adding user works with username ID`() {
    val project = stubber.projects.new(userIdType = UserIdType.USERNAME)
    val creator = Stubs.userCreator.copy(userId = "username", projectId = project.id)
    stubGenerators()

    assertThat(service.addUser(creator))
      .isDataClassEqualTo(
        Stubs.user.copy(
          id = UserId(userId = "username", project.id),
          verificationToken = TokenGenerator.nextEmailToken,
          createdAt = timeProvider.currentDate,
          updatedAt = timeProvider.currentDate,
        ),
      )
  }

  @DirtiesContext(methodMode = MethodMode.BEFORE_METHOD)
  @Test fun `adding user works with email ID`() {
    val project = stubber.projects.new(userIdType = UserIdType.EMAIL)
    val creator = Stubs.userCreator.copy(userId = "email@domain.com", projectId = project.id)
    stubGenerators()

    assertThat(service.addUser(creator))
      .isDataClassEqualTo(
        Stubs.user.copy(
          id = UserId(userId = "email@domain.com", project.id),
          verificationToken = TokenGenerator.nextEmailToken,
          createdAt = timeProvider.currentDate,
          updatedAt = timeProvider.currentDate,
        ),
      )
  }

  @DirtiesContext(methodMode = MethodMode.BEFORE_METHOD)
  @Test fun `adding user works with phone ID (and phone contact)`() {
    val project = stubber.projects.new(userIdType = UserIdType.PHONE)
    val creator = Stubs.userCreator.copy(
      userId = "+491760000000",
      contactType = ContactType.PHONE,
      contact = "+491760000000",
      projectId = project.id,
      languageTag = Locale.US.toLanguageTag(),
    )
    stubGenerators()

    assertThat(service.addUser(creator))
      .isDataClassEqualTo(
        Stubs.user.copy(
          id = UserId("+491760000000", project.id),
          verificationToken = TokenGenerator.nextPhoneToken,
          contactType = ContactType.PHONE,
          contact = "+491760000000",
          createdAt = timeProvider.currentDate,
          updatedAt = timeProvider.currentDate,
        ),
      )
  }

  @DirtiesContext(methodMode = MethodMode.BEFORE_METHOD)
  @Test fun `adding user works with custom ID (and no contact)`() {
    val project = stubber.projects.new(userIdType = UserIdType.CUSTOM)
    val creator = Stubs.userCreator.copy(
      userId = "custom_id",
      contactType = ContactType.CUSTOM,
      contact = null,
      projectId = project.id,
    )
    stubGenerators()

    assertThat(service.addUser(creator))
      .isDataClassEqualTo(
        Stubs.user.copy(
          id = UserId(userId = "custom_id", project.id),
          verificationToken = null,
          contactType = ContactType.CUSTOM,
          contact = null,
          createdAt = timeProvider.currentDate,
          updatedAt = timeProvider.currentDate,
        ),
      )
  }

  @DirtiesContext(methodMode = MethodMode.BEFORE_METHOD)
  @Test fun `adding user works with random ID (minimal data)`() {
    val project = stubber.projects.new(userIdType = UserIdType.RANDOM)
    val creator = UserCreator(
      userId = null,
      projectId = project.id,
      rawSignature = "12345678",
      name = null,
      type = Type.PERSONAL,
      authority = Authority.DEFAULT,
      allowsSpam = false,
      contact = null,
      contactType = ContactType.CUSTOM,
      birthday = null,
      company = null,
      languageTag = null,
      signupCode = null,
    )
    stubGenerators()

    assertThat(service.addUser(creator))
      .isDataClassEqualTo(
        User(
          id = UserId("user_id", project.id),
          signature = "87654321",
          name = null,
          type = Type.PERSONAL,
          authority = Authority.DEFAULT,
          allowsSpam = false,
          contact = null,
          contactType = ContactType.CUSTOM,
          verificationToken = null,
          birthday = null,
          createdAt = timeProvider.currentDate,
          updatedAt = timeProvider.currentDate,
          company = null,
          languageTag = null,
        ),
      )
  }

  @DirtiesContext(methodMode = MethodMode.BEFORE_METHOD)
  @Test fun `adding user notifies subscribers`() {
    val project = stubber.projects.new(userIdType = UserIdType.CUSTOM)
    val creator = Stubs.userCreator.copy(
      userId = "custom_id",
      projectId = project.id,
      rawSignature = "12345678",
    )
    stubGenerators()

    assertAll {
      val expectedUserId = UserId(creator.userId!!, project.id)

      assertThat(service.addUser(creator))
        .isInstanceOf(User::class)

      assertThat(eventBus.lastPublished)
        .transform { it as UserCreated }
        // check IDs only, as the rest is already tested elsewhere
        .transform { it.ownerProject.id to it.payload.id }
        .isEqualTo(project.id to expectedUserId)
    }
  }

  // Fetching

  @Test fun `fetching user fails with invalid user ID`() {
    val project = stubber.projects.new(userIdType = UserIdType.RANDOM)
    val targetId = UserId(" ", project.id)
    assertFailure { service.fetchUserByUserId(targetId) }
      .all {
        hasClass(ResponseStatusException::class)
        messageContains("User ID")
      }
  }

  @Test fun `fetching user works with a user ID`() {
    val project = stubber.projects.new(userIdType = UserIdType.RANDOM)
    val creator = Stubs.userCreator.copy(projectId = project.id)
    val storedUser = service.addUser(creator).cleanDates()
    val fetchedUser = service.fetchUserByUserId(storedUser.id).cleanDates()

    assertThat(fetchedUser)
      .isDataClassEqualTo(storedUser)
  }

  @Test fun `fetching user fails with invalid universal ID`() {
    assertFailure { service.fetchUserByUniversalId(" ") }
      .all {
        hasClass(ResponseStatusException::class)
        messageContains("User ID")
      }
  }

  @Test fun `fetching user works with a universal ID`() {
    val project = stubber.projects.new(userIdType = UserIdType.RANDOM)
    val creator = Stubs.userCreator.copy(projectId = project.id)
    val storedUser = service.addUser(creator).cleanDates()
    val fetchedUser = service.fetchUserByUniversalId(storedUser.id.toUniversalFormat()).cleanDates()

    assertThat(fetchedUser)
      .isDataClassEqualTo(storedUser)
  }

  @Test fun `fetching users fails with invalid project ID`() {
    assertFailure { service.fetchAllUsersByProjectId(-1) }
      .all {
        hasClass(ResponseStatusException::class)
        messageContains("Project ID")
      }
  }

  @DirtiesContext(methodMode = MethodMode.BEFORE_METHOD)
  @Test fun `fetching users works with a project ID`() {
    val project = stubber.projects.new(userIdType = UserIdType.RANDOM)
    val creator = Stubs.userCreator.copy(projectId = project.id)
    val storedUser = service.addUser(creator).cleanDates()
    val fetchedUsers = service.fetchAllUsersByProjectId(project.id).map { it.cleanDates() }

    assertThat(fetchedUsers)
      .isEqualTo(listOf(storedUser))
  }

  @Test fun `fetching user by verification token fails with invalid user ID`() {
    assertFailure { service.fetchUserByUserIdAndVerificationToken(UserId("invalid", -1), "token") }
      .all {
        hasClass(ResponseStatusException::class)
        messageContains("User ID")
      }
  }

  @Test fun `fetching user by verification token fails with invalid token`() {
    assertFailure { service.fetchUserByUserIdAndVerificationToken(Stubs.userId, "\t \n") }
      .all {
        hasClass(ResponseStatusException::class)
        messageContains("Verification Token")
      }
  }

  @Test fun `fetching user by verification token fails with non-matching token`() {
    val project = stubber.projects.new()
    val user = stubber.users(project).default(autoVerified = false)

    assertFailure { service.fetchUserByUserIdAndVerificationToken(user.id, "invalid") }
      .all {
        hasClass(EmptyResultDataAccessException::class)
      }
  }

  @DirtiesContext(methodMode = MethodMode.BEFORE_METHOD)
  @Test fun `fetching user by verification token fails with a valid token`() {
    val project = stubber.projects.new()
    val user = stubber.users(project).default(autoVerified = false).cleanDates()

    assertThat(
      service.fetchUserByUserIdAndVerificationToken(user.id, user.verificationToken!!)
        .cleanDates(),
    )
      .isDataClassEqualTo(user)
  }

  @Test fun `searching users by name fails with invalid project ID`() {
    assertFailure { service.searchByName(-1, Stubs.user.name!!) }
      .all {
        hasClass(ResponseStatusException::class)
        messageContains("Project ID")
      }
  }

  @Test fun `searching users by name fails with invalid name`() {
    assertFailure { service.searchByName(Stubs.project.id, " ") }
      .all {
        hasClass(ResponseStatusException::class)
        messageContains("Name")
      }
  }

  @DirtiesContext(methodMode = MethodMode.BEFORE_METHOD)
  @Test fun `searching users by name works with a valid name`() {
    val project = stubber.projects.new(userIdType = UserIdType.RANDOM)
    val creator = Stubs.userCreator.copy(projectId = project.id, name = "my name")
    val storedUser = service.addUser(creator).cleanDates()
    val fetchedUsers = service.searchByName(project.id, "%name").map { it.cleanDates() }

    assertThat(fetchedUsers)
      .isEqualTo(listOf(storedUser))
  }

  @Test fun `searching users by contact fails with invalid project ID`() {
    assertFailure { service.searchByContact(-1, Stubs.user.contact!!) }
      .all {
        hasClass(ResponseStatusException::class)
        messageContains("Project ID")
      }
  }

  @Test fun `searching users by contact fails with invalid contact`() {
    assertFailure { service.searchByContact(Stubs.project.id, " ") }
      .all {
        hasClass(ResponseStatusException::class)
        messageContains("Contact")
      }
  }

  @DirtiesContext(methodMode = MethodMode.BEFORE_METHOD)
  @Test fun `searching users by name works with a valid contact`() {
    val project = stubber.projects.new(userIdType = UserIdType.RANDOM)
    val creator = Stubs.userCreator.copy(projectId = project.id, contact = "contact@example.com")
    val storedUser = service.addUser(creator).cleanDates()
    val fetchedUsers = service.searchByContact(project.id, "%@example.com").map { it.cleanDates() }

    assertThat(fetchedUsers)
      .isEqualTo(listOf(storedUser))
  }

  // Updating

  @Test fun `updating user fails with invalid project ID`() {
    val updater = Stubs.userUpdater.copy(id = Stubs.userId.copy(projectId = -1))

    assertFailure { service.updateUser(updater) }
      .all {
        hasClass(ResponseStatusException::class)
        messageContains("User ID")
      }
  }

  @Test fun `updating user fails with invalid username`() {
    val project = stubber.projects.new(userIdType = UserIdType.USERNAME)
    val updater = Stubs.userUpdater.copy(id = UserId(" ", projectId = project.id))

    assertFailure { service.updateUser(updater) }
      .all {
        hasClass(ResponseStatusException::class)
        messageContains("User ID")
      }
  }

  @Test fun `updating user fails with invalid email`() {
    val project = stubber.projects.new(userIdType = UserIdType.EMAIL)
    val updater = Stubs.userUpdater.copy(id = UserId("invalid", project.id))

    assertFailure { service.updateUser(updater) }
      .all {
        hasClass(ResponseStatusException::class)
        messageContains("Email ID")
      }
  }

  @Test fun `updating user fails with invalid phone`() {
    val project = stubber.projects.new(userIdType = UserIdType.PHONE)
    val updater = Stubs.userUpdater.copy(id = UserId("invalid", project.id))

    assertFailure { service.updateUser(updater) }
      .all {
        hasClass(ResponseStatusException::class)
        messageContains("Phone ID")
      }
  }

  @Test fun `updating user fails with invalid custom user ID`() {
    val project = stubber.projects.new(userIdType = UserIdType.CUSTOM)
    val updater = Stubs.userUpdater.copy(id = UserId(" ", project.id))

    assertFailure { service.updateUser(updater) }
      .all {
        hasClass(ResponseStatusException::class)
        messageContains("User ID")
      }
  }

  @Test fun `updating user fails with invalid random user ID`() {
    val project = stubber.projects.new(userIdType = UserIdType.RANDOM)
    val updater = Stubs.userUpdater.copy(id = UserId(" ", project.id))

    assertFailure { service.updateUser(updater) }
      .all {
        hasClass(ResponseStatusException::class)
        messageContains("User ID")
      }
  }

  @Test fun `updating user fails with invalid raw signature`() {
    val project = stubber.projects.new(userIdType = Stubs.project.userIdType)
    val updater = Stubs.userUpdater.copy(id = Stubs.userId.copy(projectId = project.id), rawSignature = Settable(" "))

    assertFailure { service.updateUser(updater) }
      .all {
        hasClass(ResponseStatusException::class)
        messageContains("Signature")
      }
  }

  @Test fun `updating user fails with invalid contact email`() {
    val project = stubber.projects.new(userIdType = Stubs.project.userIdType)
    val updater = Stubs.userUpdater.copy(
      id = Stubs.userId.copy(projectId = project.id),
      contactType = Settable(ContactType.EMAIL),
      contact = Settable("invalid"),
    )

    assertFailure { service.updateUser(updater) }
      .all {
        hasClass(ResponseStatusException::class)
        messageContains("Contact Email")
      }
  }

  @Test fun `updating user fails with invalid contact phone`() {
    val project = stubber.projects.new(userIdType = Stubs.project.userIdType)
    val updater = Stubs.userUpdater.copy(
      id = Stubs.userId.copy(projectId = project.id),
      contactType = Settable(ContactType.PHONE),
      contact = Settable("invalid"),
    )

    assertFailure { service.updateUser(updater) }
      .all {
        hasClass(ResponseStatusException::class)
        messageContains("Contact Phone")
      }
  }

  @Test fun `updating user fails with invalid custom contact`() {
    val project = stubber.projects.new(userIdType = Stubs.project.userIdType)
    val updater = Stubs.userUpdater.copy(
      id = Stubs.userId.copy(projectId = project.id),
      contactType = Settable(ContactType.CUSTOM),
      contact = Settable(" "),
    )

    assertFailure { service.updateUser(updater) }
      .all {
        hasClass(ResponseStatusException::class)
        messageContains("Contact")
      }
  }

  @Test fun `updating user fails with invalid name`() {
    val project = stubber.projects.new(userIdType = Stubs.project.userIdType)
    val updater = Stubs.userUpdater.copy(id = Stubs.userId.copy(projectId = project.id), name = Settable(" "))

    assertFailure { service.updateUser(updater) }
      .all {
        hasClass(ResponseStatusException::class)
        messageContains("Name")
      }
  }

  @Test fun `updating user fails with invalid token`() {
    val project = stubber.projects.new(userIdType = Stubs.project.userIdType)
    val updater = Stubs.userUpdater.copy(
      id = Stubs.userId.copy(projectId = project.id),
      verificationToken = Settable(" "),
    )

    assertFailure { service.updateUser(updater) }
      .all {
        hasClass(ResponseStatusException::class)
        messageContains("Verification Token")
      }
  }

  @Test fun `updating user fails with invalid company name`() {
    val project = stubber.projects.new(userIdType = Stubs.project.userIdType)
    val updater = Stubs.userUpdater.copy(
      id = Stubs.userId.copy(projectId = project.id),
      company = Settable(
        Stubs.companyUpdater.copy(name = Settable(" ")),
      ),
    )

    assertFailure { service.updateUser(updater) }
      .all {
        hasClass(ResponseStatusException::class)
        messageContains("Company Name")
      }
  }

  @Test fun `updating user fails with invalid company street`() {
    val project = stubber.projects.new(userIdType = Stubs.project.userIdType)
    val updater = Stubs.userUpdater.copy(
      id = Stubs.userId.copy(projectId = project.id),
      company = Settable(
        Stubs.companyUpdater.copy(street = Settable(" ")),
      ),
    )

    assertFailure { service.updateUser(updater) }
      .all {
        hasClass(ResponseStatusException::class)
        messageContains("Company Street")
      }
  }

  @Test fun `updating user fails with invalid company postcode`() {
    val project = stubber.projects.new(userIdType = Stubs.project.userIdType)
    val updater = Stubs.userUpdater.copy(
      id = Stubs.userId.copy(projectId = project.id),
      company = Settable(
        Stubs.companyUpdater.copy(postcode = Settable(" ")),
      ),
    )

    assertFailure { service.updateUser(updater) }
      .all {
        hasClass(ResponseStatusException::class)
        messageContains("Company Postcode")
      }
  }

  @Test fun `updating user fails with invalid company city`() {
    val project = stubber.projects.new(userIdType = Stubs.project.userIdType)
    val updater = Stubs.userUpdater.copy(
      id = Stubs.userId.copy(projectId = project.id),
      company = Settable(
        Stubs.companyUpdater.copy(city = Settable(" ")),
      ),
    )

    assertFailure { service.updateUser(updater) }
      .all {
        hasClass(ResponseStatusException::class)
        messageContains("Company City")
      }
  }

  @Test fun `updating user fails with invalid company country code`() {
    val project = stubber.projects.new(userIdType = Stubs.project.userIdType)
    val updater = Stubs.userUpdater.copy(
      id = Stubs.userId.copy(projectId = project.id),
      company = Settable(
        Stubs.companyUpdater.copy(countryCode = Settable("d")),
      ),
    )

    assertFailure { service.updateUser(updater) }
      .all {
        hasClass(ResponseStatusException::class)
        messageContains("Company Country Code")
      }
  }

  @Test fun `updating user fails with invalid birthday`() {
    val project = stubber.projects.new(userIdType = Stubs.project.userIdType)
    val fiveYearsMillis: Long = ChronoUnit.YEARS.duration.multipliedBy(5).toMillis()
    val tooYoungDate = Date(timeProvider.currentMillis - fiveYearsMillis)
    val updater = Stubs.userUpdater.copy(
      id = Stubs.userId.copy(projectId = project.id),
      birthday = Settable(tooYoungDate),
    )

    assertFailure { service.updateUser(updater) }
      .all {
        hasClass(ResponseStatusException::class)
        messageContains("Birthday")
      }
  }

  @DirtiesContext(methodMode = MethodMode.BEFORE_METHOD)
  @Test fun `updating user works with changed data`() {
    val project = stubber.projects.new(userIdType = UserIdType.RANDOM)
    val creator = Stubs.userCreator.copy(
      projectId = project.id,
      contactType = ContactType.EMAIL,
      contact = "email@domain.com",
    )
    stubGenerators()
    val storedUser = service.addUser(creator).cleanDates()

    val updater = Stubs.userUpdater.copy(id = storedUser.id)
    val updatedUser = service.updateUser(updater).cleanDates()

    assertThat(updatedUser)
      .isDataClassEqualTo(
        Stubs.userUpdated.copy(
          id = storedUser.id,
          verificationToken = TokenGenerator.nextPhoneToken,
          createdAt = storedUser.createdAt,
          updatedAt = timeProvider.currentDate,
        ).cleanDates(),
      )
  }

  @DirtiesContext(methodMode = MethodMode.BEFORE_METHOD)
  @Test fun `updating user works with erased data (with username ID)`() {
    val project = stubber.projects.new(userIdType = UserIdType.USERNAME)
    val creator = Stubs.userCreator.copy(
      projectId = project.id,
      contactType = ContactType.EMAIL,
      contact = "email@domain.com",
    )
    stubGenerators()
    val storedUser = service.addUser(creator).cleanDates()

    val updater = UserUpdater(
      id = storedUser.id,
      rawSignature = null,
      type = null,
      authority = null,
      contactType = null,
      allowsSpam = null,
      name = Settable(null),
      contact = Settable(null),
      verificationToken = Settable(null),
      birthday = Settable(null),
      company = Settable(null),
    )
    val updatedUser = service.updateUser(updater).cleanDates()

    assertThat(updatedUser)
      .isDataClassEqualTo(
        storedUser.copy(
          id = storedUser.id,
          name = null,
          contactType = ContactType.CUSTOM,
          contact = null,
          verificationToken = null,
          birthday = null,
          company = null,
          createdAt = storedUser.createdAt,
          updatedAt = timeProvider.currentDate,
        ).cleanDates(),
      )
  }

  @Test fun `resetting signature by ID fails with invalid user ID`() {
    assertFailure { service.resetSignatureById(UserId("invalid", -1)) }
      .all {
        hasClass(ResponseStatusException::class)
        messageContains("User ID")
      }
  }

  @Test fun `resetting signature by ID succeeds`() {
    stubGenerators()

    val user = stubber.creators.default()
    val expectedSignature = passwordEncoder.encode(SignatureGenerator.nextSignature)

    assertAll {
      val expectedUser = user.copy(signature = expectedSignature)
      val expectedPayload = user.copy(signature = SignatureGenerator.nextSignature)
      val expectedEvent = UserAuthResetCompleted(stubber.projects.creator(), expectedPayload)

      assertThat(service.resetSignatureById(user.id))
        .isDataClassEqualTo(expectedUser)

      assertThat(eventBus.lastPublished)
        .transform { it as UserAuthResetCompleted }
        .isDataClassEqualTo(expectedEvent)
    }
  }

  // Removing

  @Test fun `removing user fails with invalid user ID`() {
    assertFailure { service.removeUserById(Stubs.userId.copy(projectId = -1)) }
      .all {
        hasClass(ResponseStatusException::class)
        messageContains("User ID")
      }
  }

  @Test fun `removing user works with a user ID`() {
    val project = stubber.projects.new(userIdType = UserIdType.RANDOM)
    val creator = Stubs.userCreator.copy(userId = null, projectId = project.id)
    val storedUser = service.addUser(creator)

    assertAll {
      assertThat(service.fetchUserByUserId(storedUser.id))
        .isInstanceOf(User::class) // user is there
      assertThat(service.removeUserById(storedUser.id))
        .isEqualTo(Unit)
      assertFailure { service.fetchUserByUserId(storedUser.id) } // user is not there anymore
    }
  }

  @Test fun `removing user fails with invalid universal user ID`() {
    assertFailure { service.removeUserByUniversalId("invalid") }
  }

  @Test fun `removing user works with a universal user ID`() {
    val project = stubber.projects.new(userIdType = UserIdType.RANDOM)
    val creator = Stubs.userCreator.copy(userId = null, projectId = project.id)
    val storedUser = service.addUser(creator)

    assertAll {
      assertThat(service.fetchUserByUniversalId(storedUser.id.toUniversalFormat()))
        .isInstanceOf(User::class) // user is there
      assertThat(service.removeUserByUniversalId(storedUser.id.toUniversalFormat()))
        .isEqualTo(Unit)
      assertFailure { service.fetchUserByUniversalId(storedUser.id.toUniversalFormat()) } // user is not there anymore
    }
  }

  @Test fun `removing users fails with invalid project ID`() {
    assertFailure { service.removeAllUsersByProjectId(-1) }
      .all {
        hasClass(ResponseStatusException::class)
        messageContains("Project ID")
      }
  }

  @DirtiesContext(methodMode = MethodMode.AFTER_METHOD)
  @Test fun `removing users works with a project ID`() {
    val project = stubber.projects.new(userIdType = UserIdType.RANDOM)
    val creator = Stubs.userCreator.copy(userId = null, projectId = project.id)
    val storedUser = service.addUser(creator)

    assertAll {
      assertThat(service.fetchAllUsersByProjectId(storedUser.id.projectId).size)
        .isGreaterThan(0)
      assertThat(service.removeAllUsersByProjectId(storedUser.id.projectId))
        .isEqualTo(Unit)
      assertThat(service.fetchAllUsersByProjectId(storedUser.id.projectId))
        .isEmpty()
    }
  }

  // Helpers

  private fun stubGenerators() {
    UserIdGenerator.interceptor = { "user_id" }
    TokenGenerator.emailInterceptor = { "email_token" }
    TokenGenerator.phoneInterceptor = { "phone_token" }
    SignatureGenerator.interceptor = { "signature" }
  }

  private fun User.cleanDates() = copy(
    birthday = birthday?.truncateTo(ChronoUnit.DAYS),
    createdAt = createdAt.truncateTo(ChronoUnit.SECONDS),
    updatedAt = updatedAt.truncateTo(ChronoUnit.SECONDS),
  )

}
