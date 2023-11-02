package com.appifyhub.monolith.repository.messaging

import com.appifyhub.monolith.domain.messaging.PushDevice
import com.appifyhub.monolith.domain.user.User

interface PushDeviceRepository {

  @Throws fun addDevice(device: PushDevice): PushDevice

  @Throws fun fetchDeviceById(id: String): PushDevice

  @Throws fun fetchAllDevicesByUser(owner: User): List<PushDevice>

  @Throws fun deleteDeviceById(id: String)

  @Throws fun deleteAllDevicesByUser(owner: User)

}
