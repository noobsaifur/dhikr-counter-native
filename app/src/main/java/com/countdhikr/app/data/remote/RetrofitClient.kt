package com.countdhikr.app.data.remote

import com.google.gson.GsonBuilder
import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import java.util.concurrent.TimeUnit

/**
 * Singleton that provides [Retrofit] instances for the two external APIs
 * the app depends on: Aladhan (prayer times) and BigDataCloud (geocoding).
 */
object RetrofitClient {

    // ── Base URLs ────────────────────────────────────────────────────────────

    private const val ALADHAN_BASE_URL = "https://api.aladhan.com/"
    private const val GEOCODING_BASE_URL = "https://api.bigdatacloud.net/"

    // ── Shared OkHttp Client ─────────────────────────────────────────────────

    private val loggingInterceptor: HttpLoggingInterceptor by lazy {
        HttpLoggingInterceptor().apply {
            level = HttpLoggingInterceptor.Level.BODY
        }
    }

    private val okHttpClient: OkHttpClient by lazy {
        OkHttpClient.Builder()
            .addInterceptor(loggingInterceptor)
            .connectTimeout(30, TimeUnit.SECONDS)
            .readTimeout(30, TimeUnit.SECONDS)
            .writeTimeout(30, TimeUnit.SECONDS)
            .build()
    }

    private val gson by lazy {
        GsonBuilder()
            .setLenient()
            .create()
    }

    // ── Aladhan API ──────────────────────────────────────────────────────────

    private val aladhanRetrofit: Retrofit by lazy {
        Retrofit.Builder()
            .baseUrl(ALADHAN_BASE_URL)
            .client(okHttpClient)
            .addConverterFactory(GsonConverterFactory.create(gson))
            .build()
    }

    val aladhanApi: AladhanApi by lazy {
        aladhanRetrofit.create(AladhanApi::class.java)
    }

    // ── Geocoding API ────────────────────────────────────────────────────────

    private val geocodingRetrofit: Retrofit by lazy {
        Retrofit.Builder()
            .baseUrl(GEOCODING_BASE_URL)
            .client(okHttpClient)
            .addConverterFactory(GsonConverterFactory.create(gson))
            .build()
    }

    val geocodingApi: GeocodingApi by lazy {
        geocodingRetrofit.create(GeocodingApi::class.java)
    }
}
