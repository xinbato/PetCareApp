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
        assertEquals("", PetRepository.userProfile.value.name)
        assertTrue(PetRepository.pets.value.isEmpty())
        assertEquals("", PetRepository.currentSelectedPetId.value)
        assertEquals("", PetRepository.getSelectedPet().name)
        assertTrue(PetRepository.accounts.value.isEmpty())
        assertTrue(PetRepository.vaccinations.value.isEmpty())
        assertTrue(PetRepository.weightRecords.value.isEmpty())
        assertTrue(PetRepository.checkupRecords.value.isEmpty())
        assertTrue(PetRepository.medications.value.isEmpty())
        assertTrue(PetRepository.medicalRecords.value.isEmpty())
        assertTrue(PetRepository.schedules.value.isEmpty())
        assertEquals(5, PetRepository.petPlaces.value.size)
    }

    @Test
    fun testAddAndSwitchAccount() {
        PetRepository.addAccount("Tester")
        assertEquals(1, PetRepository.accounts.value.size)
        val firstAcc = PetRepository.accounts.value.first()
        assertEquals("Tester", firstAcc.name)
        assertTrue(firstAcc.isCurrent)
        assertEquals("Tester", PetRepository.userProfile.value.name)

        PetRepository.addAccount("Second")
        assertEquals(2, PetRepository.accounts.value.size)
        val secondAcc = PetRepository.accounts.value.last()
        assertTrue(secondAcc.isCurrent)
        assertEquals("Second", PetRepository.userProfile.value.name)

        PetRepository.switchAccount(firstAcc.id)
        val updatedFirst = PetRepository.accounts.value.first { it.id == firstAcc.id }
        assertTrue(updatedFirst.isCurrent)
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
    fun testPetOperations() {
        val pet1 = PetModel(id = "1", name = "Momo", species = "Dog", iconRes = R.drawable.dog)
        val pet2 = PetModel(id = "2", name = "Luna", species = "Cat", iconRes = R.drawable.cat)

        PetRepository.savePet(pet1)
        assertEquals(1, PetRepository.pets.value.size)
        assertEquals("1", PetRepository.currentSelectedPetId.value)
        assertEquals("Momo", PetRepository.getSelectedPet().name)

        PetRepository.savePet(pet2)
        assertEquals(2, PetRepository.pets.value.size)

        PetRepository.selectPet("2")
        assertEquals("2", PetRepository.currentSelectedPetId.value)
        assertEquals("Luna", PetRepository.getSelectedPet().name)

        val fetched = PetRepository.getPetById("1")
        assertNotNull(fetched)
        assertEquals("Momo", fetched?.name)

        val nonExistent = PetRepository.getPetById("999")
        assertNull(nonExistent)

        val updatedPet1 = pet1.copy(name = "Momo Updated", weight = "14.0 kg")
        PetRepository.savePet(updatedPet1)
        assertEquals("Momo Updated", PetRepository.getPetById("1")?.name)
        assertEquals("14.0 kg", PetRepository.getPetById("1")?.weight)

        val pet2BeforeFav = PetRepository.getPetById("2")!!
        val initialFav = pet2BeforeFav.isFavorite
        PetRepository.toggleFavorite("2")
        val pet2AfterFav = PetRepository.getPetById("2")!!
        assertEquals(!initialFav, pet2AfterFav.isFavorite)

        PetRepository.deletePet("2")
        assertNull(PetRepository.getPetById("2"))
        assertEquals(1, PetRepository.pets.value.size)
        assertEquals("1", PetRepository.currentSelectedPetId.value)
        assertEquals("Momo Updated", PetRepository.getSelectedPet().name)
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
        assertTrue(PetRepository.getVaccinationsForPet("1").isEmpty())

        val newVax = VaccinationModel(
            id = "v_new",
            petId = "1",
            title = "Rabies Booster",
            dateTime = "01/01/2027",
            status = "Kế hoạch"
        )
        PetRepository.addVaccination(newVax)

        val pet1Vax = PetRepository.getVaccinationsForPet("1")
        assertEquals(1, pet1Vax.size)
        assertEquals("v_new", pet1Vax.first().id)
        assertEquals("Rabies Booster", pet1Vax.first().title)
    }

    @Test
    fun testMedicationOperations() {
        assertTrue(PetRepository.getMedicationsForPet("1").isEmpty())

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
        assertEquals(1, updatedMeds.size)
        assertEquals("m_new", updatedMeds.first().id)
    }

    @Test
    fun testMedicalRecordOperations() {
        assertTrue(PetRepository.getMedicalRecordsForPet("1").isEmpty())

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
        assertEquals(1, updatedRecords.size)
        assertEquals("r_new", updatedRecords.first().id)
    }

    @Test
    fun testScheduleOperations() {
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
        assertEquals(1, pet1Weights.size)
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
        assertEquals(1, pet1Checkups.size)
        assertEquals("c_new", pet1Checkups.first().id)
    }
}
