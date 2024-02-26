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
import com.appifyhub.monolith.util.Stubber
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
    assertFailure { service.markCodeUsed("invalid") }
      .messageContains("Signup Code")
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

    assertThat(service.markCodeUsed(code))
      .transform { it.cleanDates() }
      .isDataClassEqualTo(expected)
  }

  // service code yet to be tested:
  //
  //  override fun markCodeUsed(code: String): SignupCode? {
  //    log.debug("Marking a signup code as used $code")
  //
  //    val normalizedCode = Normalizers.SignupCode.run(code).requireValid { "Signup Code" }
  //
  //    return repository.markCodeUsed(normalizedCode)
  //  }

  // code from a similar class handling push devices:
  //
  //  @Test fun `adding a new push device succeeds`() {
  //    val owner = stubber.users(stubber.projects.new()).default()
  //    val device = PushDevice("d e v _ 1", ANDROID, owner)
  //    val expected = device.copy(deviceId = "dev_1")
  //
  //    assertThat(service.addDevice(device).cleanDates())
  //      .isDataClassEqualTo(expected.cleanDates())
  //  }
  //
  //  @Test fun `fetching a push device works`() {
  //    val owner = stubber.users(stubber.projects.new()).default()
  //    val device = service.addDevice(PushDevice("d e v _ 1", ANDROID, owner))
  //
  //    assertThat(service.fetchDeviceById("dev_1").cleanDates())
  //      .isDataClassEqualTo(device.cleanDates())
  //  }
  //
  //  @Test fun `fetching all push devices for user works`() {
  //    val owner = stubber.users(stubber.projects.new()).default()
  //    val device1 = service.addDevice(PushDevice("d e v _ 1", ANDROID, owner))
  //    val device2 = service.addDevice(PushDevice("d e v _ 2", ANDROID, owner))
  //
  //    val result = service.fetchAllDevicesByUser(owner).map { it.cleanDates() }
  //    val expected = listOf(device1, device2).map { it.cleanDates() }
  //    assertThat(result).isEqualTo(expected)
  //  }
  //
  //  @Test fun `deleting a push device by ID works`() {
  //    val owner = stubber.users(stubber.projects.new()).default()
  //    val device1 = service.addDevice(PushDevice("d e v _ 1", ANDROID, owner))
  //
  //    service.deleteDeviceById(device1.deviceId)
  //
  //    assertFailure { service.fetchDeviceById(device1.deviceId) }
  //      .messageContains("No value present")
  //  }
  //
  //  @Test fun `deleting all push devices for user works`() {
  //    val owner = stubber.users(stubber.projects.new()).default()
  //    service.addDevice(PushDevice("d e v _ 1", ANDROID, owner))
  //    service.addDevice(PushDevice("d e v _ 2", ANDROID, owner))
  //
  //    service.deleteAllDevicesByUser(owner)
  //
  //    assertThat(service.fetchAllDevicesByUser(owner))
  //      .isEmpty()
  //  }

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
