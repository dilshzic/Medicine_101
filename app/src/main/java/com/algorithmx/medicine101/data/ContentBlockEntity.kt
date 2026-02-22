package com.algorithmx.medicine101.data

import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey

@Entity(
    tableName = "content_blocks",
    foreignKeys = [
        ForeignKey(
            entity = NoteEntity::class,
            parentColumns = ["id"],
            childColumns = ["noteId"],
            onDelete = ForeignKey.CASCADE
        )
    ],
    indices = [Index("noteId")]
)
data class ContentBlockEntity(
    @PrimaryKey(autoGenerate = true) val blockId: Long = 0,
    val noteId: String,
    val type: String, // "header", "callout", "table", etc.
    val content: String, // The actual text or serialized sub-data
    val orderIndex: Int,
    val tabName: String = "General"// Crucial for maintaining the sequence of paragraphs
)