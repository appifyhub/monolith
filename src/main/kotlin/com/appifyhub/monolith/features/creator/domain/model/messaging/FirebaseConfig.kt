package com.appifyhub.monolith.features.creator.domain.model.messaging

data class FirebaseConfig(
  val projectName: String,
  val serviceAccountKeyJsonBase64: String,
)
