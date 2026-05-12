package com.siscontrol.mobile.data.repository

import com.siscontrol.mobile.data.remote.ApiErrorParser
import com.siscontrol.mobile.data.remote.GuardFlowApiService
import com.siscontrol.mobile.data.remote.dto.EntityIdRefDto
import com.siscontrol.mobile.data.remote.dto.RegisterScanRequest
import com.siscontrol.mobile.data.remote.dto.RoundExecutionResponse
import com.siscontrol.mobile.data.remote.dto.ScanResponse
import com.siscontrol.mobile.data.remote.dto.ShiftResponse
import com.siscontrol.mobile.domain.repository.GuardFlowRepository
import retrofit2.Response
import java.io.IOException

class GuardFlowRepositoryImpl(
    private val apiService: GuardFlowApiService
) : GuardFlowRepository {

    override suspend fun startShift(userId: Long, installationId: Long): Result<ShiftResponse> =
        execute { apiService.startShift(userId, installationId) }

    override suspend fun finishShift(shiftId: Long): Result<ShiftResponse> =
        execute { apiService.finishShift(shiftId) }

    override suspend fun startRound(userId: Long, installationId: Long): Result<RoundExecutionResponse> =
        execute { apiService.startRound(userId, installationId) }

    override suspend fun finishRound(executionId: Long): Result<RoundExecutionResponse> =
        execute { apiService.finishRound(executionId) }

    override suspend fun registerScan(executionId: Long, checkpointId: Long): Result<ScanResponse> =
        execute {
            apiService.registerScan(
                RegisterScanRequest(
                    roundExecution = EntityIdRefDto(executionId),
                    checkpoint = EntityIdRefDto(checkpointId)
                )
            )
        }

    private suspend fun <T> execute(call: suspend () -> Response<T>): Result<T> {
        return try {
            val response = call()
            val body = response.body()
            if (response.isSuccessful && body != null) {
                Result.success(body)
            } else {
                Result.failure(Exception(ApiErrorParser.messageFrom(response)))
            }
        } catch (e: IOException) {
            Result.failure(Exception("Error de red, revisa tu conexión y la baseUrl del backend."))
        } catch (e: Exception) {
            Result.failure(Exception(ApiErrorParser.normalize(e.message ?: "Error inesperado.")))
        }
    }
}
