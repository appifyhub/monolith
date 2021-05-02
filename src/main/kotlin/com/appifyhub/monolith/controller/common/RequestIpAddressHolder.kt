package com.appifyhub.monolith.controller.common

import org.slf4j.LoggerFactory
import org.springframework.web.context.request.RequestContextHolder
import org.springframework.web.context.request.ServletRequestAttributes

private val IP_HEADERS = listOf(
  "X-Forwarded-For",
  "Proxy-Client-IP",
  "WL-Proxy-Client-IP",
  "HTTP_X_FORWARDED_FOR",
  "HTTP_X_FORWARDED",
  "HTTP_X_CLUSTER_CLIENT_IP",
  "HTTP_CLIENT_IP",
  "HTTP_FORWARDED_FOR",
  "HTTP_FORWARDED",
  "HTTP_VIA",
  "REMOTE_ADDR",
)

private val IP_UNSUPPORTED_VALUES = listOf(
  "unknown",
  "0.0.0.0",
  "1.1.1.1",
  "0.0.0.1",
  "255.255.255.255",
  "1:1:1:1:1:1:1:1",
  "0:0:0:0:0:0:0:0",
  "0:0:0:0:0:0:0:1",
  "ff:ff:ff:ff:ff:ff:ff:ff",
  "ffff:ffff:ffff:ffff:ffff:ffff:ffff:ffff",
)

private const val HEADER_VALUE_DELIMITER = ","

interface RequestIpAddressHolder {

  fun getRequestIpAddress(): String? = try {
    val requestAttributes = RequestContextHolder.currentRequestAttributes()
    val request = (requestAttributes as ServletRequestAttributes).request

    IP_HEADERS
      // pull the actual header values
      .map { request.getHeader(it) }
      // filter out nulls and blanks
      .filterNot { it.isNullOrBlank() }
      // take out individual values (header is comma-concatenated)
      .flatMap { it?.split(HEADER_VALUE_DELIMITER).orEmpty() }
      // filter out blanks (now split)
      .filterNot { it.isBlank() }
      // filter out unsupported values (now split), pick the first good option
      .firstOrNull { IP_UNSUPPORTED_VALUES.doesNotContain(it) }
      // take from the request data as a last resort
      ?: request.remoteAddr?.takeIf { IP_UNSUPPORTED_VALUES.doesNotContain(it) }
  } catch (t: Throwable) {
    val log = LoggerFactory.getLogger(this::class.java)
    log.warn("Can't extract IP address for request on ${Thread.currentThread().name}", t)
    null
  }

  private fun List<String>.doesNotContain(item: String): Boolean {
    forEach {
      if (it.equals(item, ignoreCase = true)) return false
    }
    return true
  }

}
