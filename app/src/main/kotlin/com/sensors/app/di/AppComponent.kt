package com.sensors.app.di

import com.sensors.app.viewmodel.MainViewModel
import com.sensors.app.viewmodel.MapsViewModel
import dagger.Component
import javax.inject.Singleton

@Singleton
@Component(modules = [DataModule::class, AppModule::class])
interface AppComponent {
    fun mainViewModel(): MainViewModel

    fun mapsViewModel(): MapsViewModel
}