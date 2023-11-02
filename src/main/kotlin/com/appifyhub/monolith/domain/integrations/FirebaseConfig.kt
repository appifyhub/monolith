package com.appifyhub.monolith.domain.integrations

data class FirebaseConfig(
  val projectName: String,
  val serviceAccountKeyJsonBase64: String,
)
