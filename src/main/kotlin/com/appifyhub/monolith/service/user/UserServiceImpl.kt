package com.appifyhub.monolith.service.user

import com.appifyhub.monolith.domain.common.Settable
import com.appifyhub.monolith.domain.common.mapValueNonNull
import com.appifyhub.monolith.domain.common.mapValueNullable
import com.appifyhub.monolith.domain.creator.Project.UserIdType
import com.appifyhub.monolith.domain.user.User
import com.appifyhub.monolith.domain.user.User.ContactType
import com.appifyhub.monolith.domain.user.UserId
import com.appifyhub.monolith.domain.user.ops.OrganizationUpdater
import com.appifyhub.monolith.domain.user.ops.UserCreator
import com.appifyhub.monolith.domain.user.ops.UserUpdater
import com.appifyhub.monolith.eventbus.EventPublisher
import com.appifyhub.monolith.eventbus.UserAuthResetCompleted
import com.appifyhub.monolith.eventbus.UserCreated
import com.appifyhub.monolith.repository.creator.SignatureGenerator
import com.appifyhub.monolith.repository.user.UserRepository
import com.appifyhub.monolith.service.creator.CreatorService
import com.appifyhub.monolith.util.TimeProvider
import com.appifyhub.monolith.util.ext.requireValid
import com.appifyhub.monolith.util.ext.throwPreconditionFailed
import com.appifyhub.monolith.validation.impl.Normalizers
import org.slf4j.LoggerFactory
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional

@Service
class UserServiceImpl(
  private val userRepository: UserRepository,
  private val creatorService: CreatorService,
  private val signupCodeService: SignupCodeService,
  private val eventPublisher: EventPublisher,
  private val timeProvider: TimeProvider,
) : UserService {

  private val log = LoggerFactory.getLogger(this::class.java)

  @Transactional(rollbackFor = [Exception::class])
  override fun addUser(creator: UserCreator): User {
    log.debug("Adding user $creator")

    val project = creatorService.fetchProjectById(creator.projectId)
    val userIdType = project.userIdType
    val normalizedId = when (userIdType) {
      UserIdType.USERNAME -> Normalizers.Username.run(creator.userId).requireValid { "Username ID" }
      UserIdType.EMAIL -> Normalizers.Email.run(creator.userId).requireValid { "Email ID" }
      UserIdType.PHONE -> Normalizers.Phone.run(creator.userId).requireValid { "Phone ID" }
      UserIdType.CUSTOM -> Normalizers.CustomUserId.run(creator.userId).requireValid { "User ID" }
      UserIdType.RANDOM -> null
    }
    val normalizedRawSignature = Normalizers.RawSignature.run(creator.rawSignature).requireValid { "Signature" }
    val normalizedName = Normalizers.Name.run(creator.name).requireValid { "Name" }
    val normalizedContact = when (creator.contactType) {
      ContactType.EMAIL -> Normalizers.Email.run(creator.contact).requireValid { "Contact Email" }
      ContactType.PHONE -> Normalizers.Phone.run(creator.contact).requireValid { "Contact Phone" }
      ContactType.CUSTOM -> Normalizers.CustomContact.run(creator.contact).requireValid { "Contact" }
    }
    val normalizedContactType = if (normalizedContact == null) ContactType.CUSTOM else creator.contactType
    val normalizedCompany = Normalizers.Organization.run(creator.company).requireValid { "Company" }
    val normalizedBirthday = Normalizers.BDay.run(creator.birthday to timeProvider).requireValid { "Birthday" }?.first
    val normalizedLanguageTag = Normalizers.LanguageTag.run(creator.languageTag).requireValid { "Language Tag" }
    val normalizedSignupCode = if (project.requiresSignupCodes)
      Normalizers.SignupCode.run(creator.signupCode).requireValid { "Signup Code" }
    else null

    val normalizedCreator = UserCreator(
      userId = normalizedId,
      projectId = creator.projectId,
      rawSignature = normalizedRawSignature,
      name = normalizedName,
      type = creator.type,
      authority = creator.authority,
      allowsSpam = creator.allowsSpam,
      contact = normalizedContact,
      contactType = normalizedContactType,
      birthday = normalizedBirthday,
      company = normalizedCompany,
      languageTag = normalizedLanguageTag,
      signupCode = normalizedSignupCode,
    )

    // check project requirement: max users
    val maxUsers = creatorService.fetchProjectById(normalizedCreator.projectId).maxUsers
    val totalUsers = userRepository.countUsers(normalizedCreator.projectId)
    if (totalUsers >= maxUsers) throwPreconditionFailed { "Maximum users reached" }

    // check project requirements: signup codes
    if (project.requiresSignupCodes) {
      // safe because the whole block is transactional
      val signupCode = signupCodeService.markCodeUsed(normalizedSignupCode!!, normalizedCreator.projectId)
      log.info("Signup code $signupCode used for user ${normalizedCreator.userId}")
    }

    return userRepository.addUser(normalizedCreator, userIdType)
      .also { result ->
        eventPublisher.publish(UserCreated(ownerProject = project, payload = result))
      }
  }

  override fun fetchUserByUserId(id: UserId): User {
    log.debug("Fetching user by $id")
    val normalizedUserId = Normalizers.UserId.run(id).requireValid { "User ID" }
    return userRepository.fetchUserByUserId(normalizedUserId)
  }

  override fun fetchUserByUniversalId(universalId: String): User {
    log.debug("Fetching user by $universalId")
    val normalizedUniversalId = Normalizers.Dense.run(universalId).requireValid { "User ID" }
    return userRepository.fetchUserByUniversalId(normalizedUniversalId)
  }

  override fun fetchAllUsersByProjectId(projectId: Long): List<User> {
    log.debug("Fetching all users by project $projectId")
    val normalizedProjectId = Normalizers.ProjectId.run(projectId).requireValid { "Project ID" }
    return userRepository.fetchAllUsersByProjectId(normalizedProjectId)
  }

  override fun fetchUserByUserIdAndVerificationToken(userId: UserId, verificationToken: String): User {
    log.debug("Fetching user by $userId, token=$verificationToken")
    val normalizedUserId = Normalizers.UserId.run(userId).requireValid { "User ID" }
    val normalizedVerificationToken = Normalizers.Dense.run(verificationToken).requireValid { "Verification Token" }

    return userRepository.fetchUserByUserIdAndVerificationToken(normalizedUserId, normalizedVerificationToken)
  }

  override fun searchByName(projectId: Long, name: String): List<User> {
    log.debug("Searching users by name $name in $projectId")
    val normalizedProjectId = Normalizers.ProjectId.run(projectId).requireValid { "Project ID" }
    val normalizedName = Normalizers.NotBlank.run(name).requireValid { "Name" }
    return userRepository.searchByName(normalizedProjectId, normalizedName)
  }

  override fun searchByContact(projectId: Long, contact: String): List<User> {
    log.debug("Searching users by contact $contact in $projectId")
    val normalizedProjectId = Normalizers.ProjectId.run(projectId).requireValid { "Project ID" }
    val normalizedContact = Normalizers.NotBlank.run(contact).requireValid { "Contact" }
    return userRepository.searchByContact(normalizedProjectId, normalizedContact)
  }

  override fun updateUser(updater: UserUpdater): User {
    log.debug("Updating user by $updater")

    // non-nullable properties
    Normalizers.UserId.run(updater.id).requireValid { "User ID" }
    val userIdType = creatorService.fetchProjectById(updater.id.projectId).userIdType
    val normalizedUserId = when (userIdType) {
      UserIdType.USERNAME -> Normalizers.Username.run(updater.id.userId).requireValid { "Username ID" }
      UserIdType.EMAIL -> Normalizers.Email.run(updater.id.userId).requireValid { "Email ID" }
      UserIdType.PHONE -> Normalizers.Phone.run(updater.id.userId).requireValid { "Phone ID" }
      UserIdType.CUSTOM -> Normalizers.CustomUserId.run(updater.id.userId).requireValid { "User ID" }
      UserIdType.RANDOM -> Normalizers.CustomUserId.run(updater.id.userId).requireValid { "User ID" }
    }
    val normalizedId = UserId(normalizedUserId, updater.id.projectId)
    val normalizedRawSignature = updater.rawSignature?.mapValueNonNull {
      Normalizers.RawSignature.run(it).requireValid { "Signature" }
    }

    // nullable properties

    val normalizedContactType: Settable<ContactType>?
    val normalizedContact: Settable<String?>?
    when {
      // contact is being erased, we force contact type to 'custom'
      updater.contact != null && updater.contact.value == null -> {
        normalizedContactType = Settable(ContactType.CUSTOM)
        normalizedContact = Settable(null)
      }
      // contact type is not set at all, we ignore the request
      updater.contactType?.value == null || updater.contact == null -> {
        normalizedContactType = null
        normalizedContact = null
      }

      else -> {
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
    val normalizedBirthday = updater.birthday?.mapValueNullable {
      Normalizers.BDay.run(it to timeProvider).requireValid { "Birthday" }?.first
    }
    val normalizedLanguageTag = updater.languageTag?.mapValueNullable {
      Normalizers.LanguageTag.run(it).requireValid { "Language Tag" }
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
      languageTag = normalizedLanguageTag,
    )

    return userRepository.updateUser(normalizedUpdater, userIdType)
  }

  override fun resetSignatureById(id: UserId): User {
    log.debug("Resetting signature by $id")

    val normalizedUserId = Normalizers.UserId.run(id).requireValid { "User ID" }
    val project = creatorService.fetchProjectById(normalizedUserId.projectId)
    val newSignature = SignatureGenerator.nextSignature
    val updater = UserUpdater(
      id = normalizedUserId,
      rawSignature = Settable(newSignature),
    )

    return userRepository.updateUser(updater, project.userIdType)
      .also { result ->
        val payload = result.copy(signature = newSignature)
        eventPublisher.publish(UserAuthResetCompleted(ownerProject = project, payload = payload))
      }
  }

  override fun removeUserById(id: UserId) {
    log.debug("Removing user by $id")
    val normalizedUserId = Normalizers.UserId.run(id).requireValid { "User ID" }
    return userRepository.removeUserById(normalizedUserId)
  }

  override fun removeUserByUniversalId(universalId: String) {
    log.debug("Removing user by $universalId")
    val normalizedIdHashProjectId = Normalizers.Dense.run(universalId).requireValid { "User ID" }
    return userRepository.removeUserByUniversalId(normalizedIdHashProjectId)
  }

  override fun removeAllUsersByProjectId(projectId: Long) {
    log.debug("Removing all users from project $projectId")
    val normalizedProjectId = Normalizers.ProjectId.run(projectId).requireValid { "Project ID" }
    return userRepository.removeAllUsersByProjectId(normalizedProjectId)
  }

}
