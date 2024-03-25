package com.appifyhub.monolith.features.geo.repository

import com.appifyhub.monolith.features.geo.domain.model.Geolocation

interface GeolocationRepository {

  fun fetchGeolocationForIp(ipAddress: String?): Geolocation?

}
