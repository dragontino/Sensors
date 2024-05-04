package com.sensors.app.app

import android.app.Application
import com.sensors.app.BuildConfig
import com.sensors.app.di.AppComponent
import com.sensors.app.di.AppModule
import com.sensors.app.di.DaggerAppComponent
import com.sensors.app.di.DataModule
import com.yandex.mapkit.MapKitFactory

class App : Application() {
    val appComponent: AppComponent by lazy {
        DaggerAppComponent.builder()
            .appModule(AppModule(application = this))
            .dataModule(DataModule(context = this))
            .build()
    }

    override fun onCreate() {
        super.onCreate()
        MapKitFactory.setApiKey(BuildConfig.MAPKIT_API_KEY)
    }
}