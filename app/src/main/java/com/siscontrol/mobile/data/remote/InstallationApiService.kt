package com.siscontrol.mobile.data.remote

import com.siscontrol.mobile.data.remote.dto.CheckpointCreateResponse
import com.siscontrol.mobile.data.remote.dto.CheckpointDto
import com.siscontrol.mobile.data.remote.dto.CheckpointRequestDto
import com.siscontrol.mobile.data.remote.dto.InstallationCreateResponse
import com.siscontrol.mobile.data.remote.dto.InstallationDto
import com.siscontrol.mobile.data.remote.dto.InstallationRequestDto
import retrofit2.http.Body
import retrofit2.http.GET
import retrofit2.http.POST
import retrofit2.http.Path

/**
 * Servicio Retrofit para instalaciones y checkpoints según el backend real.
 */
interface InstallationApiService {

    @GET("/api/instalaciones")
    suspend fun getInstallations(): List<InstallationDto>

    @POST("/api/instalaciones")
    suspend fun createInstallation(@Body request: InstallationRequestDto): InstallationCreateResponse

    @GET("/api/instalaciones/{id}/checkpoints")
    suspend fun getCheckpoints(@Path("id") installationId: Long): List<CheckpointDto>

    @POST("/api/instalaciones/checkpoints")
    suspend fun createCheckpoint(@Body request: CheckpointRequestDto): CheckpointCreateResponse
}
