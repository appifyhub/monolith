package com.appifyhub.monolith.repository.geo

import com.appifyhub.monolith.domain.geo.Geolocation

interface GeolocationRepository {

  fun fetchGeolocationForIp(ipAddress: String): Geolocation?

}
