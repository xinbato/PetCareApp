package com.example.petcare.ui.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.petcare.data.model.PetModel
import com.example.petcare.data.repository.AccountUser
import com.example.petcare.data.repository.PetRepository
import com.example.petcare.data.repository.UserProfileData
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

data class ProfileUiState(
    val userProfile: UserProfileData = UserProfileData(),
    val accounts: List<AccountUser> = emptyList(),
    val myPets: List<PetModel> = emptyList(),
    val isLoading: Boolean = false,
) {
    val petCount: Int
        get() = myPets.size
}

class ProfileViewModel(
    private val repository: PetRepository = PetRepository,
) : ViewModel() {

    val uiState: StateFlow<ProfileUiState> = combine(
        repository.userProfile,
        repository.accounts,
        repository.pets,
        repository.isLoading,
    ) { userProfile, accounts, pets, loading ->
        ProfileUiState(
            userProfile = userProfile,
            accounts = accounts,
            myPets = pets,
            isLoading = loading,
        )
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5_000),
        initialValue = ProfileUiState(
            userProfile = repository.userProfile.value,
            accounts = repository.accounts.value,
            myPets = repository.pets.value,
        ),
    )

    fun switchAccount(accountId: String) {
        viewModelScope.launch {
            repository.switchAccount(accountId)
        }
    }

    fun addAccount(name: String) {
        viewModelScope.launch {
            repository.addAccount(name)
        }
    }

    fun updateProfile(
        name: String? = null,
        email: String? = null,
        phone: String? = null,
        dob: String? = null,
        address: String? = null,
        avatarUri: String? = null,
        avatarRes: Int? = null,
        headerBgColorHex: Long? = null,
        headerBgUri: String? = null,
        clearAvatar: Boolean = false,
        clearHeaderBgUri: Boolean = false,
    ) {
        viewModelScope.launch {
            repository.updateUserProfile(
                name = name,
                email = email,
                phone = phone,
                dob = dob,
                address = address,
                avatarUri = avatarUri,
                avatarRes = avatarRes,
                headerBgColorHex = headerBgColorHex,
                headerBgUri = headerBgUri,
                clearAvatar = clearAvatar,
                clearHeaderBgUri = clearHeaderBgUri,
            )
        }
    }

    fun updateUserProfile(
        name: String? = null,
        email: String? = null,
        phone: String? = null,
        dob: String? = null,
        address: String? = null,
        avatarUri: String? = null,
        avatarRes: Int? = null,
        headerBgColorHex: Long? = null,
        headerBgUri: String? = null,
        clearAvatar: Boolean = false,
        clearHeaderBgUri: Boolean = false,
    ) = updateProfile(
        name = name,
        email = email,
        phone = phone,
        dob = dob,
        address = address,
        avatarUri = avatarUri,
        avatarRes = avatarRes,
        headerBgColorHex = headerBgColorHex,
        headerBgUri = headerBgUri,
        clearAvatar = clearAvatar,
        clearHeaderBgUri = clearHeaderBgUri,
    )
}
