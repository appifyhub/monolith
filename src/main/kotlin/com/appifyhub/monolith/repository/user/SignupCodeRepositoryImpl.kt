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
import javax.transaction.Transactional
import kotlin.jvm.optionals.getOrNull

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

  override fun fetchAllCodesByOwner(owner: User): List<SignupCode> {
    log.debug("Fetching signup codes by user $owner")

    return signupCodeDao.findAllByOwner(owner.toData()).map(SignupCodeDbm::toDomain)
  }

  @Transactional // to prevent concurrency issues
  override fun markCodeUsed(code: String): SignupCode {
    log.debug("Marking signup code $code as used")

    val signupCode = signupCodeDao.findById(code).getOrNull()?.toDomain()
    if (signupCode == null) {
      log.info("Signup code $code not found")
      throw IllegalStateException("Signup code not found")
    }

    if (signupCode.isUsed) {
      log.info("Signup code $code already used")
      throw IllegalStateException("Signup code already used")
    }

    val signupCodeToSave = signupCode.copy(
      isUsed = true,
      usedAt = timeProvider.currentDate,
    )

    return signupCodeDao.save(signupCodeToSave.toData()).toDomain()
  }

}
