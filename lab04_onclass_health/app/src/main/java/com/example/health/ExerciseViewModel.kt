package com.example.health

import android.app.Application
import android.content.Intent
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import androidx.health.services.client.ExerciseClient
import androidx.health.services.client.ExerciseUpdateCallback
import androidx.health.services.client.HealthServices
import androidx.health.services.client.data.DataType
import androidx.health.services.client.data.ExerciseConfig
import androidx.health.services.client.data.ExerciseLapSummary
import androidx.health.services.client.data.ExerciseType
import androidx.health.services.client.data.ExerciseUpdate
import androidx.health.services.client.data.Availability
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.guava.await
import kotlinx.coroutines.launch
import androidx.core.content.ContextCompat

class ExerciseViewModel(application: Application) : AndroidViewModel(application) {

    private val exerciseClient: ExerciseClient = HealthServices.getClient(application).exerciseClient

    private val _uiState = MutableStateFlow(ExerciseUiState())
    val uiState: StateFlow<ExerciseUiState> = _uiState.asStateFlow()

    private var timerJob: Job? = null

    private val callback = object : ExerciseUpdateCallback {
        override fun onAvailabilityChanged(dataType: DataType<*, *>, availability: Availability) = Unit

        override fun onRegistered() = Unit

        override fun onRegistrationFailed(throwable: Throwable) {
            _uiState.update {
                it.copy(errorMessage = throwable.message ?: "Callback registration failed")
            }
        }

        override fun onLapSummaryReceived(lapSummary: ExerciseLapSummary) = Unit

        override fun onExerciseUpdateReceived(update: ExerciseUpdate) {
            val metrics = update.latestMetrics
            val heartRate = (metrics.sampleDataPoints.lastOrNull { it.dataType == DataType.HEART_RATE_BPM }?.value as? Number)?.toInt() ?: 0
            val calories = (metrics.cumulativeDataPoints.lastOrNull { it.dataType == DataType.CALORIES_TOTAL }?.total as? Number)?.toDouble() ?: 0.0
            val distanceMeters = (metrics.cumulativeDataPoints.lastOrNull { it.dataType == DataType.DISTANCE_TOTAL }?.total as? Number)?.toDouble() ?: 0.0
            val steps = (metrics.cumulativeDataPoints.lastOrNull { it.dataType == DataType.STEPS_TOTAL }?.total as? Number)?.toLong() ?: 0L

            _uiState.update {
                it.copy(
                    heartRateBpm = heartRate,
                    calories = calories,
                    distanceKm = distanceMeters / 1000.0,
                    steps = steps
                )
            }
        }
    }

    init {
        viewModelScope.launch {
            checkAvailability()
            registerUpdates()
        }
    }

    private suspend fun checkAvailability() {
        runCatching {
            val capabilities = exerciseClient.getCapabilitiesAsync().await()
            val supported = capabilities.supportedExerciseTypes.contains(ExerciseType.RUNNING)
            _uiState.update {
                it.copy(
                    isSupported = supported,
                    errorMessage = if (supported) null else "Exercise not available on this device"
                )
            }
        }.onFailure {
            _uiState.update { state ->
                state.copy(
                    isSupported = false,
                    errorMessage = "Exercise not available on this device"
                )
            }
        }
    }

    private suspend fun registerUpdates() {
        runCatching {
            exerciseClient.setUpdateCallback(callback)
        }
    }

    fun toggleStartEnd() {
        if (!_uiState.value.isSupported) return

        if (_uiState.value.isRunning) {
            endExercise()
        } else {
            startExercise()
        }
    }

    fun togglePauseResume() {
        if (!_uiState.value.isRunning) return

        if (_uiState.value.isPaused) {
            resumeExercise()
        } else {
            pauseExercise()
        }
    }

    private fun startExercise() {
        viewModelScope.launch {
            runCatching {
                val config = ExerciseConfig.builder(ExerciseType.RUNNING)
                    .setDataTypes(
                        setOf(
                            DataType.HEART_RATE_BPM,
                            DataType.CALORIES_TOTAL,
                            DataType.DISTANCE_TOTAL,
                            DataType.STEPS_TOTAL
                        )
                    )
                    .build()

                exerciseClient.startExerciseAsync(config).await()

                _uiState.update {
                    it.copy(isRunning = true, isPaused = false, elapsedSeconds = 0L, errorMessage = null)
                }

                startTimer()
                startOngoingService()
            }.onFailure {
                _uiState.update { state -> state.copy(errorMessage = it.message ?: "Failed to start exercise") }
            }
        }
    }

    private fun pauseExercise() {
        viewModelScope.launch {
            runCatching {
                exerciseClient.pauseExerciseAsync().await()
                _uiState.update { it.copy(isPaused = true) }
                stopTimer()
            }.onFailure {
                _uiState.update { state -> state.copy(errorMessage = it.message ?: "Failed to pause exercise") }
            }
        }
    }

    private fun resumeExercise() {
        viewModelScope.launch {
            runCatching {
                exerciseClient.resumeExerciseAsync().await()
                _uiState.update { it.copy(isPaused = false) }
                startTimer()
            }.onFailure {
                _uiState.update { state -> state.copy(errorMessage = it.message ?: "Failed to resume exercise") }
            }
        }
    }

    private fun endExercise() {
        viewModelScope.launch {
            runCatching {
                exerciseClient.endExerciseAsync().await()
                _uiState.update {
                    it.copy(isRunning = false, isPaused = false, elapsedSeconds = 0L)
                }
                stopTimer()
                stopOngoingService()
            }.onFailure {
                _uiState.update { state -> state.copy(errorMessage = it.message ?: "Failed to end exercise") }
            }
        }
    }

    private fun startTimer() {
        timerJob?.cancel()
        timerJob = viewModelScope.launch {
            while (true) {
                delay(1000)
                _uiState.update { state ->
                    if (state.isRunning && !state.isPaused) {
                        state.copy(elapsedSeconds = state.elapsedSeconds + 1)
                    } else {
                        state
                    }
                }
            }
        }
    }

    private fun stopTimer() {
        timerJob?.cancel()
        timerJob = null
    }

    private fun startOngoingService() {
        val context = getApplication<Application>()
        val intent = Intent(context, OngoingExerciseService::class.java).apply {
            action = OngoingExerciseService.ACTION_START
        }
        ContextCompat.startForegroundService(context, intent)
    }

    private fun stopOngoingService() {
        val context = getApplication<Application>()
        val intent = Intent(context, OngoingExerciseService::class.java).apply {
            action = OngoingExerciseService.ACTION_STOP
        }
        context.startService(intent)
    }

    override fun onCleared() {
        super.onCleared()
        stopTimer()
        viewModelScope.launch {
            runCatching { exerciseClient.clearUpdateCallbackAsync(callback).await() }
        }
    }
}

