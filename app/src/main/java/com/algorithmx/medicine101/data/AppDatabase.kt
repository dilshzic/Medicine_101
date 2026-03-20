package com.algorithmx.medicine101.data

import androidx.room.Database
import androidx.room.RoomDatabase
import androidx.room.TypeConverters
import androidx.room.migration.Migration
import androidx.sqlite.db.SupportSQLiteDatabase

@Database(entities = [NoteEntity::class, ContentBlockEntity::class], version = 3, exportSchema = false)
@TypeConverters(Converters::class)
abstract class AppDatabase : RoomDatabase() {
    abstract fun noteDao(): NoteDao

    companion object {
        val MIGRATION_1_2 = object : Migration(1, 2) {
            override fun migrate(db: SupportSQLiteDatabase) {
                // Add isPinned column to notes table
                db.execSQL("ALTER TABLE notes ADD COLUMN isPinned INTEGER NOT NULL DEFAULT 0")
            }
        }

        val MIGRATION_2_3 = object : Migration(2, 3) {
            override fun migrate(db: SupportSQLiteDatabase) {
                // Add indexes for frequently filtered columns
                db.execSQL("CREATE INDEX IF NOT EXISTS index_notes_isDeleted ON notes (isDeleted)")
                db.execSQL("CREATE INDEX IF NOT EXISTS index_notes_isPinned ON notes (isPinned)")
                db.execSQL("CREATE INDEX IF NOT EXISTS index_notes_isFolder ON notes (isFolder)")
                db.execSQL("CREATE INDEX IF NOT EXISTS index_notes_parentId_isDeleted_isFolder ON notes (parentId, isDeleted, isFolder)")
                // Composite index for ordered block retrieval
                db.execSQL("CREATE INDEX IF NOT EXISTS index_content_blocks_noteId_orderIndex ON content_blocks (noteId, orderIndex)")
            }
        }
    }
}