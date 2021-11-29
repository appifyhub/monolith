package com.appifyhub.monolith.storage.dao

import assertk.assertThat
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
class DaoTest {

  @Autowired lateinit var geolocationDao: GeolocationDao
  @Autowired lateinit var creationDao: ProjectCreationDao
  @Autowired lateinit var projectDao: ProjectDao
  @Autowired lateinit var propertyDao: PropertyDao
  @Autowired lateinit var schemaDao: SchemaDao
  @Autowired lateinit var tokenDetailsDao: TokenDetailsDao
  @Autowired lateinit var userDao: UserDao

  @Test fun `DAO autowiring works`() {
    assertThat(geolocationDao).isNotNull()
    assertThat(creationDao).isNotNull()
    assertThat(projectDao).isNotNull()
    assertThat(propertyDao).isNotNull()
    assertThat(schemaDao).isNotNull()
    assertThat(tokenDetailsDao).isNotNull()
    assertThat(userDao).isNotNull()
  }

}
