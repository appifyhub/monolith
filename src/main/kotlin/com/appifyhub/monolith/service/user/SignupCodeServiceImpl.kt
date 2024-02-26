package com.appifyhub.monolith.service.user

import com.appifyhub.monolith.domain.user.SignupCode
import com.appifyhub.monolith.domain.user.UserId
import com.appifyhub.monolith.repository.user.SignupCodeRepository
import com.appifyhub.monolith.repository.user.UserRepository
import com.appifyhub.monolith.service.creator.CreatorService
import com.appifyhub.monolith.util.ext.requireValid
import com.appifyhub.monolith.util.ext.throwPreconditionFailed
import com.appifyhub.monolith.validation.impl.Normalizers
import org.slf4j.LoggerFactory
import org.springframework.stereotype.Service
import javax.transaction.Transactional

@Service
class SignupCodeServiceImpl(
  private val repository: SignupCodeRepository,
  private val userRepository: UserRepository,
  private val creatorService: CreatorService,
) : SignupCodeService {

  private val log = LoggerFactory.getLogger(this::class.java)

  override fun createCode(ownerId: UserId): SignupCode {
    log.debug("Creating a signup code for $ownerId")

    val normalizedOwnerId = Normalizers.UserId.run(ownerId).requireValid { "User ID" }

    val project = creatorService.fetchProjectById(normalizedOwnerId.projectId)
    val normalizedOwner = userRepository.fetchUserByUserId(normalizedOwnerId)
    val signupCodeCount = repository.fetchAllCodesByOwner(normalizedOwner).size

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

    return repository.fetchAllCodesByOwner(normalizedOwner)
  }

  @Transactional
  override fun markCodeUsed(code: String): SignupCode {
    log.debug("Marking a signup code as used $code")

    val normalizedCode = Normalizers.SignupCode.run(code).requireValid { "Signup Code" }

    return repository.markCodeUsed(normalizedCode)
  }

}
