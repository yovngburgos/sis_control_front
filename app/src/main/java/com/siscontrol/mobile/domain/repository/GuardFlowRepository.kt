package com.siscontrol.mobile.domain.repository

import com.siscontrol.mobile.data.remote.dto.RoundExecutionResponse
import com.siscontrol.mobile.data.remote.dto.ScanResponse
import com.siscontrol.mobile.data.remote.dto.ShiftResponse

interface GuardFlowRepository {
    suspend fun startShift(userId: Long, installationId: Long): Result<ShiftResponse>
    suspend fun finishShift(shiftId: Long): Result<ShiftResponse>
    suspend fun startRound(userId: Long, installationId: Long): Result<RoundExecutionResponse>
    suspend fun finishRound(executionId: Long): Result<RoundExecutionResponse>
    suspend fun registerScan(executionId: Long, checkpointId: Long): Result<ScanResponse>

    suspend fun registerNfcScan(executionId: Long, nfcTagCode: String): Result<ScanResponse>
}
