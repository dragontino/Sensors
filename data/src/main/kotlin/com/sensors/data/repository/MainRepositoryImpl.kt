package com.sensors.data.repository

import android.content.Context
import com.sensors.domain.model.Accelerometer
import com.sensors.domain.model.Cell
import com.sensors.domain.model.Gyroscope
import com.sensors.domain.model.Location
import com.sensors.domain.repository.MainRepository
import java.io.FileWriter
import java.io.IOException
import java.util.UUID
import kotlin.io.path.Path
import kotlin.io.path.absolutePathString
import kotlin.io.path.createParentDirectories
import kotlin.io.path.exists
import kotlin.reflect.KCallable

class MainRepositoryImpl(private val context: Context) : MainRepository {
    private companion object {
        const val UID_COLUMN_NAME = "Uid"
        const val DATA_FILES_DIR = "datafiles"
    }

    private enum class FileTypes {
        Accelerometer,
        Gyroscope,
        Location,
        Cell;

        val fileName: String = "${name.lowercase()}-data.csv"
    }

    override suspend fun writeAccelerometerInfo(accelerometer: Accelerometer): Result<String> {
        return writeToCsvFile(
            fileType = FileTypes.Accelerometer,
            createHeader = {
                with(accelerometer) { createCSVHeader(::timestamp, ::x, ::y, ::z) }
            },
            getContent = {
                with(accelerometer) { createCsvLine(timestamp, x, y, z) }
            }
        )
    }

    override suspend fun writeGyroscopeInfo(gyroscope: Gyroscope): Result<String> {
        return writeToCsvFile(
            fileType = FileTypes.Gyroscope,
            createHeader = {
                with(gyroscope) { createCSVHeader(::timestamp, ::x, ::y, ::z) }
            },
            getContent = {
                with(gyroscope) { createCsvLine(timestamp, x, y, z) }
            }
        )
    }

    override suspend fun writeLocationInfo(location: Location): Result<String> {
        return writeToCsvFile(
            fileType = FileTypes.Location,
            createHeader = {
                with(location) {
                    createCSVHeader(::timestamp, ::provider, ::latitude, ::longitude, ::altitude)
                }
            },
            getContent = {
                with(location) {
                    createCsvLine(timestamp, provider.toString(), latitude, longitude, altitude)
                }
            }
        )
    }

    override suspend fun writeCellInfo(cell: Cell): Result<String> {
        return writeToCsvFile(
            fileType = FileTypes.Cell,
            createHeader = {
                with(cell) {
                    createCSVHeader(
                        ::timestamp,
                        ::type,
                        ::id,
                        ::registered,
                        ::mobileNetworkOperator,
                        ::signalStrengthIndicator,
                        ::locationAreaCode
                    )
                }
            },
            getContent = {
                with(cell) {
                    createCsvLine(
                        timestamp,
                        type,
                        id,
                        registered,
                        mobileNetworkOperator.toString(),
                        signalStrengthIndicator,
                        locationAreaCode
                    )
                }
            }
        )
    }


    private inline fun writeToCsvFile(
        fileType: FileTypes,
        createHeader: () -> CharSequence,
        getContent: () -> CharSequence
    ): Result<String> {
        val dataPath = Path(context.filesDir.absolutePath, DATA_FILES_DIR, fileType.fileName)
        val isNew = !dataPath.exists()
        if (isNew) {
            dataPath.createParentDirectories()
        }
        return try {
            FileWriter(dataPath.toFile(), true).use {
                if (isNew) {
                    it.append(createHeader())
                }

                it.append(getContent())
                Result.success(dataPath.absolutePathString())
            }
        } catch (e: IOException) {
            return Result.failure(e)
        }
    }



    private fun createCSVHeader(vararg columnNames: KCallable<*>): String {
        val columnNamesTitled = columnNames.map { it.name.titleCase() }
        val uidColumn = arrayOf(UID_COLUMN_NAME)
        return (uidColumn + columnNamesTitled)
            .joinToString(separator = ",", postfix = "\n")
    }

    private fun createCsvLine(
        vararg values: Any,
        uid: UUID = UUID.randomUUID()
    ): String {
        val stringValues = values.map { it.toString() }
        return (arrayOf(uid.toString()) + stringValues)
            .joinToString(separator = ",", postfix = "\n")
    }



    private fun String.titleCase() =
        this[0].titlecase() + slice(1 ..< length)
}