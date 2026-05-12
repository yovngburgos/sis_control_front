package com.siscontrol.mobile.presentation.guard

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.LocationOn
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material.icons.filled.Wifi
import androidx.compose.material.icons.automirrored.filled.KeyboardArrowRight
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.siscontrol.mobile.presentation.components.SISTopBar
import com.siscontrol.mobile.presentation.theme.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun GuardStartRoundScreen(
    paddingValues: PaddingValues,
    viewModel: GuardFlowViewModel,
    onRoundStarted: () -> Unit
) {
    val state by viewModel.uiState.collectAsState()
    val setupState by viewModel.setupState.collectAsState()
    var expandedInstallation by remember { mutableStateOf(false) }

    LaunchedEffect(state) {
        val ready = state as? GuardFlowUiState.Ready
        if (ready?.executionId != null) onRoundStarted()
    }
    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(BackgroundColor)
            .padding(paddingValues)
    ) {
        SISTopBar(
            title = "Iniciar Nueva Ronda",
            subtitle = "Selecciona la instalación",
            showAdminLogo = false
        )

        LazyColumn(
            modifier = Modifier.fillMaxSize(),
            contentPadding = PaddingValues(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            item {
                // GPS Alert
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(Color(0xFFEFF6FF), RoundedCornerShape(8.dp))
                        .border(1.dp, Color(0xFFBFDBFE), RoundedCornerShape(8.dp))
                        .padding(16.dp)
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(Icons.Default.LocationOn, contentDescription = null, tint = DangerColor, modifier = Modifier.size(16.dp))
                        Spacer(modifier = Modifier.width(8.dp))
                        Text("Ubicación GPS detectada correctamente", color = PrimaryColor, fontSize = 14.sp, fontWeight = FontWeight.Medium)
                    }
                }
            }

            item {
                Text("Datos de inicio", fontSize = 18.sp, fontWeight = FontWeight.Bold, color = TextPrimary, modifier = Modifier.padding(top = 8.dp))
            }

            item {
                OutlinedTextField(
                    value = setupState.userId,
                    onValueChange = viewModel::updateUserId,
                    label = { Text("UserId del guardia") },
                    supportingText = { Text("El login del backend no devuelve userId; ingrésalo para llamar a rondas.") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )
            }

            item {
                ExposedDropdownMenuBox(
                    expanded = expandedInstallation,
                    onExpandedChange = { expandedInstallation = !expandedInstallation }
                ) {
                    val selected = setupState.installations.find { it.id == setupState.selectedInstallationId }
                    OutlinedTextField(
                        readOnly = true,
                        value = selected?.name ?: "Selecciona instalación",
                        onValueChange = {},
                        label = { Text("Instalación") },
                        trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = expandedInstallation) },
                        modifier = Modifier.menuAnchor().fillMaxWidth()
                    )
                    ExposedDropdownMenu(
                        expanded = expandedInstallation,
                        onDismissRequest = { expandedInstallation = false }
                    ) {
                        setupState.installations.forEach { installation ->
                            DropdownMenuItem(
                                text = { Text(installation.name) },
                                onClick = {
                                    viewModel.selectInstallation(installation.id)
                                    expandedInstallation = false
                                }
                            )
                        }
                    }
                }
                if (setupState.loadingInstallations) {
                    LinearProgressIndicator(modifier = Modifier.fillMaxWidth().padding(top = 8.dp))
                }
                setupState.message?.let { message ->
                    Text(message, color = DangerColor, fontWeight = FontWeight.Medium, modifier = Modifier.padding(top = 8.dp))
                }
            }

            item {
                InstallationCard(
                    name = setupState.installations.find { it.id == setupState.selectedInstallationId }?.name ?: "Instalación seleccionada",
                    distance = "ID: ${setupState.selectedInstallationId ?: "--"}",
                    checkpoints = "Carga checkpoints NFC antes de escanear",
                    onStartRound = { viewModel.startShiftAndRound() }
                )
            }
            item {
                when (state) {
                    is GuardFlowUiState.Loading -> LinearProgressIndicator(modifier = Modifier.fillMaxWidth())
                    is GuardFlowUiState.Error -> Text(
                        text = (state as GuardFlowUiState.Error).message,
                        color = DangerColor,
                        fontWeight = FontWeight.Medium
                    )
                    is GuardFlowUiState.Ready -> Text(
                        text = (state as GuardFlowUiState.Ready).message.orEmpty(),
                        color = SuccessColor,
                        fontWeight = FontWeight.Medium
                    )
                    else -> Unit
                }
            }

            item {
                // Verification box
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(Color(0xFFFEFCE8), RoundedCornerShape(8.dp))
                        .border(1.dp, Color(0xFFFEF08A), RoundedCornerShape(8.dp))
                        .padding(16.dp)
                ) {
                    Column {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(Icons.Default.Warning, contentDescription = null, tint = Color(0xFFD97706), modifier = Modifier.size(16.dp))
                            Spacer(modifier = Modifier.width(8.dp))
                            Text("Antes de comenzar, verifica:", color = Color(0xFF92400E), fontSize = 14.sp, fontWeight = FontWeight.Medium)
                        }
                        Spacer(modifier = Modifier.height(8.dp))
                        Column(modifier = Modifier.padding(start = 24.dp), verticalArrangement = Arrangement.spacedBy(4.dp)) {
                            VerificationItem("GPS activado")
                            VerificationItem("NFC habilitado")
                            VerificationItem("Conexión a internet estable")
                            VerificationItem("UserId del guardia confirmado")
                            VerificationItem("Instalación seleccionada y checkpoints cargados")
                            VerificationItem("Batería suficiente (>30%)")
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun InstallationCard(name: String, distance: String, checkpoints: String, onStartRound: () -> Unit) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(8.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        border = BorderStroke(1.dp, Color(0xFFE5E7EB)),
        elevation = CardDefaults.cardElevation(defaultElevation = 0.dp)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Box(modifier = Modifier.size(40.dp).background(PrimaryColor.copy(alpha=0.1f), CircleShape), contentAlignment = Alignment.Center) {
                        Icon(Icons.Default.LocationOn, contentDescription = null, tint = PrimaryColor)
                    }
                    Spacer(modifier = Modifier.width(12.dp))
                    Column {
                        Text(name, color = TextPrimary, fontSize = 16.sp, fontWeight = FontWeight.Bold)
                        Spacer(modifier = Modifier.height(4.dp))
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(Icons.Default.LocationOn, contentDescription = null, tint = DangerColor, modifier = Modifier.size(12.dp))
                            Spacer(modifier = Modifier.width(4.dp))
                            Text(distance, color = TextSecondary, fontSize = 12.sp)
                            Spacer(modifier = Modifier.width(8.dp))
                            Icon(Icons.Default.Wifi, contentDescription = null, tint = TextSecondary, modifier = Modifier.size(12.dp))
                            Spacer(modifier = Modifier.width(4.dp))
                            Text(checkpoints, color = TextSecondary, fontSize = 12.sp)
                        }
                    }
                }
                Icon(Icons.AutoMirrored.Filled.KeyboardArrowRight, contentDescription = null, tint = TextSecondary)
            }
            Spacer(modifier = Modifier.height(16.dp))
            Button(
                onClick = onStartRound,
                modifier = Modifier.fillMaxWidth().height(44.dp),
                colors = ButtonDefaults.buttonColors(containerColor = SuccessColor),
                shape = RoundedCornerShape(8.dp)
            ) {
                Text("Iniciar Jornada y Ronda", color = Color.White, fontSize = 14.sp, fontWeight = FontWeight.Bold)
            }
        }
    }
}

@Composable
fun VerificationItem(text: String) {
    Row(verticalAlignment = Alignment.CenterVertically) {
        Box(modifier = Modifier.size(4.dp).background(Color(0xFF92400E), CircleShape))
        Spacer(modifier = Modifier.width(8.dp))
        Text(text, color = Color(0xFF92400E), fontSize = 13.sp)
    }
}
