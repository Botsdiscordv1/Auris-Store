package com.auris.store.oficial.ui

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.auris.store.oficial.data.ApkFile
import com.auris.store.oficial.data.ApkRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

sealed interface UiState {
    object Loading : UiState
    data class Success(val apks: List<ApkFile>) : UiState
    data class Error(val message: String) : UiState
}

class ApkListViewModel(
    private val repository: ApkRepository = ApkRepository()
) : ViewModel() {

    private val _uiState = MutableStateFlow<UiState>(UiState.Loading)
    val uiState: StateFlow<UiState> = _uiState.asStateFlow()

    init {
        fetchApks()
    }

    fun fetchApks() {
        viewModelScope.launch {
            _uiState.value = UiState.Loading
            try {
                val apks = repository.getApkFiles()
                _uiState.value = UiState.Success(apks)
            } catch (e: Exception) {
                _uiState.value = UiState.Error(e.localizedMessage ?: "Error desconocido")
            }
        }
    }
}
