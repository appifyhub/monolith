package com.appifyhub.monolith.repository.auth

import com.appifyhub.monolith.domain.admin.Project
import com.appifyhub.monolith.domain.auth.OwnedToken
import com.appifyhub.monolith.domain.auth.Token
import com.appifyhub.monolith.domain.common.stubUser
import com.appifyhub.monolith.domain.mapper.toData
import com.appifyhub.monolith.domain.mapper.toDomain
import com.appifyhub.monolith.domain.user.User
import com.appifyhub.monolith.domain.user.UserId
import com.appifyhub.monolith.storage.dao.OwnedTokenDao
import com.appifyhub.monolith.storage.model.auth.OwnedTokenDbm
import com.appifyhub.monolith.util.TimeProvider
import org.slf4j.LoggerFactory
import org.springframework.stereotype.Repository
import java.util.Date
import java.util.concurrent.TimeUnit

@Repository
class OwnedTokenRepositoryImpl(
  private val ownedTokenDao: OwnedTokenDao,
  private val timeProvider: TimeProvider,
) : OwnedTokenRepository {

  private val log = LoggerFactory.getLogger(this::class.java)

  override fun addToken(
    userId: UserId,
    token: Token,
    createdAt: Date,
    expiresAt: Date,
    origin: String?,
  ): OwnedToken {
    log.debug("Adding token $token to user $userId")
    val owned = OwnedToken(
      token = token,
      isBlocked = false,
      origin = origin,
      createdAt = createdAt,
      expiresAt = expiresAt,
      owner = stubUser().copy(userId = userId),
    )
    return ownedTokenDao.save(owned.toData()).toDomain()
  }

  override fun fetchTokenDetails(token: Token): OwnedToken {
    log.debug("Finding token details for $token")
    return ownedTokenDao.findById(token.tokenLocator).get().toDomain()
  }

  override fun fetchAllTokenDetails(tokens: List<Token>): List<OwnedToken> {
    log.debug("Finding all token details for $tokens")
    return ownedTokenDao.findAllById(tokens.map(Token::tokenLocator)).map(OwnedTokenDbm::toDomain)
  }

  override fun fetchAllBlockedTokens(owner: User, project: Project): List<OwnedToken> {
    log.debug("Finding all blocked tokens for user $owner")
    return ownedTokenDao.findAllByOwnerAndBlocked(
      owner = owner.toData(project),
      blocked = true,
    ).map(OwnedTokenDbm::toDomain)
  }

  override fun fetchAllValidTokens(owner: User, project: Project): List<OwnedToken> {
    log.debug("Finding all valid tokens for user $owner")
    return ownedTokenDao.findAllByOwnerAndBlocked(
      owner = owner.toData(project),
      blocked = false,
    ).map(OwnedTokenDbm::toDomain)
  }

  override fun fetchAllTokens(owner: User, project: Project): List<OwnedToken> {
    log.debug("Finding all tokens for user $owner")
    return ownedTokenDao.findAllByOwner(owner.toData(project)).map(OwnedTokenDbm::toDomain)
  }

  override fun checkIsExpired(token: Token): Boolean {
    log.debug("Checking if $token is a valid token")
    return ownedTokenDao.findById(token.tokenLocator).map { it.toDomain().isExpired }.orElse(true)
  }

  override fun checkIsBlocked(token: Token): Boolean {
    log.debug("Checking if $token is a blocked token")
    return ownedTokenDao.findById(token.tokenLocator).map { it.toDomain().isBlocked }.orElse(true)
  }

  override fun blockToken(token: Token): OwnedToken {
    log.debug("Blocking token $token")
    val ownedToken = ownedTokenDao.findById(token.tokenLocator).get().toDomain()
    if (ownedToken.isBlocked) return ownedToken
    val blocked = ownedToken.copy(isBlocked = true)
    return ownedTokenDao.save(blocked.toData()).toDomain()
  }

  override fun blockAllTokens(tokens: List<Token>): List<OwnedToken> {
    log.debug("Blocking tokens $tokens")
    val tokensData = tokens.map(Token::tokenLocator)
    val ownedTokens = ownedTokenDao.findAllById(tokensData).map(OwnedTokenDbm::toDomain)
    if (ownedTokens.isEmpty()) return ownedTokens

    val valid = ownedTokens.filter { !it.isExpired && !it.isBlocked }
    val toBlock = valid.map { it.copy(isBlocked = true) }

    return ownedTokenDao.saveAll(toBlock.map(OwnedToken::toData)).map(OwnedTokenDbm::toDomain)
  }

  override fun blockAllTokensFromModel(owner: User): List<OwnedToken> {
    log.debug("Blocking tokens for $owner")

    val valid = owner.ownedTokens.filter { !it.isExpired && !it.isBlocked }
    val toBlock = valid.map { it.copy(isBlocked = true) }

    return ownedTokenDao.saveAll(toBlock.map(OwnedToken::toData)).also {
      val expired = owner.ownedTokens.filter { it.isExpired }
      ownedTokenDao.deleteAll(expired.map(OwnedToken::toData)) // housekeeping
    }.map(OwnedTokenDbm::toDomain)
  }

  // Helpers

  private val OwnedToken.isExpired: Boolean
    get() {
      val secondsUntilExpired = TimeUnit.MILLISECONDS.toSeconds(expiresAt.time - timeProvider.currentMillis)
      return secondsUntilExpired < 0
    }

}
