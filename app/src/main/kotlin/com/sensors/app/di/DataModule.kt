package com.sensors.app.di

import android.content.Context
import com.sensors.data.repository.MainRepositoryImpl
import com.sensors.domain.repository.MainRepository
import dagger.Module
import dagger.Provides
import javax.inject.Singleton

@Module
class DataModule(private val context: Context) {

    @Provides
    @Singleton
    fun provideMainRepository(): MainRepository {
        return MainRepositoryImpl(context)
    }
}