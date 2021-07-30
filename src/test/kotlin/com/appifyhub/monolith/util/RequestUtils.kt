package com.appifyhub.monolith.util

import java.nio.charset.Charset
import org.springframework.http.HttpEntity
import org.springframework.http.HttpHeaders
import org.springframework.http.MediaType

fun blankRequest() = HttpEntity<Any?>(
  HttpHeaders().apply {
    acceptCharset = listOf(Charset.defaultCharset())
    accept = listOf(MediaType.APPLICATION_JSON)
  }
)

fun <T> bodyRequest(body: T) = HttpEntity<T>(
  body,
  HttpHeaders().apply {
    acceptCharset = listOf(Charset.defaultCharset())
    accept = listOf(MediaType.APPLICATION_JSON)
  }
)

fun bearerBlankRequest(token: String) = HttpEntity<Any?>(
  HttpHeaders().apply {
    acceptCharset = listOf(Charset.defaultCharset())
    accept = listOf(MediaType.APPLICATION_JSON)
    setBearerAuth(token)
  }
)

fun <T> bearerBodyRequest(body: T, token: String) = HttpEntity<T>(
  body,
  HttpHeaders().apply {
    acceptCharset = listOf(Charset.defaultCharset())
    accept = listOf(MediaType.APPLICATION_JSON)
    setBearerAuth(token)
  }
)

fun blankUriVariables() = emptyMap<String, String>()
