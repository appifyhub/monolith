package com.appifyhub.monolith.storage.model.messaging

import java.io.Serializable
import java.util.Date
import javax.persistence.Column
import javax.persistence.EmbeddedId
import javax.persistence.Entity
import javax.persistence.FetchType
import javax.persistence.JoinColumn
import javax.persistence.ManyToOne
import javax.persistence.MapsId
import javax.persistence.Temporal
import javax.persistence.TemporalType

@Entity(name = "variable_binding")
class VariableBindingDbm(

  @EmbeddedId
  var id: VariableBindingKeyDbm,

  @Suppress("unused") // ignored in equals/hashCode/toString, but used for relation mapping
  @MapsId("templateId")
  @JoinColumn(referencedColumnName = "id", nullable = false, updatable = false)
  @ManyToOne(fetch = FetchType.LAZY)
  var template: MessageTemplateDbm,

  @Column(nullable = true, length = 64)
  var bindingCode: String,

  @Column(nullable = false, updatable = false)
  @Temporal(TemporalType.TIMESTAMP)
  var createdAt: Date,

  @Column(nullable = false)
  @Temporal(TemporalType.TIMESTAMP)
  var updatedAt: Date = createdAt,

) : Serializable {

  override fun equals(other: Any?): Boolean {
    if (this === other) return true
    if (other !is VariableBindingDbm) return false

    if (id != other.id) return false
    if (bindingCode != other.bindingCode) return false
    if (createdAt != other.createdAt) return false
    if (updatedAt != other.updatedAt) return false

    return true
  }

  override fun hashCode(): Int {
    var result = id.hashCode()
    result = 31 * result + bindingCode.hashCode()
    result = 31 * result + createdAt.hashCode()
    result = 31 * result + updatedAt.hashCode()
    return result
  }

  override fun toString(): String {
    return "VariableBindingDbm(" +
      "id=$id, " +
      "bindingCode='$bindingCode', " +
      "createdAt=$createdAt, " +
      "updatedAt=$updatedAt" +
      ")"
  }

}
