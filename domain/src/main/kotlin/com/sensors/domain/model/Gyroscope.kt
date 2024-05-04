package com.sensors.domain.model

import java.util.Date

data class Gyroscope(
    val timestamp: Date = Date(System.currentTimeMillis()),
    val x: Float,
    val y: Float,
    val z: Float
)
