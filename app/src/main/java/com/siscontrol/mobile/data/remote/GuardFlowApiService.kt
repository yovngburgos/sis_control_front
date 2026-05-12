package com.siscontrol.mobile.data.remote

import com.siscontrol.mobile.data.remote.dto.RegisterScanRequest
import com.siscontrol.mobile.data.remote.dto.RoundExecutionResponse
import com.siscontrol.mobile.data.remote.dto.ScanResponse
import com.siscontrol.mobile.data.remote.dto.ShiftResponse
import retrofit2.Response
import retrofit2.http.Body
import retrofit2.http.POST
import retrofit2.http.PUT
import retrofit2.http.Path
import retrofit2.http.Query

interface GuardFlowApiService {
    @POST("/api/rondas/jornada/iniciar")
    suspend fun startShift(
        @Query("userId") userId: Long,
        @Query("installationId") installationId: Long
    ): Response<ShiftResponse>

    @PUT("/api/rondas/jornada/finalizar/{id}")
    suspend fun finishShift(@Path("id") shiftId: Long): Response<ShiftResponse>

    @POST("/api/rondas/iniciar")
    suspend fun startRound(
        @Query("userId") userId: Long,
        @Query("installationId") installationId: Long
    ): Response<RoundExecutionResponse>

    @PUT("/api/rondas/finalizar/{id}")
    suspend fun finishRound(@Path("id") executionId: Long): Response<RoundExecutionResponse>

    @POST("/api/rondas/escaneo")
    suspend fun registerScan(@Body request: RegisterScanRequest): Response<ScanResponse>
}
