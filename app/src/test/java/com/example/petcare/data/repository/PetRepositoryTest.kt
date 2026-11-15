package com.example.petcare.data.repository

import com.example.petcare.R
import com.example.petcare.data.model.CheckupRecordModel
import com.example.petcare.data.model.MedicalRecordEntryModel
import com.example.petcare.data.model.MedicationModel
import com.example.petcare.data.model.PetModel
import com.example.petcare.data.model.ScheduleModel
import com.example.petcare.data.model.VaccinationModel
import com.example.petcare.data.model.WeightRecordModel
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test

class PetRepositoryTest {

    @Before
    fun setUp() {
        PetRepository.resetToDefaults()
    }

    @Test
    fun testInitialState() {
        assertEquals("User", PetRepository.userProfile.value.name)
        assertEquals(3, PetRepository.pets.value.size)
        assertEquals("1", PetRepository.currentSelectedPetId.value)
        assertEquals("Momo", PetRepository.getSelectedPet().name)
        assertEquals(2, PetRepository.accounts.value.size)
        assertEquals(8, PetRepository.vaccinations.value.size)
        assertEquals(7, PetRepository.weightRecords.value.size)
        assertEquals(4, PetRepository.checkupRecords.value.size)
        assertEquals(2, PetRepository.medications.value.size)
        assertEquals(3, PetRepository.medicalRecords.value.size)
        assertEquals(3, PetRepository.schedules.value.size)
        assertEquals(5, PetRepository.petPlaces.value.size)
    }

    @Test
    fun testSwitchAccount() {
        PetRepository.switchAccount("a2")
        val currentAcc = PetRepository.accounts.value.find { it.id == "a2" }
        assertNotNull(currentAcc)
        assertTrue(currentAcc!!.isCurrent)

        val previousAcc = PetRepository.accounts.value.find { it.id == "a1" }
        assertFalse(previousAcc!!.isCurrent)

        assertEquals("Nguyên đz", PetRepository.userProfile.value.name)
    }

    @Test
    fun testAddAccount() {
        val initialCount = PetRepository.accounts.value.size
        PetRepository.addAccount("Tester")

        val accounts = PetRepository.accounts.value
        assertEquals(initialCount + 1, accounts.size)
        val newAcc = accounts.last()
        assertEquals("Tester", newAcc.name)
        assertTrue(newAcc.isCurrent)
        assertEquals("Tester", PetRepository.userProfile.value.name)
    }

    @Test
    fun testUpdateUserProfile() {
        PetRepository.updateUserProfile(
            name = "John Doe",
            email = "johndoe@test.com",
            phone = "0123456789",
            dob = "01/01/2000",
            address = "Danang, Vietnam"
        )

        val profile = PetRepository.userProfile.value
        assertEquals("John Doe", profile.name)
        assertEquals("johndoe@test.com", profile.email)
        assertEquals("0123456789", profile.phone)
        assertEquals("01/01/2000", profile.dob)
        assertEquals("Danang, Vietnam", profile.address)
    }

    @Test
    fun testSelectPet() {
        PetRepository.selectPet("2")
        assertEquals("2", PetRepository.currentSelectedPetId.value)
        assertEquals("Luna", PetRepository.getSelectedPet().name)

        PetRepository.setCurrentSelectedPetId("3")
        assertEquals("3", PetRepository.currentSelectedPetId.value)
        assertEquals("Cậu Vàng", PetRepository.getSelectedPet().name)
    }

    @Test
    fun testGetPetById() {
        val pet = PetRepository.getPetById("1")
        assertNotNull(pet)
        assertEquals("Momo", pet?.name)

        val nonExistent = PetRepository.getPetById("999")
        assertNull(nonExistent)
    }

    @Test
    fun testSavePetUpdateExisting() {
        val originalPet = PetRepository.getPetById("1")!!
        val updatedPet = originalPet.copy(name = "Momo Updated", weight = "14.0 kg")
        PetRepository.savePet(updatedPet)

        val fetched = PetRepository.getPetById("1")
        assertEquals("Momo Updated", fetched?.name)
        assertEquals("14.0 kg", fetched?.weight)
    }

    @Test
    fun testSavePetAddNew() {
        val initialSize = PetRepository.pets.value.size
        val newPet = PetModel(id = "pet_99", name = "Bé Bự", species = "Hamster", iconRes = R.drawable.dog)
        PetRepository.savePet(newPet)

        assertEquals(initialSize + 1, PetRepository.pets.value.size)
        val fetched = PetRepository.getPetById("pet_99")
        assertNotNull(fetched)
        assertEquals("Bé Bự", fetched?.name)
    }

    @Test
    fun testDeletePet() {
        PetRepository.deletePet("2")
        assertNull(PetRepository.getPetById("2"))
        assertEquals(2, PetRepository.pets.value.size)
    }

    @Test
    fun testDeleteSelectedPetUpdatesCurrentSelectedPetId() {
        PetRepository.selectPet("1")
        PetRepository.deletePet("1")

        // Once pet 1 is deleted, selected pet id should fall back to first remaining pet
        assertEquals("2", PetRepository.currentSelectedPetId.value)
        assertEquals("Luna", PetRepository.getSelectedPet().name)
    }

    @Test
    fun testToggleFavorite() {
        val pet = PetRepository.getPetById("2")!!
        val initialFav = pet.isFavorite
        PetRepository.toggleFavorite("2")

        val updatedPet = PetRepository.getPetById("2")!!
        assertEquals(!initialFav, updatedPet.isFavorite)
    }

    @Test
    fun testToggleSavePlace() {
        val place = PetRepository.petPlaces.value.first { it.id == "p2" }
        assertFalse(place.isSaved)

        PetRepository.toggleSavePlace("p2")
        val updated = PetRepository.petPlaces.value.first { it.id == "p2" }
        assertTrue(updated.isSaved)

        PetRepository.toggleSavePlace("p2")
        val toggledBack = PetRepository.petPlaces.value.first { it.id == "p2" }
        assertFalse(toggledBack.isSaved)
    }

    @Test
    fun testVaccinationOperations() {
        val pet1Vax = PetRepository.getVaccinationsForPet("1")
        assertEquals(3, pet1Vax.size)

        val newVax = VaccinationModel(
            id = "v_new",
            petId = "1",
            title = "Rabies Booster",
            dateTime = "01/01/2027",
            status = "Kế hoạch"
        )
        PetRepository.addVaccination(newVax)

        val updatedPet1Vax = PetRepository.getVaccinationsForPet("1")
        assertEquals(4, updatedPet1Vax.size)
        assertEquals("v_new", updatedPet1Vax.first().id)
    }

    @Test
    fun testMedicationOperations() {
        val pet1Meds = PetRepository.getMedicationsForPet("1")
        assertEquals(2, pet1Meds.size)

        val newMed = MedicationModel(
            id = "m_new",
            petId = "1",
            name = "Vitamin C",
            dosage = "1 tablet",
            scheduleTime = "09:00",
            quantity = "10 tablets",
            status = "Đang dùng"
        )
        PetRepository.addMedication(newMed)

        val updatedMeds = PetRepository.getMedicationsForPet("1")
        assertEquals(3, updatedMeds.size)
        assertEquals("m_new", updatedMeds.first().id)
    }

    @Test
    fun testMedicalRecordOperations() {
        val pet1Records = PetRepository.getMedicalRecordsForPet("1")
        assertEquals(3, pet1Records.size)

        val newRecord = MedicalRecordEntryModel(
            id = "r_new",
            petId = "1",
            petName = "Momo",
            dateTime = "15/10/2026-10h00",
            activityType = "Khám lại",
            description = "Bình thường",
            emotion = "Vui vẻ"
        )
        PetRepository.addMedicalRecord(newRecord)

        val updatedRecords = PetRepository.getMedicalRecordsForPet("1")
        assertEquals(4, updatedRecords.size)
        assertEquals("r_new", updatedRecords.first().id)
    }

    @Test
    fun testScheduleOperations() {
        val schedule = PetRepository.getScheduleById("s1")
        assertNotNull(schedule)
        assertEquals("Bữa tối", schedule?.title)

        val newSchedule = ScheduleModel(
            id = "s_new",
            title = "Đi dạo",
            description = "Đi dạo công viên",
            petId = "1",
            petName = "Momo",
            dateText = "20/10/2026",
            timeText = "17:00",
            timeOrDateLabel = "17:00"
        )
        PetRepository.addSchedule(newSchedule)
        assertNotNull(PetRepository.getScheduleById("s_new"))

        val updatedSchedule = newSchedule.copy(title = "Đi dạo công viên bờ kè")
        PetRepository.updateSchedule(updatedSchedule)
        assertEquals("Đi dạo công viên bờ kè", PetRepository.getScheduleById("s_new")?.title)

        PetRepository.deleteSchedule("s_new")
        assertNull(PetRepository.getScheduleById("s_new"))
    }

    @Test
    fun testWeightAndCheckupOperations() {
        val newWeight = WeightRecordModel(
            id = "w_new",
            petId = "1",
            petName = "Momo",
            weight = "12.6 kg",
            dateTime = "15/09/2026",
            status = "Ổn định"
        )
        PetRepository.addWeightRecord(newWeight)
        val pet1Weights = PetRepository.getWeightRecordsForPet("1")
        assertEquals("w_new", pet1Weights.first().id)

        val newCheckup = CheckupRecordModel(
            id = "c_new",
            petId = "1",
            petName = "Momo",
            title = "Siêu âm bụng",
            doctorOrClinic = "BS. Nam",
            dateTime = "10/09/2026",
            result = "Bình thường"
        )
        PetRepository.addCheckupRecord(newCheckup)
        val pet1Checkups = PetRepository.getCheckupRecordsForPet("1")
        assertEquals("c_new", pet1Checkups.first().id)
    }
}
