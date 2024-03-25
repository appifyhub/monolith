package com.appifyhub.monolith.features.user.api

import com.appifyhub.monolith.features.user.api.model.PushDeviceRequest
import com.appifyhub.monolith.features.user.api.model.PushDeviceResponse
import com.appifyhub.monolith.features.user.api.model.PushDevicesResponse
import com.appifyhub.monolith.features.user.domain.model.PushDevice
import com.appifyhub.monolith.features.user.domain.model.User

fun PushDevice.toNetwork() = PushDeviceResponse(
  deviceId = deviceId,
  type = type.name,
)

fun Collection<PushDevice>.toNetwork() = PushDevicesResponse(
  devices = map(PushDevice::toNetwork),
)

fun PushDeviceRequest.toDomain(owner: User) = PushDevice(
  deviceId = deviceId,
  type = PushDevice.Type.find(type),
  owner = owner,
)
