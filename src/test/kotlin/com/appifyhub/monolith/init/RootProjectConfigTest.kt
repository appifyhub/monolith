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
class RootProjectConfigTest {

  @Autowired lateinit var rootProjectConfig: RootProjectConfig

  @Test fun `root project config is autowired`() {
    assertThat(rootProjectConfig)
      .all {
        isNotNull()
        transform { it.rootProjectName }.isEqualTo("AppifyHub")
        transform { it.rootOwnerName }.isEqualTo("Owner")
        transform { it.rootOwnerSignature }.isEmpty()
        transform { it.rootOwnerEmail }.isEqualTo("admin@appifyhub.com")
      }
  }

}
