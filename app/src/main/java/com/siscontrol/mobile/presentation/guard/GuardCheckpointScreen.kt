package com.siscontrol.mobile.presentation.guard

import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Wifi
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.siscontrol.mobile.core.NfcTagReader
import com.siscontrol.mobile.presentation.theme.*

@Composable
fun GuardCheckpointScreen(
    checkpointName: String = "Checkpoint NFC",
    checkpointNumber: Int = 1,
    viewModel: GuardFlowViewModel,
    onScanSuccess: () -> Unit
) {
    val state by viewModel.uiState.collectAsState()
    var manualNfcCode by rememberSaveable { mutableStateOf("") }

    LaunchedEffect(Unit) {
        NfcTagReader.tagCodes.collect { tagCode ->
            manualNfcCode = tagCode
            viewModel.registerNfcScan(tagCode)
        }
    }

    LaunchedEffect(state) {
        if (state is GuardFlowUiState.ScanSuccess) onScanSuccess()
    }

    val infiniteTransition = rememberInfiniteTransition(label = "nfc_pulse")
    val outerScale by infiniteTransition.animateFloat(
        initialValue = 1f,
        targetValue = 1.25f,
        animationSpec = infiniteRepeatable(
            animation = tween(1600, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "outer_scale"
    )
    val outerAlpha by infiniteTransition.animateFloat(
        initialValue = 0.35f,
        targetValue = 0.12f,
        animationSpec = infiniteRepeatable(
            animation = tween(1600, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "outer_alpha"
    )

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(Color.White)
    ) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .background(PrimaryColor)
                .statusBarsPadding()
                .padding(horizontal = 16.dp, vertical = 16.dp)
        ) {
            Column {
                Text("Escanear Checkpoint", color = Color.White, fontSize = 20.sp, fontWeight = FontWeight.Bold)
                Text("Checkpoint $checkpointNumber: $checkpointName", color = Color.White.copy(alpha = 0.85f), fontSize = 14.sp)
            }
        }

        Column(
            modifier = Modifier
                .weight(1f)
                .fillMaxWidth(),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            Box(contentAlignment = Alignment.Center, modifier = Modifier.size(220.dp)) {
                Box(
                    modifier = Modifier
                        .size(190.dp)
                        .scale(outerScale)
                        .background(Color(0xFFBFDBFE).copy(alpha = outerAlpha), CircleShape)
                )
                Box(
                    modifier = Modifier
                        .size(150.dp)
                        .background(Color(0xFF93C5FD).copy(alpha = 0.25f), CircleShape)
                )
                Box(
                    modifier = Modifier
                        .size(110.dp)
                        .background(Color(0xFF93C5FD).copy(alpha = 0.45f), CircleShape),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(Icons.Default.Wifi, contentDescription = "NFC", tint = PrimaryColor, modifier = Modifier.size(54.dp))
                }
            }

            Spacer(modifier = Modifier.height(24.dp))
            Text("Acerca tu dispositivo", fontSize = 22.sp, fontWeight = FontWeight.Bold, color = TextPrimary)
            Spacer(modifier = Modifier.height(8.dp))
            Text(
                "La app cruza el NFC con checkpoints y registra en POST /api/rondas/escaneo.",
                fontSize = 14.sp,
                color = TextSecondary,
                textAlign = TextAlign.Center,
                modifier = Modifier.padding(horizontal = 40.dp)
            )

            Spacer(modifier = Modifier.height(24.dp))
            OutlinedTextField(
                value = manualNfcCode,
                onValueChange = { manualNfcCode = it },
                label = { Text("Código NFC leído") },
                singleLine = true,
                modifier = Modifier.fillMaxWidth().padding(horizontal = 24.dp)
            )

            Spacer(modifier = Modifier.height(12.dp))
            Button(
                onClick = { viewModel.registerNfcScan(manualNfcCode) },
                modifier = Modifier.fillMaxWidth().padding(horizontal = 24.dp).height(52.dp),
                enabled = state !is GuardFlowUiState.Loading,
                colors = ButtonDefaults.buttonColors(containerColor = SuccessColor),
                shape = RoundedCornerShape(8.dp)
            ) {
                Text("Enviar escaneo NFC", color = Color.White, fontWeight = FontWeight.Bold, fontSize = 16.sp)
            }

            Spacer(modifier = Modifier.height(12.dp))
            when (state) {
                is GuardFlowUiState.Loading -> CircularProgressIndicator(color = PrimaryColor)
                is GuardFlowUiState.Error -> Text(
                    text = (state as GuardFlowUiState.Error).message,
                    color = DangerColor,
                    textAlign = TextAlign.Center,
                    modifier = Modifier.padding(horizontal = 24.dp)
                )
                is GuardFlowUiState.ScanSuccess -> Text(
                    text = (state as GuardFlowUiState.ScanSuccess).message,
                    color = SuccessColor,
                    fontWeight = FontWeight.Medium,
                    textAlign = TextAlign.Center,
                    modifier = Modifier.padding(horizontal = 24.dp)
                )
                else -> Text(
                    "El NFC debe estar activado. También puedes pegar el código para pruebas.",
                    color = Color(0xFF854D0E),
                    fontSize = 13.sp,
                    textAlign = TextAlign.Center,
                    modifier = Modifier.padding(horizontal = 24.dp)
                )
            }
        }

        Spacer(modifier = Modifier.navigationBarsPadding())
    }
}
