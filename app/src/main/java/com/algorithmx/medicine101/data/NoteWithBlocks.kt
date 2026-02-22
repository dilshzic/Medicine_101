package com.algorithmx.medicine101.data

import androidx.room.Embedded
import androidx.room.Relation

data class NoteWithBlocks(
    @Embedded val note: NoteEntity,
    @Relation(
        parentColumn = "id",
        entityColumn = "noteId"
    )
    val blocks: List<ContentBlockEntity>
)