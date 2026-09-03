package com.pega.constellation.sdk.kmp.samples.basecmpapp.ui.screens.services

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import androidx.lifecycle.viewmodel.initializer
import androidx.lifecycle.viewmodel.viewModelFactory
import com.pega.constellation.sdk.kmp.samples.basecmpapp.Injector
import com.pega.constellation.sdk.kmp.samples.basecmpapp.data.Assignment
import com.pega.constellation.sdk.kmp.samples.basecmpapp.data.AssignmentsRepository
import kotlinx.coroutines.FlowPreview
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.debounce
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

class ServicesViewModel(
    val assignmentsRepository: AssignmentsRepository
) : ViewModel() {
    private val _uiState = MutableStateFlow<UiState>(UiState.Loading)
    private var loadJob: Job? = null

    @OptIn(FlowPreview::class)
    val uiState: StateFlow<UiState> = _uiState
        .debounce(500)
        .stateIn(viewModelScope, SharingStarted.Lazily, UiState.Loading)

    sealed class UiState {
        object Loading : UiState()
        data class Success(val assignments: List<Assignment>) : UiState()
        data class Error(val message: String) : UiState()
    }

    fun loadAssignments() {
        loadJob?.cancel()
        loadJob = viewModelScope.launch {
            try {
                _uiState.value = UiState.Loading
                val assignments = assignmentsRepository.fetchAssignments()
                _uiState.value = UiState.Success(assignments)
            } catch (cancellation: CancellationException) {
                throw cancellation
            } catch (error: Exception) {
                _uiState.value = UiState.Error("Failed to load assignments: ${error.message}")
            }
        }.also { job ->
            job.invokeOnCompletion {
                if (loadJob === job) {
                    loadJob = null
                }
            }
        }
    }

    fun reset() {
        loadJob?.cancel()
        loadJob = null
        _uiState.value = UiState.Loading
    }

    companion object {
        val Factory = viewModelFactory {
            initializer {
                ServicesViewModel(
                    AssignmentsRepository(Injector.authManager.httpClient)
                )
            }
        }
    }
}
