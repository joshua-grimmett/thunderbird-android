package com.fsck.k9.storage.migrations

import android.database.sqlite.SQLiteDatabase
import assertk.assertThat
import assertk.assertions.isEqualTo
import assertk.assertions.isTrue
import kotlin.test.Test
import org.junit.After
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner

@RunWith(RobolectricTestRunner::class)
class MigrationTo92Test {
    private val database = createDatabaseVersion91()
    private val migration = MigrationTo92(database)

    @After
    fun tearDown() {
        database.close()
    }

    @Test
    fun `should add four header columns to messages table`() {
        migration.addSmartInboxHeaderColumns()

        assertThat(database.columnExists("messages", "list_unsubscribe")).isTrue()
        assertThat(database.columnExists("messages", "list_id")).isTrue()
        assertThat(database.columnExists("messages", "precedence")).isTrue()
        assertThat(database.columnExists("messages", "auto_submitted")).isTrue()
    }

    @Test
    fun `should leave existing rows with NULL values in new columns`() {
        database.execSQL("INSERT INTO messages (subject) VALUES ('hello')")

        migration.addSmartInboxHeaderColumns()

        database.rawQuery(
            "SELECT list_unsubscribe, list_id, precedence, auto_submitted FROM messages",
            null,
        ).use { cursor ->
            assertThat(cursor.moveToFirst()).isTrue()
            assertThat(cursor.isNull(0)).isTrue()
            assertThat(cursor.isNull(1)).isTrue()
            assertThat(cursor.isNull(2)).isTrue()
            assertThat(cursor.isNull(3)).isTrue()
        }
    }

    @Test
    fun `should be idempotent when columns already exist`() {
        migration.addSmartInboxHeaderColumns()

        migration.addSmartInboxHeaderColumns()

        val columnCount = database.rawQuery("PRAGMA table_info(messages)", null).use { cursor ->
            var matches = 0
            val nameIndex = cursor.getColumnIndexOrThrow("name")
            while (cursor.moveToNext()) {
                val name = cursor.getString(nameIndex)
                if (name in setOf("list_unsubscribe", "list_id", "precedence", "auto_submitted")) {
                    matches++
                }
            }
            matches
        }
        assertThat(columnCount).isEqualTo(4)
    }

    private fun SQLiteDatabase.columnExists(table: String, column: String): Boolean {
        rawQuery("PRAGMA table_info($table)", null).use { cursor ->
            val nameIndex = cursor.getColumnIndexOrThrow("name")
            while (cursor.moveToNext()) {
                if (cursor.getString(nameIndex) == column) return true
            }
        }
        return false
    }

    @Suppress("LongMethod")
    private fun createDatabaseVersion91(): SQLiteDatabase {
        return SQLiteDatabase.create(null).apply {
            execSQL(
                """
                CREATE TABLE messages (
                    id INTEGER PRIMARY KEY,
                    deleted INTEGER default 0,
                    folder_id INTEGER,
                    uid TEXT,
                    subject TEXT,
                    date INTEGER,
                    flags TEXT,
                    sender_list TEXT,
                    to_list TEXT,
                    cc_list TEXT,
                    bcc_list TEXT,
                    reply_to_list TEXT,
                    attachment_count INTEGER,
                    internal_date INTEGER,
                    message_id TEXT,
                    preview_type TEXT default "none",
                    preview TEXT,
                    mime_type TEXT,
                    normalized_subject_hash INTEGER,
                    empty INTEGER default 0,
                    read INTEGER default 0,
                    flagged INTEGER default 0,
                    answered INTEGER default 0,
                    forwarded INTEGER default 0,
                    message_part_id INTEGER,
                    encryption_type TEXT,
                    new_message INTEGER default 0,
                    account_id TEXT
                )
                """.trimIndent(),
            )
        }
    }
}
