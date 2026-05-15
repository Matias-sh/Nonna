package com.cocido.nonna.di

import android.content.Context
import android.content.SharedPreferences
import com.cocido.nonna.notifications.FirebasePushTokenProvider
import com.cocido.nonna.notifications.PushTokenProvider
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

/**
 * Módulo de Dagger Hilt para dependencias de aplicación.
 */
@Module
@InstallIn(SingletonComponent::class)
object AppModule {
    
    @Provides
    @Singleton
    fun provideSharedPreferences(@ApplicationContext context: Context): SharedPreferences {
        return context.getSharedPreferences("nonna_prefs", Context.MODE_PRIVATE)
    }

    @Provides
    @Singleton
    fun providePushTokenProvider(
        impl: FirebasePushTokenProvider
    ): PushTokenProvider = impl
}
