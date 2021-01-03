package com.appifyhub.monolith.jwt

import com.appifyhub.monolith.util.TimeProvider
import com.auth0.jwt.JWT
import com.auth0.jwt.algorithms.Algorithm
import org.springframework.beans.factory.annotation.Value
import org.springframework.stereotype.Component
import java.security.interfaces.RSAPrivateKey
import java.security.interfaces.RSAPublicKey
import java.util.Calendar

@Component
class JwtHelper(
  private val privateKey: RSAPrivateKey,
  private val publicKey: RSAPublicKey,
  private val timeProvider: TimeProvider,
) {

  @Value("\${app.security.jwt.default-expiration-days}")
  private var defaultExpirationDays: Int = 1

  fun createJwtForClaims(
    subject: String,
    claims: Map<String, String>,
  ): String = JWT.create()
    .withSubject(subject)
    .apply {
      claims.forEach { withClaim(it.key, it.value) }
    }
    .withNotBefore(timeProvider.currentCalendar.time)
    .withIssuedAt(timeProvider.currentCalendar.time)
    .withExpiresAt(
      timeProvider.currentCalendar.apply {
        add(Calendar.DAY_OF_MONTH, defaultExpirationDays)
      }.time
    )
    .sign(Algorithm.RSA256(publicKey, privateKey))

}