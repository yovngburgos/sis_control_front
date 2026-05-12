package com.siscontrol.mobile.domain.usecase

/**
 * Modelo de dominio para una autenticación exitosa.
 * El backend actual no devuelve token ni userId.
 */
data class LoginResult(
    val message: String,
    val username: String,
    val role: String
)
