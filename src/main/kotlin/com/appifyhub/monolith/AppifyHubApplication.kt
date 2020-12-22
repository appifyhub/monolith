package com.appifyhub.monolith

import org.springframework.boot.autoconfigure.SpringBootApplication
import org.springframework.boot.runApplication

@SpringBootApplication
class AppifyHubApplication

fun main(args: Array<String>) {
  runApplication<AppifyHubApplication>(*args)
}
