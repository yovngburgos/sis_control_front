package com.siscontrol.mobile.presentation.guard

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.siscontrol.mobile.data.remote.dto.InstallationDto
import com.siscontrol.mobile.di.SessionManager
import com.siscontrol.mobile.domain.usecase.FinishRoundUseCase
import com.siscontrol.mobile.domain.usecase.FinishShiftUseCase
import com.siscontrol.mobile.domain.usecase.GetCheckpointsUseCase
import com.siscontrol.mobile.domain.usecase.GetInstallationsUseCase
import com.siscontrol.mobile.domain.usecase.RegisterScanUseCase
import com.siscontrol.mobile.domain.usecase.StartRoundUseCase
import com.siscontrol.mobile.domain.usecase.StartShiftUseCase
import com.siscontrol.mobile.domain.usecase.RegisterNfcScanUseCase
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

sealed class GuardFlowUiState {
    object Idle : GuardFlowUiState()
    object Loading : GuardFlowUiState()
    data class Ready(
        val shiftId: Long? = null,
        val executionId: Long? = null,
        val installationId: Long? = null,
        val message: String? = null
    ) : GuardFlowUiState()
    data class ScanSuccess(val message: String, val checkpointName: String?, val checklogId: Long?) : GuardFlowUiState()
    data class Error(val message: String) : GuardFlowUiState()
}

data class GuardSetupState(
    val installations: List<InstallationDto> = emptyList(),
    val selectedInstallationId: Long? = null,
    val userId: String = "",
    val loadingInstallations: Boolean = false,
    val message: String? = null
)

class GuardFlowViewModel(
    private val sessionManager: SessionManager,
    private val startShiftUseCase: StartShiftUseCase,
    private val finishShiftUseCase: FinishShiftUseCase,
    private val startRoundUseCase: StartRoundUseCase,
    private val finishRoundUseCase: FinishRoundUseCase,
    private val registerScanUseCase: RegisterScanUseCase,
    private val registerNfcScanUseCase: RegisterNfcScanUseCase,
    private val getInstallationsUseCase: GetInstallationsUseCase,
    private val getCheckpointsUseCase: GetCheckpointsUseCase
) : ViewModel() {

    private val _uiState = MutableStateFlow<GuardFlowUiState>(GuardFlowUiState.Idle)
    val uiState: StateFlow<GuardFlowUiState> = _uiState.asStateFlow()

    private val _setupState = MutableStateFlow(GuardSetupState())
    val setupState: StateFlow<GuardSetupState> = _setupState.asStateFlow()

    init {
        val session = sessionManager.getSessionSync()
        _setupState.value = _setupState.value.copy(
            userId = session.userId?.toString().orEmpty(),
            selectedInstallationId = session.installationId
        )
        loadInstallations()
    }

    fun loadInstallations() {
        viewModelScope.launch {
            _setupState.value = _setupState.value.copy(loadingInstallations = true, message = null)
            getInstallationsUseCase()
                .onSuccess { installations ->
                    _setupState.value = _setupState.value.copy(
                        installations = installations,
                        selectedInstallationId = _setupState.value.selectedInstallationId ?: installations.firstOrNull()?.id,
                        loadingInstallations = false
                    )
                }
                .onFailure { error ->
                    _setupState.value = _setupState.value.copy(
                        loadingInstallations = false,
                        message = error.message ?: "No fue posible cargar instalaciones."
                    )
                }
        }
    }

    fun updateUserId(value: String) {
        _setupState.value = _setupState.value.copy(userId = value.filter { it.isDigit() })
    }

    fun selectInstallation(installationId: Long) {
        _setupState.value = _setupState.value.copy(selectedInstallationId = installationId)
    }

    fun startShiftAndRound() {
        viewModelScope.launch {
            _uiState.value = GuardFlowUiState.Loading
            val userId = _setupState.value.userId.toLongOrNull()
            val installationId = _setupState.value.selectedInstallationId

            if (userId == null) {
                _uiState.value = GuardFlowUiState.Error(
                    "El backend no devuelve userId en login. Ingresa el userId del guardia para iniciar jornada."
                )
                return@launch
            }
            if (installationId == null) {
                _uiState.value = GuardFlowUiState.Error("Selecciona una instalación antes de iniciar jornada.")
                return@launch
            }

            sessionManager.saveGuardContext(userId, installationId)
            preloadCheckpoints(installationId)
                .onFailure { error ->
                    _uiState.value = GuardFlowUiState.Error(error.safeMessage())
                    return@launch
                }

            startShiftUseCase(userId, installationId)
                .onSuccess { shiftResponse ->
                    val shiftId = shiftResponse.shift.id
                    sessionManager.saveShiftId(shiftId)
                    startRoundUseCase(userId, installationId)
                        .onSuccess { roundResponse ->
                            val executionId = roundResponse.round.id
                            sessionManager.saveExecutionId(executionId)
                            _uiState.value = GuardFlowUiState.Ready(
                                shiftId = shiftId,
                                executionId = executionId,
                                installationId = installationId,
                                message = roundResponse.message ?: "Jornada y ronda iniciadas."
                            )
                        }
                        .onFailure { error -> _uiState.value = GuardFlowUiState.Error(error.safeMessage()) }
                }
                .onFailure { error -> _uiState.value = GuardFlowUiState.Error(error.safeMessage()) }
        }
    }

    fun registerNfcScan(nfcTagCode: String) {
        viewModelScope.launch {
            val code = nfcTagCode.trim().uppercase()

            if (code.isBlank()) {
                _uiState.value = GuardFlowUiState.Error("No se leyó un código NFC válido.")
                return@launch
            }

            val session = sessionManager.getSessionSync()
            val executionId = session.executionId

            if (executionId == null) {
                _uiState.value = GuardFlowUiState.Error(
                    "No tienes una ronda activa. Inicia una ronda antes de escanear NFC."
                )
                return@launch
            }

            _uiState.value = GuardFlowUiState.Loading

            registerNfcScanUseCase(executionId, code)
                .onSuccess { response ->
                    _uiState.value = GuardFlowUiState.ScanSuccess(
                        message = response.message ?: "Escaneo NFC registrado.",
                        checkpointName = response.scan.checkpoint?.name,
                        checklogId = response.scan.id
                    )
                }
                .onFailure { error ->
                    _uiState.value = GuardFlowUiState.Error(error.safeMessage())
                }
        }
    }

    fun finishRound(onCompleted: () -> Unit = {}) {
        viewModelScope.launch {
            val executionId = sessionManager.getSessionSync().executionId
            if (executionId == null) {
                _uiState.value = GuardFlowUiState.Error("No hay executionId activo para finalizar ronda.")
                return@launch
            }
            _uiState.value = GuardFlowUiState.Loading
            finishRoundUseCase(executionId)
                .onSuccess {
                    sessionManager.clearExecutionId()
                    _uiState.value = GuardFlowUiState.Ready(message = it.message ?: "Ronda finalizada.")
                    onCompleted()
                }
                .onFailure { error -> _uiState.value = GuardFlowUiState.Error(error.safeMessage()) }
        }
    }

    fun finishShift(onCompleted: () -> Unit = {}) {
        viewModelScope.launch {
            val shiftId = sessionManager.getSessionSync().shiftId
            if (shiftId == null) {
                _uiState.value = GuardFlowUiState.Error("No hay shiftId activo para finalizar jornada.")
                return@launch
            }
            _uiState.value = GuardFlowUiState.Loading
            finishShiftUseCase(shiftId)
                .onSuccess {
                    sessionManager.clearExecutionId()
                    sessionManager.clearShiftId()
                    _uiState.value = GuardFlowUiState.Ready(message = it.message ?: "Jornada finalizada.")
                    onCompleted()
                }
                .onFailure { error -> _uiState.value = GuardFlowUiState.Error(error.safeMessage()) }
        }
    }

    private suspend fun preloadCheckpoints(installationId: Long): Result<Unit> {
        return getCheckpointsUseCase(installationId).fold(
            onSuccess = { checkpoints ->
                sessionManager.saveCheckpointMap(
                    checkpoints.associate { checkpoint -> checkpoint.nfcTagCode.uppercase() to checkpoint.id }
                )
                Result.success(Unit)
            },
            onFailure = { error -> Result.failure(error) }
        )
    }

    private fun Throwable.safeMessage(): String = message ?: "No fue posible completar la operación."
}
