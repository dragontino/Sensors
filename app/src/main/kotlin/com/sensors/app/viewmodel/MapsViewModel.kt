package com.sensors.app.viewmodel

import android.app.Application
import android.content.res.Configuration
import android.os.Build
import androidx.lifecycle.AndroidViewModel
import com.sensors.app.app.App
import javax.inject.Inject

class MapsViewModel @Inject constructor(
    application: Application
) : AndroidViewModel(application) {
    val isDarkTheme: Boolean
        get() {
            with(getApplication<App>().resources.configuration) {
                return when {
                    Build.VERSION.SDK_INT >= Build.VERSION_CODES.R -> isNightModeActive
                    else -> uiMode and Configuration.UI_MODE_NIGHT_MASK == Configuration.UI_MODE_NIGHT_YES
                }
            }
        }
}