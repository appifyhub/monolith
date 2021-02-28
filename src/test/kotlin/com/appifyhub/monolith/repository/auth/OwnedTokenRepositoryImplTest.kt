package com.appifyhub.monolith.repository.auth

import assertk.assertAll
import assertk.assertThat
import assertk.assertions.isDataClassEqualTo
import assertk.assertions.isEqualTo
import assertk.assertions.isFalse
import assertk.assertions.isTrue
import com.appifyhub.monolith.domain.mapper.toData
import com.appifyhub.monolith.storage.dao.OwnedTokenDao
import com.appifyhub.monolith.storage.model.auth.OwnedTokenDbm
import com.appifyhub.monolith.util.Stubs
import com.appifyhub.monolith.util.TimeProviderFake
import com.nhaarman.mockitokotlin2.any
import com.nhaarman.mockitokotlin2.doAnswer
import com.nhaarman.mockitokotlin2.doReturn
import com.nhaarman.mockitokotlin2.mock
import com.nhaarman.mockitokotlin2.never
import com.nhaarman.mockitokotlin2.stub
import com.nhaarman.mockitokotlin2.verify
import org.junit.jupiter.api.AfterEach
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import java.util.Optional
import java.util.concurrent.TimeUnit

class OwnedTokenRepositoryImplTest {

  private val ownedTokenDao = mock<OwnedTokenDao>()
  private val timeProvider = TimeProviderFake()

  private val repository: OwnedTokenRepository = OwnedTokenRepositoryImpl(
    ownedTokenDao = ownedTokenDao,
    timeProvider = timeProvider,
  )

  @BeforeEach fun setup() {
    ownedTokenDao.stub {
      onGeneric { save(any()) } doAnswer { it.arguments.first() as OwnedTokenDbm }
    }
  }

  @AfterEach fun teardown() {
    timeProvider.staticTime = { null }
  }

  @Test fun `adding token data saves an owned token`() {
    val token = repository.addToken(
      userId = Stubs.userId,
      token = Stubs.token,
      createdAt = Stubs.ownedToken.createdAt,
      expiresAt = Stubs.ownedToken.expiresAt,
      origin = Stubs.ownedToken.origin,
    )

    assertAll {
      // separate ID check because token user is stubbed
      // internally and wouldn't match the test stub
      assertThat(token.owner.userId)
        .isDataClassEqualTo(Stubs.userId)
      assertThat(token)
        .transform { it.copy(owner = Stubs.ownedToken.owner) }
        .isDataClassEqualTo(
          Stubs.ownedToken.copy(
            isBlocked = false,
          )
        )
    }
  }

  @Test fun `fetching token details works`() {
    ownedTokenDao.stub {
      onGeneric { findById(Stubs.token.tokenLocator) } doReturn Optional.of(Stubs.ownedTokenDbm)
    }

    assertThat(repository.fetchTokenDetails(Stubs.token))
      // token DAO knows nothing about users, so:
      .transform { it.copy(owner = Stubs.ownedToken.owner) }
      .isDataClassEqualTo(Stubs.ownedToken)
  }

  @Test fun `fetching all token details works`() {
    ownedTokenDao.stub {
      onGeneric { findAllById(listOf(Stubs.token.tokenLocator)) } doReturn listOf(Stubs.ownedTokenDbm)
    }

    assertThat(repository.fetchAllTokenDetails(listOf(Stubs.token)))
      // token DAO knows nothing about users, so:
      .transform { tokens -> tokens.map { it.copy(owner = Stubs.ownedToken.owner) } }
      .isEqualTo(listOf(Stubs.ownedToken))
  }

  @Test fun `fetching all blocked tokens works`() {
    ownedTokenDao.stub {
      onGeneric { findAllByOwnerAndBlocked(Stubs.userDbm, blocked = true) } doReturn listOf(Stubs.ownedTokenDbm)
    }

    assertThat(repository.fetchAllBlockedTokens(Stubs.user, Stubs.project))
      // token DAO knows nothing about users, so:
      .transform { tokens -> tokens.map { it.copy(owner = Stubs.ownedToken.owner) } }
      .isEqualTo(listOf(Stubs.ownedToken))
  }

  @Test fun `fetching all valid tokens works`() {
    ownedTokenDao.stub {
      onGeneric { findAllByOwnerAndBlocked(Stubs.userDbm, blocked = false) } doReturn listOf(Stubs.ownedTokenDbm)
    }

    assertThat(repository.fetchAllValidTokens(Stubs.user, Stubs.project))
      // token DAO knows nothing about users, so:
      .transform { tokens -> tokens.map { it.copy(owner = Stubs.ownedToken.owner) } }
      .isEqualTo(listOf(Stubs.ownedToken))
  }

  @Test fun `fetching all tokens works`() {
    ownedTokenDao.stub {
      onGeneric { findAllByOwner(Stubs.userDbm) } doReturn listOf(Stubs.ownedTokenDbm)
    }

    assertThat(repository.fetchAllTokens(Stubs.user, Stubs.project))
      // token DAO knows nothing about users, so:
      .transform { tokens -> tokens.map { it.copy(owner = Stubs.ownedToken.owner) } }
      .isEqualTo(listOf(Stubs.ownedToken))
  }

  @Test fun `checking that expired tokens report as expired`() {
    ownedTokenDao.stub {
      onGeneric { findById(Stubs.token.tokenLocator) } doReturn Optional.of(Stubs.ownedTokenDbm)
    }
    timeProvider.staticTime = { Stubs.ownedToken.expiresAt.time + TimeUnit.SECONDS.toMillis(1) }

    assertThat(repository.checkIsExpired(Stubs.token))
      .isTrue()
  }

  @Test fun `checking that non-expired tokens report as non-expired`() {
    ownedTokenDao.stub {
      onGeneric { findById(Stubs.token.tokenLocator) } doReturn Optional.of(Stubs.ownedTokenDbm)
    }
    timeProvider.staticTime = { Stubs.ownedToken.expiresAt.time - TimeUnit.SECONDS.toMillis(1) }

    assertThat(repository.checkIsExpired(Stubs.token))
      .isFalse()
  }

  @Test fun `checking that blocked tokens report as blocked`() {
    ownedTokenDao.stub {
      onGeneric { findById(Stubs.token.tokenLocator) } doReturn Optional.of(Stubs.ownedTokenDbm)
    }

    assertThat(repository.checkIsBlocked(Stubs.token))
      .isTrue()
  }

  @Test fun `checking that non-blocked tokens report as non-blocked`() {
    ownedTokenDao.stub {
      onGeneric { findById(Stubs.token.tokenLocator) } doReturn Optional.of(
        Stubs.ownedToken.copy(isBlocked = false).toData(Stubs.project),
      )
    }

    assertThat(repository.checkIsBlocked(Stubs.token))
      .isFalse()
  }

  @Test fun `blocking already blocked token works`() {
    ownedTokenDao.stub {
      onGeneric { findById(Stubs.token.tokenLocator) } doReturn Optional.of(Stubs.ownedTokenDbm)
    }

    assertAll {
      assertThat(repository.blockToken(Stubs.token))
        // token DAO knows nothing about users, so:
        .transform { it.copy(owner = Stubs.ownedToken.owner) }
        .isDataClassEqualTo(Stubs.ownedToken)

      verify(ownedTokenDao, never()).save(any())
    }
  }

  @Test fun `blocking a valid token works`() {
    ownedTokenDao.stub {
      onGeneric { findById(Stubs.token.tokenLocator) } doReturn Optional.of(
        Stubs.ownedToken.copy(isBlocked = false).toData(Stubs.project),
      )
    }

    assertAll {
      assertThat(repository.blockToken(Stubs.token))
        // token DAO knows nothing about users, so:
        .transform { it.copy(owner = Stubs.ownedToken.owner) }
        .isDataClassEqualTo(Stubs.ownedToken)

      verify(ownedTokenDao).save(any())
    }
  }

  @Test fun `blocking only valid tokens works`() {
    ownedTokenDao.stub {
      onGeneric {
        findAllById(
          listOf(
            Stubs.token.tokenLocator,
            Stubs.token.tokenLocator,
          ),
        )
      } doReturn listOf(
        Stubs.ownedTokenDbm,
        Stubs.ownedToken.copy(isBlocked = false).toData(Stubs.project),
      )

      onGeneric { saveAll(any<List<OwnedTokenDbm>>()) } doAnswer {
        @Suppress("UNCHECKED_CAST")
        it.arguments.first() as List<OwnedTokenDbm>
      }
    }

    val toBlock = listOf(Stubs.token, Stubs.token)
    assertThat(repository.blockAllTokens(toBlock))
      // token DAO knows nothing about users, so:
      .transform { tokens -> tokens.map { it.copy(owner = Stubs.ownedToken.owner) } }
      .isEqualTo(listOf(Stubs.ownedToken))
  }

  @Test fun `blocking all user's tokens works`() {
    ownedTokenDao.stub {
      onGeneric { saveAll(any<List<OwnedTokenDbm>>()) } doAnswer {
        @Suppress("UNCHECKED_CAST")
        it.arguments.first() as List<OwnedTokenDbm>
      }

      onGeneric { deleteAll(any<List<OwnedTokenDbm>>()) } doAnswer { }
    }

    val user = Stubs.user.copy(
      ownedTokens = Stubs.user.ownedTokens.map {
        it.copy(isBlocked = false)
      },
    )
    assertThat(repository.blockAllTokensFrom(user))
      // token DAO knows nothing about users, so:
      .transform { tokens -> tokens.map { it.copy(owner = Stubs.ownedToken.owner) } }
      .isEqualTo(listOf(Stubs.ownedToken))
  }

}