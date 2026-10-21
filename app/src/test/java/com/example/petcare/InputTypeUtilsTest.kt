package com.example.petcare

import com.example.petcare.utils.InputType
import com.example.petcare.utils.InputTypeUtils
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class InputTypeUtilsTest {

    @Test
    fun testIsEmail() {
        assertTrue(InputTypeUtils.isEmail("test@example.com"))
        assertTrue(InputTypeUtils.isEmail("user.name+tag@domain.co.uk"))
        assertFalse(InputTypeUtils.isEmail("invalid-email"))
        assertFalse(InputTypeUtils.isEmail("123456789"))
    }

    @Test
    fun testIsPhoneNumber() {
        assertTrue(InputTypeUtils.isPhoneNumber("0987654321"))
        assertTrue(InputTypeUtils.isPhoneNumber("+84987654321"))
        assertTrue(InputTypeUtils.isPhoneNumber("098-765-4321"))
        assertFalse(InputTypeUtils.isPhoneNumber("123"))
        assertFalse(InputTypeUtils.isPhoneNumber("test@example.com"))
    }

    @Test
    fun testIsUsername() {
        assertTrue(InputTypeUtils.isUsername("chubekuto123"))
        assertTrue(InputTypeUtils.isUsername("user_99"))
        assertFalse(InputTypeUtils.isUsername("ab")) // too short
        assertFalse(InputTypeUtils.isUsername("user@domain"))
    }

    @Test
    fun testDetermineInputType() {
        assertEquals(InputType.EMAIL, InputTypeUtils.determineInputType("test@example.com"))
        assertEquals(InputType.PHONE, InputTypeUtils.determineInputType("0987654321"))
        assertEquals(InputType.USERNAME, InputTypeUtils.determineInputType("chubekuto123"))
        assertEquals(InputType.UNKNOWN, InputTypeUtils.determineInputType("@invalid!"))
        assertEquals(InputType.UNKNOWN, InputTypeUtils.determineInputType(""))
    }
}
