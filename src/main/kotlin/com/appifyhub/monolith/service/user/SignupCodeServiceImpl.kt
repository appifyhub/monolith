package com.appifyhub.monolith.service.user

import com.appifyhub.monolith.domain.user.SignupCode
import com.appifyhub.monolith.domain.user.UserId
import com.appifyhub.monolith.repository.user.SignupCodeRepository
import com.appifyhub.monolith.repository.user.UserRepository
import com.appifyhub.monolith.service.creator.CreatorService
import com.appifyhub.monolith.util.TimeProvider
import com.appifyhub.monolith.util.extension.requireValid
import com.appifyhub.monolith.util.extension.throwPreconditionFailed
import com.appifyhub.monolith.validation.impl.Normalizers
import org.slf4j.LoggerFactory
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional

@Service
class SignupCodeServiceImpl(
  private val repository: SignupCodeRepository,
  private val userRepository: UserRepository,
  private val creatorService: CreatorService,
  private val timeProvider: TimeProvider,
) : SignupCodeService {

  private val log = LoggerFactory.getLogger(this::class.java)

  override fun createCode(ownerId: UserId): SignupCode {
    log.debug("Creating a signup code for $ownerId")

    val normalizedOwnerId = Normalizers.UserId.run(ownerId).requireValid { "User ID" }

    val project = creatorService.fetchProjectById(normalizedOwnerId.projectId)
    val normalizedOwner = userRepository.fetchUserByUserId(normalizedOwnerId)
    val signupCodeCount = repository.fetchAllSignupCodesByOwner(normalizedOwner).size

    if (signupCodeCount >= project.maxSignupCodesPerUser) {
      throwPreconditionFailed {
        "User $ownerId has reached the maximum number of signup codes (${project.maxSignupCodesPerUser})"
      }
    }

    return repository.createCode(normalizedOwner)
  }

  override fun fetchAllCodesByOwner(ownerId: UserId): List<SignupCode> {
    log.debug("Fetching all signup codes by owner $ownerId")

    val normalizedOwnerId = Normalizers.UserId.run(ownerId).requireValid { "User ID" }
    val normalizedOwner = userRepository.fetchUserByUserId(normalizedOwnerId)

    return repository.fetchAllSignupCodesByOwner(normalizedOwner)
  }

  @Transactional(rollbackFor = [Exception::class])
  override fun markCodeUsed(code: String, projectId: Long): SignupCode {
    log.debug("Marking a signup code as used $code")

    val normalizedCode = Normalizers.SignupCode.run(code).requireValid { "Signup Code" }
    val normalizedProjectId = Normalizers.ProjectId.run(projectId).requireValid { "Project ID" }
    val signupCode = repository.fetchSignupCodeById(normalizedCode)

    if (signupCode.isUsed) throwPreconditionFailed {
      "Signup code already used"
    }

    if (signupCode.owner.id.projectId != normalizedProjectId) throwPreconditionFailed {
      "Signup code does not belong to the same project as the user"
    }

    val signupCodeToSave = signupCode.copy(isUsed = true, usedAt = timeProvider.currentDate)
    return repository.saveSignupCode(signupCodeToSave)
  }

}
