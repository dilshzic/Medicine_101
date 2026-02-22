package com.algorithmx.medicine101

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.algorithmx.medicine101.data.DatabaseSeeder
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class MainViewModel @Inject constructor(
    private val seeder: DatabaseSeeder
) : ViewModel() {

    // Removed the init {} block and made it an explicit function
    fun triggerSeeding() {
        viewModelScope.launch {
            seeder.seedDatabase()
        }
    }
}