package com.appifyhub.monolith.repository.messaging

import assertk.all
import assertk.assertThat
import assertk.assertions.hasSize
import assertk.assertions.isDataClassEqualTo
import assertk.assertions.isEqualTo
import com.appifyhub.monolith.storage.dao.PushDeviceDao
import com.appifyhub.monolith.storage.model.messaging.PushDeviceDbm
import com.appifyhub.monolith.storage.model.user.UserDbm
import com.appifyhub.monolith.util.Stubs
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.mockito.kotlin.any
import org.mockito.kotlin.argumentCaptor
import org.mockito.kotlin.doAnswer
import org.mockito.kotlin.doReturn
import org.mockito.kotlin.mock
import org.mockito.kotlin.stub
import org.mockito.kotlin.verify
import java.util.Optional

class PushDeviceRepositoryImplTest {

  private val pushDeviceDao = mock<PushDeviceDao>()

  private val repository: PushDeviceRepository = PushDeviceRepositoryImpl(
    pushDeviceDao = pushDeviceDao,
  )

  @BeforeEach fun setup() {
    pushDeviceDao.stub {
      onGeneric { save(any()) } doAnswer { it.arguments.first() as PushDeviceDbm }
    }
  }

  @Test fun `saving a new push device works`() {
    repository.addDevice(Stubs.pushDevice)

    // data conversion loses some attributes from the foreign key user,
    // so this is a workaround to test only what matters
    val captor = argumentCaptor<PushDeviceDbm>()
    verify(pushDeviceDao).save(captor.capture())
    assertThat(captor.firstValue).all {
      transform { it.deviceId }.isEqualTo(Stubs.pushDevice.deviceId)
      transform { it.type }.isEqualTo(Stubs.pushDevice.type.name)
      transform { it.owner.id }.isEqualTo(Stubs.userDbm.id)
    }
  }

  @Test fun `fetching a push device by ID works`() {
    pushDeviceDao.stub {
      onGeneric { findById(any()) } doReturn Optional.of(Stubs.pushDeviceDbm)
    }

    assertThat(repository.fetchDeviceById(Stubs.pushDevice.deviceId))
      .isDataClassEqualTo(Stubs.pushDevice)
  }

  @Test fun `fetching push devices by owner works`() {
    pushDeviceDao.stub {
      onGeneric { findAllByOwner(any()) } doReturn listOf(Stubs.pushDeviceDbm)
    }

    assertThat(repository.fetchAllDevicesByUser(Stubs.user))
      .all {
        hasSize(1)
        transform { it.first() }.isDataClassEqualTo(Stubs.pushDevice)
      }
  }

  @Test fun `deleting a push device by ID works`() {
    repository.deleteDeviceById(Stubs.pushDevice.deviceId)

    verify(pushDeviceDao).deleteById(Stubs.pushDevice.deviceId)
  }

  @Test fun `deleting push devices by owner works`() {
    repository.deleteAllDevicesByUser(Stubs.user)

    // data conversion loses some attributes from the foreign key user,
    // so this is a workaround to test only what matters
    val captor = argumentCaptor<UserDbm>()
    verify(pushDeviceDao).deleteAllByOwner(captor.capture())
    assertThat(captor.firstValue).all {
      transform { it.id }.isEqualTo(Stubs.userDbm.id)
    }
  }

}
