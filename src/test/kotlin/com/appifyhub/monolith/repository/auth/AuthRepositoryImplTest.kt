package com.appifyhub.monolith.repository.auth

import com.appifyhub.monolith.TestAppifyHubApplication
import org.junit.jupiter.api.extension.ExtendWith
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.test.context.ActiveProfiles
import org.springframework.test.context.junit.jupiter.SpringExtension

// FIXME/MM This is placeholder code -- to be removed
@ExtendWith(SpringExtension::class)
@ActiveProfiles(TestAppifyHubApplication.PROFILE)
@SpringBootTest(classes = [TestAppifyHubApplication::class])
class AuthRepositoryImplTest {
//
//  @Autowired
//  private lateinit var repo: AuthRepository
//
//  @Test fun generateToken() {
//    val creds = UserCredentialsRequest(
//      identifier = "username",
//      idTypeType = User.IdType.USERNAME,
//      secret = "ssap",
//      secretType = UserCredentialsRequest.Secret.PASSPHRASE,
//    )
//
//    val token = repo.generateToken(creds, 11)
//
//    assertEquals("id=username,serviceId=11", token)
//  }
//
//  @Test fun getUserFromToken() {
//    val token = "id=username,serviceId=11"
//
//    val user = repo.getUserFromToken(token, 11)
//
//    assertEquals(UserDbm(
//      projectId = 11,
//      idType = "USERNAME",
//      identifier = "username",
//      signature = "ssap",
//      createdAt = TimeProviderFake.currentDate,
//    ), user)
//  }

}