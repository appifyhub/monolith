package com.appifyhub.monolith.repository.auth.locator

interface TokenLocatorDecoder {

  @Throws fun decode(encoded: String): TokenLocator

}