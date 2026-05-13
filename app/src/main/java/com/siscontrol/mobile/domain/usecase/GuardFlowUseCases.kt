package com.siscontrol.mobile.domain.usecase

import com.siscontrol.mobile.domain.repository.GuardFlowRepository

class StartShiftUseCase(private val repository: GuardFlowRepository) {
    suspend operator fun invoke(userId: Long, installationId: Long) = repository.startShift(userId, installationId)
}

class FinishShiftUseCase(private val repository: GuardFlowRepository) {
    suspend operator fun invoke(shiftId: Long) = repository.finishShift(shiftId)
}

class StartRoundUseCase(private val repository: GuardFlowRepository) {
    suspend operator fun invoke(userId: Long, installationId: Long) = repository.startRound(userId, installationId)
}

class FinishRoundUseCase(private val repository: GuardFlowRepository) {
    suspend operator fun invoke(executionId: Long) = repository.finishRound(executionId)
}

class RegisterScanUseCase(private val repository: GuardFlowRepository) {
    suspend operator fun invoke(executionId: Long, checkpointId: Long) = repository.registerScan(executionId, checkpointId)
}

class RegisterNfcScanUseCase(private val repository: GuardFlowRepository) {
    suspend operator fun invoke(executionId: Long, nfcTagCode: String) =
        repository.registerNfcScan(executionId, nfcTagCode)
}
