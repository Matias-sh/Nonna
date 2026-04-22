package com.cocido.nonna.util

import java.time.LocalDate

object FormValidators {
    private val emailRegex = Regex("^[A-Za-z0-9+_.-]+@[A-Za-z0-9.-]+$")
    private val usernameRegex = Regex("^[A-Za-z0-9._-]{3,30}$")

    fun isValidEmail(value: String): Boolean = emailRegex.matches(value.trim())

    fun isValidUsername(value: String): Boolean = usernameRegex.matches(value.trim())

    fun hasMinLength(value: String, min: Int): Boolean = value.trim().length >= min

    fun isValidIsoDate(value: String): Boolean {
        if (value.isBlank()) return false
        return runCatching { LocalDate.parse(value) }.isSuccess
    }

    fun isBirthAfterDeath(birthDate: String, deathDate: String): Boolean {
        if (birthDate.isBlank() || deathDate.isBlank()) return false
        return runCatching {
            LocalDate.parse(birthDate).isAfter(LocalDate.parse(deathDate))
        }.getOrDefault(false)
    }
}
