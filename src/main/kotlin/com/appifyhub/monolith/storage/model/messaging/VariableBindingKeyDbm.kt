package com.appifyhub.monolith.storage.model.messaging

import java.io.Serializable
import javax.persistence.Column
import javax.persistence.Embeddable

@Embeddable
class VariableBindingKeyDbm(

  @Column(nullable = false, updatable = false)
  var templateId: Long,

  @Column(nullable = false, length = 32)
  var variableName: String,

) : Serializable {

  override fun equals(other: Any?): Boolean {
    if (this === other) return true
    if (other !is VariableBindingKeyDbm) return false

    if (templateId != other.templateId) return false
    if (variableName != other.variableName) return false

    return true
  }

  override fun hashCode(): Int {
    var result = templateId.hashCode()
    result = 31 * result + variableName.hashCode()
    return result
  }

  override fun toString(): String {
    return "VariableBindingKeyDbm(" +
      "templateId=$templateId, " +
      "variableName='$variableName'" +
      ")"
  }

}
