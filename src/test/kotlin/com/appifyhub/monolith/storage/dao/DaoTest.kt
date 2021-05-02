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

  @Autowired lateinit var accountDao: AccountDao
  @Autowired lateinit var tokenDetailsDao: TokenDetailsDao
  @Autowired lateinit var projectDao: ProjectDao
  @Autowired lateinit var schemaDao: SchemaDao
  @Autowired lateinit var userDao: UserDao

  @Test fun `DAO autowiring works`() {
    assertThat(accountDao).isNotNull()
    assertThat(tokenDetailsDao).isNotNull()
    assertThat(projectDao).isNotNull()
    assertThat(schemaDao).isNotNull()
    assertThat(userDao).isNotNull()
  }

}
