package com.algorithmx.medicine101.ui.screens.search

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.algorithmx.medicine101.data.NoteEntity
import com.algorithmx.medicine101.data.NoteRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.FlowPreview
import kotlinx.coroutines.flow.*
import javax.inject.Inject

data class CategorizedResults(
    val notes: List<NoteEntity> = emptyList(),
    val pdfNodes: List<NoteEntity> = emptyList(),
    val findings: List<NoteEntity> = emptyList()
)

@HiltViewModel
class SearchViewModel @Inject constructor(
    private val repository: NoteRepository
) : ViewModel() {

    private val _query = MutableStateFlow("")
    val query: StateFlow<String> = _query.asStateFlow()

    @OptIn(FlowPreview::class, ExperimentalCoroutinesApi::class)
    val categorizedResults: StateFlow<CategorizedResults> = _query
        .debounce(300)
        .distinctUntilChanged()
        .flatMapLatest { text ->
            if (text.isBlank()) {
                flowOf(CategorizedResults())
            } else {
                combine(
                    repository.searchNotes(text),
                    repository.getBooks() // Simplification: in a real app, you'd have a specific TOC search
                ) { allNotes, books ->
                    val queryLower = text.lowercase()
                    
                    val pdfs = allNotes.filter { it.pdfUri != null }
                    val notes = allNotes.filter { it.pdfUri == null && !it.isFolder }
                    
                    CategorizedResults(
                        notes = notes,
                        pdfNodes = pdfs,
                        findings = emptyList() // Future: items found in ContentBlocks
                    )
                }
            }
        }
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = CategorizedResults()
        )

    fun onQueryChange(newQuery: String) {
        _query.value = newQuery
    }
}
