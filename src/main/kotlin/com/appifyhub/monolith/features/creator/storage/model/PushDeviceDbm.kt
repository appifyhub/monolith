package com.appifyhub.monolith.features.creator.storage.model

import com.appifyhub.monolith.storage.model.user.UserDbm
import java.io.Serializable
import javax.persistence.Column
import javax.persistence.Entity
import javax.persistence.FetchType
import javax.persistence.Id
import javax.persistence.ManyToOne

@Entity(name = "push_device")
class PushDeviceDbm(

  @Id
  @Column(unique = true, nullable = false, updatable = false, length = 1024)
  var deviceId: String,

  @Column(nullable = false, updatable = false, length = 16)
  var type: String, // Android, iOS, Web, ...

  @ManyToOne(fetch = FetchType.EAGER)
  var owner: UserDbm,

) : Serializable {

  @Suppress("DuplicatedCode") // false positive
  override fun equals(other: Any?): Boolean {
    if (this === other) return true
    if (other !is PushDeviceDbm) return false

    if (deviceId != other.deviceId) return false
    if (type != other.type) return false
    if (owner != other.owner) return false

    return true
  }

  @Suppress("DuplicatedCode") // false positive
  override fun hashCode(): Int {
    var result = deviceId.hashCode()
    result = 31 * result + type.hashCode()
    result = 31 * result + owner.hashCode()
    return result
  }

  @Suppress("DuplicatedCode") // false positive
  override fun toString(): String {
    return "PushDeviceDbm(" +
      "deviceId='$deviceId', " +
      "type=$type, " +
      "owner=$owner" +
      ")"
  }

}
