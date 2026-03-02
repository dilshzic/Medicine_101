package com.algorithmx.medicine101.data.remote

import com.google.firebase.firestore.PropertyName

// Mirrors NoteEntity but adds userId for security rules
data class CloudNote(
    val id: String = "",
    val userId: String = "", 
    val title: String = "",
    val category: String = "",
    val tags: String = "",
    val type: String = "note",
    
    @get:PropertyName("isFolder")
    @set:PropertyName("isFolder")
    var isFolder: Boolean = false,
    
    val parentId: String? = null,
    val sortOrder: Int = 0,
    
    @get:PropertyName("isSystemNote")
    @set:PropertyName("isSystemNote")
    var isSystemNote: Boolean = false,
    
    val lastModified: Long = 0,
    
    @get:PropertyName("isPinned")
    @set:PropertyName("isPinned")
    var isPinned: Boolean = false,
    
    val createdAt: Long = 0,
    val updatedAt: Long = 0,
    
    @get:PropertyName("isDeleted")
    @set:PropertyName("isDeleted")
    var isDeleted: Boolean = false,
    
    val pdfUri: String? = null,
    val pdfPage: Int? = null
)

// Mirrors ContentBlockEntity
data class CloudContentBlock(
    val blockId: String = "",
    val type: String = "",
    val content: String = "",
    val orderIndex: Int = 0,
    val tabName: String = "General"
)
