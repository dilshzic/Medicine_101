package com.algorithmx.medicine101.ui.screens.search

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.algorithmx.medicine101.data.NoteEntity
import com.algorithmx.medicine101.data.NoteRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.FlowPreview
import kotlinx.coroutines.flow.*
import javax.inject.Inject

@HiltViewModel
class SearchViewModel @Inject constructor(
    private val repository: NoteRepository
) : ViewModel() {

    private val _query = MutableStateFlow("")
    val query: StateFlow<String> = _query.asStateFlow()
//
//    @OptIn(FlowPreview::class)
//    val results: StateFlow<List<NoteEntity>> = _query
//        .debounce(300) // Wait 300ms after typing stops
//        .distinctUntilChanged()
//        .flatMapLatest { text ->
//            if (text.isBlank()) {
//                flowOf(emptyList())
//            } else {
//                repository.searchNotes(text)
//            }
//        }
//        .stateIn(
//            scope = viewModelScope,
//            started = SharingStarted.WhileSubscribed(5000),
//            initialValue = emptyList()
//        )
//
//    fun onQueryChange(newQuery: String) {
//        _query.value = newQuery
//    }
}