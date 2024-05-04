package com.sensors.domain.model

import java.util.Date

data class Cell(
    val timestamp: Date = Date(System.currentTimeMillis()),
    val type: CellType,
    val id: String,
    val registered: Boolean,
    val mobileNetworkOperator: String?,
    val signalStrengthIndicator: String,
    val locationAreaCode: String
)
