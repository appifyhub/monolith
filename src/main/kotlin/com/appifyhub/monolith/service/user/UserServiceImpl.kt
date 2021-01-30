package com.appifyhub.monolith.service.user

import com.appifyhub.monolith.domain.admin.Account
import com.appifyhub.monolith.domain.admin.Project
import com.appifyhub.monolith.domain.admin.Project.UserIdType
import com.appifyhub.monolith.domain.common.Settable
import com.appifyhub.monolith.domain.common.mapValueNonNull
import com.appifyhub.monolith.domain.common.mapValueNullable
import com.appifyhub.monolith.domain.user.User
import com.appifyhub.monolith.domain.user.User.Authority
import com.appifyhub.monolith.domain.user.User.ContactType
import com.appifyhub.monolith.domain.user.UserId
import com.appifyhub.monolith.domain.user.ops.OrganizationUpdater
import com.appifyhub.monolith.domain.user.ops.UserCreator
import com.appifyhub.monolith.domain.user.ops.UserUpdater
import com.appifyhub.monolith.repository.user.UserRepository
import com.appifyhub.monolith.service.validation.Normalizers
import com.appifyhub.monolith.util.TimeProvider
import com.appifyhub.monolith.util.ext.requireValid
import org.slf4j.LoggerFactory
import org.springframework.stereotype.Service

@Service
class UserServiceImpl(
  private val userRepository: UserRepository,
  private val timeProvider: TimeProvider,
) : UserService {

  enum class UserPrivilege(val level: Authority) {
    READ(Authority.MODERATOR),
    WRITE(Authority.ADMIN),
    ;
  }

  private val log = LoggerFactory.getLogger(this::class.java)

  override fun addUser(creator: UserCreator, project: Project): User {
    log.debug("Adding user $creator")

    val normalizedId = when (project.userIdType) {
      UserIdType.USERNAME -> Normalizers.Username.run(creator.id).requireValid { "Username ID" }
      UserIdType.EMAIL -> Normalizers.Email.run(creator.id).requireValid { "Email ID" }
      UserIdType.PHONE -> Normalizers.Phone.run(creator.id).requireValid { "Phone ID" }
      UserIdType.CUSTOM -> Normalizers.CustomUserId.run(creator.id).requireValid { "User ID" }
      UserIdType.RANDOM -> null
    }
    val normalizedRawSignature = Normalizers.RawSignature.run(creator.rawSignature).requireValid { "Signature" }
    val normalizedName = Normalizers.Name.run(creator.name).requireValid { "Name" }
    val normalizedContact = when (creator.contactType) {
      ContactType.EMAIL -> Normalizers.Email.run(creator.contact).requireValid { "Contact Email" }
      ContactType.PHONE -> Normalizers.Phone.run(creator.contact).requireValid { "Contact Phone" }
      ContactType.CUSTOM -> Normalizers.CustomContact.run(creator.contact).requireValid { "Contact" }
    }
    val normalizedBirthday = Normalizers.BDay.run(creator.birthday to timeProvider).requireValid { "Birthday" }?.first
    val normalizedCompany = Normalizers.Organization.run(creator.company).requireValid { "Organization" }

    val normalizedCreator = UserCreator(
      id = normalizedId,
      projectId = project.id,
      rawSignature = normalizedRawSignature,
      name = normalizedName,
      type = creator.type,
      authority = creator.authority,
      allowsSpam = creator.allowsSpam,
      contact = normalizedContact,
      contactType = creator.contactType,
      birthday = normalizedBirthday,
      company = normalizedCompany,
    )

    return userRepository.addUser(normalizedCreator, project)
  }

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

}