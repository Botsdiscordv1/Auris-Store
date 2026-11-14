package com.xuper.tv.apkmanager.ui

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.xuper.tv.apkmanager.data.ApkRepository
import com.xuper.tv.apkmanager.data.GitHubFile
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

sealed interface UiState {
    object Loading : UiState
    data class Success(val apks: List<GitHubFile>) : UiState
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
