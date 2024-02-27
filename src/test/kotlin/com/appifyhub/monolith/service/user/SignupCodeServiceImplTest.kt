package com.appifyhub.monolith.service.user

import assertk.assertFailure
import assertk.assertThat
import assertk.assertions.isDataClassEqualTo
import assertk.assertions.isEqualTo
import assertk.assertions.messageContains
import com.appifyhub.monolith.TestAppifyHubApplication
import com.appifyhub.monolith.domain.user.SignupCode
import com.appifyhub.monolith.domain.user.User
import com.appifyhub.monolith.domain.user.UserId
import com.appifyhub.monolith.repository.user.SignupCodeGenerator
import com.appifyhub.monolith.repository.user.SignupCodeRepository
import com.appifyhub.monolith.util.Stubber
import com.appifyhub.monolith.util.Stubs
import com.appifyhub.monolith.util.TimeProviderFake
import com.appifyhub.monolith.util.ext.truncateTo
import org.junit.jupiter.api.AfterEach
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.extension.ExtendWith
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.test.annotation.DirtiesContext
import org.springframework.test.annotation.DirtiesContext.ClassMode.BEFORE_EACH_TEST_METHOD
import org.springframework.test.context.ActiveProfiles
import org.springframework.test.context.junit.jupiter.SpringExtension
import java.time.temporal.ChronoUnit

@ExtendWith(SpringExtension::class)
@ActiveProfiles(TestAppifyHubApplication.PROFILE)
@DirtiesContext(classMode = BEFORE_EACH_TEST_METHOD)
@SpringBootTest(classes = [TestAppifyHubApplication::class])
class SignupCodeServiceImplTest {

  @Autowired lateinit var service: SignupCodeService
  @Autowired lateinit var repository: SignupCodeRepository
  @Autowired lateinit var timeProvider: TimeProviderFake
  @Autowired lateinit var stubber: Stubber

  @BeforeEach fun setup() {
    timeProvider.staticTime = { 0 }
  }

  @AfterEach fun teardown() {
    SignupCodeGenerator.interceptor = { null }
    timeProvider.staticTime = { null }
  }

  @Test fun `creating a signup code fails for invalid user ID`() {
    assertFailure { service.createCode(UserId("invalid", -1)) }
      .messageContains("User ID")
  }

  @Test fun `creating a signup code fails for user with max codes`() {
    val maxSignupCodesPerUser = 5
    val project = stubber.projects.new(maxSignupCodesPerUser = maxSignupCodesPerUser)
    val ownerId = stubber.users(project).default().id
    repeat(maxSignupCodesPerUser) { service.createCode(ownerId) }

    assertFailure { service.createCode(ownerId) }
      .messageContains("User $ownerId has reached the maximum number of signup codes ($maxSignupCodesPerUser)")
  }

  @Test fun `creating a signup code succeeds`() {
    val project = stubber.projects.new()
    val owner = stubber.users(project).default()
    val code = "FAKE-CODE-1234"

    val expected = SignupCode(
      code = code,
      isUsed = false,
      owner = owner,
      createdAt = timeProvider.currentDate,
      usedAt = null,
    )

    SignupCodeGenerator.interceptor = { code }
    assertThat(service.createCode(owner.id).cleanDates())
      .isDataClassEqualTo(expected)
  }

  @Test fun `fetching all signup codes by owner fails for invalid user ID`() {
    assertFailure { service.fetchAllCodesByOwner(UserId("invalid", -1)) }
      .messageContains("User ID")
  }

  @Test fun `fetching all signup codes by owner succeeds`() {
    val code1 = "FAKE-CODE-1234"
    val code2 = "FAKE-CODE-5678"
    val project = stubber.projects.new()
    val owner = stubber.users(project).default()

    SignupCodeGenerator.interceptor = { code1 }
    val signupCode1 = service.createCode(owner.id)
    SignupCodeGenerator.interceptor = { code2 }
    val signupCode2 = service.createCode(owner.id)

    val result = service.fetchAllCodesByOwner(owner.id)

    assertThat(result.map { it.cleanDates() })
      .isEqualTo(listOf(signupCode1, signupCode2))
  }

  @Test fun `marking a signup code as used fails for invalid code`() {
    assertFailure { service.markCodeUsed("invalid", Stubs.project.id) }
      .messageContains("Signup Code")
  }

  @Test fun `marking a signup code as used fails for invalid project`() {
    val project = stubber.projects.new()
    val owner = stubber.users(project).default()

    val signupCode = service.createCode(owner.id)
    assertFailure { service.markCodeUsed(signupCode.code, -1) }
      .messageContains("Project ID")
  }

  @Test fun `marking a signup code as used fails for already used code`() {
    val project = stubber.projects.new()
    val owner = stubber.users(project).default()

    val signupCode = service.createCode(owner.id).copy(isUsed = true, usedAt = timeProvider.currentDate)
    repository.saveSignupCode(signupCode)

    assertFailure { service.markCodeUsed(signupCode.code, project.id) }
      .messageContains("Signup code already used")
  }

  @Test fun `marking a signup code as used fails for different project`() {
    val project1 = stubber.projects.new()
    val project2 = stubber.projects.new()
    val owner = stubber.users(project1).default()

    val signupCode = service.createCode(owner.id)
    assertFailure { service.markCodeUsed(signupCode.code, project2.id) }
      .messageContains("Signup code does not belong to the same project as the user")
  }

  @Test fun `marking a signup code as used succeeds`() {
    val code = "FAKE-CODE-1234"
    val project = stubber.projects.new()
    val owner = stubber.users(project).default()

    SignupCodeGenerator.interceptor = { code }
    val signupCode = service.createCode(owner.id)

    timeProvider.staticTime = { 1000000 }
    val expected = signupCode.copy(
      isUsed = true,
      usedAt = timeProvider.currentDate,
    )

    assertThat(service.markCodeUsed(code, project.id))
      .transform { it.cleanDates() }
      .isDataClassEqualTo(expected)
  }

  // Helpers

  private fun SignupCode.cleanDates() = copy(
    owner = owner.cleanDates(),
    createdAt = createdAt.truncateTo(ChronoUnit.SECONDS),
    usedAt = usedAt?.truncateTo(ChronoUnit.SECONDS),
  )

  private fun User.cleanDates() = copy(
    createdAt = createdAt.truncateTo(ChronoUnit.SECONDS),
    updatedAt = updatedAt.truncateTo(ChronoUnit.SECONDS),
  )

}
