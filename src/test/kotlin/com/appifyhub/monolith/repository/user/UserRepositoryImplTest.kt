package com.appifyhub.monolith.repository.user

import assertk.all
import assertk.assertThat
import assertk.assertions.hasClass
import assertk.assertions.hasMessage
import assertk.assertions.hasSize
import assertk.assertions.isDataClassEqualTo
import assertk.assertions.isFailure
import assertk.assertions.isSuccess
import assertk.assertions.messageContains
import com.appifyhub.monolith.domain.admin.Project.UserIdType
import com.appifyhub.monolith.domain.common.Settable
import com.appifyhub.monolith.domain.mapper.toDomain
import com.appifyhub.monolith.domain.user.UserId
import com.appifyhub.monolith.domain.user.ops.UserUpdater
import com.appifyhub.monolith.repository.admin.AdminRepository
import com.appifyhub.monolith.repository.auth.OwnedTokenRepository
import com.appifyhub.monolith.storage.dao.UserDao
import com.appifyhub.monolith.storage.model.user.UserDbm
import com.appifyhub.monolith.util.PasswordEncoderFake
import com.appifyhub.monolith.util.Stubs
import com.appifyhub.monolith.util.TimeProviderFake
import com.nhaarman.mockitokotlin2.any
import com.nhaarman.mockitokotlin2.doAnswer
import com.nhaarman.mockitokotlin2.doReturn
import com.nhaarman.mockitokotlin2.doThrow
import com.nhaarman.mockitokotlin2.mock
import com.nhaarman.mockitokotlin2.stub
import org.junit.jupiter.api.AfterEach
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import java.util.Date
import java.util.Optional

class UserRepositoryImplTest {

  private val userDao = mock<UserDao>()
  private val ownedTokenRepo = mock<OwnedTokenRepository>()
  private val adminRepo = mock<AdminRepository>()
  private val springUserManager = mock<SpringSecurityUserManager>()
  private val passwordEncoder = PasswordEncoderFake()
  private val timeProvider = TimeProviderFake()

  private val repository: UserRepository = UserRepositoryImpl(
    userDao = userDao,
    ownedTokenRepository = ownedTokenRepo,
    adminRepository = adminRepo,
    passwordEncoder = passwordEncoder,
    timeProvider = timeProvider,
    springSecurityUserManager = springUserManager,
  )

  @BeforeEach fun setup() {
    userDao.stub {
      onGeneric { save(any()) } doAnswer { it.arguments.first() as UserDbm }
    }
    adminRepo.stub {
      on { fetchProjectById(Stubs.project.id) } doReturn Stubs.project
    }
    ownedTokenRepo.stub {
      on { fetchAllTokens(Stubs.userDbm.toDomain(), Stubs.project) } doReturn Stubs.user.ownedTokens
    }
  }

  @AfterEach fun teardown() {
    timeProvider.staticTime = { null }
    UserIdGenerator.interceptor = { null }
    TokenGenerator.emailInterceptor = { null }
    TokenGenerator.phoneInterceptor = { null }
  }

  // region Add user

  @Test fun `adding user fails with null ID and non-random ID type`() {
    val creator = Stubs.userCreator.copy(id = null)

    assertThat { repository.addUser(creator, UserIdType.USERNAME) }
      .isFailure()
      .all {
        hasClass(IllegalArgumentException::class)
        messageContains("Missing user ID")
      }
  }

  @Test fun `adding user fails with existing ID and random ID type`() {
    val creator = Stubs.userCreator.copy(id = "non-null")

    assertThat { repository.addUser(creator, UserIdType.RANDOM) }
      .isFailure()
      .all {
        hasClass(IllegalArgumentException::class)
        messageContains("Provided user ID")
      }
  }

  @Test fun `adding user generates new ID for random ID type`() {
    val creator = Stubs.userCreator.copy(id = null)

    // create & update times from the stub
    val times = listOf(0xC00000, 0xA00000).iterator()
    timeProvider.staticTime = { times.next().toLong() }
    UserIdGenerator.interceptor = { "randomUserId" }
    TokenGenerator.emailInterceptor = { "abcd1234" }

    assertThat(repository.addUser(creator, UserIdType.RANDOM))
      .isDataClassEqualTo(
        Stubs.user.copy(
          userId = UserId("randomUserId", creator.projectId),
          account = null,
          ownedTokens = emptyList(),
        )
      )
  }

  @Test fun `adding user saves existing ID for non-random ID type`() {
    val creator = Stubs.userCreator.copy(id = "username")

    // create & update times from the stub
    val times = listOf(0xC00000, 0xA00000).iterator()
    timeProvider.staticTime = { times.next().toLong() }
    TokenGenerator.emailInterceptor = { "abcd1234" }

    assertThat(repository.addUser(creator, Stubs.project.userIdType))
      .isDataClassEqualTo(
        Stubs.user.copy(
          account = null,
          ownedTokens = emptyList(),
        )
      )
  }

  @Test fun `adding user with phone ID generates a phone-friendly verification token`() {
    val creator = Stubs.userCreator.copy(id = "+1234567890")

    // create & update times from the stub
    val times = listOf(0xC00000, 0xA00000).iterator()
    timeProvider.staticTime = { times.next().toLong() }
    TokenGenerator.phoneInterceptor = { "123456" }

    assertThat(repository.addUser(creator, UserIdType.PHONE))
      .isDataClassEqualTo(
        Stubs.user.copy(
          userId = UserId(creator.id!!, creator.projectId),
          account = null,
          ownedTokens = emptyList(),
          verificationToken = "123456"
        )
      )
  }

  // endregion

  // region Fetch by ID

  @Test fun `fetching user by invalid ID throws`() {
    userDao.stub {
      onGeneric { findById(Stubs.userIdDbm) } doThrow IllegalArgumentException("failed")
    }

    assertThat { repository.fetchUserByUserId(Stubs.userId, withTokens = false) }
      .isFailure()
      .all {
        hasClass(IllegalArgumentException::class)
        hasMessage("failed")
      }
  }

  @Test fun `fetching user by ID with no tokens works`() {
    userDao.stub {
      onGeneric { findById(Stubs.userIdDbm) } doReturn Optional.of(Stubs.userDbm)
    }

    assertThat(repository.fetchUserByUserId(Stubs.userId, withTokens = false))
      .isDataClassEqualTo(Stubs.user.copy(ownedTokens = emptyList()))
  }

  @Test fun `fetching user by ID with tokens works`() {
    userDao.stub {
      onGeneric { findById(Stubs.userIdDbm) } doReturn Optional.of(Stubs.userDbm)
    }

    assertThat(repository.fetchUserByUserId(Stubs.userId, withTokens = true))
      .isDataClassEqualTo(Stubs.user)
  }

  // endregion

  // region Fetch by unified ID

  @Test fun `fetching user by malformed unified ID throws`() {
    assertThat { repository.fetchUserByUnifiedId("malformed", withTokens = false) }
      .isFailure()
      .hasClass(NumberFormatException::class)
  }

  @Test fun `fetching user by invalid unified ID throws`() {
    userDao.stub {
      onGeneric { findById(Stubs.userIdDbm) } doThrow IllegalArgumentException("failed")
    }

    assertThat { repository.fetchUserByUnifiedId(Stubs.unifiedUserId, withTokens = false) }
      .isFailure()
      .all {
        hasClass(IllegalArgumentException::class)
        hasMessage("failed")
      }
  }

  @Test fun `fetching user by unified ID with no tokens works`() {
    userDao.stub {
      onGeneric { findById(Stubs.userIdDbm) } doReturn Optional.of(Stubs.userDbm)
    }

    assertThat(repository.fetchUserByUnifiedId(Stubs.unifiedUserId, withTokens = false))
      .isDataClassEqualTo(Stubs.user.copy(ownedTokens = emptyList()))
  }

  @Test fun `fetching user by unified ID with tokens works`() {
    userDao.stub {
      onGeneric { findById(Stubs.userIdDbm) } doReturn Optional.of(Stubs.userDbm)
    }

    assertThat(repository.fetchUserByUnifiedId(Stubs.unifiedUserId, withTokens = true))
      .isDataClassEqualTo(Stubs.user)
  }

  // endregion

  // region Fetch by contact

  @Test fun `fetching users by invalid contact throws`() {
    userDao.stub {
      onGeneric { findAllByContact(Stubs.user.contact!!) } doThrow IllegalArgumentException("failed")
    }

    assertThat { repository.fetchAllUsersByContact(Stubs.user.contact!!) }
      .isFailure()
      .all {
        hasClass(IllegalArgumentException::class)
        hasMessage("failed")
      }
  }

  @Test fun `fetching users by contact works`() {
    userDao.stub {
      onGeneric { findAllByContact(Stubs.user.contact!!) } doReturn listOf(Stubs.userDbm)
    }

    assertThat(repository.fetchAllUsersByContact(Stubs.user.contact!!))
      .all {
        hasSize(1)
        transform { it.first() }
          .isDataClassEqualTo(Stubs.user.copy(ownedTokens = emptyList()))
      }
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
        transform { it.first() }
          .isDataClassEqualTo(Stubs.user.copy(ownedTokens = emptyList()))
      }
  }

  // endregion

  // region Fetch by Account

  @Test fun `fetching users by invalid account throws`() {
    userDao.stub {
      onGeneric { findAllByAccount(Stubs.accountDbm) } doThrow IllegalArgumentException("failed")
    }

    assertThat { repository.fetchAllUsersByAccount(Stubs.account) }
      .isFailure()
      .all {
        hasClass(IllegalArgumentException::class)
        hasMessage("failed")
      }
  }

  @Test fun `fetching users by account works`() {
    userDao.stub {
      onGeneric { findAllByAccount(Stubs.accountDbm) } doReturn listOf(Stubs.userDbm)
    }

    assertThat(repository.fetchAllUsersByAccount(Stubs.account))
      .all {
        hasSize(1)
        transform { it.first() }
          .isDataClassEqualTo(Stubs.user.copy(ownedTokens = emptyList()))
      }
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
      .isDataClassEqualTo(
        Stubs.userUpdated.copy(
          ownedTokens = emptyList(), // tokens are not pulled for updates
          account = Stubs.accountUpdated.copy(owners = emptyList()), // dropped during storage for updates
        )
      )
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
          ownedTokens = emptyList(), // tokens are not pulled for updates
          account = Stubs.account.copy(owners = emptyList()), // dropped during storage for updates
        )
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
          ownedTokens = emptyList(), // tokens are not pulled for updates
          account = Stubs.accountUpdated.copy(owners = emptyList()), // dropped during storage for updates
        )
      )
  }

  // endregion

  // region Remove user

  @Test fun `removing user by invalid ID throws`() {
    userDao.stub {
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
      onGeneric { deleteById(Stubs.userIdDbm) } doAnswer {}
    }

    assertThat { repository.removeUserById(Stubs.userId) }
      .isSuccess()
  }

  @Test fun `removing user by malformed unified ID throws`() {
    assertThat { repository.removeUserByUnifiedFormat("malformed") }
      .isFailure()
      .hasClass(NumberFormatException::class)
  }

  @Test fun `removing user by invalid unified ID throws`() {
    userDao.stub {
      onGeneric { deleteById(Stubs.userIdDbm) } doThrow IllegalArgumentException("failed")
    }

    assertThat { repository.removeUserByUnifiedFormat(Stubs.unifiedUserId) }
      .isFailure()
      .all {
        hasClass(IllegalArgumentException::class)
        hasMessage("failed")
      }
  }

  @Test fun `removing user by unified ID with tokens works`() {
    userDao.stub {
      onGeneric { deleteById(Stubs.userIdDbm) } doAnswer {}
    }

    assertThat { repository.removeUserByUnifiedFormat(Stubs.unifiedUserId) }
      .isSuccess()
  }

  // endregion

}
