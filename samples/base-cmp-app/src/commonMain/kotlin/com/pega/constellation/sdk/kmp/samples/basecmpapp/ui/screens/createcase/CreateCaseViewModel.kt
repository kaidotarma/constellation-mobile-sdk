package com.pega.constellation.sdk.kmp.samples.basecmpapp.ui.screens.createcase

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import androidx.lifecycle.viewmodel.initializer
import androidx.lifecycle.viewmodel.viewModelFactory
import com.pega.constellation.sdk.kmp.core.Log
import com.pega.constellation.sdk.kmp.samples.basecmpapp.Injector
import com.pega.constellation.sdk.kmp.samples.basecmpapp.SDKConfig
import com.pega.constellation.sdk.kmp.samples.basecmpapp.data.CaseType
import com.pega.constellation.sdk.kmp.samples.basecmpapp.data.CaseTypesRepository
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

data class CreateCaseUiState(
    val pickerVisible: Boolean = false,
    val loading: Boolean = false,
    val caseTypes: List<CaseType> = emptyList(),
    val query: String = "",
    val selectedCaseType: CaseType? = null,
    val errorMessage: String? = null,
)

class CreateCaseViewModel(
    private val caseTypesRepository: CaseTypesRepository,
) : ViewModel() {
    private val _uiState = MutableStateFlow(CreateCaseUiState())
    val uiState: StateFlow<CreateCaseUiState> = _uiState.asStateFlow()

    private var catalogLoaded = false
    private var loadJob: Job? = null

    fun reset() {
        loadJob?.cancel()
        loadJob = null
        catalogLoaded = false
        _uiState.value = CreateCaseUiState()
        Log.i(TAG, "Case type picker state reset.")
    }

    fun openPicker() {
        _uiState.update {
            it.copy(
                pickerVisible = true,
                query = "",
                selectedCaseType = null,
                errorMessage = null,
            )
        }
        Log.i(TAG, "Case type picker opened: cached=${catalogLoaded}.")
        if (!catalogLoaded && !_uiState.value.loading) {
            loadCaseTypes()
        }
    }

    fun closePicker() {
        _uiState.update {
            it.copy(
                pickerVisible = false,
                query = "",
                selectedCaseType = null,
                errorMessage = null,
            )
        }
        Log.i(TAG, "Case type picker closed.")
    }

    fun retry() {
        if (!_uiState.value.loading) {
            Log.i(TAG, "Retrying case type catalog request.")
            loadCaseTypes()
        }
    }

    fun updateQuery(query: String) {
        _uiState.update { it.copy(query = query) }
        Log.i(TAG, "Case type picker query changed: length=${query.length}.")
    }

    fun selectCaseType(caseType: CaseType) {
        _uiState.update { it.copy(selectedCaseType = caseType) }
        Log.i(TAG, "Case type selected: ${caseType.className}.")
    }

    private fun loadCaseTypes() {
        _uiState.update { it.copy(loading = true, errorMessage = null) }
        loadJob = viewModelScope.launch {
            try {
                val caseTypes = caseTypesRepository.fetchCaseTypes()
                catalogLoaded = true
                _uiState.update {
                    it.copy(
                        loading = false,
                        caseTypes = caseTypes,
                        errorMessage = null,
                    )
                }
                Log.i(TAG, "Case type picker catalog ready: count=${caseTypes.size}.")
            } catch (cancellation: CancellationException) {
                throw cancellation
            } catch (error: Exception) {
                _uiState.update {
                    it.copy(
                        loading = false,
                        errorMessage = error.message ?: "Unable to load case types.",
                    )
                }
                Log.e(TAG, "Case type picker catalog failed: ${error.message}.", error)
            }
        }.also { job ->
            job.invokeOnCompletion {
                if (loadJob === job) {
                    loadJob = null
                }
            }
        }
    }

    companion object {
        val Factory = viewModelFactory {
            initializer {
                CreateCaseViewModel(
                    CaseTypesRepository(
                        httpClient = Injector.authManager.httpClient,
                        pegaUrl = SDKConfig.PEGA_URL,
                    )
                )
            }
        }

        private const val TAG = "CreateCaseViewModel"
    }
}
