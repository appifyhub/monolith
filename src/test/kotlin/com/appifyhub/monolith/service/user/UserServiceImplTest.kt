package com.appifyhub.monolith.service.user

import assertk.all
import assertk.assertAll
import assertk.assertThat
import assertk.assertions.hasClass
import assertk.assertions.isDataClassEqualTo
import assertk.assertions.isEmpty
import assertk.assertions.isEqualTo
import assertk.assertions.isFailure
import assertk.assertions.isGreaterThan
import assertk.assertions.isSuccess
import assertk.assertions.messageContains
import com.appifyhub.monolith.TestAppifyHubApplication
import com.appifyhub.monolith.domain.admin.Project.UserIdType
import com.appifyhub.monolith.domain.common.Settable
import com.appifyhub.monolith.domain.user.User
import com.appifyhub.monolith.domain.user.User.Authority
import com.appifyhub.monolith.domain.user.User.ContactType
import com.appifyhub.monolith.domain.user.User.Type
import com.appifyhub.monolith.domain.user.UserId
import com.appifyhub.monolith.domain.user.ops.UserCreator
import com.appifyhub.monolith.domain.user.ops.UserUpdater
import com.appifyhub.monolith.repository.user.TokenGenerator
import com.appifyhub.monolith.repository.user.UserIdGenerator
import com.appifyhub.monolith.util.Stubber
import com.appifyhub.monolith.util.Stubs
import com.appifyhub.monolith.util.TimeProviderFake
import com.appifyhub.monolith.util.ext.truncateTo
import java.time.temporal.ChronoUnit
import java.util.Date
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
class UserServiceImplTest {

  @Autowired lateinit var service: UserService
  @Autowired lateinit var timeProvider: TimeProviderFake
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
  }

  // Adding

  @Test fun `adding user fails with invalid username`() {
    val project = newProject(UserIdType.USERNAME)
    val creator = Stubs.userCreator.copy(userId = " ", projectId = project.id)

    assertThat { service.addUser(creator) }
      .isFailure()
      .all {
        hasClass(ResponseStatusException::class)
        messageContains("Username ID")
      }
  }

  @Test fun `adding user fails with invalid email`() {
    val project = newProject(UserIdType.EMAIL)
    val creator = Stubs.userCreator.copy(userId = "invalid", projectId = project.id)

    assertThat { service.addUser(creator) }
      .isFailure()
      .all {
        hasClass(ResponseStatusException::class)
        messageContains("Email ID")
      }
  }

  @Test fun `adding user fails with invalid phone`() {
    val project = newProject(UserIdType.PHONE)
    val creator = Stubs.userCreator.copy(userId = "invalid", projectId = project.id)

    assertThat { service.addUser(creator) }
      .isFailure()
      .all {
        hasClass(ResponseStatusException::class)
        messageContains("Phone ID")
      }
  }

  @Test fun `adding user fails with invalid custom user ID`() {
    val project = newProject(UserIdType.CUSTOM)
    val creator = Stubs.userCreator.copy(userId = " ", projectId = project.id)

    assertThat { service.addUser(creator) }
      .isFailure()
      .all {
        hasClass(ResponseStatusException::class)
        messageContains("User ID")
      }
  }

  @Test fun `adding user fails with invalid raw signature`() {
    val project = newProject(Stubs.project.userIdType)
    val creator = Stubs.userCreator.copy(rawSecret = " ", projectId = project.id)

    assertThat { service.addUser(creator) }
      .isFailure()
      .all {
        hasClass(ResponseStatusException::class)
        messageContains("Signature")
      }
  }

  @Test fun `adding user fails with invalid name`() {
    val project = newProject(Stubs.project.userIdType)
    val creator = Stubs.userCreator.copy(name = " ", projectId = project.id)

    assertThat { service.addUser(creator) }
      .isFailure()
      .all {
        hasClass(ResponseStatusException::class)
        messageContains("Name")
      }
  }

  @Test fun `adding user fails with invalid contact email`() {
    val project = newProject(Stubs.project.userIdType)
    val creator = Stubs.userCreator.copy(contactType = ContactType.EMAIL, contact = "invalid", projectId = project.id)

    assertThat { service.addUser(creator) }
      .isFailure()
      .all {
        hasClass(ResponseStatusException::class)
        messageContains("Contact Email")
      }
  }

  @Test fun `adding user fails with invalid contact phone`() {
    val project = newProject(Stubs.project.userIdType)
    val creator = Stubs.userCreator.copy(contactType = ContactType.PHONE, contact = "invalid", projectId = project.id)

    assertThat { service.addUser(creator) }
      .isFailure()
      .all {
        hasClass(ResponseStatusException::class)
        messageContains("Contact Phone")
      }
  }

  @Test fun `adding user fails with invalid custom contact`() {
    val project = newProject(Stubs.project.userIdType)
    val creator = Stubs.userCreator.copy(contactType = ContactType.CUSTOM, contact = " ", projectId = project.id)

    assertThat { service.addUser(creator) }
      .isFailure()
      .all {
        hasClass(ResponseStatusException::class)
        messageContains("Contact")
      }
  }

  @Test fun `adding user fails with invalid birthday`() {
    val project = newProject(Stubs.project.userIdType)
    val fiveYearsMillis: Long = ChronoUnit.YEARS.duration.multipliedBy(5).toMillis()
    val birthday = Date(timeProvider.currentMillis - fiveYearsMillis)
    val creator = Stubs.userCreator.copy(birthday = birthday, projectId = project.id)

    assertThat { service.addUser(creator) }
      .isFailure()
      .all {
        hasClass(ResponseStatusException::class)
        messageContains("Birthday")
      }
  }

  @Test fun `adding user fails with invalid organization`() {
    val project = newProject(Stubs.project.userIdType)
    val creator = Stubs.userCreator.copy(company = Stubs.company.copy(countryCode = "D"), projectId = project.id)

    assertThat { service.addUser(creator) }
      .isFailure()
      .all {
        hasClass(ResponseStatusException::class)
        messageContains("Company")
      }
  }

  @DirtiesContext(methodMode = MethodMode.BEFORE_METHOD)
  @Test fun `adding user works with random ID`() {
    val project = newProject(UserIdType.RANDOM)
    val creator = Stubs.userCreator.copy(userId = null, projectId = project.id)
    stubGenerators()

    assertThat(service.addUser(creator))
      .isDataClassEqualTo(
        Stubs.user.copy(
          id = UserId(userId = UserIdGenerator.nextId, project.id),
          verificationToken = TokenGenerator.nextEmailToken,
          createdAt = timeProvider.currentDate,
          updatedAt = timeProvider.currentDate,
        )
      )
  }

  @DirtiesContext(methodMode = MethodMode.BEFORE_METHOD)
  @Test fun `adding user works with username ID`() {
    val project = newProject(UserIdType.USERNAME)
    val creator = Stubs.userCreator.copy(userId = "username", projectId = project.id)
    stubGenerators()

    assertThat(service.addUser(creator))
      .isDataClassEqualTo(
        Stubs.user.copy(
          id = UserId(userId = "username", project.id),
          verificationToken = TokenGenerator.nextEmailToken,
          createdAt = timeProvider.currentDate,
          updatedAt = timeProvider.currentDate,
        )
      )
  }

  @DirtiesContext(methodMode = MethodMode.BEFORE_METHOD)
  @Test fun `adding user works with email ID`() {
    val project = newProject(UserIdType.EMAIL)
    val creator = Stubs.userCreator.copy(userId = "email@domain.com", projectId = project.id)
    stubGenerators()

    assertThat(service.addUser(creator))
      .isDataClassEqualTo(
        Stubs.user.copy(
          id = UserId(userId = "email@domain.com", project.id),
          verificationToken = TokenGenerator.nextEmailToken,
          createdAt = timeProvider.currentDate,
          updatedAt = timeProvider.currentDate,
        )
      )
  }

  @DirtiesContext(methodMode = MethodMode.BEFORE_METHOD)
  @Test fun `adding user works with phone ID (and phone contact)`() {
    val project = newProject(UserIdType.PHONE)
    val creator = Stubs.userCreator.copy(
      userId = "+491760000000",
      contactType = ContactType.PHONE,
      contact = "+491760000000",
      projectId = project.id,
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
        )
      )
  }

  @DirtiesContext(methodMode = MethodMode.BEFORE_METHOD)
  @Test fun `adding user works with custom ID (and no contact)`() {
    val project = newProject(UserIdType.CUSTOM)
    val creator = Stubs.userCreator.copy(
      userId = "custom_id",
      contactType = ContactType.CUSTOM,
      contact = null,
      projectId = project.id
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
        )
      )
  }

  @DirtiesContext(methodMode = MethodMode.BEFORE_METHOD)
  @Test fun `adding user works with random ID (minimal data)`() {
    val project = newProject(UserIdType.RANDOM)
    val creator = UserCreator(
      userId = null,
      projectId = project.id,
      rawSecret = "12345678",
      name = null,
      type = Type.PERSONAL,
      authority = Authority.DEFAULT,
      allowsSpam = false,
      contact = null,
      contactType = ContactType.CUSTOM,
      birthday = null,
      company = null,
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
        )
      )
  }

  // Fetching

  @Test fun `fetching user fails with invalid user ID`() {
    val project = newProject(UserIdType.RANDOM)
    val targetId = UserId(" ", project.id)
    assertThat { service.fetchUserByUserId(targetId) }
      .isFailure()
      .all {
        hasClass(ResponseStatusException::class)
        messageContains("User ID")
      }
  }

  @Test fun `fetching user works with a user ID`() {
    val project = newProject(UserIdType.RANDOM)
    val creator = Stubs.userCreator.copy(projectId = project.id)
    val storedUser = service.addUser(creator).cleanDates()
    val fetchedUser = service.fetchUserByUserId(storedUser.id).cleanDates()

    assertThat(fetchedUser)
      .isDataClassEqualTo(storedUser)
  }

  @Test fun `fetching user fails with invalid universal ID`() {
    assertThat { service.fetchUserByUniversalId(" ") }
      .isFailure()
      .all {
        hasClass(ResponseStatusException::class)
        messageContains("User ID")
      }
  }

  @Test fun `fetching user works with a universal ID`() {
    val project = newProject(UserIdType.RANDOM)
    val creator = Stubs.userCreator.copy(projectId = project.id)
    val storedUser = service.addUser(creator).cleanDates()
    val fetchedUser = service.fetchUserByUniversalId(storedUser.id.toUniversalFormat()).cleanDates()

    assertThat(fetchedUser)
      .isDataClassEqualTo(storedUser)
  }

  @Test fun `fetching users fails with invalid contact`() {
    assertThat { service.fetchAllUsersByContact(" ") }
      .isFailure()
      .all {
        hasClass(ResponseStatusException::class)
        messageContains("Contact")
      }
  }

  @DirtiesContext(methodMode = MethodMode.BEFORE_METHOD)
  @Test fun `fetching users works with a contact`() {
    val project = newProject(UserIdType.RANDOM)
    val creator = Stubs.userCreator.copy(contact = "contact@email.com", projectId = project.id)
    val storedUser = service.addUser(creator).cleanDates()
    val fetchedUsers = service.fetchAllUsersByContact(creator.contact!!).map { it.cleanDates() }

    assertThat(fetchedUsers)
      .isEqualTo(listOf(storedUser))
  }

  @Test fun `fetching users fails with invalid project ID`() {
    assertThat { service.fetchAllUsersByProjectId(-1) }
      .isFailure()
      .all {
        hasClass(ResponseStatusException::class)
        messageContains("Project ID")
      }
  }

  @DirtiesContext(methodMode = MethodMode.BEFORE_METHOD)
  @Test fun `fetching users works with a project ID`() {
    val project = newProject(UserIdType.RANDOM)
    val creator = Stubs.userCreator.copy(projectId = project.id)
    val storedUser = service.addUser(creator).cleanDates()
    val fetchedUsers = service.fetchAllUsersByProjectId(project.id).map { it.cleanDates() }

    assertThat(fetchedUsers)
      .isEqualTo(listOf(storedUser))
  }

  // Updating

  @Test fun `updating user fails with invalid project ID`() {
    val updater = Stubs.userUpdater.copy(id = Stubs.userId.copy(projectId = -1))

    assertThat { service.updateUser(updater) }
      .isFailure()
      .all {
        hasClass(ResponseStatusException::class)
        messageContains("User ID")
      }
  }

  @Test fun `updating user fails with invalid username`() {
    val project = newProject(UserIdType.USERNAME)
    val updater = Stubs.userUpdater.copy(id = UserId(" ", projectId = project.id))

    assertThat { service.updateUser(updater) }
      .isFailure()
      .all {
        hasClass(ResponseStatusException::class)
        messageContains("User ID")
      }
  }

  @Test fun `updating user fails with invalid email`() {
    val project = newProject(UserIdType.EMAIL)
    val updater = Stubs.userUpdater.copy(id = UserId("invalid", project.id))

    assertThat { service.updateUser(updater) }
      .isFailure()
      .all {
        hasClass(ResponseStatusException::class)
        messageContains("Email ID")
      }
  }

  @Test fun `updating user fails with invalid phone`() {
    val project = newProject(UserIdType.PHONE)
    val updater = Stubs.userUpdater.copy(id = UserId("invalid", project.id))

    assertThat { service.updateUser(updater) }
      .isFailure()
      .all {
        hasClass(ResponseStatusException::class)
        messageContains("Phone ID")
      }
  }

  @Test fun `updating user fails with invalid custom user ID`() {
    val project = newProject(UserIdType.CUSTOM)
    val updater = Stubs.userUpdater.copy(id = UserId(" ", project.id))

    assertThat { service.updateUser(updater) }
      .isFailure()
      .all {
        hasClass(ResponseStatusException::class)
        messageContains("User ID")
      }
  }

  @Test fun `updating user fails with invalid random user ID`() {
    val project = newProject(UserIdType.RANDOM)
    val updater = Stubs.userUpdater.copy(id = UserId(" ", project.id))

    assertThat { service.updateUser(updater) }
      .isFailure()
      .all {
        hasClass(ResponseStatusException::class)
        messageContains("User ID")
      }
  }

  @Test fun `updating user fails with invalid raw signature`() {
    val project = newProject(Stubs.project.userIdType)
    val updater = Stubs.userUpdater.copy(id = Stubs.userId.copy(projectId = project.id), rawSignature = Settable(" "))

    assertThat { service.updateUser(updater) }
      .isFailure()
      .all {
        hasClass(ResponseStatusException::class)
        messageContains("Signature")
      }
  }

  @Test fun `updating user fails with invalid contact email`() {
    val project = newProject(Stubs.project.userIdType)
    val updater = Stubs.userUpdater.copy(
      id = Stubs.userId.copy(projectId = project.id),
      contactType = Settable(ContactType.EMAIL),
      contact = Settable("invalid"),
    )

    assertThat { service.updateUser(updater) }
      .isFailure()
      .all {
        hasClass(ResponseStatusException::class)
        messageContains("Contact Email")
      }
  }

  @Test fun `updating user fails with invalid contact phone`() {
    val project = newProject(Stubs.project.userIdType)
    val updater = Stubs.userUpdater.copy(
      id = Stubs.userId.copy(projectId = project.id),
      contactType = Settable(ContactType.PHONE),
      contact = Settable("invalid"),
    )

    assertThat { service.updateUser(updater) }
      .isFailure()
      .all {
        hasClass(ResponseStatusException::class)
        messageContains("Contact Phone")
      }
  }

  @Test fun `updating user fails with invalid custom contact`() {
    val project = newProject(Stubs.project.userIdType)
    val updater = Stubs.userUpdater.copy(
      id = Stubs.userId.copy(projectId = project.id),
      contactType = Settable(ContactType.CUSTOM),
      contact = Settable(" "),
    )

    assertThat { service.updateUser(updater) }
      .isFailure()
      .all {
        hasClass(ResponseStatusException::class)
        messageContains("Contact")
      }
  }

  @Test fun `updating user fails with invalid name`() {
    val project = newProject(Stubs.project.userIdType)
    val updater = Stubs.userUpdater.copy(id = Stubs.userId.copy(projectId = project.id), name = Settable(" "))

    assertThat { service.updateUser(updater) }
      .isFailure()
      .all {
        hasClass(ResponseStatusException::class)
        messageContains("Name")
      }
  }

  @Test fun `updating user fails with invalid token`() {
    val project = newProject(Stubs.project.userIdType)
    val updater = Stubs.userUpdater.copy(
      id = Stubs.userId.copy(projectId = project.id),
      verificationToken = Settable(" "),
    )

    assertThat { service.updateUser(updater) }
      .isFailure()
      .all {
        hasClass(ResponseStatusException::class)
        messageContains("Verification Token")
      }
  }

  @Test fun `updating user fails with invalid company name`() {
    val project = newProject(Stubs.project.userIdType)
    val updater = Stubs.userUpdater.copy(
      id = Stubs.userId.copy(projectId = project.id),
      company = Settable(
        Stubs.companyUpdater.copy(name = Settable(" "))
      ),
    )

    assertThat { service.updateUser(updater) }
      .isFailure()
      .all {
        hasClass(ResponseStatusException::class)
        messageContains("Company Name")
      }
  }

  @Test fun `updating user fails with invalid company street`() {
    val project = newProject(Stubs.project.userIdType)
    val updater = Stubs.userUpdater.copy(
      id = Stubs.userId.copy(projectId = project.id),
      company = Settable(
        Stubs.companyUpdater.copy(street = Settable(" "))
      ),
    )

    assertThat { service.updateUser(updater) }
      .isFailure()
      .all {
        hasClass(ResponseStatusException::class)
        messageContains("Company Street")
      }
  }

  @Test fun `updating user fails with invalid company postcode`() {
    val project = newProject(Stubs.project.userIdType)
    val updater = Stubs.userUpdater.copy(
      id = Stubs.userId.copy(projectId = project.id),
      company = Settable(
        Stubs.companyUpdater.copy(postcode = Settable(" "))
      ),
    )

    assertThat { service.updateUser(updater) }
      .isFailure()
      .all {
        hasClass(ResponseStatusException::class)
        messageContains("Company Postcode")
      }
  }

  @Test fun `updating user fails with invalid company city`() {
    val project = newProject(Stubs.project.userIdType)
    val updater = Stubs.userUpdater.copy(
      id = Stubs.userId.copy(projectId = project.id),
      company = Settable(
        Stubs.companyUpdater.copy(city = Settable(" "))
      ),
    )

    assertThat { service.updateUser(updater) }
      .isFailure()
      .all {
        hasClass(ResponseStatusException::class)
        messageContains("Company City")
      }
  }

  @Test fun `updating user fails with invalid company country code`() {
    val project = newProject(Stubs.project.userIdType)
    val updater = Stubs.userUpdater.copy(
      id = Stubs.userId.copy(projectId = project.id),
      company = Settable(
        Stubs.companyUpdater.copy(countryCode = Settable("d"))
      ),
    )

    assertThat { service.updateUser(updater) }
      .isFailure()
      .all {
        hasClass(ResponseStatusException::class)
        messageContains("Company Country Code")
      }
  }

  @Test fun `updating user fails with invalid birthday`() {
    val project = newProject(Stubs.project.userIdType)
    val fiveYearsMillis: Long = ChronoUnit.YEARS.duration.multipliedBy(5).toMillis()
    val tooYoungDate = Date(timeProvider.currentMillis - fiveYearsMillis)
    val updater = Stubs.userUpdater.copy(
      id = Stubs.userId.copy(projectId = project.id),
      birthday = Settable(tooYoungDate),
    )

    assertThat { service.updateUser(updater) }
      .isFailure()
      .all {
        hasClass(ResponseStatusException::class)
        messageContains("Birthday")
      }
  }

  @DirtiesContext(methodMode = MethodMode.BEFORE_METHOD)
  @Test fun `updating user works with changed data`() {
    val project = newProject(UserIdType.RANDOM)
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
        ).cleanDates()
      )
  }

  @DirtiesContext(methodMode = MethodMode.BEFORE_METHOD)
  @Test fun `updating user works with erased data (with username ID)`() {
    val project = newProject(UserIdType.USERNAME)
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
        ).cleanDates()
      )
  }

  // Removing

  @Test fun `removing user fails with invalid user ID`() {
    assertThat { service.removeUserById(Stubs.userId.copy(projectId = -1)) }
      .isFailure()
      .all {
        hasClass(ResponseStatusException::class)
        messageContains("User ID")
      }
  }

  @Test fun `removing user works with a user ID`() {
    val project = newProject(UserIdType.RANDOM)
    val creator = Stubs.userCreator.copy(userId = null, projectId = project.id)
    val storedUser = service.addUser(creator)

    assertAll {
      assertThat { service.fetchUserByUserId(storedUser.id) }
        .isSuccess() // user is there
      assertThat { service.removeUserById(storedUser.id) }
        .isSuccess()
      assertThat { service.fetchUserByUserId(storedUser.id) }
        .isFailure() // user is not there anymore
    }
  }

  @Test fun `removing user fails with invalid universal user ID`() {
    assertThat { service.removeUserByUniversalId("invalid") }
      .isFailure()
  }

  @Test fun `removing user works with a universal user ID`() {
    val project = newProject(UserIdType.RANDOM)
    val creator = Stubs.userCreator.copy(userId = null, projectId = project.id)
    val storedUser = service.addUser(creator)

    assertAll {
      assertThat { service.fetchUserByUniversalId(storedUser.id.toUniversalFormat()) }
        .isSuccess() // user is there
      assertThat { service.removeUserByUniversalId(storedUser.id.toUniversalFormat()) }
        .isSuccess()
      assertThat { service.fetchUserByUniversalId(storedUser.id.toUniversalFormat()) }
        .isFailure() // user is not there anymore
    }
  }

  @Test fun `removing users fails with invalid project ID`() {
    assertThat { service.removeAllUsersByProjectId(-1) }
      .isFailure()
      .all {
        hasClass(ResponseStatusException::class)
        messageContains("Project ID")
      }
  }

  @DirtiesContext(methodMode = MethodMode.AFTER_METHOD)
  @Test fun `removing users works with a project ID`() {
    val project = newProject(UserIdType.RANDOM)
    val creator = Stubs.userCreator.copy(userId = null, projectId = project.id)
    val storedUser = service.addUser(creator)

    assertAll {
      assertThat(service.fetchAllUsersByProjectId(storedUser.id.projectId).size)
        .isGreaterThan(0)
      assertThat { service.removeAllUsersByProjectId(storedUser.id.projectId) }
        .isSuccess()
      assertThat(service.fetchAllUsersByProjectId(storedUser.id.projectId))
        .isEmpty()
    }
  }

  // Helpers

  private fun newProject(userIdType: UserIdType) = stubber.projects.new(userIdType = userIdType)

  private fun stubGenerators() {
    UserIdGenerator.interceptor = { "user_id" }
    TokenGenerator.emailInterceptor = { "email_token" }
    TokenGenerator.phoneInterceptor = { "phone_token" }
  }

  private fun User.cleanDates() = copy(
    birthday = birthday?.truncateTo(ChronoUnit.DAYS),
    createdAt = createdAt.truncateTo(ChronoUnit.SECONDS),
    updatedAt = updatedAt.truncateTo(ChronoUnit.SECONDS),
  )

}
