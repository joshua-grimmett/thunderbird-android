package com.fsck.k9.storage.migrations

import android.database.sqlite.SQLiteDatabase

/**
 * Migration to version 92.
 *
 * Adds four nullable header columns to the `messages` table so they can be selected as part
 * of the list projection and fed into the Smart Inbox classifier without having to parse the
 * `message_parts.header` MIME blob per row.
 *
 * Columns: `list_unsubscribe`, `list_id`, `precedence`, `auto_submitted`.
 *
 * No backfill — existing rows get NULL. New messages have the headers extracted on save.
 */
internal class MigrationTo92(private val db: SQLiteDatabase) {

    fun addSmartInboxHeaderColumns() {
        for (column in COLUMNS) {
            if (!columnExists(column)) {
                db.execSQL("ALTER TABLE messages ADD $column TEXT")
            }
        }
    }

    private fun columnExists(columnName: String): Boolean {
        db.rawQuery("PRAGMA table_info(messages)", null).use { cursor ->
            val nameIndex = cursor.getColumnIndexOrThrow("name")
            while (cursor.moveToNext()) {
                if (cursor.getString(nameIndex) == columnName) return true
            }
        }
        return false
    }

    private companion object {
        val COLUMNS = listOf("list_unsubscribe", "list_id", "precedence", "auto_submitted")
    }
}
