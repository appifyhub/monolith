package com.appifyhub.monolith.init

import assertk.all
import assertk.assertThat
import assertk.assertions.isEmpty
import assertk.assertions.isEqualTo
import assertk.assertions.isNotNull
import com.appifyhub.monolith.TestAppifyHubApplication
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.extension.ExtendWith
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.test.context.ActiveProfiles
import org.springframework.test.context.junit.jupiter.SpringExtension

@ExtendWith(SpringExtension::class)
@ActiveProfiles(TestAppifyHubApplication.PROFILE)
@SpringBootTest(classes = [TestAppifyHubApplication::class])
class CreatorProjectConfigTest {

  @Autowired lateinit var creatorProjectConfig: CreatorProjectConfig

  @Test fun `creator project config is autowired`() {
    assertThat(creatorProjectConfig)
      .all {
        isNotNull()

        transform { it.ownerName }.isEqualTo("Owner")
        transform { it.ownerSignature }.isEmpty()
        transform { it.ownerEmail }.isEqualTo("creator@appifyhub.com")

        transform { it.projectName }.isEqualTo("AppifyHub")

        transform { it.mailgunApiKey }.isEmpty()
        transform { it.mailgunDomain }.isEqualTo("mailgun.appifyhub.com")
        transform { it.mailgunSenderName }.isEqualTo("AppifyHub")
        transform { it.mailgunSenderEmail }.isEqualTo("no-reply@appifyhub.com")

        transform { it.twilioAccountSid }.isEmpty()
        transform { it.twilioAuthToken }.isEmpty()
        transform { it.twilioMessagingServiceId }.isEmpty()
        transform { it.twilioMaxPricePerMessage }.isEqualTo("2")
        transform { it.twilioMaxRetryAttempts }.isEqualTo("2")
        transform { it.twilioDefaultSenderName }.isEqualTo("APPIFY")
        transform { it.twilioDefaultSenderNumber }.isEmpty()

        transform { it.firebaseProjectName }.isEqualTo("Appify Hub")
        transform { it.firebaseServiceAccountKeyBase64 }.isEmpty()
      }
  }

}
