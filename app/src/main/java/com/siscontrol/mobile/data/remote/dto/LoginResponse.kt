package com.siscontrol.mobile.data.remote.dto

/**
 * Respuesta real de POST /api/auth/login.
 * El backend no emite JWT ni devuelve userId.
 */
data class LoginResponse(
    val message: String,
    val success: Boolean,
    val id: Long?,
    val username: String?,
    val role: String?
)