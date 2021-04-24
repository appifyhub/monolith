package com.appifyhub.monolith.util

import org.springframework.http.HttpEntity
import org.springframework.http.HttpHeaders
import org.springframework.http.MediaType
import java.nio.charset.Charset

fun emptyRequestEntity(): HttpEntity<*> = HttpEntity<Any?>(
  HttpHeaders().apply {
    acceptCharset = listOf(Charset.defaultCharset())
    accept = listOf(MediaType.APPLICATION_JSON)
  }
)

fun bearerAuthRequestEntity(token: String): HttpEntity<*> = HttpEntity<Any?>(
  HttpHeaders().apply {
    acceptCharset = listOf(Charset.defaultCharset())
    accept = listOf(MediaType.APPLICATION_JSON)
    setBearerAuth(token)
  }
)

fun emptyUriVariables() = emptyMap<String, String>()
