package com.appifyhub.monolith.heartbeat

import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.RestController
import java.time.Instant

@RestController("/")
class HeartbeatController {

  @GetMapping("heartbeat")
  fun beat() = Instant.now().toString()

}