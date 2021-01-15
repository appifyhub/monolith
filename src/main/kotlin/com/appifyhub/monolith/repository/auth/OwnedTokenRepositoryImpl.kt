package com.appifyhub.monolith.repository.auth

import com.appifyhub.monolith.domain.admin.Project
import com.appifyhub.monolith.domain.auth.OwnedToken
import com.appifyhub.monolith.domain.auth.Token
import com.appifyhub.monolith.domain.mapper.toData
import com.appifyhub.monolith.domain.mapper.toDomain
import com.appifyhub.monolith.domain.user.User
import com.appifyhub.monolith.storage.dao.OwnedTokenDao
import com.appifyhub.monolith.storage.model.auth.OwnedTokenDbm
import com.appifyhub.monolith.util.TimeProvider
import org.slf4j.LoggerFactory
import org.springframework.stereotype.Repository
import java.util.Date

@Repository
class OwnedTokenRepositoryImpl(
  private val ownedTokenDao: OwnedTokenDao,
  private val timeProvider: TimeProvider,
) : OwnedTokenRepository {

  private val log = LoggerFactory.getLogger(this::class.java)

  override fun addToken(user: User, token: Token, origin: String?): OwnedToken {
    log.debug("Adding token $token to user $user")
    val owned = OwnedToken(
      token = token,
      isBlocked = false,
      origin = origin,
      createdAt = Date(timeProvider.currentMillis),
      owner = user,
    )
    return ownedTokenDao.save(owned.toData()).toDomain()
  }

  override fun fetchTokenDetails(token: Token): OwnedToken {
    log.debug("Finding token details for $token")
    return ownedTokenDao.findById(token.toData()).get().toDomain()
  }

  override fun fetchAllBlockedTokens(owner: User, project: Project): List<OwnedToken> {
    log.debug("Finding all blocked tokens for user $owner")
    return ownedTokenDao.findAllByOwnerIsAndBlocked(
      owner = owner.toData(project),
      blocked = true,
    ).map(OwnedTokenDbm::toDomain)
  }

  override fun fetchAllValidTokens(owner: User, project: Project): List<OwnedToken> {
    log.debug("Finding all valid tokens for user $owner")
    return ownedTokenDao.findAllByOwnerIsAndBlocked(
      owner = owner.toData(project),
      blocked = false,
    ).map(OwnedTokenDbm::toDomain)
  }

  override fun fetchAllTokens(owner: User, project: Project): List<OwnedToken> {
    log.debug("Finding all tokens for user $owner")
    return ownedTokenDao.findAllByOwner(owner.toData(project)).map(OwnedTokenDbm::toDomain)
  }

  override fun checkIsValid(token: Token): Boolean {
    log.debug("Checking if $token is a valid token")
    return ownedTokenDao.findById(token.toData()).map { !it.toDomain().isBlocked }.orElse(false)
  }

  override fun checkIsBlocked(token: Token): Boolean {
    log.debug("Checking if $token is a blocked token")
    return ownedTokenDao.findById(token.toData()).map { it.toDomain().isBlocked }.orElse(true)
  }

  override fun blockToken(token: Token): OwnedToken {
    log.debug("Blocking token $token")
    val owned = ownedTokenDao.findById(token.toData()).get().toDomain()
    if (owned.isBlocked) return owned
    val blocked = owned.copy(isBlocked = true)
    return ownedTokenDao.save(blocked.toData()).toDomain()
  }

}