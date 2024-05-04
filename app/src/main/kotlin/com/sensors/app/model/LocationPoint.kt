package com.sensors.app.model

import android.location.Location
import android.os.Parcelable
import com.yandex.mapkit.geometry.Point
import kotlinx.parcelize.Parcelize

@Parcelize
data class LocationPoint(val latitude: Double, val longitude: Double, val azimuth: Float) : Parcelable {
    constructor(location: Location) : this(
        latitude = location.latitude,
        longitude = location.longitude,
        azimuth = if (location.hasBearing()) location.bearing else 0f
    )

    fun asMapPoint() = Point(latitude, longitude)
}