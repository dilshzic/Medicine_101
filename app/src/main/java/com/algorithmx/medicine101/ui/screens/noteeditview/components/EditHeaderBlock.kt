package com.algorithmx.medicine101.ui.screens.noteeditview.components

import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import com.algorithmx.medicine101.data.ContentBlock


@Composable
fun EditHeaderBlock(
    block: ContentBlock,
    onUpdate: (ContentBlock) -> Unit
) {
    OutlinedTextField(
        value = block.text ?: "",
        onValueChange = { onUpdate(block.copy(text = it)) },
        label = { Text("Header Title") },
        modifier = Modifier.fillMaxWidth()
    )
    // You can add a Dropdown here later to change Header Level (H1/H2)
}

@Composable
fun EditTextBlock(
    block: ContentBlock,
    label: String = "Content",
    onUpdate: (ContentBlock) -> Unit
) {
    OutlinedTextField(
        value = block.text ?: "",
        onValueChange = { onUpdate(block.copy(text = it)) },
        label = { Text(label) },
        modifier = Modifier.fillMaxWidth(),
        minLines = 3
    )
}