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
class AdminProjectConfigTest {

  @Autowired lateinit var adminProjectConfig: AdminProjectConfig

  @Test fun `admin project config is autowired`() {
    assertThat(adminProjectConfig)
      .all {
        isNotNull()
        transform { it.projectName }.isEqualTo("AppifyHub")
        transform { it.ownerName }.isEqualTo("Owner")
        transform { it.ownerSecret }.isEmpty()
        transform { it.ownerEmail }.isEqualTo("admin@appifyhub.com")
      }
  }

}
