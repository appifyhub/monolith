package com.appifyhub.monolith.features.user.domain.service

import com.appifyhub.monolith.features.user.domain.model.PushDevice
import com.appifyhub.monolith.features.user.domain.model.User

interface PushDeviceService {

  @Throws fun addDevice(device: PushDevice): PushDevice

  @Throws fun fetchDeviceById(id: String): PushDevice

  @Throws fun fetchAllDevicesByUser(owner: User): List<PushDevice>

  @Throws fun deleteDeviceById(id: String)

  @Throws fun deleteAllDevicesByUser(owner: User)

}
