package com.algorithmx.medicine101.ui.screens.brain

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.algorithmx.medicine101.brain.BrainCategory
import com.algorithmx.medicine101.brain.BrainDataStoreManager
import com.algorithmx.medicine101.brain.BrainProvider
import com.algorithmx.medicine101.brain.BrainRegistry
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

data class BrainUiState(
    val selectedCategory: BrainCategory = BrainCategory.TEXT_TO_TEXT,
    val selectedProvider: BrainProvider = BrainProvider.GEMINI,
    val selectedModel: String = "gemini-1.5-flash",
    val availableModels: List<String> = emptyList(),
    val systemInstruction: String = "You are a helpful medical assistant.",
    val totalRequests: Int = 0,
    val totalTokensUsed: Int = 0
)

@HiltViewModel
class BrainManagerViewModel @Inject constructor(
    private val dataStoreManager: BrainDataStoreManager
) : ViewModel() {

    private val _uiState = MutableStateFlow(BrainUiState())
    val uiState: StateFlow<BrainUiState> = _uiState.asStateFlow()

    init {
        viewModelScope.launch {
            dataStoreManager.brainConfigFlow.collect { config ->
                _uiState.update {
                    it.copy(
                        selectedCategory = config.category,
                        selectedProvider = config.provider,
                        selectedModel = config.modelName,
                        systemInstruction = config.systemInstruction,
                        totalRequests = config.totalRequests,
                        totalTokensUsed = config.totalTokens
                    )
                }
                updateAvailableModels(config.category)
            }
        }
    }

    fun updateCategory(category: BrainCategory) {
        _uiState.update { it.copy(selectedCategory = category) }
        updateAvailableModels(category)
    }

    private fun updateAvailableModels(category: BrainCategory) {
        val models = BrainRegistry.categoryMap[category] ?: emptyList()
        _uiState.update { 
            it.copy(
                availableModels = models,
                selectedModel = if (models.contains(it.selectedModel)) it.selectedModel else models.firstOrNull() ?: ""
            ) 
        }
    }

    fun updateModel(model: String) {
        val provider = BrainRegistry.getProviderForModel(model)
        _uiState.update { it.copy(selectedModel = model, selectedProvider = provider) }
    }

    fun updateSystemInstruction(instruction: String) {
        _uiState.update { it.copy(systemInstruction = instruction) }
    }

    fun saveConfiguration(onComplete: () -> Unit) {
        viewModelScope.launch {
            val state = _uiState.value
            dataStoreManager.saveEngineConfiguration(
                provider = state.selectedProvider,
                modelName = state.selectedModel,
                category = state.selectedCategory,
                systemInstruction = state.systemInstruction
            )
            onComplete()
        }
    }
}
