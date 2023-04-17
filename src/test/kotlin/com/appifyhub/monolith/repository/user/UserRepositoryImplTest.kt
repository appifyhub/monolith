package com.appifyhub.monolith.repository.user

import assertk.all
import assertk.assertThat
import assertk.assertions.hasClass
import assertk.assertions.hasMessage
import assertk.assertions.hasSize
import assertk.assertions.isDataClassEqualTo
import assertk.assertions.isEqualTo
import assertk.assertions.isFailure
import assertk.assertions.isSuccess
import assertk.assertions.messageContains
import com.appifyhub.monolith.domain.common.Settable
import com.appifyhub.monolith.domain.creator.Project.UserIdType
import com.appifyhub.monolith.domain.user.UserId
import com.appifyhub.monolith.domain.user.ops.UserUpdater
import com.appifyhub.monolith.repository.auth.TokenDetailsRepository
import com.appifyhub.monolith.repository.messaging.PushDeviceRepository
import com.appifyhub.monolith.storage.dao.UserDao
import com.appifyhub.monolith.storage.model.user.UserDbm
import com.appifyhub.monolith.util.PasswordEncoderFake
import com.appifyhub.monolith.util.Stubs
import com.appifyhub.monolith.util.TimeProviderFake
import org.junit.jupiter.api.AfterEach
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.mockito.kotlin.any
import org.mockito.kotlin.anyOrNull
import org.mockito.kotlin.doAnswer
import org.mockito.kotlin.doReturn
import org.mockito.kotlin.doThrow
import org.mockito.kotlin.mock
import org.mockito.kotlin.stub
import java.util.Date
import java.util.Optional

class UserRepositoryImplTest {

  private val userDao = mock<UserDao>()
  private val tokenDetailsRepository = mock<TokenDetailsRepository>()
  private val pushDeviceRepository = mock<PushDeviceRepository>()
  private val springUserManager = mock<SpringSecurityUserManager>()
  private val passwordEncoder = PasswordEncoderFake()
  private val timeProvider = TimeProviderFake()

  private val repository: UserRepository = UserRepositoryImpl(
    userDao = userDao,
    tokenDetailsRepository = tokenDetailsRepository,
    pushDeviceRepository = pushDeviceRepository,
    passwordEncoder = passwordEncoder,
    springSecurityUserManager = springUserManager,
    timeProvider = timeProvider,
  )

  // region Setup

  @BeforeEach fun setup() {
    userDao.stub {
      onGeneric { save(any()) } doAnswer { it.arguments.first() as UserDbm }
    }
    tokenDetailsRepository.stub {
      onGeneric { blockAllTokens(any()) } doAnswer {
        @Suppress("UNCHECKED_CAST")
        val tokenValues = it.arguments.first() as List<String>
        tokenValues.map { value -> Stubs.tokenDetails.copy(tokenValue = value, isBlocked = true) }
      }
      onGeneric { fetchAllValidTokens(any(), anyOrNull()) } doReturn listOf(Stubs.tokenDetails)
      onGeneric { removeTokensFor(Stubs.user, null) } doAnswer {}
    }
    pushDeviceRepository.stub {
      onGeneric { deleteAllDevicesByUser(any()) } doAnswer {}
    }
  }

  @AfterEach fun teardown() {
    timeProvider.staticTime = { null }
    UserIdGenerator.interceptor = { null }
    TokenGenerator.emailInterceptor = { null }
    TokenGenerator.phoneInterceptor = { null }
  }

  // endregion

  // region Add user

  @Test fun `adding user fails with null ID and non-random ID type`() {
    val creator = Stubs.userCreator.copy(userId = null)

    assertThat { repository.addUser(creator, UserIdType.USERNAME) }
      .isFailure()
      .all {
        hasClass(IllegalArgumentException::class)
        messageContains("Missing user ID")
      }
  }

  @Test fun `adding user fails with existing ID and random ID type`() {
    val creator = Stubs.userCreator.copy(userId = "non-null")

    assertThat { repository.addUser(creator, UserIdType.RANDOM) }
      .isFailure()
      .all {
        hasClass(IllegalArgumentException::class)
        messageContains("Provided user ID")
      }
  }

  @Test fun `adding user generates new ID for random ID type`() {
    val creator = Stubs.userCreator.copy(userId = null)

    // create & update times from the stub
    val times = listOf(0xC00000, 0xA00000).iterator()
    timeProvider.staticTime = { times.next().toLong() }
    UserIdGenerator.interceptor = { "randomUserId" }
    TokenGenerator.emailInterceptor = { "abcd1234" }

    assertThat(repository.addUser(creator, UserIdType.RANDOM))
      .isDataClassEqualTo(
        Stubs.user.copy(id = UserId("randomUserId", creator.projectId)),
      )
  }

  @Test fun `adding user saves existing ID for non-random ID type`() {
    val creator = Stubs.userCreator.copy(userId = "username")

    // create & update times from the stub
    val times = listOf(0xC00000, 0xA00000).iterator()
    timeProvider.staticTime = { times.next().toLong() }
    TokenGenerator.emailInterceptor = { "abcd1234" }

    assertThat(repository.addUser(creator, Stubs.project.userIdType))
      .isDataClassEqualTo(Stubs.user)
  }

  @Test fun `adding user with phone ID generates a phone-friendly verification token`() {
    val creator = Stubs.userCreator.copy(userId = "+1234567890")

    // create & update times from the stub
    val times = listOf(0xC00000, 0xA00000).iterator()
    timeProvider.staticTime = { times.next().toLong() }
    TokenGenerator.phoneInterceptor = { "123456" }

    assertThat(repository.addUser(creator, UserIdType.PHONE))
      .isDataClassEqualTo(
        Stubs.user.copy(
          id = UserId(creator.userId!!, creator.projectId),
          verificationToken = "123456",
        ),
      )
  }

  // endregion

  // region Fetch by ID

  @Test fun `fetching user by invalid ID throws`() {
    userDao.stub {
      onGeneric { findById(Stubs.userIdDbm) } doThrow IllegalArgumentException("failed")
    }

    assertThat { repository.fetchUserByUserId(Stubs.userId) }
      .isFailure()
      .all {
        hasClass(IllegalArgumentException::class)
        hasMessage("failed")
      }
  }

  @Test fun `fetching user by ID works`() {
    userDao.stub {
      onGeneric { findById(Stubs.userIdDbm) } doReturn Optional.of(Stubs.userDbm)
    }

    assertThat(repository.fetchUserByUserId(Stubs.userId))
      .isDataClassEqualTo(Stubs.user)
  }

  // endregion

  // region Fetch by universal ID

  @Test fun `fetching user by malformed universal ID throws`() {
    assertThat { repository.fetchUserByUniversalId("malformed") }
      .isFailure()
      .hasClass(NumberFormatException::class)
  }

  @Test fun `fetching user by invalid universal ID throws`() {
    userDao.stub {
      onGeneric { findById(Stubs.userIdDbm) } doThrow IllegalArgumentException("failed")
    }

    assertThat { repository.fetchUserByUniversalId(Stubs.universalUserId) }
      .isFailure()
      .all {
        hasClass(IllegalArgumentException::class)
        hasMessage("failed")
      }
  }

  @Test fun `fetching user by universal ID works`() {
    userDao.stub {
      onGeneric { findById(Stubs.userIdDbm) } doReturn Optional.of(Stubs.userDbm)
    }

    assertThat(repository.fetchUserByUniversalId(Stubs.universalUserId))
      .isDataClassEqualTo(Stubs.user)
  }

  // endregion

  // region Fetch by Project

  @Test fun `fetching users by invalid project ID throws`() {
    userDao.stub {
      onGeneric { findAllByProject_ProjectId(Stubs.project.id) } doThrow IllegalArgumentException("failed")
    }

    assertThat { repository.fetchAllUsersByProjectId(Stubs.project.id) }
      .isFailure()
      .all {
        hasClass(IllegalArgumentException::class)
        hasMessage("failed")
      }
  }

  @Test fun `fetching users by project ID works`() {
    userDao.stub {
      onGeneric { findAllByProject_ProjectId(Stubs.project.id) } doReturn listOf(Stubs.userDbm)
    }

    assertThat(repository.fetchAllUsersByProjectId(Stubs.project.id))
      .all {
        hasSize(1)
        transform { it.first() }.isDataClassEqualTo(Stubs.user)
      }
  }

  // endregion

  // region Fetch by Verification Token

  @Test fun `fetching users by verification token throws when not found`() {
    userDao.stub {
      onGeneric {
        findByIdAndVerificationToken(
          userId = Stubs.userIdDbm,
          verificationToken = Stubs.user.verificationToken!!,
        )
      } doThrow IllegalArgumentException("failed")
    }

    assertThat { repository.fetchUserByUserIdAndVerificationToken(Stubs.userId, Stubs.user.verificationToken!!) }
      .isFailure()
      .all {
        hasClass(IllegalArgumentException::class)
        hasMessage("failed")
      }
  }

  @Test fun `fetching users by verification token works`() {
    userDao.stub {
      onGeneric {
        findByIdAndVerificationToken(
          userId = Stubs.userIdDbm,
          verificationToken = Stubs.user.verificationToken!!,
        )
      } doReturn Stubs.userDbm
    }

    assertThat(repository.fetchUserByUserIdAndVerificationToken(Stubs.userId, Stubs.user.verificationToken!!))
      .isDataClassEqualTo(Stubs.user)
  }

  // endregion

  // region Search by Name

  @Test fun `searching users by invalid name throws`() {
    userDao.stub {
      onGeneric {
        searchAllByProject_ProjectIdAndNameLike(Stubs.project.id, Stubs.user.name!!)
      } doThrow IllegalArgumentException("failed")
    }

    assertThat { repository.searchByName(Stubs.project.id, Stubs.user.name!!) }
      .isFailure()
      .all {
        hasClass(IllegalArgumentException::class)
        hasMessage("failed")
      }
  }

  @Test fun `searching users by name works`() {
    userDao.stub {
      onGeneric {
        searchAllByProject_ProjectIdAndNameLike(Stubs.project.id, Stubs.user.name!!)
      } doReturn listOf(Stubs.userDbm)
    }

    assertThat(repository.searchByName(Stubs.project.id, Stubs.user.name!!))
      .all {
        hasSize(1)
        transform { it.first() }.isDataClassEqualTo(Stubs.user)
      }
  }

  // endregion

  // region Search by Contact

  @Test fun `searching users by invalid contact throws`() {
    userDao.stub {
      onGeneric {
        searchAllByProject_ProjectIdAndContactLike(Stubs.project.id, Stubs.user.contact!!)
      } doThrow IllegalArgumentException("failed")
    }

    assertThat { repository.searchByContact(Stubs.project.id, Stubs.user.contact!!) }
      .isFailure()
      .all {
        hasClass(IllegalArgumentException::class)
        hasMessage("failed")
      }
  }

  @Test fun `searching users by contact works`() {
    userDao.stub {
      onGeneric {
        searchAllByProject_ProjectIdAndContactLike(Stubs.project.id, Stubs.user.contact!!)
      } doReturn listOf(Stubs.userDbm)
    }

    assertThat(repository.searchByContact(Stubs.project.id, Stubs.user.contact!!))
      .all {
        hasSize(1)
        transform { it.first() }.isDataClassEqualTo(Stubs.user)
      }
  }

  // endregion

  // region Counting

  @Test fun `counting users by project works`() {
    userDao.stub {
      onGeneric {
        countAllByProject_ProjectId(Stubs.project.id)
      } doReturn 10
    }

    assertThat(repository.count(Stubs.project.id))
      .isEqualTo(10)
  }

  // endregion

  // region Update user

  @Test fun `updating user with invalid ID throws`() {
    userDao.stub {
      onGeneric { findById(Stubs.userIdDbm) } doThrow IllegalArgumentException("failed")
    }

    assertThat { repository.updateUser(Stubs.userUpdater, Stubs.project.userIdType) }
      .isFailure()
      .all {
        hasClass(IllegalArgumentException::class)
        hasMessage("failed")
      }
  }

  @Test fun `updating user works`() {
    userDao.stub {
      onGeneric { findById(Stubs.userIdDbm) } doReturn Optional.of(Stubs.userDbm)
    }
    timeProvider.staticTime = { 0xA00001 }
    TokenGenerator.phoneInterceptor = { "abcd12341" }

    assertThat(repository.updateUser(Stubs.userUpdater, Stubs.project.userIdType))
      .isDataClassEqualTo(Stubs.userUpdated)
  }

  @Test fun `updating user's email contact generates a new verification token`() {
    userDao.stub {
      onGeneric { findById(Stubs.userIdDbm) } doReturn Optional.of(Stubs.userDbm)
    }
    timeProvider.staticTime = { 0xA00001 }
    TokenGenerator.emailInterceptor = { "abcd12341" }

    val updater = UserUpdater(id = Stubs.userId, contact = Settable("new@email.com"))
    assertThat(repository.updateUser(updater, Stubs.project.userIdType))
      .isDataClassEqualTo(
        Stubs.user.copy(
          contact = updater.contact!!.value,
          verificationToken = "abcd12341",
          updatedAt = Date(0xA00001),
        ),
      )
  }

  @Test fun `updating user's phone contact generates a new phone-friendly verification token`() {
    userDao.stub {
      // updated user has a phone
      onGeneric { findById(Stubs.userIdDbm) } doReturn Optional.of(Stubs.userUpdatedDbm)
    }
    timeProvider.staticTime = { 0xA00001 }
    TokenGenerator.phoneInterceptor = { "abcd12342" }

    val updater = UserUpdater(id = Stubs.userId, contact = Settable("+9876543210"))
    assertThat(repository.updateUser(updater, Stubs.project.userIdType)) // just changes the phone
      .isDataClassEqualTo(
        Stubs.userUpdated.copy(
          contact = updater.contact!!.value,
          verificationToken = "abcd12342",
        ),
      )
  }

  // endregion

  // region Remove user

  @Test fun `removing user by invalid ID throws`() {
    userDao.stub {
      onGeneric { findById(Stubs.userIdDbm) } doReturn Optional.of(Stubs.userDbm)
      onGeneric { deleteById(Stubs.userIdDbm) } doThrow IllegalArgumentException("failed")
    }

    assertThat { repository.removeUserById(Stubs.userId) }
      .isFailure()
      .all {
        hasClass(IllegalArgumentException::class)
        hasMessage("failed")
      }
  }

  @Test fun `removing user by ID works`() {
    userDao.stub {
      onGeneric { findById(Stubs.userIdDbm) } doReturn Optional.of(Stubs.userDbm)
      onGeneric { deleteById(Stubs.userIdDbm) } doAnswer {}
    }

    assertThat { repository.removeUserById(Stubs.userId) }
      .isSuccess()
  }

  @Test fun `removing user by malformed universal ID throws`() {
    assertThat { repository.removeUserByUniversalId("malformed") }
      .isFailure()
      .hasClass(NumberFormatException::class)
  }

  @Test fun `removing user by invalid universal ID throws`() {
    userDao.stub {
      onGeneric { findById(Stubs.userIdDbm) } doReturn Optional.of(Stubs.userDbm)
      onGeneric { deleteById(Stubs.userIdDbm) } doThrow IllegalArgumentException("failed")
    }

    assertThat { repository.removeUserByUniversalId(Stubs.universalUserId) }
      .isFailure()
      .all {
        hasClass(IllegalArgumentException::class)
        hasMessage("failed")
      }
  }

  @Test fun `removing user by universal ID works`() {
    userDao.stub {
      onGeneric { findById(Stubs.userIdDbm) } doReturn Optional.of(Stubs.userDbm)
      onGeneric { deleteById(Stubs.userIdDbm) } doAnswer {}
    }

    assertThat { repository.removeUserByUniversalId(Stubs.universalUserId) }
      .isSuccess()
  }

  @Test fun `removing user by invalid project ID throws`() {
    userDao.stub {
      onGeneric { findAllByProject_ProjectId(Stubs.project.id) } doReturn listOf(Stubs.userDbm)
      onGeneric { deleteAllByProject_ProjectId(Stubs.project.id) } doThrow IllegalArgumentException("failed")
    }

    assertThat { repository.removeAllUsersByProjectId(Stubs.project.id) }
      .isFailure()
      .all {
        hasClass(IllegalArgumentException::class)
        hasMessage("failed")
      }
  }

  @Test fun `removing user by project ID works`() {
    userDao.stub {
      onGeneric { findAllByProject_ProjectId(Stubs.project.id) } doReturn listOf(Stubs.userDbm)
      onGeneric { deleteAllByProject_ProjectId(Stubs.project.id) } doAnswer {}
    }

    assertThat { repository.removeAllUsersByProjectId(Stubs.project.id) }
      .isSuccess()
  }

  // endregion

}
