package com.example.health

data class ExerciseUiState(
    val isSupported: Boolean = true,
    val isRunning: Boolean = false,
    val isPaused: Boolean = false,
    val elapsedSeconds: Long = 0L,
    val heartRateBpm: Int = 0,
    val calories: Double = 0.0,
    val distanceKm: Double = 0.0,
    val steps: Long = 0L,
    val errorMessage: String? = null
)

