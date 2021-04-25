package com.appifyhub.monolith.repository.auth.locator

interface TokenLocatorEncoder {

  fun encode(locator: TokenLocator): String

}