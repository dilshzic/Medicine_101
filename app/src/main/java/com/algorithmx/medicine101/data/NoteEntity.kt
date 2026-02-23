package com.algorithmx.medicine101.data

import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey
import java.util.UUID

@Entity(
    tableName = "notes",
    foreignKeys = [
        ForeignKey(
            entity = NoteEntity::class,
            parentColumns = ["id"],
            childColumns = ["parentId"],
            onDelete = ForeignKey.CASCADE
        )
    ],
    indices = [Index(value = ["parentId"])]
)
data class NoteEntity(
    @PrimaryKey val id: String = UUID.randomUUID().toString(),

    val title: String,
    val category: String,
    val tags: String = "",
    val type: String = "note",

    val isFolder: Boolean = false,
    val parentId: String? = null,
    val sortOrder: Int = 0,

    val isSystemNote: Boolean = false,
    val lastModified: Long = System.currentTimeMillis(),
    val isPinned: Boolean = false, // Add this field

    // SYNC METADATA
    val createdAt: Long = System.currentTimeMillis(),
    val updatedAt: Long = System.currentTimeMillis(),
    val isDeleted: Boolean = false,

    val pdfUri: String? = null,
    val pdfPage: Int? = null
)