package com.siscontrol.mobile.data.remote.dto

import com.google.gson.annotations.SerializedName

data class UserSummaryDto(
    val id: Long,
    val username: String? = null,
    val email: String? = null,
    val fullName: String? = null,
    val role: String? = null,
    val status: String? = null,
    val createdAt: String? = null
)

data class ShiftDto(
    val id: Long,
    val installation: InstallationDto? = null,
    val worker: UserSummaryDto? = null,
    val entryTime: String? = null,
    val exitTime: String? = null,
    val status: String? = null
)

data class RoundExecutionDto(
    val id: Long,
    val worker: UserSummaryDto? = null,
    val installation: InstallationDto? = null,
    val status: String? = null,
    val startTime: String? = null,
    val endTime: String? = null
)

data class ShiftResponse(
    @SerializedName(value = "mensaje", alternate = ["message"])
    val message: String? = null,
    @SerializedName(value = "jornada")
    val shift: ShiftDto
)

data class RoundExecutionResponse(
    @SerializedName(value = "mensaje", alternate = ["message"])
    val message: String? = null,
    @SerializedName(value = "ronda")
    val round: RoundExecutionDto
)

data class EntityIdRefDto(
    val id: Long
)

data class RegisterScanRequest(
    val roundExecution: EntityIdRefDto,
    val checkpoint: EntityIdRefDto
)

data class ChecklogDto(
    val id: Long,
    val timestamp: String? = null,
    val roundExecution: RoundExecutionDto? = null,
    val checkpoint: CheckpointDto? = null
)

data class ScanResponse(
    @SerializedName(value = "mensaje", alternate = ["message"])
    val message: String? = null,
    @SerializedName(value = "escaneo")
    val scan: ChecklogDto
)
