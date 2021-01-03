package com.appifyhub.monolith

import org.springframework.boot.autoconfigure.SpringBootApplication
import org.springframework.boot.runApplication

@SpringBootApplication
class AppifyHubApplication {
  object Profile {
    const val DEFAULT = "default"
    const val IN_MEM = "memory"
    const val POSTGRES = "postgres"
    const val H2 = "h2"
  }
}

fun main(args: Array<String>) {
  runApplication<AppifyHubApplication>(*args)
}
