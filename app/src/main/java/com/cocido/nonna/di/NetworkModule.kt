package com.cocido.nonna.di

import com.cocido.nonna.data.remote.ArbolFamiliarApi
import com.cocido.nonna.data.remote.AuthApi
import com.cocido.nonna.data.remote.AuthInterceptor
import com.cocido.nonna.data.remote.CofreRecuerdosApi
import com.cocido.nonna.data.remote.EmocionesApi
import com.cocido.nonna.data.remote.NotificationsApi
import com.cocido.nonna.data.remote.PagosSuscripcionApi
import com.cocido.nonna.data.remote.PlanesApi
import com.cocido.nonna.data.remote.RecuerdosApi
import com.cocido.nonna.data.remote.SuscripcionApi
import com.cocido.nonna.data.remote.UsuarioApi
import com.cocido.nonna.BuildConfig
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

// Sin /api: auth va a .../auth/signup. Si cofres/recuerdos usan /api, avisá y usamos base distinta para auth.
private const val BASE_URL = "https://apinonna.pushsoftware.com.ar/"

@Module
@InstallIn(SingletonComponent::class)
object NetworkModule {

    @Provides
    @Singleton
    fun provideOkHttpClient(authInterceptor: AuthInterceptor): OkHttpClient {
        val logging = HttpLoggingInterceptor().apply {
            level = if (BuildConfig.DEBUG) {
                HttpLoggingInterceptor.Level.BASIC
            } else {
                HttpLoggingInterceptor.Level.NONE
            }
        }
        return OkHttpClient.Builder()
            .addInterceptor(authInterceptor)
            .addInterceptor(logging)
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
    fun provideAuthApi(retrofit: Retrofit): AuthApi = retrofit.create(AuthApi::class.java)

    @Provides
    @Singleton
    fun provideUsuarioApi(retrofit: Retrofit): UsuarioApi = retrofit.create(UsuarioApi::class.java)

    @Provides
    @Singleton
    fun provideCofreRecuerdosApi(retrofit: Retrofit): CofreRecuerdosApi =
        retrofit.create(CofreRecuerdosApi::class.java)

    @Provides
    @Singleton
    fun provideRecuerdosApi(retrofit: Retrofit): RecuerdosApi =
        retrofit.create(RecuerdosApi::class.java)

    @Provides
    @Singleton
    fun provideEmocionesApi(retrofit: Retrofit): EmocionesApi =
        retrofit.create(EmocionesApi::class.java)

    @Provides
    @Singleton
    fun provideArbolFamiliarApi(retrofit: Retrofit): ArbolFamiliarApi =
        retrofit.create(ArbolFamiliarApi::class.java)

    @Provides
    @Singleton
    fun provideSuscripcionApi(retrofit: Retrofit): SuscripcionApi =
        retrofit.create(SuscripcionApi::class.java)

    @Provides
    @Singleton
    fun providePlanesApi(retrofit: Retrofit): PlanesApi =
        retrofit.create(PlanesApi::class.java)

    @Provides
    @Singleton
    fun providePagosSuscripcionApi(retrofit: Retrofit): PagosSuscripcionApi =
        retrofit.create(PagosSuscripcionApi::class.java)

    @Provides
    @Singleton
    fun provideNotificationsApi(retrofit: Retrofit): NotificationsApi =
        retrofit.create(NotificationsApi::class.java)
}
