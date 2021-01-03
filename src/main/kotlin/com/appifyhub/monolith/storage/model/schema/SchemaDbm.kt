package com.appifyhub.monolith.storage.model.schema

import java.io.Serializable
import javax.persistence.Column
import javax.persistence.Entity
import javax.persistence.Id

@Entity(name = "schema")
class SchemaDbm(

  @Id
  @Column(unique = true, nullable = false, updatable = false)
  var version: Long,

  @Column(nullable = false)
  var isInitialized: Boolean,

) : Serializable
