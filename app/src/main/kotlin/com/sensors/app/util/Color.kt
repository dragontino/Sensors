package com.sensors.app.util

import android.graphics.Color
import androidx.annotation.ColorInt

@get:ColorInt
val Int.reversedColor: Int get() = asColor().reversed.toArgb()

fun Int.asColor(): Color = Color.valueOf(this)

val Color.reversed: Color get() = Color.valueOf(
    /* r = */ 1 - red(),
    /* g = */ 1 - blue(),
    /* b = */ 1 - green(),
    /* a = */ alpha()
)