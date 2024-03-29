package com.appifyhub.monolith.features.init.repository

import assertk.all
import assertk.assertFailure
import assertk.assertThat
import assertk.assertions.hasClass
import assertk.assertions.isEqualTo
import assertk.assertions.isFalse
import assertk.assertions.isTrue
import assertk.assertions.messageContains
import com.appifyhub.monolith.features.init.storage.SchemaDao
import com.appifyhub.monolith.features.init.storage.model.SchemaDbm
import com.appifyhub.monolith.util.Stubs
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.mockito.kotlin.any
import org.mockito.kotlin.doAnswer
import org.mockito.kotlin.doReturn
import org.mockito.kotlin.doThrow
import org.mockito.kotlin.mock
import org.mockito.kotlin.stub
import java.util.Optional

class SchemaRepositoryImplTest {

  private val schemaDao = mock<SchemaDao>()

  private val repository: SchemaRepository = SchemaRepositoryImpl(
    schemaDao = schemaDao,
  )

  @BeforeEach fun setup() {
    schemaDao.stub {
      onGeneric { save(any()) } doAnswer { it.arguments.first() as SchemaDbm }
    }
  }

  // region Saving

  @Test fun `saving schema with a previous schema initialized fails`() {
    schemaDao.stub {
      onGeneric { findById(Stubs.schema.version) } doReturn Optional.of(Stubs.schemaDbm)
    }

    assertFailure { repository.save(Stubs.schema) }
      .all {
        hasClass(IllegalArgumentException::class)
        messageContains("already initialized")
      }
  }

  @Test fun `saving schema with a previous schema not initialized works`() {
    schemaDao.stub {
      val schema = SchemaDbm(Stubs.schema.version, isInitialized = false)
      onGeneric { findById(Stubs.schema.version) } doReturn Optional.of(schema)
    }

    assertThat(repository.save(Stubs.schema))
      .isEqualTo(Unit)
  }

  @Test fun `saving schema with no previous schema works`() {
    schemaDao.stub {
      onGeneric { findById(Stubs.schema.version) } doReturn Optional.empty()
    }

    assertThat(repository.save(Stubs.schema))
      .isEqualTo(Unit)
  }

  // endregion

  // region Is initialized checks

  @Test fun `fetching 'is initialized' returns false if DAO throws`() {
    schemaDao.stub {
      onGeneric { findById(Stubs.schema.version) } doThrow IllegalStateException("failed")
    }

    assertThat(repository.isInitialized(Stubs.schema.version))
      .isFalse()
  }

  @Test fun `fetching 'is initialized' returns false if schema is missing`() {
    schemaDao.stub {
      onGeneric { findById(Stubs.schema.version) } doReturn Optional.empty()
    }

    assertThat(repository.isInitialized(Stubs.schema.version))
      .isFalse()
  }

  @Test fun `fetching 'is initialized' returns false for non-initialized schemas`() {
    schemaDao.stub {
      val schema = SchemaDbm(Stubs.schema.version, isInitialized = false)
      onGeneric { findById(Stubs.schema.version) } doReturn Optional.of(schema)
    }

    assertThat(repository.isInitialized(Stubs.schema.version))
      .isFalse()
  }

  @Test fun `fetching 'is initialized' returns true for initialized schemas`() {
    schemaDao.stub {
      onGeneric { findById(Stubs.schema.version) } doReturn Optional.of(Stubs.schemaDbm)
    }

    assertThat(repository.isInitialized(Stubs.schema.version))
      .isTrue()
  }

  // endregion

}
