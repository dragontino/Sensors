package com.sensors.domain.model

import java.util.Date

data class Location(
    val timestamp: Date = Date(System.currentTimeMillis()),
    val provider: String?,
    val latitude: Double,
    val longitude: Double,
    val altitude: Double
)
