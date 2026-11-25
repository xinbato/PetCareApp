package com.example.petcare.ui.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.petcare.data.model.CheckupRecordModel
import com.example.petcare.data.model.MedicalRecordEntryModel
import com.example.petcare.data.model.MedicationModel
import com.example.petcare.data.model.PetModel
import com.example.petcare.data.model.VaccinationModel
import com.example.petcare.data.model.WeightRecordModel
import com.example.petcare.data.repository.PetRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn

data class HealthUiState(
    val selectedPetId: String = "1",
    val currentPet: PetModel? = null,
    val pets: List<PetModel> = emptyList(),
    val vaccinations: List<VaccinationModel> = emptyList(),
    val medications: List<MedicationModel> = emptyList(),
    val medicalRecords: List<MedicalRecordEntryModel> = emptyList(),
    val weightRecords: List<WeightRecordModel> = emptyList(),
    val checkupRecords: List<CheckupRecordModel> = emptyList(),
    val filterPetId: String = "all",
    val isLoading: Boolean = false,
) {
    val filteredVaccinations: List<VaccinationModel>
        get() = if (filterPetId == "all" || filterPetId.isBlank()) vaccinations else vaccinations.filter { it.petId == filterPetId }

    val filteredMedications: List<MedicationModel>
        get() = if (filterPetId == "all" || filterPetId.isBlank()) medications else medications.filter { it.petId == filterPetId }

    val filteredMedicalRecords: List<MedicalRecordEntryModel>
        get() = if (filterPetId == "all" || filterPetId.isBlank()) medicalRecords else medicalRecords.filter { it.petId == filterPetId }
}

class HealthViewModel(
    private val repository: PetRepository = PetRepository,
) : ViewModel() {

    private val _filterPetId = MutableStateFlow("all")

    val uiState: StateFlow<HealthUiState> = combine(
        repository.pets,
        repository.currentSelectedPetId,
        _filterPetId,
        combine(
            repository.vaccinations,
            repository.medications,
            repository.medicalRecords,
            repository.weightRecords,
            repository.checkupRecords,
        ) { vaccs, meds, medRecs, weightRecs, checkups ->
            HealthDataBundle(vaccs, meds, medRecs, weightRecs, checkups)
        },
        repository.isLoading,
    ) { pets, selectedId, filterId, bundle, loading ->
        val currentPet = pets.find { it.id == selectedId } ?: pets.firstOrNull()
        HealthUiState(
            selectedPetId = selectedId,
            currentPet = currentPet,
            pets = pets,
            vaccinations = bundle.vaccinations,
            medications = bundle.medications,
            medicalRecords = bundle.medicalRecords,
            weightRecords = bundle.weightRecords,
            checkupRecords = bundle.checkupRecords,
            filterPetId = filterId,
            isLoading = loading,
        )
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5_000),
        initialValue = HealthUiState(
            selectedPetId = repository.currentSelectedPetId.value,
            currentPet = repository.getSelectedPet(),
            pets = repository.pets.value,
            vaccinations = repository.vaccinations.value,
            medications = repository.medications.value,
            medicalRecords = repository.medicalRecords.value,
            weightRecords = repository.weightRecords.value,
            checkupRecords = repository.checkupRecords.value,
        ),
    )

    fun setFilterPetId(petId: String) {
        _filterPetId.value = petId
    }

    fun selectPet(petId: String) {
        repository.selectPet(petId)
        _filterPetId.value = petId
    }

    fun addVaccination(vaccination: VaccinationModel) {
        val resolvedName = vaccination.petName.ifBlank {
            uiState.value.pets.find { it.id == vaccination.petId }?.name.orEmpty()
        }
        val parsedMillis = if (vaccination.scheduledDateMillis > 0L) {
            vaccination.scheduledDateMillis
        } else {
            com.example.petcare.utils.DateUtils.parseDateMillis(vaccination.dateTime)
                ?: System.currentTimeMillis()
        }
        val completed = vaccination.completed ||
            vaccination.status.equals("Đã tiêm", ignoreCase = true)
        repository.addVaccination(
            vaccination.copy(
                petName = resolvedName,
                scheduledDateMillis = parsedMillis,
                completed = completed,
            )
        )
    }

    fun addVaccination(
        title: String,
        dateTime: String,
        status: String = "Đã tiêm",
        petId: String? = null,
    ) {
        val targetPetId = petId ?: uiState.value.selectedPetId
        val resolvedName = uiState.value.pets.find { it.id == targetPetId }?.name.orEmpty()
        val parsedMillis = com.example.petcare.utils.DateUtils.parseDateMillis(dateTime)
            ?: System.currentTimeMillis()
        val completed = status.equals("Đã tiêm", ignoreCase = true)
        val item = VaccinationModel(
            id = "v_${System.currentTimeMillis()}",
            petId = targetPetId,
            petName = resolvedName,
            title = title.trim(),
            dateTime = dateTime.trim(),
            status = status.trim(),
            scheduledDateMillis = parsedMillis,
            completed = completed,
            reminderEnabled = !completed,
        )
        repository.addVaccination(item)
    }

    fun addMedication(medication: MedicationModel) {
        val resolvedName = medication.petName.ifBlank {
            uiState.value.pets.find { it.id == medication.petId }?.name.orEmpty()
        }
        val (hour, minute) = com.example.petcare.utils.NotificationHelper.parseHourMinute(
            medication.scheduleTime
        ) ?: (medication.reminderHour.toInt() to medication.reminderMinute.toInt())
        repository.addMedication(
            medication.copy(
                petName = resolvedName,
                reminderHour = hour.toLong(),
                reminderMinute = minute.toLong(),
            )
        )
    }

    fun addMedication(
        name: String,
        dosage: String,
        scheduleTime: String,
        quantity: String = "30 viên",
        status: String = "Đang dùng",
        petId: String? = null,
    ) {
        val targetPetId = petId ?: uiState.value.selectedPetId
        val resolvedName = uiState.value.pets.find { it.id == targetPetId }?.name.orEmpty()
        val (hour, minute) = com.example.petcare.utils.NotificationHelper.parseHourMinute(scheduleTime)
            ?: (8 to 0)
        val active = !status.equals("Đã xong", ignoreCase = true)
        val item = MedicationModel(
            id = "m_${System.currentTimeMillis()}",
            petId = targetPetId,
            petName = resolvedName,
            name = name.trim(),
            dosage = dosage.trim(),
            scheduleTime = scheduleTime.trim(),
            quantity = quantity.trim(),
            status = status.trim(),
            reminderHour = hour.toLong(),
            reminderMinute = minute.toLong(),
            active = active,
            reminderEnabled = active,
        )
        repository.addMedication(item)
    }

    fun toggleVaccinationCompleted(vaccination: VaccinationModel) {
        repository.toggleVaccinationCompleted(vaccination)
    }

    fun deleteVaccination(vaccinationId: String) {
        repository.deleteVaccination(vaccinationId)
    }

    fun toggleMedicationTakenToday(medication: MedicationModel) {
        repository.toggleMedicationTakenToday(medication)
    }

    fun deleteMedication(medicationId: String) {
        repository.deleteMedication(medicationId)
    }

    fun addWeightRecord(record: WeightRecordModel) {
        val resolvedName = record.petName.ifBlank {
            uiState.value.pets.find { it.id == record.petId }?.name.orEmpty()
        }
        val parsedKg = if (record.weightKg > 0.0) {
            record.weightKg
        } else {
            com.example.petcare.utils.DateUtils.parseWeightKg(record.weight)
        }
        repository.addWeightRecord(
            record.copy(
                petName = resolvedName,
                weightKg = parsedKg,
            )
        )
    }

    fun addCheckupRecord(record: CheckupRecordModel) {
        val resolvedName = record.petName.ifBlank {
            uiState.value.pets.find { it.id == record.petId }?.name.orEmpty()
        }
        repository.addCheckupRecord(record.copy(petName = resolvedName))
    }

    fun addMedicalRecord(record: MedicalRecordEntryModel) {
        repository.addMedicalRecord(record)
    }

    fun addMedicalRecord(
        activityType: String,
        description: String,
        dateTime: String = "Hôm nay",
        emotion: String = "Bình thường",
        photoUri: String? = null,
        petId: String? = null,
    ) {
        val targetPetId = petId ?: uiState.value.selectedPetId
        val petName = uiState.value.pets.find { it.id == targetPetId }?.name ?: "Bé cưng"
        val item = MedicalRecordEntryModel(
            id = "r_${System.currentTimeMillis()}",
            petId = targetPetId,
            petName = petName,
            dateTime = dateTime.trim(),
            activityType = activityType.trim(),
            description = description.trim(),
            emotion = emotion.trim(),
            photoUri = photoUri,
        )
        repository.addMedicalRecord(item)
    }

    fun getPetById(id: String): PetModel? = repository.getPetById(id)

    fun getSelectedPet(): PetModel = repository.getSelectedPet()

    fun getVaccinationsForPet(petId: String): List<VaccinationModel> =
        repository.getVaccinationsForPet(petId)

    fun getMedicationsForPet(petId: String): List<MedicationModel> =
        repository.getMedicationsForPet(petId)

    fun getMedicalRecordsForPet(petId: String): List<MedicalRecordEntryModel> =
        repository.getMedicalRecordsForPet(petId)
}

private data class HealthDataBundle(
    val vaccinations: List<VaccinationModel>,
    val medications: List<MedicationModel>,
    val medicalRecords: List<MedicalRecordEntryModel>,
    val weightRecords: List<WeightRecordModel>,
    val checkupRecords: List<CheckupRecordModel>,
)
