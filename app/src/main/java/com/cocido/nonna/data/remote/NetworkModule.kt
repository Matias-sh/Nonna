package com.cocido.nonna.data.remote

import com.cocido.nonna.data.remote.api.AuthApiService
import com.cocido.nonna.data.remote.api.MemoryApiService
import com.cocido.nonna.data.remote.api.PersonApiService
import com.cocido.nonna.data.remote.api.VaultApiService
import com.cocido.nonna.data.remote.interceptor.AuthInterceptor
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import java.util.concurrent.TimeUnit
import javax.inject.Singleton

/**
 * Módulo de Dagger Hilt para configuración de red
 */
@Module
@InstallIn(SingletonComponent::class)
object NetworkModule {
    
    private const val BASE_URL = "http://10.0.2.2:8000/api/" // Para emulador Android
    
    @Provides
    @Singleton
    fun provideOkHttpClient(
        authInterceptor: AuthInterceptor
    ): OkHttpClient {
        val loggingInterceptor = HttpLoggingInterceptor().apply {
            level = HttpLoggingInterceptor.Level.BODY
        }
        
        return OkHttpClient.Builder()
            .addInterceptor(authInterceptor)
            .addInterceptor(loggingInterceptor)
            .connectTimeout(30, TimeUnit.SECONDS)
            .readTimeout(30, TimeUnit.SECONDS)
            .writeTimeout(30, TimeUnit.SECONDS)
            .build()
    }
    
    @Provides
    @Singleton
    fun provideRetrofit(okHttpClient: OkHttpClient): Retrofit {
        return Retrofit.Builder()
            .baseUrl(BASE_URL)
            .client(okHttpClient)
            .addConverterFactory(GsonConverterFactory.create())
            .build()
    }
    
    @Provides
    @Singleton
    fun provideMemoryApiService(retrofit: Retrofit): MemoryApiService {
        return retrofit.create(MemoryApiService::class.java)
    }
    
    @Provides
    @Singleton
    fun provideVaultApiService(retrofit: Retrofit): VaultApiService {
        return retrofit.create(VaultApiService::class.java)
    }
    
    @Provides
    @Singleton
    fun provideAuthApiService(retrofit: Retrofit): AuthApiService {
        return retrofit.create(AuthApiService::class.java)
    }
    
    @Provides
    @Singleton
    fun providePersonApiService(retrofit: Retrofit): PersonApiService {
        return retrofit.create(PersonApiService::class.java)
    }
}
