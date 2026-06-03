package com.example.health

import android.Manifest
import android.content.pm.PackageManager
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.result.contract.ActivityResultContracts
import androidx.activity.viewModels
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.content.ContextCompat
import androidx.wear.compose.material3.Button
import androidx.wear.compose.material3.ButtonDefaults
import androidx.wear.compose.material3.MaterialTheme
import androidx.wear.compose.material3.Text
import com.example.health.ui.theme.HealthTheme
import kotlin.math.max

class MainActivity : ComponentActivity() {
    private val viewModel: ExerciseViewModel by viewModels()

    private var hasAllPermissions by mutableStateOf(false)

    private val permissionLauncher = registerForActivityResult(
        ActivityResultContracts.RequestMultiplePermissions()
    ) { results ->
        hasAllPermissions = results.values.all { it }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        hasAllPermissions = hasRuntimePermissions()
        if (!hasAllPermissions) {
            permissionLauncher.launch(runtimePermissions())
        }

        setContent {
            HealthTheme {
                val uiState by viewModel.uiState.collectAsState()
                ExerciseScreen(
                    uiState = uiState,
                    hasPermissions = hasAllPermissions,
                    onToggleStartEnd = {
                        if (hasAllPermissions) {
                            viewModel.toggleStartEnd()
                        } else {
                            permissionLauncher.launch(runtimePermissions())
                        }
                    },
                    onPauseResume = {
                        if (hasAllPermissions) {
                            viewModel.togglePauseResume()
                        } else {
                            permissionLauncher.launch(runtimePermissions())
                        }
                    }
                )
            }
        }
    }

    private fun hasRuntimePermissions(): Boolean {
        return runtimePermissions().all { permission ->
            ContextCompat.checkSelfPermission(this, permission) == PackageManager.PERMISSION_GRANTED
        }
    }

    private fun runtimePermissions(): Array<String> {
        val permissions = mutableListOf(
            Manifest.permission.BODY_SENSORS,
            Manifest.permission.ACTIVITY_RECOGNITION
        )
        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.TIRAMISU) {
            permissions.add(Manifest.permission.POST_NOTIFICATIONS)
        }
        return permissions.toTypedArray()
    }
}

@Composable
fun ExerciseScreen(
    uiState: ExerciseUiState,
    hasPermissions: Boolean,
    onToggleStartEnd: () -> Unit,
    onPauseResume: () -> Unit
) {
    if (!uiState.isSupported) {
        ErrorScreen()
        return
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(10.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.SpaceBetween
    ) {
        TimerText(elapsedSeconds = uiState.elapsedSeconds)

        MetricsGrid(
            heartRate = uiState.heartRateBpm,
            calories = uiState.calories,
            distanceKm = uiState.distanceKm,
            steps = uiState.steps
        )

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.Center,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Button(
                onClick = onToggleStartEnd,
                colors = ButtonDefaults.buttonColors(
                    containerColor = Color(0xFFFF9800)
                ),
                modifier = Modifier.weight(1f)
            ) {
                Text(text = if (uiState.isRunning) "END" else "START")
            }

            Spacer(modifier = Modifier.width(8.dp))

            Button(
                onClick = onPauseResume,
                enabled = uiState.isRunning,
                colors = ButtonDefaults.buttonColors(
                    containerColor = Color.DarkGray
                ),
                modifier = Modifier.weight(1f)
            ) {
                Text(text = if (uiState.isPaused) "PAUSE" else "PAUSE")
            }
        }

        if (!hasPermissions) {
            Text(
                text = "Grant permissions to track health data",
                fontSize = 10.sp,
                textAlign = TextAlign.Center
            )
        }
    }
}

@Composable
private fun TimerText(elapsedSeconds: Long) {
    val minutes = max(0, elapsedSeconds / 60)
    val seconds = max(0, elapsedSeconds % 60)
    Text(
        text = String.format("%02d:%02d", minutes, seconds),
        style = MaterialTheme.typography.displaySmall,
        fontWeight = FontWeight.Bold
    )
}

@Composable
private fun MetricsGrid(
    heartRate: Int,
    calories: Double,
    distanceKm: Double,
    steps: Long
) {
    Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
        Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
            MetricTile(title = "HR", value = "$heartRate bpm", modifier = Modifier.weight(1f))
            MetricTile(title = "CAL", value = String.format("%.1f cal", calories), modifier = Modifier.weight(1f))
        }
        Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
            MetricTile(title = "DIST", value = String.format("%.2f km", distanceKm), modifier = Modifier.weight(1f))
            MetricTile(title = "STEPS", value = steps.toString(), modifier = Modifier.weight(1f))
        }
    }
}

@Composable
private fun MetricTile(title: String, value: String, modifier: Modifier = Modifier) {
    Column(
        modifier = modifier
            .background(color = Color(0xFF202124), shape = RoundedCornerShape(10.dp))
            .padding(8.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text(text = title, fontSize = 10.sp)
        Spacer(modifier = Modifier.height(2.dp))
        Text(text = value, fontSize = 12.sp, fontWeight = FontWeight.SemiBold, textAlign = TextAlign.Center)
    }
}

@Composable
private fun ErrorScreen() {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Text(text = "-", fontSize = 36.sp, fontWeight = FontWeight.Bold)
        Spacer(modifier = Modifier.height(8.dp))
        Text(
            text = "Exercise not available on this device",
            textAlign = TextAlign.Center,
            fontSize = 12.sp
        )
    }
}

@Preview
@Composable
fun ExerciseScreenPreview() {
    HealthTheme {
        ExerciseScreen(
            uiState = ExerciseUiState(isRunning = true, elapsedSeconds = 85, heartRateBpm = 104, calories = 22.4, distanceKm = 0.65, steps = 932),
            hasPermissions = true,
            onToggleStartEnd = {},
            onPauseResume = {}
        )
    }
}