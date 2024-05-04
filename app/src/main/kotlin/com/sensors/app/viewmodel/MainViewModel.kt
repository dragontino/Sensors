package com.sensors.app.viewmodel

import android.app.Application
import android.graphics.Typeface
import android.location.Location
import android.os.Build
import android.text.Spannable
import android.text.SpannableString
import android.text.style.TypefaceSpan
import android.util.Log
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.google.android.gms.location.LocationCallback
import com.google.android.gms.location.LocationRequest
import com.google.android.gms.location.LocationResult
import com.sensors.app.R
import com.sensors.app.app.App
import com.sensors.domain.model.Accelerometer
import com.sensors.domain.model.Cell
import com.sensors.domain.model.Gyroscope
import com.sensors.domain.repository.MainRepository
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.async
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.util.Date
import javax.inject.Inject

class MainViewModel @Inject constructor(
    application: Application,
    private val repository: MainRepository
) : AndroidViewModel(application) {

    val deniedPermissions = mutableSetOf<String>()

    var accelerometerData: Accelerometer? = null

    var gyroscopeData: Gyroscope? = null
    var location: Location? = null
        set(value) {
            when (null) {
                value -> return
                field -> field = value
                else -> field?.set(value)
            }
        }
    var cellData: Cell? = null


    var onLocationUpdated: (() -> Unit)? = null

    val locationRequest = LocationRequest.Builder(10_000L)
        .setPriority(100)
        .build()

    val locationCallback: LocationCallback = object : LocationCallback() {
        override fun onLocationResult(locationResult: LocationResult) {
            val location = with(locationResult.locations) {
                when {
                    Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU -> firstOrNull { it.isComplete }
                    else -> firstOrNull()
                }
            }
            this@MainViewModel.location = location
            onLocationUpdated?.invoke()
        }
    }

    var locationUpdatesStarted = false
    var cellUpdatesStarted = false


    val accelerometerText: SpannableString?
        get() = accelerometerData?.let {
            val text = getApplication<App>().getString(
                R.string.accelerometer_data,
                it.x, it.y, it.z
            )
            val colonIndex = text.indexOf(':').coerceAtLeast(0)
            text.trimIndent().formatAsBold(endInclusive = colonIndex)
        }


    val gyroscopeText: SpannableString?
        get() = gyroscopeData?.let {
            val resourcesText = getApplication<App>().getString(
                R.string.gyroscope_data,
                it.x, it.y, it.z
            )
            val colonIndex = resourcesText.indexOf(':').coerceAtLeast(0)
            return resourcesText.trimIndent().formatAsBold(endInclusive = colonIndex)
        }


    val locationText: SpannableString?
        get() = location?.let {
            val resourcesText = getApplication<App>().getString(
                R.string.location_text,
                it.latitude,
                it.longitude,
                it.altitude,
                it.provider,
                Date(it.time),
            )
            val colonIndex = resourcesText.indexOf(':')
            resourcesText.trimIndent().formatAsBold(endInclusive = colonIndex)
        }


    val cellText: SpannableString?
        get() = cellData?.let {
            val resourcesText = getApplication<App>().getString(
                R.string.cell_text,
                it.type.name,
                it.id,
                it.mobileNetworkOperator,
                it.signalStrengthIndicator,
                it.locationAreaCode,
                it.timestamp
            )
            val colonIndex = resourcesText.indexOf(":")
            resourcesText.trimIndent().formatAsBold(endInclusive = colonIndex)
        }


    private fun String.formatAsBold(
        start: Int = 0,
        endInclusive: Int = lastIndex
    ) = SpannableString(this).apply {
        setSpan(
            TypefaceSpan(Typeface.DEFAULT_BOLD),
            start, endInclusive + 1,
            Spannable.SPAN_EXCLUSIVE_EXCLUSIVE
        )
    }


    fun writeAccelerometerData(
        onSuccess: (path: String) -> Unit = {},
        onFailure: (String) -> Unit = {}
    ) {
        viewModelScope.launch {
            accelerometerData
                ?.let { accelerometer ->
                    withContext(Dispatchers.IO) { repository.writeAccelerometerInfo(accelerometer) }
                }
                ?.handle(onSuccess = onSuccess, onFailure = onFailure)
        }
    }

    fun writeGyroscopeData(
        onSuccess: (path: String) -> Unit = {},
        onFailure: (String) -> Unit = {}
    ) {
        viewModelScope.launch {
            gyroscopeData
                ?.let { gyroscope ->
                    withContext(Dispatchers.IO) { repository.writeGyroscopeInfo(gyroscope) }
                }
                ?.handle(onSuccess, onFailure)
        }
    }

    fun writeLocationData(
        onSuccess: (path: String) -> Unit = {},
        onFailure: (String) -> Unit = {}
    ) {
        viewModelScope.launch {
            location
                ?.let {
                    withContext(Dispatchers.IO) { repository.writeLocationInfo(Location(it)) }
                }
                ?.handle(onSuccess, onFailure)
        }
    }

    fun writeCellData(onSuccess: (path: String) -> Unit = {}, onFailure: (String) -> Unit = {}) {
        viewModelScope.launch {
            cellData
                ?.let { cell ->
                    repository.writeCellInfo(cell)
                }
                ?.handle(onSuccess, onFailure)
        }
    }

    fun writeAllData(
        onSuccess: (path: String) -> Unit = {},
        onFailure: (String) -> Unit = {}
    ) {
        viewModelScope.launch {
            val accelerometerAsync = async(Dispatchers.IO) {
                accelerometerData?.let { repository.writeAccelerometerInfo(it) }
            }
            val gyroscopeAsync = async(Dispatchers.IO) {
                gyroscopeData?.let { repository.writeGyroscopeInfo(it) }
            }
            val locationAsync = async(Dispatchers.IO) {
                location?.let { repository.writeLocationInfo(Location(it)) }
            }
            val cellAsync = async(Dispatchers.IO) {
                cellData?.let { repository.writeCellInfo(it) }
            }

            arrayOf(
                accelerometerAsync.await(),
                gyroscopeAsync.await(),
                locationAsync.await(),
                cellAsync.await()
            ).map { it?.handle(onSuccess, onFailure) }
        }
    }


    private fun Result<String>.handle(
        onSuccess: (path: String) -> Unit,
        onFailure: (String) -> Unit
    ) {
        onSuccess {
            Log.i(TAG, "file path = $it")
            onSuccess(it)
        }
        onFailure {
            Log.e("MainViewModel", it.localizedMessage, it)
            onFailure(it.localizedMessage ?: it.message ?: it.toString())
        }
    }


    private fun Location(androidLocation: Location) = com.sensors.domain.model.Location(
        timestamp = Date(androidLocation.time),
        provider = androidLocation.provider,
        latitude = androidLocation.latitude,
        longitude = androidLocation.longitude,
        altitude = androidLocation.altitude
    )


    private companion object {
        const val TAG = "MainViewModel"
    }
}