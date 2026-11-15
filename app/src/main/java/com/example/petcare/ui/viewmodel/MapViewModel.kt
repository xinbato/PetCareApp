package com.example.petcare.ui.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.petcare.data.model.PetPlaceModel
import com.example.petcare.data.repository.PetRepository
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

data class MapUiState(
    val places: List<PetPlaceModel> = emptyList(),
    val isLoading: Boolean = false,
)

class MapViewModel(
    private val repository: PetRepository = PetRepository,
) : ViewModel() {

    val uiState: StateFlow<MapUiState> = repository.petPlaces
        .map { places -> MapUiState(places = places, isLoading = false) }
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5_000),
            initialValue = MapUiState(places = repository.petPlaces.value),
        )

    fun toggleSavePlace(placeId: String) {
        viewModelScope.launch {
            repository.toggleSavePlace(placeId)
        }
    }
}
