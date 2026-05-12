package com.siscontrol.mobile.data.remote.dto

/**
 * DTO real de /api/instalaciones.
 */
data class InstallationDto(
    val id: Long,
    val name: String,
    val address: String? = null,
    val clientName: String? = null,
    val location: String? = null,
    val status: String? = null,
    val createdAt: String? = null,
    val lat: Double? = null,
    val lng: Double? = null
)

data class InstallationRequestDto(
    val name: String,
    val address: String,
    val clientName: String? = null,
    val location: String? = null
)


data class InstallationCreateResponse(
    val mensaje: String? = null,
    val instalacion: InstallationDto
)
