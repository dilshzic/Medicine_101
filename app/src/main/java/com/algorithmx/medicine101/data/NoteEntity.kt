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
    // PRIMARY KEY IS A STRING UUID (Firebase Ready)
    @PrimaryKey val id: String = UUID.randomUUID().toString(),

    val title: String,
    val category: String,
    val tags: String = "",
    val type: String = "note",


    // HIERARCHY
    val isFolder: Boolean = false,
    val parentId: String? = null,
    val sortOrder: Int = 0, // Points to the UUID of the parent

    // ADD THESE TWO FIELDS
    val isSystemNote: Boolean = false,
    val lastModified: Long = System.currentTimeMillis(),

    // SYNC METADATA
    val createdAt: Long = System.currentTimeMillis(),
    val updatedAt: Long = System.currentTimeMillis(),
    val isDeleted: Boolean = false ,// Soft Delete (Crucial for Sync)

    // --- NEW PDF FIELDS ---
    val pdfUri: String? = null, // Path to the copied PDF in internal storage
    val pdfPage: Int? = null    // The page this note should jump to

)