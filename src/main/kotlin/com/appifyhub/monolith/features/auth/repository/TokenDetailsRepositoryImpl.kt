package com.appifyhub.monolith.features.auth.repository

import com.appifyhub.monolith.features.user.domain.toData
import com.appifyhub.monolith.features.user.domain.model.User
import com.appifyhub.monolith.features.auth.domain.model.TokenDetails
import com.appifyhub.monolith.features.auth.domain.security.JwtHelper
import com.appifyhub.monolith.features.auth.domain.toData
import com.appifyhub.monolith.features.auth.domain.toDomain
import com.appifyhub.monolith.features.auth.storage.TokenDetailsDao
import com.appifyhub.monolith.features.creator.domain.model.Project
import com.appifyhub.monolith.util.TimeProvider
import org.slf4j.LoggerFactory
import org.springframework.stereotype.Repository
import java.util.concurrent.TimeUnit

@Repository
class TokenDetailsRepositoryImpl(
  private val jwtHelper: JwtHelper,
  private val tokenDetailsDao: TokenDetailsDao,
  private val timeProvider: TimeProvider,
) : TokenDetailsRepository {

  private val log = LoggerFactory.getLogger(this::class.java)

  override fun addToken(token: TokenDetails): TokenDetails {
    log.debug("Adding token $token")
    return tokenDetailsDao.save(token.toData()).toDomain(jwtHelper)
  }

  override fun fetchTokenDetails(tokenValue: String): TokenDetails {
    log.debug("Finding token details for $tokenValue")
    return tokenDetailsDao.findById(tokenValue).get().toDomain(jwtHelper)
  }

  override fun fetchAllTokenDetails(tokenValues: List<String>): List<TokenDetails> {
    log.debug("Finding all token details for $tokenValues")
    return tokenDetailsDao.findAllById(tokenValues).map { it.toDomain(jwtHelper) }
  }

  override fun fetchAllBlockedTokens(owner: User, project: Project?): List<TokenDetails> {
    log.debug("Finding all blocked tokens for user $owner")
    return tokenDetailsDao.findAllByOwnerAndBlocked(
      owner = owner.toData(project),
      blocked = true,
    ).map { it.toDomain(jwtHelper) }
  }

  override fun fetchAllValidTokens(owner: User, project: Project?): List<TokenDetails> {
    log.debug("Finding all valid tokens for user $owner")
    return tokenDetailsDao.findAllByOwnerAndBlocked(
      owner = owner.toData(project),
      blocked = false,
    ).map { it.toDomain(jwtHelper) }
  }

  override fun fetchAllTokens(owner: User, project: Project?): List<TokenDetails> {
    log.debug("Finding all tokens for user $owner")
    return tokenDetailsDao.findAllByOwner(owner.toData(project)).map { it.toDomain(jwtHelper) }
  }

  override fun checkIsExpired(tokenValue: String): Boolean {
    log.debug("Checking if $tokenValue is a valid token")
    return tokenDetailsDao.findById(tokenValue).map { it.toDomain(jwtHelper).isExpired }.orElse(true)
  }

  override fun checkIsBlocked(tokenValue: String): Boolean {
    log.debug("Checking if $tokenValue is a blocked token")
    return tokenDetailsDao.findById(tokenValue).map { it.toDomain(jwtHelper).isBlocked }.orElse(true)
  }

  override fun checkIsStatic(tokenValue: String): Boolean {
    log.debug("Checking if $tokenValue is a static token")
    return tokenDetailsDao.findById(tokenValue).map { it.toDomain(jwtHelper).isStatic }.orElse(false)
  }

  override fun blockToken(tokenValue: String): TokenDetails {
    log.debug("Blocking token $tokenValue")
    val tokenDetails = tokenDetailsDao.findById(tokenValue).get().toDomain(jwtHelper)
    if (tokenDetails.isBlocked) return tokenDetails
    val blocked = tokenDetails.copy(isBlocked = true)
    return tokenDetailsDao.save(blocked.toData()).toDomain(jwtHelper)
  }

  override fun blockAllTokens(tokenValues: List<String>): List<TokenDetails> {
    log.debug("Blocking tokens $tokenValues")
    val allTokenDetails = tokenDetailsDao.findAllById(tokenValues).map { it.toDomain(jwtHelper) }
    if (allTokenDetails.isEmpty()) return allTokenDetails

    val valid = allTokenDetails.filter { !it.isExpired && !it.isBlocked }
    val toBlock = valid.map { it.copy(isBlocked = true) }

    return tokenDetailsDao.saveAll(toBlock.map(TokenDetails::toData)).map { it.toDomain(jwtHelper) }
  }

  override fun removeTokensFor(owner: User, project: Project?) {
    log.debug("Removing tokens for $owner, project=$project")

    tokenDetailsDao.deleteAllByOwner(owner.toData(project))
  }

  // Helpers

  private val TokenDetails.isExpired: Boolean
    get() {
      val secondsUntilExpired = TimeUnit.MILLISECONDS.toSeconds(expiresAt.time - timeProvider.currentMillis)
      return secondsUntilExpired < 0
    }

}
