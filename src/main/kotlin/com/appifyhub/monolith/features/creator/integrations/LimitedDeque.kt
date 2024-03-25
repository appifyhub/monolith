package com.appifyhub.monolith.features.creator.integrations

import com.appifyhub.monolith.features.creator.integrations.email.LogEmailSender
import kotlin.properties.ReadWriteProperty
import kotlin.reflect.KProperty

/**
 * Produces a delegated property of ArrayDeque type.
 * The custom property holds the collection internally and limits it to a maximum of 'limit' items.
 * Limiting/trimming happens on each read and write of the property.
 *
 * @param limit How big can the collection grow before it gets trimmed
 * @return Kotlin's delegated property
 * @sample LogEmailSender.history
 */
@Suppress("SameParameterValue")
fun <E> limitedDeque(limit: Int): ReadWriteProperty<Any, ArrayDeque<E>> =
  object : ReadWriteProperty<Any, ArrayDeque<E>> {

    private var deque: ArrayDeque<E> = ArrayDeque(limit)

    override fun getValue(thisRef: Any, property: KProperty<*>): ArrayDeque<E> {
      applyLimit()
      return deque
    }

    override fun setValue(thisRef: Any, property: KProperty<*>, value: ArrayDeque<E>) {
      deque = value
      applyLimit()
    }

    private fun applyLimit() {
      while (deque.size > limit) {
        deque.removeFirst()
      }
    }

  }
