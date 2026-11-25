package com.example.petcare.ui.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.petcare.data.model.PetPlaceModel
import com.example.petcare.data.repository.PetRepository
import com.example.petcare.data.repository.PlacesRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

data class MapUiState(
    val places: List<PetPlaceModel> = emptyList(),
    val isLoading: Boolean = false,
    val selectedRadiusMeters: Int = 5000,
    val locationLabel: String = "Khu vực mặc định",
    val activeLat: Double = 10.7769,
    val activeLon: Double = 106.7009,
    val errorMessage: String? = null,
)

class MapViewModel(
    private val repository: PetRepository = PetRepository,
    private val placesRepository: PlacesRepository = PlacesRepository(),
) : ViewModel() {

    private val fetchedPlaces = MutableStateFlow<List<PetPlaceModel>>(emptyList())
    private val savedPlaceIds = MutableStateFlow<Set<String>>(emptySet())
    private val isLoading = MutableStateFlow(false)
    private val selectedRadiusMeters = MutableStateFlow(5000)
    private val locationLabel = MutableStateFlow("Khu vực mặc định")
    private val activeCoords = MutableStateFlow(10.7769 to 106.7009)
    private val errorMessage = MutableStateFlow<String?>(null)

    private data class QueryMeta(
        val loading: Boolean,
        val radius: Int,
        val label: String,
        val coords: Pair<Double, Double>,
        val error: String?,
    )

    private val queryMeta = combine(
        isLoading,
        selectedRadiusMeters,
        locationLabel,
        activeCoords,
        errorMessage,
    ) { loading, radius, label, coords, error ->
        QueryMeta(loading, radius, label, coords, error)
    }

    val uiState: StateFlow<MapUiState> = combine(
        fetchedPlaces,
        repository.petPlaces,
        savedPlaceIds,
        queryMeta,
    ) { live, repoPlaces, savedIds, meta ->
        val repoSavedMap = repoPlaces.associateBy({ it.id }, { it.isSaved })
        val basePlaces = if (live.isNotEmpty()) live else repoPlaces
        val merged = basePlaces.map { place ->
            val isSaved = place.id in savedIds || (repoSavedMap[place.id] ?: place.isSaved)
            if (isSaved == place.isSaved) place else place.copy(isSaved = isSaved)
        }
        MapUiState(
            places = merged,
            isLoading = meta.loading,
            selectedRadiusMeters = meta.radius,
            locationLabel = meta.label,
            activeLat = meta.coords.first,
            activeLon = meta.coords.second,
            errorMessage = meta.error,
        )
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = MapUiState(places = repository.petPlaces.value),
    )

    init {
        loadNearbyFromCoordinates(
            lat = 10.7769,
            lon = 106.7009,
            label = "TP. HCM (mặc định)",
            radiusMeters = 5000,
            silentOnFailure = true,
        )
    }

    fun loadNearbyFromCoordinates(
        lat: Double,
        lon: Double,
        label: String = "Vị trí hiện tại",
        radiusMeters: Int = selectedRadiusMeters.value,
        silentOnFailure: Boolean = false,
    ) {
        activeCoords.value = lat to lon
        selectedRadiusMeters.value = radiusMeters
        locationLabel.value = label
        errorMessage.value = null
        viewModelScope.launch {
            isLoading.value = true
            val result = placesRepository.searchNearbyPetFacilities(
                lat = lat,
                lon = lon,
                radiusMeters = radiusMeters,
            )
            result.onSuccess { places ->
                if (places.isNotEmpty()) {
                    fetchedPlaces.value = places
                } else if (!silentOnFailure) {
                    errorMessage.value = "Không tìm thấy cơ sở thú y trong bán kính ${radiusMeters / 1000} km."
                }
            }.onFailure { err ->
                if (!silentOnFailure) {
                    errorMessage.value = err.message ?: "Không tải được dữ liệu bản đồ."
                }
            }
            isLoading.value = false
        }
    }

    fun searchByAreaName(areaName: String) {
        val clean = areaName.trim()
        if (clean.isBlank()) {
            errorMessage.value = "Vui lòng nhập tên quận, thành phố hoặc khu vực."
            return
        }
        viewModelScope.launch {
            isLoading.value = true
            errorMessage.value = null
            val geoResult = placesRepository.geocodeArea(clean)
            geoResult.onSuccess { (lat, lon) ->
                activeCoords.value = lat to lon
                locationLabel.value = clean
                val nearby = placesRepository.searchNearbyPetFacilities(
                    lat = lat,
                    lon = lon,
                    radiusMeters = selectedRadiusMeters.value,
                )
                nearby.onSuccess { places ->
                    if (places.isNotEmpty()) {
                        fetchedPlaces.value = places
                    } else {
                        errorMessage.value = "Không tìm thấy cơ sở thú y quanh \"$clean\"."
                    }
                }.onFailure { err ->
                    errorMessage.value = err.message ?: "Không tải được dữ liệu cơ sở thú y."
                }
            }.onFailure { err ->
                errorMessage.value = err.message ?: "Không định vị được khu vực \"$clean\"."
            }
            isLoading.value = false
        }
    }

    fun changeRadius(radiusMeters: Int) {
        if (selectedRadiusMeters.value == radiusMeters && fetchedPlaces.value.isNotEmpty()) return
        val (lat, lon) = activeCoords.value
        loadNearbyFromCoordinates(
            lat = lat,
            lon = lon,
            label = locationLabel.value,
            radiusMeters = radiusMeters,
        )
    }

    fun toggleSavePlace(placeId: String) {
        val current = savedPlaceIds.value
        val alreadySaved = placeId in current ||
            uiState.value.places.firstOrNull { it.id == placeId }?.isSaved == true
        savedPlaceIds.value = if (alreadySaved) current - placeId else current + placeId
        repository.toggleSavePlace(placeId)
    }

    fun reportLocationError(message: String) {
        errorMessage.value = message
    }

    fun clearError() {
        errorMessage.value = null
    }
}
