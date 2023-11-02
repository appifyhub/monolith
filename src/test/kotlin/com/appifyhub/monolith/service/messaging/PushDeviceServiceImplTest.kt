package com.appifyhub.monolith.service.messaging

import assertk.assertFailure
import assertk.assertThat
import assertk.assertions.isDataClassEqualTo
import assertk.assertions.isEmpty
import assertk.assertions.isEqualTo
import assertk.assertions.messageContains
import com.appifyhub.monolith.TestAppifyHubApplication
import com.appifyhub.monolith.domain.messaging.PushDevice
import com.appifyhub.monolith.domain.messaging.PushDevice.Type.ANDROID
import com.appifyhub.monolith.domain.user.User
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
class PushDeviceServiceImplTest {

  @Autowired lateinit var service: PushDeviceService
  @Autowired lateinit var timeProvider: TimeProviderFake
  @Autowired lateinit var stubber: Stubber

  @BeforeEach fun setup() {
    timeProvider.staticTime = { 0 }
  }

  @AfterEach fun teardown() {
    timeProvider.staticTime = { null }
  }

  @Test fun `adding a new push device succeeds`() {
    val owner = stubber.users(stubber.projects.new()).default()
    val device = PushDevice("d e v _ 1", ANDROID, owner)
    val expected = device.copy(deviceId = "dev_1")

    assertThat(service.addDevice(device).cleanDates())
      .isDataClassEqualTo(expected.cleanDates())
  }

  @Test fun `fetching a push device works`() {
    val owner = stubber.users(stubber.projects.new()).default()
    val device = service.addDevice(PushDevice("d e v _ 1", ANDROID, owner))

    assertThat(service.fetchDeviceById("dev_1").cleanDates())
      .isDataClassEqualTo(device.cleanDates())
  }

  @Test fun `fetching all push devices for user works`() {
    val owner = stubber.users(stubber.projects.new()).default()
    val device1 = service.addDevice(PushDevice("d e v _ 1", ANDROID, owner))
    val device2 = service.addDevice(PushDevice("d e v _ 2", ANDROID, owner))

    val result = service.fetchAllDevicesByUser(owner).map { it.cleanDates() }
    val expected = listOf(device1, device2).map { it.cleanDates() }
    assertThat(result).isEqualTo(expected)
  }

  @Test fun `deleting a push device by ID works`() {
    val owner = stubber.users(stubber.projects.new()).default()
    val device1 = service.addDevice(PushDevice("d e v _ 1", ANDROID, owner))

    service.deleteDeviceById(device1.deviceId)

    assertFailure { service.fetchDeviceById(device1.deviceId) }
      .messageContains("No value present")
  }

  @Test fun `deleting all push devices for user works`() {
    val owner = stubber.users(stubber.projects.new()).default()
    service.addDevice(PushDevice("d e v _ 1", ANDROID, owner))
    service.addDevice(PushDevice("d e v _ 2", ANDROID, owner))

    service.deleteAllDevicesByUser(owner)

    assertThat(service.fetchAllDevicesByUser(owner))
      .isEmpty()
  }

  // Helpers

  private fun PushDevice.cleanDates() = copy(
    owner = owner.cleanDates(),
  )

  private fun User.cleanDates() = copy(
    createdAt = createdAt.truncateTo(ChronoUnit.SECONDS),
    updatedAt = updatedAt.truncateTo(ChronoUnit.SECONDS),
  )

}
