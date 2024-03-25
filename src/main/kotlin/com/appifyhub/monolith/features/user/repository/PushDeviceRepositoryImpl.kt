package com.appifyhub.monolith.features.user.repository

import com.appifyhub.monolith.features.user.domain.model.PushDevice
import com.appifyhub.monolith.features.user.domain.model.User
import com.appifyhub.monolith.features.user.domain.toData
import com.appifyhub.monolith.features.user.domain.toDomain
import com.appifyhub.monolith.features.user.storage.PushDeviceDao
import com.appifyhub.monolith.features.user.storage.model.PushDeviceDbm
import org.slf4j.LoggerFactory
import org.springframework.stereotype.Repository

@Repository
class PushDeviceRepositoryImpl(
  private val pushDeviceDao: PushDeviceDao,
) : PushDeviceRepository {

  private val log = LoggerFactory.getLogger(this::class.java)

  override fun addDevice(device: PushDevice): PushDevice {
    log.debug("Adding push device $device")

    return pushDeviceDao.save(device.toData()).toDomain()
  }

  override fun fetchDeviceById(id: String): PushDevice {
    log.debug("Fetching push device by ID $id")

    return pushDeviceDao.findById(id).get().toDomain()
  }

  override fun fetchAllDevicesByUser(owner: User): List<PushDevice> {
    log.debug("Fetching push devices by user $owner")

    return pushDeviceDao.findAllByOwner(owner.toData()).map(PushDeviceDbm::toDomain)
  }

  override fun deleteDeviceById(id: String) {
    log.debug("Deleting push device by ID $id")

    pushDeviceDao.deleteById(id)
  }

  override fun deleteAllDevicesByUser(owner: User) {
    log.debug("Deleting push devices by user $owner")

    pushDeviceDao.deleteAllByOwner(owner.toData())
  }

}
