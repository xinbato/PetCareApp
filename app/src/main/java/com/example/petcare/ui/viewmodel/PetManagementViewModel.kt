package com.example.petcare.ui.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.petcare.data.model.PetModel
import com.example.petcare.data.repository.PetRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import java.util.UUID

data class PetManagementUiState(
    val pets: List<PetModel> = emptyList(),
    val selectedPetId: String = "1",
    val activePet: PetModel? = null,
    val isLoading: Boolean = false,
    val isActionSuccess: Boolean = false,
    val errorMessage: String? = null,
) {
    val selectedPet: PetModel?
        get() = pets.find { it.id == selectedPetId } ?: pets.firstOrNull()
}

class PetManagementViewModel(
    private val repository: PetRepository = PetRepository,
) : ViewModel() {

    private val _activePetId = MutableStateFlow<String?>(null)
    private val _isActionSuccess = MutableStateFlow(false)
    private val _errorMessage = MutableStateFlow<String?>(null)

    val uiState: StateFlow<PetManagementUiState> = combine(
        repository.pets,
        repository.currentSelectedPetId,
        _activePetId,
        _isActionSuccess,
        _errorMessage,
    ) { pets, selectedId, activeId, isSuccess, error ->
        val activePet = if (activeId != null) {
            pets.find { it.id == activeId }
        } else {
            pets.find { it.id == selectedId } ?: pets.firstOrNull()
        }
        PetManagementUiState(
            pets = pets,
            selectedPetId = selectedId,
            activePet = activePet,
            isLoading = false,
            isActionSuccess = isSuccess,
            errorMessage = error,
        )
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5_000),
        initialValue = PetManagementUiState(
            pets = repository.pets.value,
            selectedPetId = repository.currentSelectedPetId.value,
            activePet = repository.getSelectedPet(),
        ),
    )

    fun setActivePet(petId: String) {
        _activePetId.value = petId
    }

    fun selectPet(petId: String) {
        repository.selectPet(petId)
    }

    fun savePet(pet: PetModel) {
        viewModelScope.launch {
            try {
                repository.savePet(pet)
                _isActionSuccess.value = true
            } catch (e: Exception) {
                _errorMessage.value = e.message ?: "Lỗi khi lưu thông tin thú cưng"
            }
        }
    }

    fun createAndSavePet(
        name: String,
        species: String = "Chó",
        breed: String = "",
        dateOfBirth: String = "01/01/2024",
        age: String = "1 tuổi",
        weight: String = "5.0 kg",
        gender: String = "Đực",
        iconRes: Int,
        statusText: String = "Khỏe mạnh",
    ): PetModel {
        val newPet = PetModel(
            id = UUID.randomUUID().toString(),
            name = name.trim(),
            species = species.trim(),
            breed = breed.trim().ifEmpty { "Chưa rõ giống" },
            dateOfBirth = dateOfBirth.trim(),
            age = age,
            weight = weight.trim().ifEmpty { "5.0 kg" },
            gender = gender,
            iconRes = iconRes,
            statusText = statusText,
            isFavorite = false,
        )
        savePet(newPet)
        return newPet
    }

    fun deletePet(petId: String) {
        viewModelScope.launch {
            try {
                repository.deletePet(petId)
                _isActionSuccess.value = true
            } catch (e: Exception) {
                _errorMessage.value = e.message ?: "Lỗi khi xóa thú cưng"
            }
        }
    }

    fun toggleFavorite(petId: String) {
        repository.toggleFavorite(petId)
    }

    fun getPetById(id: String): PetModel? = repository.getPetById(id)

    fun getSelectedPet(): PetModel = repository.getSelectedPet()

    fun clearActionState() {
        _isActionSuccess.value = false
        _errorMessage.value = null
    }
}
