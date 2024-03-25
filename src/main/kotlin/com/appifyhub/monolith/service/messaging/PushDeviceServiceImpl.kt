package com.appifyhub.monolith.service.messaging

import com.appifyhub.monolith.domain.messaging.PushDevice
import com.appifyhub.monolith.domain.user.User
import com.appifyhub.monolith.repository.messaging.PushDeviceRepository
import com.appifyhub.monolith.util.extension.requireValid
import com.appifyhub.monolith.validation.impl.Normalizers
import org.slf4j.LoggerFactory
import org.springframework.stereotype.Service

@Service
class PushDeviceServiceImpl(
  private val repository: PushDeviceRepository,
) : PushDeviceService {

  private val log = LoggerFactory.getLogger(this::class.java)

  override fun addDevice(device: PushDevice): PushDevice {
    log.debug("Adding a push device $device")

    val normalizedDeviceId = Normalizers.PushDeviceToken.run(device.deviceId).requireValid { "Push Device Token" }
    val normalizedPushDevice = PushDevice(
      deviceId = normalizedDeviceId,
      type = device.type,
      owner = device.owner,
    )
    return repository.addDevice(normalizedPushDevice)
  }

  override fun fetchDeviceById(id: String): PushDevice {
    log.debug("Fetching a push device by ID $id")
    val normalizedDeviceId = Normalizers.PushDeviceToken.run(id).requireValid { "Push Device Token" }
    return repository.fetchDeviceById(normalizedDeviceId)
  }

  override fun fetchAllDevicesByUser(owner: User): List<PushDevice> {
    log.debug("Fetching push devices by owner $owner")
    return repository.fetchAllDevicesByUser(owner)
  }

  override fun deleteDeviceById(id: String) {
    log.debug("Deleting a push device by ID $id")
    val normalizedDeviceId = Normalizers.PushDeviceToken.run(id).requireValid { "Push Device Token" }
    repository.deleteDeviceById(normalizedDeviceId)
  }

  override fun deleteAllDevicesByUser(owner: User) {
    log.debug("Deleting push devices by owner $owner")
    repository.deleteAllDevicesByUser(owner)
  }

}
