package com.example.petcare.utils

enum class InputType(val displayName: String) {
    EMAIL("Email"),
    PHONE("Số điện thoại"),
    USERNAME("Tên đăng nhập"),
    UNKNOWN("Chưa xác định")
}

object InputTypeUtils {
    fun isEmail(input: String): Boolean {
        val emailRegex = "^[A-Za-z0-9._%+-]+@[A-Za-z0-9.-]+\\.[A-Za-z]{2,}$"
        return input.trim().matches(Regex(emailRegex))
    }

    fun isPhoneNumber(input: String): Boolean {
        val clean = input.trim().replace(" ", "").replace("-", "")
        val phoneRegex = "^(\\+?[0-9]{9,12}|0[0-9]{8,11})$"
        return clean.matches(Regex(phoneRegex))
    }

    fun isUsername(input: String): Boolean {
        val trimmed = input.trim()
        if (trimmed.length < 3) return false
        val usernameRegex = "^[a-zA-Z0-9_.]+$"
        return trimmed.matches(Regex(usernameRegex))
    }

    fun determineInputType(input: String): InputType {
        val trimmed = input.trim()
        if (trimmed.isEmpty()) return InputType.UNKNOWN
        return when {
            isEmail(trimmed) -> InputType.EMAIL
            isPhoneNumber(trimmed) -> InputType.PHONE
            isUsername(trimmed) -> InputType.USERNAME
            else -> InputType.UNKNOWN
        }
    }

    fun determineEmailOrPhone(input: String): InputType {
        val trimmed = input.trim()
        if (trimmed.isEmpty()) return InputType.UNKNOWN
        return when {
            isEmail(trimmed) -> InputType.EMAIL
            isPhoneNumber(trimmed) -> InputType.PHONE
            else -> InputType.UNKNOWN
        }
    }
}
