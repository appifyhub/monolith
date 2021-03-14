package com.appifyhub.monolith.service.user

import assertk.all
import assertk.assertThat
import assertk.assertions.hasClass
import assertk.assertions.hasSize
import assertk.assertions.isDataClassEqualTo
import assertk.assertions.isEqualTo
import assertk.assertions.isFailure
import assertk.assertions.messageContains
import com.appifyhub.monolith.TestAppifyHubApplication
import com.appifyhub.monolith.domain.admin.Project.UserIdType
import com.appifyhub.monolith.domain.common.Settable
import com.appifyhub.monolith.domain.user.User
import com.appifyhub.monolith.domain.user.User.ContactType
import com.appifyhub.monolith.domain.user.ops.UserUpdater
import com.appifyhub.monolith.repository.admin.AdminRepository
import com.appifyhub.monolith.repository.user.TokenGenerator
import com.appifyhub.monolith.repository.user.UserIdGenerator
import com.appifyhub.monolith.util.Stubs
import com.appifyhub.monolith.util.TimeProviderFake
import com.appifyhub.monolith.util.ext.truncateTo
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
import java.time.temporal.ChronoUnit
import java.util.Date

private const val ROOT_OWNER_NAME = "Administrator"

@ExtendWith(SpringExtension::class)
@ActiveProfiles(TestAppifyHubApplication.PROFILE)
@SpringBootTest(classes = [TestAppifyHubApplication::class])
class UserServiceImplTest {

  @Autowired lateinit var service: UserService
  @Autowired lateinit var timeProvider: TimeProviderFake
  @Autowired lateinit var adminRepo: AdminRepository

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
    val creator = Stubs.userCreator.copy(id = " ")

    assertThat { service.addUser(creator, UserIdType.USERNAME) }
      .isFailure()
      .all {
        hasClass(ResponseStatusException::class)
        messageContains("Username ID")
      }
  }

  @Test fun `adding user fails with invalid email`() {
    val creator = Stubs.userCreator.copy(id = "invalid")

    assertThat { service.addUser(creator, UserIdType.EMAIL) }
      .isFailure()
      .all {
        hasClass(ResponseStatusException::class)
        messageContains("Email ID")
      }
  }

  @Test fun `adding user fails with invalid phone`() {
    val creator = Stubs.userCreator.copy(id = "invalid")

    assertThat { service.addUser(creator, UserIdType.PHONE) }
      .isFailure()
      .all {
        hasClass(ResponseStatusException::class)
        messageContains("Phone ID")
      }
  }

  @Test fun `adding user fails with invalid custom user ID`() {
    val creator = Stubs.userCreator.copy(id = " ")

    assertThat { service.addUser(creator, UserIdType.CUSTOM) }
      .isFailure()
      .all {
        hasClass(ResponseStatusException::class)
        messageContains("User ID")
      }
  }

  @Test fun `adding user fails with invalid raw signature`() {
    val creator = Stubs.userCreator.copy(rawSignature = " ")

    assertThat { service.addUser(creator, Stubs.project.userIdType) }
      .isFailure()
      .all {
        hasClass(ResponseStatusException::class)
        messageContains("Signature")
      }
  }

  @Test fun `adding user fails with invalid name`() {
    val creator = Stubs.userCreator.copy(name = " ")

    assertThat { service.addUser(creator, Stubs.project.userIdType) }
      .isFailure()
      .all {
        hasClass(ResponseStatusException::class)
        messageContains("Name")
      }
  }

  @Test fun `adding user fails with invalid contact email`() {
    val creator = Stubs.userCreator.copy(contactType = ContactType.EMAIL, contact = "invalid")

    assertThat { service.addUser(creator, Stubs.project.userIdType) }
      .isFailure()
      .all {
        hasClass(ResponseStatusException::class)
        messageContains("Contact Email")
      }
  }

  @Test fun `adding user fails with invalid contact phone`() {
    val creator = Stubs.userCreator.copy(contactType = ContactType.PHONE, contact = "invalid")

    assertThat { service.addUser(creator, Stubs.project.userIdType) }
      .isFailure()
      .all {
        hasClass(ResponseStatusException::class)
        messageContains("Contact Phone")
      }
  }

  @Test fun `adding user fails with invalid custom contact`() {
    val creator = Stubs.userCreator.copy(contactType = ContactType.CUSTOM, contact = " ")

    assertThat { service.addUser(creator, Stubs.project.userIdType) }
      .isFailure()
      .all {
        hasClass(ResponseStatusException::class)
        messageContains("Contact")
      }
  }

  @Test fun `adding user fails with invalid birthday`() {
    val fiveYearsMillis: Long = ChronoUnit.YEARS.duration.multipliedBy(5).toMillis()
    val creator = Stubs.userCreator.copy(birthday = Date(timeProvider.currentMillis - fiveYearsMillis))

    assertThat { service.addUser(creator, Stubs.project.userIdType) }
      .isFailure()
      .all {
        hasClass(ResponseStatusException::class)
        messageContains("Birthday")
      }
  }

  @Test fun `adding user fails with invalid organization`() {
    val creator = Stubs.userCreator.copy(company = Stubs.company.copy(countryCode = "D"))

    assertThat { service.addUser(creator, Stubs.project.userIdType) }
      .isFailure()
      .all {
        hasClass(ResponseStatusException::class)
        messageContains("Company")
      }
  }

  @DirtiesContext(methodMode = MethodMode.BEFORE_METHOD)
  @Test fun `adding user works with random ID`() {
    val creator = Stubs.userCreator.copy(id = null)
    stubGenerators()

    assertThat(service.addUser(creator, UserIdType.RANDOM))
      .isDataClassEqualTo(
        Stubs.user.copy(
          userId = Stubs.userId.copy(id = UserIdGenerator.nextId),
          verificationToken = TokenGenerator.nextEmailToken,
          ownedTokens = emptyList(),
          account = null,
          createdAt = timeProvider.currentDate,
          updatedAt = timeProvider.currentDate,
        )
      )
  }

  @DirtiesContext(methodMode = MethodMode.BEFORE_METHOD)
  @Test fun `adding user works with username ID`() {
    val creator = Stubs.userCreator.copy(id = "username")
    stubGenerators()

    assertThat(service.addUser(creator, UserIdType.USERNAME))
      .isDataClassEqualTo(
        Stubs.user.copy(
          userId = Stubs.userId.copy(id = "username"),
          verificationToken = TokenGenerator.nextEmailToken,
          ownedTokens = emptyList(),
          account = null,
          createdAt = timeProvider.currentDate,
          updatedAt = timeProvider.currentDate,
        )
      )
  }

  @DirtiesContext(methodMode = MethodMode.BEFORE_METHOD)
  @Test fun `adding user works with email ID`() {
    val creator = Stubs.userCreator.copy(id = "email@domain.com")
    stubGenerators()

    assertThat(service.addUser(creator, UserIdType.EMAIL))
      .isDataClassEqualTo(
        Stubs.user.copy(
          userId = Stubs.userId.copy(id = "email@domain.com"),
          verificationToken = TokenGenerator.nextEmailToken,
          ownedTokens = emptyList(),
          account = null,
          createdAt = timeProvider.currentDate,
          updatedAt = timeProvider.currentDate,
        )
      )
  }

  @DirtiesContext(methodMode = MethodMode.BEFORE_METHOD)
  @Test fun `adding user works with phone ID (and phone contact)`() {
    val creator = Stubs.userCreator.copy(
      id = "+491760000000",
      contactType = ContactType.PHONE,
      contact = "+491760000000",
    )
    stubGenerators()

    assertThat(service.addUser(creator, UserIdType.PHONE))
      .isDataClassEqualTo(
        Stubs.user.copy(
          userId = Stubs.userId.copy(id = "+491760000000"),
          verificationToken = TokenGenerator.nextPhoneToken,
          ownedTokens = emptyList(),
          contactType = ContactType.PHONE,
          contact = "+491760000000",
          account = null,
          createdAt = timeProvider.currentDate,
          updatedAt = timeProvider.currentDate,
        )
      )
  }

  @DirtiesContext(methodMode = MethodMode.BEFORE_METHOD)
  @Test fun `adding user works with custom ID (and no contact)`() {
    val creator = Stubs.userCreator.copy(id = "custom_id", contactType = ContactType.CUSTOM, contact = null)
    stubGenerators()

    assertThat(service.addUser(creator, UserIdType.CUSTOM))
      .isDataClassEqualTo(
        Stubs.user.copy(
          userId = Stubs.userId.copy(id = "custom_id"),
          verificationToken = null,
          ownedTokens = emptyList(),
          contactType = ContactType.CUSTOM,
          contact = null,
          account = null,
          createdAt = timeProvider.currentDate,
          updatedAt = timeProvider.currentDate,
        )
      )
  }

  // Fetching

  @Test fun `fetching user fails with invalid user ID`() {
    assertThat { service.fetchUserByUserId(Stubs.userId.copy(id = " "), withTokens = true) }
      .isFailure()
      .all {
        hasClass(ResponseStatusException::class)
        messageContains("User ID")
      }
  }

  @Test fun `fetching user works with a user ID`() {
    val storedUser = service.addUser(Stubs.userCreator, UserIdType.RANDOM).cleanDates()
    val fetchedUser = service.fetchUserByUserId(storedUser.userId, withTokens = true).cleanDates()

    assertThat(fetchedUser)
      .isDataClassEqualTo(storedUser)
  }

  @Test fun `fetching user fails with invalid unified ID`() {
    assertThat { service.fetchUserByUnifiedId(" ", withTokens = true) }
      .isFailure()
      .all {
        hasClass(ResponseStatusException::class)
        messageContains("User ID")
      }
  }

  @Test fun `fetching user works with a unified ID`() {
    val storedUser = service.addUser(Stubs.userCreator, UserIdType.RANDOM).cleanDates()
    val fetchedUser = service.fetchUserByUnifiedId(storedUser.userId.toUnifiedFormat(), withTokens = true).cleanDates()

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
    val creator = Stubs.userCreator.copy(contact = "contact@email.com")
    val storedUser = service.addUser(creator, UserIdType.RANDOM).cleanDates()
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
    val storedUser = service.addUser(Stubs.userCreator, UserIdType.RANDOM).cleanDates()
    val fetchedUsers = service.fetchAllUsersByProjectId(Stubs.userCreator.projectId)
      .map { it.cleanDates() }
      .filter { it.name != ROOT_OWNER_NAME } // root user is ignored

    assertThat(fetchedUsers)
      .isEqualTo(listOf(storedUser))
  }

  @Test fun `fetching users fails with invalid account ID`() {
    assertThat { service.fetchAllUsersByAccount(Stubs.account.copy(id = -1)) }
      .isFailure()
      .all {
        hasClass(ResponseStatusException::class)
        messageContains("Account ID")
      }
  }

  @Test fun `fetching users works with an admin ID`() {
    val adminAccount = adminRepo.getAdminProject().account
    val fetchedUsers = service.fetchAllUsersByAccount(adminAccount)
      .map { it.cleanDates() }

    assertThat(fetchedUsers)
      .all {
        hasSize(1)
        transform { it.first().name }
          .isEqualTo(ROOT_OWNER_NAME)
      }
  }

  // Updating

  @Test fun `updating user fails with invalid project ID`() {
    val updater = Stubs.userUpdater.copy(id = Stubs.userId.copy(projectId = -1))

    assertThat { service.updateUser(updater, UserIdType.CUSTOM) }
      .isFailure()
      .all {
        hasClass(ResponseStatusException::class)
        messageContains("User ID")
      }
  }

  @Test fun `updating user fails with invalid username`() {
    val updater = Stubs.userUpdater.copy(id = Stubs.userId.copy(id = " "))

    assertThat { service.updateUser(updater, UserIdType.USERNAME) }
      .isFailure()
      .all {
        hasClass(ResponseStatusException::class)
        messageContains("User ID")
      }
  }

  @Test fun `updating user fails with invalid email`() {
    val updater = Stubs.userUpdater.copy(id = Stubs.userId.copy(id = "invalid"))

    assertThat { service.updateUser(updater, UserIdType.EMAIL) }
      .isFailure()
      .all {
        hasClass(ResponseStatusException::class)
        messageContains("Email ID")
      }
  }

  @Test fun `updating user fails with invalid phone`() {
    val updater = Stubs.userUpdater.copy(id = Stubs.userId.copy(id = "invalid"))

    assertThat { service.updateUser(updater, UserIdType.PHONE) }
      .isFailure()
      .all {
        hasClass(ResponseStatusException::class)
        messageContains("Phone ID")
      }
  }

  @Test fun `updating user fails with invalid custom user ID`() {
    val updater = Stubs.userUpdater.copy(id = Stubs.userId.copy(id = " "))

    assertThat { service.updateUser(updater, UserIdType.CUSTOM) }
      .isFailure()
      .all {
        hasClass(ResponseStatusException::class)
        messageContains("User ID")
      }
  }

  @Test fun `updating user fails with invalid random user ID`() {
    val updater = Stubs.userUpdater.copy(id = Stubs.userId.copy(id = " "))

    assertThat { service.updateUser(updater, UserIdType.RANDOM) }
      .isFailure()
      .all {
        hasClass(ResponseStatusException::class)
        messageContains("User ID")
      }
  }

  @Test fun `updating user fails with invalid raw signature`() {
    val updater = Stubs.userUpdater.copy(rawSignature = Settable(" "))

    assertThat { service.updateUser(updater, Stubs.project.userIdType) }
      .isFailure()
      .all {
        hasClass(ResponseStatusException::class)
        messageContains("Signature")
      }
  }

  @Test fun `updating user fails with invalid contact email`() {
    val updater = Stubs.userUpdater.copy(
      contactType = Settable(ContactType.EMAIL),
      contact = Settable("invalid"),
    )

    assertThat { service.updateUser(updater, Stubs.project.userIdType) }
      .isFailure()
      .all {
        hasClass(ResponseStatusException::class)
        messageContains("Contact Email")
      }
  }

  @Test fun `updating user fails with invalid contact phone`() {
    val updater = Stubs.userUpdater.copy(
      contactType = Settable(ContactType.PHONE),
      contact = Settable("invalid"),
    )

    assertThat { service.updateUser(updater, Stubs.project.userIdType) }
      .isFailure()
      .all {
        hasClass(ResponseStatusException::class)
        messageContains("Contact Phone")
      }
  }

  @Test fun `updating user fails with invalid custom contact`() {
    val updater = Stubs.userUpdater.copy(
      contactType = Settable(ContactType.CUSTOM),
      contact = Settable(" "),
    )

    assertThat { service.updateUser(updater, Stubs.project.userIdType) }
      .isFailure()
      .all {
        hasClass(ResponseStatusException::class)
        messageContains("Contact")
      }
  }

  @Test fun `updating user fails with invalid name`() {
    val updater = Stubs.userUpdater.copy(name = Settable(" "))

    assertThat { service.updateUser(updater, Stubs.project.userIdType) }
      .isFailure()
      .all {
        hasClass(ResponseStatusException::class)
        messageContains("Name")
      }
  }

  @Test fun `updating user fails with invalid token`() {
    val updater = Stubs.userUpdater.copy(verificationToken = Settable(" "))

    assertThat { service.updateUser(updater, Stubs.project.userIdType) }
      .isFailure()
      .all {
        hasClass(ResponseStatusException::class)
        messageContains("Verification Token")
      }
  }

  @Test fun `updating user fails with invalid company name`() {
    val updater = Stubs.userUpdater.copy(
      company = Settable(
        Stubs.companyUpdater.copy(
          name = Settable(" "),
        )
      ),
    )

    assertThat { service.updateUser(updater, Stubs.project.userIdType) }
      .isFailure()
      .all {
        hasClass(ResponseStatusException::class)
        messageContains("Company Name")
      }
  }

  @Test fun `updating user fails with invalid company street`() {
    val updater = Stubs.userUpdater.copy(
      company = Settable(
        Stubs.companyUpdater.copy(
          street = Settable(" "),
        )
      ),
    )

    assertThat { service.updateUser(updater, Stubs.project.userIdType) }
      .isFailure()
      .all {
        hasClass(ResponseStatusException::class)
        messageContains("Company Street")
      }
  }

  @Test fun `updating user fails with invalid company postcode`() {
    val updater = Stubs.userUpdater.copy(
      company = Settable(
        Stubs.companyUpdater.copy(
          postcode = Settable(" "),
        )
      ),
    )

    assertThat { service.updateUser(updater, Stubs.project.userIdType) }
      .isFailure()
      .all {
        hasClass(ResponseStatusException::class)
        messageContains("Company Postcode")
      }
  }

  @Test fun `updating user fails with invalid company city`() {
    val updater = Stubs.userUpdater.copy(
      company = Settable(
        Stubs.companyUpdater.copy(
          city = Settable(" "),
        )
      ),
    )

    assertThat { service.updateUser(updater, Stubs.project.userIdType) }
      .isFailure()
      .all {
        hasClass(ResponseStatusException::class)
        messageContains("Company City")
      }
  }

  @Test fun `updating user fails with invalid company country code`() {
    val updater = Stubs.userUpdater.copy(
      company = Settable(
        Stubs.companyUpdater.copy(
          countryCode = Settable("d"),
        )
      ),
    )

    assertThat { service.updateUser(updater, Stubs.project.userIdType) }
      .isFailure()
      .all {
        hasClass(ResponseStatusException::class)
        messageContains("Company Country Code")
      }
  }

  @Test fun `updating user fails with invalid birthday`() {
    val fiveYearsMillis: Long = ChronoUnit.YEARS.duration.multipliedBy(5).toMillis()
    val tooYoungDate = Date(timeProvider.currentMillis - fiveYearsMillis)
    val updater = Stubs.userUpdater.copy(birthday = Settable(tooYoungDate))

    assertThat { service.updateUser(updater, Stubs.project.userIdType) }
      .isFailure()
      .all {
        hasClass(ResponseStatusException::class)
        messageContains("Birthday")
      }
  }

  @Test fun `updating user fails with invalid account`() {
    val updater = Stubs.userUpdater.copy(account = Settable(Stubs.account.copy(id = -1)))

    assertThat { service.updateUser(updater, Stubs.project.userIdType) }
      .isFailure()
      .all {
        hasClass(ResponseStatusException::class)
        messageContains("Account ID")
      }
  }

  @DirtiesContext(methodMode = MethodMode.BEFORE_METHOD)
  @Test fun `updating user works with changed data`() {
    stubGenerators()
    val creator = Stubs.userCreator.copy(
      contactType = ContactType.EMAIL,
      contact = "email@domain.com",
    )
    val storedUser = service.addUser(creator, UserIdType.RANDOM).cleanDates()

    val updater = Stubs.userUpdater.copy(
      id = storedUser.userId,
      account = null,
    )
    val updatedUser = service.updateUser(updater, UserIdType.RANDOM).cleanDates()

    assertThat(updatedUser)
      .isDataClassEqualTo(
        Stubs.userUpdated.copy(
          userId = storedUser.userId,
          verificationToken = TokenGenerator.nextPhoneToken,
          ownedTokens = emptyList(),
          account = null,
          createdAt = storedUser.createdAt,
          updatedAt = timeProvider.currentDate,
        ).cleanDates()
      )
  }

  @DirtiesContext(methodMode = MethodMode.BEFORE_METHOD)
  @Test fun `updating user works with erased data (with username ID)`() {
    stubGenerators()
    val creator = Stubs.userCreator.copy(
      contactType = ContactType.EMAIL,
      contact = "email@domain.com",
    )
    val storedUser = service.addUser(creator, UserIdType.USERNAME).cleanDates()

    val updater = UserUpdater(
      id = storedUser.userId,
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
      account = Settable(null),
    )
    val updatedUser = service.updateUser(updater, UserIdType.USERNAME).cleanDates()

    assertThat(updatedUser)
      .isDataClassEqualTo(
        storedUser.copy(
          userId = storedUser.userId,
          ownedTokens = emptyList(),
          name = null,
          contactType = ContactType.CUSTOM,
          contact = null,
          verificationToken = null,
          birthday = null,
          company = null,
          account = null,
          createdAt = storedUser.createdAt,
          updatedAt = timeProvider.currentDate,
        ).cleanDates()
      )
  }

  /*

  override fun removeUserById(userId: UserId) {
    log.debug("Removing user by $userId")
    val normalizedUserId = Normalizers.UserId.run(userId).requireValid { "User ID" }
    return userRepository.removeUserById(normalizedUserId)
  }

  override fun removeUserByUnifiedFormat(idHashProjectId: String) {
    log.debug("Removing user by $idHashProjectId")
    val normalizedIdHashProjectId = Normalizers.Dense.run(idHashProjectId).requireValid { "User ID" }
    return userRepository.removeUserByUnifiedFormat(normalizedIdHashProjectId)
  }

  override fun removeAllUsersByProjectId(projectId: Long) {
    log.debug("Removing all users from project $projectId")
    val normalizedProjectId = Normalizers.ProjectId.run(projectId).requireValid { "Project ID" }
    return userRepository.removeAllUsersByProjectId(normalizedProjectId)
  }

   */

  // Helpers

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
