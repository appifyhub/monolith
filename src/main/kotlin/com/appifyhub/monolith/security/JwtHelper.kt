package com.appifyhub.monolith.security

import com.appifyhub.monolith.security.JwtHelper.Claims.JWT_SECTION_DELIMITER
import com.appifyhub.monolith.security.JwtHelper.Claims.VALUE
import com.auth0.jwt.JWT
import com.auth0.jwt.algorithms.Algorithm
import com.fasterxml.jackson.databind.ObjectMapper
import java.security.interfaces.RSAPrivateKey
import java.security.interfaces.RSAPublicKey
import java.util.Base64
import java.util.Date
import org.springframework.stereotype.Component

typealias JwtClaims = Map<String, *>

@Component
class JwtHelper(
  private val privateKey: RSAPrivateKey,
  private val publicKey: RSAPublicKey,
  private val jsonMapper: ObjectMapper,
) {

  object Claims {
    const val VALUE = "value"
    const val USER_ID = "user_id"
    const val PROJECT_ID = "project_id"
    const val UNIVERSAL_ID = "universal_id"
    const val CREATED_AT = "iat"
    const val EXPIRES_AT = "exp"
    const val AUTHORITIES = "authorities"
    const val ORIGIN = "origin"
    const val IP_ADDRESS = "ip_address"
    const val GEO = "geo"
    const val IS_STATIC = "is_static"

    const val AUTHORITY_DELIMITER = ","
    const val JWT_SECTION_DELIMITER = "."
  }

  private val decoder = Base64.getDecoder()

  fun createJwtForClaims(
    subject: String,
    claims: JwtClaims,
    createdAt: Date,
    expiresAt: Date,
  ): String = JWT.create()
    .withSubject(subject)
    .withIssuedAt(createdAt)
    .withExpiresAt(expiresAt)
    .apply {
      claims.forEach { withClaim(it.key, it.value.toString()) }
    }
    .sign(Algorithm.RSA256(publicKey, privateKey))

  @Suppress("UNCHECKED_CAST")
  fun extractPropertiesFromJwt(tokenValue: String): JwtClaims =
    tokenValue.split(JWT_SECTION_DELIMITER)[1] // JWT is "header.content.signature"
      .let {
        jsonMapper.readValue(
          decoder.decode(it).decodeToString(),
          HashMap::class.java,
        ) as MutableMap<String, Any>
      }
      .apply { put(VALUE, tokenValue) }

}
