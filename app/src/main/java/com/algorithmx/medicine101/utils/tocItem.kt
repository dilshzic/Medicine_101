package com.algorithmx.medicine101.utils

import kotlinx.serialization.Serializable

@Serializable
data class TocItem (
    val id: String,
    val title: String,
    val icon: String,
    val file: String
)