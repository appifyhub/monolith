package com.appifyhub.monolith.storage.dao

import com.appifyhub.monolith.domain.geo.Geolocation

interface GeolocationDao {

  @Throws fun fetchGeolocationForIp(ipAddress: String): Geolocation

}
