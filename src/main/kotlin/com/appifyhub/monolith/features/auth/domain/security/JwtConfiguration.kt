package com.appifyhub.monolith.features.auth.domain.security

import org.slf4j.LoggerFactory
import org.springframework.beans.factory.annotation.Value
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.security.oauth2.jwt.JwtDecoder
import org.springframework.security.oauth2.jwt.NimbusJwtDecoder
import java.io.FileInputStream
import java.security.KeyStore
import java.security.interfaces.RSAPrivateKey
import java.security.interfaces.RSAPublicKey

@Configuration
class JwtConfiguration {

  private val log = LoggerFactory.getLogger(this::class.java)

  @Value("\${app.security.jwt.keystore-location}")
  private lateinit var keyStorePath: String

  @Value("\${app.security.jwt.keystore-password}")
  private lateinit var keyStorePassword: String

  @Value("\${app.security.jwt.key-alias}")
  private lateinit var keyAlias: String

  @Value("\${app.security.jwt.private-key-passphrase}")
  private lateinit var privateKeyPassphrase: String

  @Bean
  fun keyStore(): KeyStore = try {
    KeyStore.getInstance(KeyStore.getDefaultType()).apply {
      val resourceAsStream = Thread.currentThread().contextClassLoader.getResourceAsStream(keyStorePath)
        ?: FileInputStream(keyStorePath) // used for external keystores
      load(resourceAsStream, keyStorePassword.toCharArray())
    }
  } catch (t: Throwable) {
    log.error("Unable to load keystore: {}", keyStorePath, t)
    error("Unable to load keystore")
  }

  @Bean
  fun jwtSigningKey(keyStore: KeyStore): RSAPrivateKey = try {
    keyStore.getKey(keyAlias, privateKeyPassphrase.toCharArray())
      ?.let { it as? RSAPrivateKey }
      ?: error("Wrong key type")
  } catch (t: Throwable) {
    log.error("Unable to load private key from keystore: {}", keyStorePath, t)
    error("Unable to load private key")
  }

  @Bean
  fun jwtValidationKey(keyStore: KeyStore): RSAPublicKey = try {
    keyStore.getCertificate(keyAlias)
      ?.publicKey
      ?.let { it as? RSAPublicKey }
      ?: error("Wrong key type")
  } catch (t: Throwable) {
    log.error("Unable to load private key from keystore: {}", keyStorePath, t)
    error("Unable to load RSA public key")
  }

  @Bean
  fun jwtDecoder(rsaPublicKey: RSAPublicKey): JwtDecoder =
    NimbusJwtDecoder.withPublicKey(rsaPublicKey).build()

}
