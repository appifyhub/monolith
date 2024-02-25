package com.appifyhub.monolith.repository.user

import assertk.all
import assertk.assertFailure
import assertk.assertThat
import assertk.assertions.isDataClassEqualTo
import assertk.assertions.isEqualTo
import assertk.assertions.isNotNull
import assertk.assertions.messageContains
import com.appifyhub.monolith.storage.dao.SignupCodeDao
import com.appifyhub.monolith.storage.model.user.SignupCodeDbm
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

  @Test fun `fetching all codes by owner works`() {
    signupCodeDao.stub {
      onGeneric { findAllByOwner(any()) } doReturn listOf(Stubs.signupCodeDbm)
    }

    assertThat(repository.fetchAllCodesByOwner(Stubs.user))
      .isEqualTo(listOf(Stubs.signupCode))
  }

  @Test fun `marking code used fails when code not found`() {
    signupCodeDao.stub {
      onGeneric { findById(any()) } doReturn Optional.empty()
    }

    assertFailure { repository.markCodeUsed(fakeCode) }
      .messageContains("Signup code not found")
  }

  @Test fun `marking code used fails when code already used`() {
    val usedSignupCodeDbm = SignupCodeDbm(
      code = fakeCode,
      isUsed = true,
      owner = Stubs.userDbm,
      createdAt = timeProvider.currentDate,
      usedAt = timeProvider.currentDate,
    )
    signupCodeDao.stub {
      onGeneric { findById(any()) } doReturn Optional.of(usedSignupCodeDbm)
    }

    assertFailure { repository.markCodeUsed(fakeCode) }
      .messageContains("Signup code already used")
  }

  @Test fun `marking code used works`() {
    signupCodeDao.stub {
      onGeneric { findById(any()) } doReturn Optional.of(Stubs.signupCodeDbm)
    }

    assertThat(repository.markCodeUsed(fakeCode))
      .isNotNull()
      .isDataClassEqualTo(
        Stubs.signupCode.copy(
          isUsed = true,
          usedAt = timeProvider.currentDate,
        )
      )
  }

}
