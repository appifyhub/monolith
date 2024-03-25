package com.appifyhub.monolith.features.user.repository

import com.appifyhub.monolith.features.user.domain.model.SignupCode
import com.appifyhub.monolith.features.user.domain.model.User
import com.appifyhub.monolith.features.user.domain.toData
import com.appifyhub.monolith.features.user.domain.toDomain
import com.appifyhub.monolith.features.user.repository.util.SignupCodeGenerator
import com.appifyhub.monolith.features.user.storage.SignupCodeDao
import com.appifyhub.monolith.features.user.storage.model.SignupCodeDbm
import com.appifyhub.monolith.util.TimeProvider
import org.slf4j.LoggerFactory
import org.springframework.stereotype.Repository

@Repository
class SignupCodeRepositoryImpl(
  private val signupCodeDao: SignupCodeDao,
  private val timeProvider: TimeProvider,
) : SignupCodeRepository {

  private val log = LoggerFactory.getLogger(this::class.java)

  override fun createCode(owner: User): SignupCode {
    log.debug("Creating signup code for user $owner")

    val code = SignupCodeGenerator.nextCode
    val signupCode = SignupCode(
      code = code,
      isUsed = false,
      owner = owner,
      createdAt = timeProvider.currentDate,
      usedAt = null,
    )

    return signupCodeDao.save(signupCode.toData()).toDomain()
  }

  override fun fetchSignupCodeById(code: String): SignupCode {
    log.debug("Fetching signup code $code")

    return signupCodeDao.findById(code).get().toDomain()
  }

  override fun fetchAllSignupCodesByOwner(owner: User): List<SignupCode> {
    log.debug("Fetching signup codes by user $owner")

    return signupCodeDao.findAllByOwner(owner.toData()).map(SignupCodeDbm::toDomain)
  }

  override fun saveSignupCode(signupCode: SignupCode): SignupCode {
    log.debug("Saving signup code $signupCode")

    return signupCodeDao.save(signupCode.toData()).toDomain()
  }

  override fun deleteAllByOwner(owner: User) {
    log.debug("Deleting all signup codes for user $owner")

    signupCodeDao.deleteAllByOwner(owner.toData())
  }

}
