package com.appifyhub.monolith.repository.auth.locator

import com.appifyhub.monolith.domain.user.UserId
import com.appifyhub.monolith.util.takeIfNotBlank
import org.springframework.stereotype.Component
import java.util.Base64

private const val HEX = 16
private const val DELIMITER = "$#TLPI#$"
private const val EMPTY_VALUE = "$#EMPTY#$"
private const val NULL_VALUE = "$#NULL#$"

@Component
class TokenLocatorCoderImpl : TokenLocatorEncoder, TokenLocatorDecoder {

  private val encoder = Base64.getEncoder()
  private val decoder = Base64.getDecoder()

  override fun encode(locator: TokenLocator): String {
    val projectId = locator.userId.projectId.toString(HEX)
    val userId = encoder.encode(
      (locator.userId.id.takeIfNotBlank() ?: EMPTY_VALUE).encodeToByteArray()
    ).decodeToString()
    val origin = encoder.encode(
      (locator.origin?.takeIfNotBlank() ?: NULL_VALUE).encodeToByteArray()
    ).decodeToString()
    val timestamp = locator.timestamp.toString(HEX)
    val rawLocator = "$projectId$DELIMITER$userId$DELIMITER$origin$DELIMITER$timestamp"
    return encoder.encode(rawLocator.encodeToByteArray()).decodeToString()
  }

  override fun decode(encoded: String): TokenLocator {
    require(encoded.isNotBlank()) { "Blank encoded Token Locator" }

    val decoded = decoder.decode(encoded).decodeToString()
    require(decoded.isNotBlank()) { "Blank decoded Token Locator: $decoded" }

    val parts = decoded.split(DELIMITER)
    require(parts.size == 4) { "Wrong Token Locator format; ${parts.joinToString()}" }

    val projectIdRaw = parts[0]
    val projectId = projectIdRaw.toLong(HEX)

    val userIdRaw = parts[1]
    val userId = decoder.decode(userIdRaw).decodeToString()
    require(userId.isNotBlank()) { "Blank decoded User ID" }
    require(userId != EMPTY_VALUE) { "User ID was empty" }

    val originRaw = parts[2]
    val originDecoded = decoder.decode(originRaw).decodeToString()
    val origin = originDecoded.takeIfNotBlank()?.takeIf { it != NULL_VALUE }

    val timestampRaw = parts[3]
    val timestamp = timestampRaw.toLong(HEX)

    return TokenLocator(
      userId = UserId(userId, projectId),
      origin = origin,
      timestamp = timestamp,
    )
  }

}

