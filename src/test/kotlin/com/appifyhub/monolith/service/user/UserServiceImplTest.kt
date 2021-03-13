package com.appifyhub.monolith.service.user

import assertk.all
import assertk.assertThat
import assertk.assertions.hasClass
import assertk.assertions.isDataClassEqualTo
import assertk.assertions.isFailure
import assertk.assertions.messageContains
import com.appifyhub.monolith.TestAppifyHubApplication
import com.appifyhub.monolith.domain.admin.Project.UserIdType
import com.appifyhub.monolith.domain.user.User.ContactType
import com.appifyhub.monolith.repository.user.TokenGenerator
import com.appifyhub.monolith.repository.user.UserIdGenerator
import com.appifyhub.monolith.util.Stubs
import com.appifyhub.monolith.util.TimeProviderFake
import org.junit.jupiter.api.AfterEach
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

@ExtendWith(SpringExtension::class)
@ActiveProfiles(TestAppifyHubApplication.PROFILE)
@SpringBootTest(classes = [TestAppifyHubApplication::class])
class UserServiceImplTest {

  @Autowired lateinit var service: UserService
  @Autowired lateinit var timeProvider: TimeProviderFake

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
    val creator = Stubs.userCreator.copy(birthday = Date(System.currentTimeMillis()))
    timeProvider.staticTime = { 0 }

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
    val fiftyYearsMillis: Long = ChronoUnit.YEARS.duration.multipliedBy(50).toMillis()
    timeProvider.staticTime = { Stubs.userCreator.birthday!!.time + fiftyYearsMillis } // ensure valid birthday
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
    val fiftyYearsMillis: Long = ChronoUnit.YEARS.duration.multipliedBy(50).toMillis()
    timeProvider.staticTime = { Stubs.userCreator.birthday!!.time + fiftyYearsMillis } // ensure valid birthday
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
    val fiftyYearsMillis: Long = ChronoUnit.YEARS.duration.multipliedBy(50).toMillis()
    timeProvider.staticTime = { Stubs.userCreator.birthday!!.time + fiftyYearsMillis } // ensure valid birthday
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
    val fiftyYearsMillis: Long = ChronoUnit.YEARS.duration.multipliedBy(50).toMillis()
    timeProvider.staticTime = { Stubs.userCreator.birthday!!.time + fiftyYearsMillis } // ensure valid birthday
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
    val fiftyYearsMillis: Long = ChronoUnit.YEARS.duration.multipliedBy(50).toMillis()
    timeProvider.staticTime = { Stubs.userCreator.birthday!!.time + fiftyYearsMillis } // ensure valid birthday
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
    assertThat { service.fetchUserByUserId(Stubs.userId.copy(id = " "), withTokens = false) }
      .isFailure()
      .all {
        hasClass(ResponseStatusException::class)
        messageContains("User ID")
      }
  }

  /*

    override fun fetchUserByUserId(userId: UserId, withTokens: Boolean): User {
      log.debug("Fetching user by $userId")
      val normalizedUserId = Normalizers.UserId.run(userId).requireValid { "User ID" }
      return userRepository.fetchUserByUserId(normalizedUserId, withTokens = withTokens)
    }

    override fun fetchUserByUnifiedIdFormat(idHashProjectId: String, withTokens: Boolean): User {
      log.debug("Fetching user by $idHashProjectId")
      val normalizedIdHashProjectId = Normalizers.Dense.run(idHashProjectId).requireValid { "User ID" }
      return userRepository.fetchUserByUnifiedIdFormat(normalizedIdHashProjectId, withTokens = withTokens)
    }

    override fun fetchAllUsersByContact(contact: String): List<User> {
      log.debug("Fetching user by $contact")
      val normalizedContact = Normalizers.Trimmed.run(contact).requireValid { "Contact" }
      return userRepository.fetchAllUsersByContact(normalizedContact)
    }

    override fun fetchAllUsersByProjectId(projectId: Long): List<User> {
      log.debug("Fetching all users by project $projectId")
      val normalizedProjectId = Normalizers.ProjectId.run(projectId).requireValid { "Project ID" }
      return userRepository.fetchAllUsersByProjectId(normalizedProjectId)
    }

    override fun fetchAllUsersByAccount(account: Account): List<User> {
      log.debug("Fetching all users for account $account")
      Normalizers.AccountId.run(account.id).requireValid { "Account ID" }
      return userRepository.fetchAllUsersByAccount(account)
    }

    override fun updateUser(updater: UserUpdater, project: Project): User {
      log.debug("Updating user by $updater")

      // non-nullable properties

      Normalizers.UserId.run(updater.id).requireValid { "User ID" }
      val normalizedUserId = when (project.userIdType) {
        UserIdType.USERNAME -> Normalizers.Username.run(updater.id.id).requireValid { "Username ID" }
        UserIdType.EMAIL -> Normalizers.Email.run(updater.id.id).requireValid { "Email ID" }
        UserIdType.PHONE -> Normalizers.Phone.run(updater.id.id).requireValid { "Phone ID" }
        UserIdType.CUSTOM -> Normalizers.CustomUserId.run(updater.id.id).requireValid { "User ID" }
        UserIdType.RANDOM -> Normalizers.CustomUserId.run(updater.id.id).requireValid { "User ID" }
      }
      val normalizedId = UserId(normalizedUserId, project.id)
      val normalizedRawSignature = updater.rawSignature?.mapValueNonNull {
        Normalizers.RawSignature.run(it).requireValid { "Signature" }
      }

      // nullable properties

      val normalizedContactType: Settable<ContactType>?
      val normalizedContact: Settable<String?>?
      if (updater.contactType == null) {
        // contact type is required to change contact data
        normalizedContactType = null
        normalizedContact = null
      } else {
        if (updater.contact == null) {
          normalizedContactType = Settable(ContactType.CUSTOM)
          normalizedContact = null
        } else {
          normalizedContactType = updater.contactType
          normalizedContact = updater.contact.mapValueNullable {
            when (normalizedContactType.value) {
              ContactType.EMAIL -> Normalizers.Email.run(it).requireValid { "Contact Email" }
              ContactType.PHONE -> Normalizers.Phone.run(it).requireValid { "Contact Phone" }
              ContactType.CUSTOM -> Normalizers.CustomContact.run(it).requireValid { "Contact" }
            }
          }
        }
      }
      val normalizedName = updater.name?.mapValueNullable {
        Normalizers.Name.run(it).requireValid { "Name" }
      }
      val normalizedVerificationToken = updater.verificationToken?.mapValueNullable {
        Normalizers.DenseNullable.run(it).requireValid { "Verification Token" }
      }
      val normalizedBirthday = updater.birthday?.mapValueNullable {
        Normalizers.BDay.run(it to timeProvider).requireValid { "Birthday" }?.first
      }
      // each component needs to be validated manually (see OrganizationUpdater)
      val normalizedCompany = updater.company?.mapValueNullable { company ->
        OrganizationUpdater(
          name = company.name?.mapValueNullable {
            Normalizers.OrganizationName.run(it).requireValid { "Company Name" }
          },
          street = company.street?.mapValueNullable {
            Normalizers.OrganizationStreet.run(it).requireValid { "Company Street" }
          },
          postcode = company.postcode?.mapValueNullable {
            Normalizers.OrganizationPostcode.run(it).requireValid { "Company Postcode" }
          },
          city = company.city?.mapValueNullable {
            Normalizers.OrganizationCity.run(it).requireValid { "Company City" }
          },
          countryCode = company.countryCode?.mapValueNullable {
            Normalizers.OrganizationCountryCode.run(it).requireValid { "Company Country Code" }
          },
        )
      }
      updater.account?.value?.let {
        Normalizers.AccountId.run(it.id).requireValid { "Account ID" }
      }

      val normalizedUpdater = UserUpdater(
        id = normalizedId,
        rawSignature = normalizedRawSignature,
        type = updater.type,
        authority = updater.authority,
        contactType = normalizedContactType,
        allowsSpam = updater.allowsSpam,
        name = normalizedName,
        contact = normalizedContact,
        verificationToken = normalizedVerificationToken,
        birthday = normalizedBirthday,
        company = normalizedCompany,
        account = updater.account,
      )

      return userRepository.updateUser(normalizedUpdater, project)
    }

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

}
