package com.siscontrol.mobile.data.remote.dto

/**
 * DTO real de /api/instalaciones/{installationId}/checkpoints.
 */
data class CheckpointDto(
    val id: Long,
    val name: String,
    val locationDescription: String? = null,
    val nfcTagCode: String,
    val installationId: Long? = null,
    val status: String? = null,
    val type: String = "NFC",
    val tagIdentifier: String = nfcTagCode,
    val lat: Double? = null,
    val lng: Double? = null,
    val sequenceOrder: Int? = null
)

data class InstallationRefDto(
    val id: Long
)

data class CheckpointRequestDto(
    val name: String,
    val locationDescription: String? = null,
    val nfcTagCode: String,
    val installation: InstallationRefDto
)


data class CheckpointCreateResponse(
    val mensaje: String? = null,
    val checkpoint: CheckpointDto
)
