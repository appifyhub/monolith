package com.appifyhub.monolith.features.common.storage

import assertk.assertThat
import assertk.assertions.isNotNull
import com.appifyhub.monolith.TestAppifyHubApplication
import com.appifyhub.monolith.features.auth.storage.TokenDetailsDao
import com.appifyhub.monolith.storage.dao.GeolocationDao
import com.appifyhub.monolith.features.creator.storage.MessageTemplateDao
import com.appifyhub.monolith.features.creator.storage.ProjectCreationDao
import com.appifyhub.monolith.features.creator.storage.ProjectDao
import com.appifyhub.monolith.storage.dao.PushDeviceDao
import com.appifyhub.monolith.storage.dao.SchemaDao
import com.appifyhub.monolith.storage.dao.SignupCodeDao
import com.appifyhub.monolith.storage.dao.UserDao
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.extension.ExtendWith
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.test.context.ActiveProfiles
import org.springframework.test.context.junit.jupiter.SpringExtension

@ExtendWith(SpringExtension::class)
@ActiveProfiles(TestAppifyHubApplication.PROFILE)
@SpringBootTest(classes = [TestAppifyHubApplication::class])
class DaoInjectionTest {

  @Autowired lateinit var geolocationDao: GeolocationDao
  @Autowired lateinit var messageTemplateDao: MessageTemplateDao
  @Autowired lateinit var projectCreationDao: ProjectCreationDao
  @Autowired lateinit var projectDao: ProjectDao
  @Autowired lateinit var pushDeviceDao: PushDeviceDao
  @Autowired lateinit var schemaDao: SchemaDao
  @Autowired lateinit var tokenDetailsDao: TokenDetailsDao
  @Autowired lateinit var userDao: UserDao
  @Autowired lateinit var signupCodeDao: SignupCodeDao

  @Test fun `DAO autowiring works`() {
    assertThat(geolocationDao).isNotNull()
    assertThat(messageTemplateDao).isNotNull()
    assertThat(projectCreationDao).isNotNull()
    assertThat(projectDao).isNotNull()
    assertThat(pushDeviceDao).isNotNull()
    assertThat(schemaDao).isNotNull()
    assertThat(tokenDetailsDao).isNotNull()
    assertThat(userDao).isNotNull()
    assertThat(signupCodeDao).isNotNull()
  }

}
