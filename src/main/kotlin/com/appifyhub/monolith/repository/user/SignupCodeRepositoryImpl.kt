package com.appifyhub.monolith.repository.user

import com.appifyhub.monolith.domain.mapper.toData
import com.appifyhub.monolith.domain.mapper.toDomain
import com.appifyhub.monolith.domain.user.SignupCode
import com.appifyhub.monolith.domain.user.User
import com.appifyhub.monolith.storage.dao.SignupCodeDao
import com.appifyhub.monolith.storage.model.user.SignupCodeDbm
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

}
