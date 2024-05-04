package com.sensors.domain.repository

import com.sensors.domain.model.Accelerometer
import com.sensors.domain.model.Cell
import com.sensors.domain.model.Gyroscope
import com.sensors.domain.model.Location

interface MainRepository {
    suspend fun writeAccelerometerInfo(accelerometer: Accelerometer): Result<String>

    suspend fun writeGyroscopeInfo(gyroscope: Gyroscope): Result<String>

    suspend fun writeLocationInfo(location: Location): Result<String>

    suspend fun writeCellInfo(cell: Cell): Result<String>
}