package com.appifyhub.monolith.features.geo.storage

import com.appifyhub.monolith.features.geo.domain.model.Geolocation

interface GeolocationDao {

  @Throws fun fetchGeolocationForIp(ipAddress: String): Geolocation

}
