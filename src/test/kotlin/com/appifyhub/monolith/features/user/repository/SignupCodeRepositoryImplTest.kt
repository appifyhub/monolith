package com.appifyhub.monolith.features.user.repository

import assertk.all
import assertk.assertThat
import assertk.assertions.isDataClassEqualTo
import assertk.assertions.isEqualTo
import assertk.assertions.isNotNull
import com.appifyhub.monolith.features.user.repository.util.SignupCodeGenerator
import com.appifyhub.monolith.features.user.storage.SignupCodeDao
import com.appifyhub.monolith.features.user.storage.model.SignupCodeDbm
import com.appifyhub.monolith.features.user.storage.model.UserDbm
import com.appifyhub.monolith.util.Stubs
import com.appifyhub.monolith.util.TimeProviderFake
import org.junit.jupiter.api.AfterEach
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.mockito.kotlin.any
import org.mockito.kotlin.argumentCaptor
import org.mockito.kotlin.doAnswer
import org.mockito.kotlin.doReturn
import org.mockito.kotlin.mock
import org.mockito.kotlin.stub
import org.mockito.kotlin.verify
import java.util.Optional

class SignupCodeRepositoryImplTest {

  private val signupCodeDao = mock<SignupCodeDao>()
  private val timeProvider = TimeProviderFake()

  private val repository: SignupCodeRepository = SignupCodeRepositoryImpl(
    signupCodeDao = signupCodeDao,
    timeProvider = timeProvider,
  )

  private val fakeTime = 0x100000L
  private val fakeCode = "CODE-1"

  @BeforeEach fun setup() {
    SignupCodeGenerator.interceptor = { fakeCode }
    timeProvider.staticTime = { fakeTime }

    signupCodeDao.stub {
      onGeneric { save(any()) } doAnswer { it.arguments.first() as SignupCodeDbm }
    }
  }

  @AfterEach fun teardown() {
    SignupCodeGenerator.interceptor = { null }
  }

  @Test fun `creating a new signup code works`() {
    repository.createCode(Stubs.user)

    // data conversion loses some attributes from the foreign key user,
    // so this is a workaround to test only what matters
    val captor = argumentCaptor<SignupCodeDbm>()
    verify(signupCodeDao).save(captor.capture())
    assertThat(captor.firstValue).all {
      transform { it.code }.isEqualTo(fakeCode)
      transform { it.isUsed }.isEqualTo(false)
      transform { it.owner.id }.isEqualTo(Stubs.userDbm.id)
      transform { it.createdAt.time }.isEqualTo(fakeTime)
      transform { it.usedAt }.isEqualTo(null)
    }
  }

  @Test fun `fetching a code by id works`() {
    signupCodeDao.stub {
      onGeneric { findById(any()) } doReturn Optional.of(Stubs.signupCodeDbm)
    }

    assertThat(repository.fetchSignupCodeById(fakeCode))
      .isEqualTo(Stubs.signupCode)
  }

  @Test fun `fetching all codes by owner works`() {
    signupCodeDao.stub {
      onGeneric { findAllByOwner(any()) } doReturn listOf(Stubs.signupCodeDbm)
    }

    assertThat(repository.fetchAllSignupCodesByOwner(Stubs.user))
      .isEqualTo(listOf(Stubs.signupCode))
  }

  @Test fun `saving a code works`() {
    assertThat(repository.saveSignupCode(Stubs.signupCode))
      .isNotNull()
      .isDataClassEqualTo(Stubs.signupCode)
  }

  @Test fun `deleting all codes by owner works`() {
    repository.deleteAllByOwner(Stubs.user)

    // data conversion loses some attributes from the foreign key user,
    // so this is a workaround to test only what matters
    val captor = argumentCaptor<UserDbm>()
    verify(signupCodeDao).deleteAllByOwner(captor.capture())

    assertThat(captor.firstValue)
      .transform(transform = UserDbm::id)
      .isEqualTo(Stubs.userDbm.id)
  }

}
