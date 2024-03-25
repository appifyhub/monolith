package com.appifyhub.monolith.features.user.domain

import com.appifyhub.monolith.features.creator.domain.model.Project
import com.appifyhub.monolith.features.user.domain.model.PushDevice
import com.appifyhub.monolith.features.user.storage.model.PushDeviceDbm

fun PushDeviceDbm.toDomain(): PushDevice = PushDevice(
  deviceId = deviceId,
  type = PushDevice.Type.find(type),
  owner = owner.toDomain(),
)

fun PushDevice.toData(project: Project? = null): PushDeviceDbm = PushDeviceDbm(
  deviceId = deviceId,
  type = type.name,
  owner = owner.toData(project),
)
