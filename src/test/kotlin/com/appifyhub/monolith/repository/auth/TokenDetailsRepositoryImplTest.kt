package com.appifyhub.monolith.repository.auth

import assertk.assertAll
import assertk.assertThat
import assertk.assertions.isDataClassEqualTo
import assertk.assertions.isEqualTo
import assertk.assertions.isFalse
import assertk.assertions.isTrue
import com.appifyhub.monolith.domain.mapper.toData
import com.appifyhub.monolith.security.JwtHelper
import com.appifyhub.monolith.storage.dao.TokenDetailsDao
import com.appifyhub.monolith.storage.model.auth.TokenDetailsDbm
import com.appifyhub.monolith.util.Stubs
import com.appifyhub.monolith.util.TimeProviderFake
import java.util.Optional
import java.util.concurrent.TimeUnit
import org.junit.jupiter.api.AfterEach
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.mockito.kotlin.any
import org.mockito.kotlin.doAnswer
import org.mockito.kotlin.doReturn
import org.mockito.kotlin.mock
import org.mockito.kotlin.never
import org.mockito.kotlin.stub
import org.mockito.kotlin.verify

class TokenDetailsRepositoryImplTest {

  private val tokenDetailsDao = mock<TokenDetailsDao>()
  private val timeProvider = TimeProviderFake()
  private val jwtHelper = mock<JwtHelper>()

  private val repository: TokenDetailsRepository = TokenDetailsRepositoryImpl(
    jwtHelper = jwtHelper,
    tokenDetailsDao = tokenDetailsDao,
    timeProvider = timeProvider,
  )

  @BeforeEach fun setup() {
    tokenDetailsDao.stub {
      onGeneric { save(any()) } doAnswer { it.arguments.first() as TokenDetailsDbm }
    }
    jwtHelper.stub {
      onGeneric { createJwtForClaims(any(), any(), any(), any()) } doReturn Stubs.tokenValue
      onGeneric { extractPropertiesFromJwt(any()) } doReturn Stubs.jwtClaims
    }
  }

  @AfterEach fun teardown() {
    timeProvider.staticTime = { null }
  }

  @Test fun `adding token data saves token details`() {
    val token = repository.addToken(Stubs.tokenDetails)

    assertAll {
      // separate ID check because token user is stubbed
      // internally and wouldn't match the test stub
      assertThat(token.ownerId)
        .isDataClassEqualTo(Stubs.userId)
      assertThat(token)
        .isDataClassEqualTo(Stubs.tokenDetails)
    }
  }

  @Test fun `fetching token details works`() {
    tokenDetailsDao.stub {
      onGeneric { findById(Stubs.tokenValue) } doReturn Optional.of(Stubs.tokenDetailsDbm)
    }

    assertThat(repository.fetchTokenDetails(Stubs.tokenValue))
      .isDataClassEqualTo(Stubs.tokenDetails)
  }

  @Test fun `fetching all token details works`() {
    tokenDetailsDao.stub {
      onGeneric { findAllById(listOf(Stubs.tokenValue)) } doReturn listOf(Stubs.tokenDetailsDbm)
    }

    assertThat(repository.fetchAllTokenDetails(listOf(Stubs.tokenValue)))
      .isEqualTo(listOf(Stubs.tokenDetails))
  }

  @Test fun `fetching all blocked tokens works`() {
    tokenDetailsDao.stub {
      onGeneric { findAllByOwnerAndBlocked(Stubs.userDbm, blocked = true) } doReturn listOf(Stubs.tokenDetailsDbm)
    }

    assertThat(repository.fetchAllBlockedTokens(Stubs.user, Stubs.project))
      .isEqualTo(listOf(Stubs.tokenDetails))
  }

  @Test fun `fetching all valid tokens works`() {
    tokenDetailsDao.stub {
      onGeneric { findAllByOwnerAndBlocked(Stubs.userDbm, blocked = false) } doReturn listOf(Stubs.tokenDetailsDbm)
    }

    assertThat(repository.fetchAllValidTokens(Stubs.user, Stubs.project))
      // token DAO knows nothing about users, so:
      .transform { tokens -> tokens.map { it.copy(ownerId = Stubs.tokenDetails.ownerId) } }
      .isEqualTo(listOf(Stubs.tokenDetails))
  }

  @Test fun `fetching all tokens works`() {
    tokenDetailsDao.stub {
      onGeneric { findAllByOwner(Stubs.userDbm) } doReturn listOf(Stubs.tokenDetailsDbm)
    }

    assertThat(repository.fetchAllTokens(Stubs.user, Stubs.project))
      .isEqualTo(listOf(Stubs.tokenDetails))
  }

  @Test fun `checking that expired tokens report as expired`() {
    tokenDetailsDao.stub {
      onGeneric { findById(Stubs.tokenValue) } doReturn Optional.of(Stubs.tokenDetailsDbm)
    }
    timeProvider.staticTime = { Stubs.tokenDetails.expiresAt.time + TimeUnit.SECONDS.toMillis(1) }

    assertThat(repository.checkIsExpired(Stubs.tokenValue))
      .isTrue()
  }

  @Test fun `checking that non-expired tokens report as non-expired`() {
    tokenDetailsDao.stub {
      onGeneric { findById(Stubs.tokenValue) } doReturn Optional.of(Stubs.tokenDetailsDbm)
    }
    timeProvider.staticTime = { Stubs.tokenDetails.expiresAt.time - TimeUnit.SECONDS.toMillis(1) }

    assertThat(repository.checkIsExpired(Stubs.tokenValue))
      .isFalse()
  }

  @Test fun `checking that blocked tokens report as blocked`() {
    tokenDetailsDao.stub {
      onGeneric { findById(Stubs.tokenValue) } doReturn Optional.of(Stubs.tokenDetailsDbm)
    }

    assertThat(repository.checkIsBlocked(Stubs.tokenValue))
      .isTrue()
  }

  @Test fun `checking that non-blocked tokens report as non-blocked`() {
    tokenDetailsDao.stub {
      onGeneric { findById(Stubs.tokenValue) } doReturn Optional.of(
        Stubs.tokenDetails.copy(isBlocked = false).toData(Stubs.user, Stubs.project),
      )
    }

    assertThat(repository.checkIsBlocked(Stubs.tokenValue))
      .isFalse()
  }

  @Test fun `checking that non-static tokens report as non-static`() {
    tokenDetailsDao.stub {
      onGeneric { findById(Stubs.tokenValue) } doReturn Optional.of(Stubs.tokenDetailsDbm)
    }

    assertThat(repository.checkIsStatic(Stubs.tokenValue))
      .isFalse()
  }

  @Test fun `checking that static tokens report as static`() {
    jwtHelper.stub {
      onGeneric { createJwtForClaims(any(), any(), any(), any()) } doReturn Stubs.tokenValueStatic
      onGeneric { extractPropertiesFromJwt(any()) } doReturn Stubs.jwtClaimsStatic
    }
    tokenDetailsDao.stub {
      onGeneric { findById(Stubs.tokenValueStatic) } doReturn Optional.of(
        Stubs.tokenDetails.copy(
          tokenValue = Stubs.tokenValueStatic,
          isStatic = true,
        ).toData(
          Stubs.user,
          Stubs.project,
        ),
      )
    }

    assertThat(repository.checkIsStatic(Stubs.tokenValueStatic))
      .isTrue()
  }

  @Test fun `blocking already blocked token works`() {
    tokenDetailsDao.stub {
      onGeneric { findById(Stubs.tokenValue) } doReturn Optional.of(Stubs.tokenDetailsDbm)
    }

    assertAll {
      assertThat(repository.blockToken(Stubs.tokenValue))
        .isDataClassEqualTo(Stubs.tokenDetails)

      verify(tokenDetailsDao, never()).save(any())
    }
  }

  @Test fun `blocking a valid token works`() {
    tokenDetailsDao.stub {
      onGeneric { findById(Stubs.tokenValue) } doReturn Optional.of(
        Stubs.tokenDetails.copy(isBlocked = false).toData(Stubs.user, Stubs.project),
      )
    }

    assertAll {
      assertThat(repository.blockToken(Stubs.tokenValue))
        .isDataClassEqualTo(Stubs.tokenDetails)

      verify(tokenDetailsDao).save(any())
    }
  }

  @Test fun `blocking only valid tokens works`() {
    tokenDetailsDao.stub {
      onGeneric {
        findAllById(
          listOf(
            Stubs.tokenValue,
            Stubs.tokenValue,
          ),
        )
      } doReturn listOf(
        Stubs.tokenDetailsDbm,
        Stubs.tokenDetails.copy(isBlocked = false).toData(Stubs.user, Stubs.project),
      )

      onGeneric { saveAll(any<List<TokenDetailsDbm>>()) } doAnswer {
        @Suppress("UNCHECKED_CAST")
        it.arguments.first() as List<TokenDetailsDbm>
      }
    }

    val toBlock = listOf(Stubs.tokenValue, Stubs.tokenValue)
    assertThat(repository.blockAllTokens(toBlock))
      .isEqualTo(listOf(Stubs.tokenDetails))
  }

  @Test fun `removing tokens by owner works`() {
    tokenDetailsDao.stub {
      onGeneric { deleteAllByOwner(any()) } doAnswer {}
    }

    assertThat(repository.removeTokensFor(Stubs.user, Stubs.project)).isEqualTo(Unit)

    verify(tokenDetailsDao).deleteAllByOwner(Stubs.userDbm)
  }

}
