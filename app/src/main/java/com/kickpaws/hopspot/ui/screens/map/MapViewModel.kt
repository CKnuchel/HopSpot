package com.kickpaws.hopspot.ui.screens.map

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.kickpaws.hopspot.domain.model.Bench
import com.kickpaws.hopspot.domain.repository.BenchFilter
import com.kickpaws.hopspot.domain.repository.BenchRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

data class MapUiState(
    val benches: List<Bench> = emptyList(),
    val isLoading: Boolean = false,
    val errorMessage: String? = null,
    val selectedBench: Bench? = null
)

@HiltViewModel
class MapViewModel @Inject constructor(
    private val benchRepository: BenchRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow(MapUiState())
    val uiState: StateFlow<MapUiState> = _uiState.asStateFlow()

    init {
        loadAllBenches()
    }

    fun loadAllBenches() {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true, errorMessage = null) }

            // Lade alle Bänke (100 = max)
            val filter = BenchFilter(page = 1, limit = 100)
            val result = benchRepository.getBenches(filter)

            result.fold(
                onSuccess = { paginated ->
                    _uiState.update {
                        it.copy(
                            isLoading = false,
                            benches = paginated.benches
                        )
                    }
                },
                onFailure = { exception ->
                    _uiState.update {
                        it.copy(
                            isLoading = false,
                            errorMessage = exception.message ?: "Fehler beim Laden"
                        )
                    }
                }
            )
        }
    }

    fun selectBench(bench: Bench?) {
        _uiState.update { it.copy(selectedBench = bench) }
    }

    fun clearSelection() {
        _uiState.update { it.copy(selectedBench = null) }
    }
}