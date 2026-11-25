package com.berlintransit.app.api

import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import java.util.concurrent.TimeUnit

object ApiClient {
    
    // Multiple API endpoints for redundancy
    // Primary: Official VBB API
    // Fallback 1: Alternative mirror
    // Fallback 2: Another mirror
    val API_ENDPOINTS = listOf(
        "https://v5.vbb.transport.rest/",
        "https://v5.bvg.transport.rest/",  // Same API, different provider
        "https://v5.db.transport.rest/"    // DB Navigator API (compatible)
    )
    
    private val loggingInterceptor = HttpLoggingInterceptor().apply {
        level = HttpLoggingInterceptor.Level.BODY
    }
    
    private val okHttpClient = OkHttpClient.Builder()
        .addInterceptor(loggingInterceptor)
        .addInterceptor { chain ->
            val request = chain.request().newBuilder()
                .header("User-Agent", "Mozilla/5.0 (Linux; Android 14) BerlinTransit/1.0")
                .header("Accept", "application/json")
                .header("Accept-Language", "de-DE,de;q=0.9,en;q=0.8")
                .build()
            chain.proceed(request)
        }
        .connectTimeout(60, TimeUnit.SECONDS)
        .readTimeout(60, TimeUnit.SECONDS)
        .writeTimeout(60, TimeUnit.SECONDS)
        .retryOnConnectionFailure(true)
        .build()
    
    // Create API service with primary endpoint
    private val retrofit = Retrofit.Builder()
        .baseUrl(API_ENDPOINTS[0])
        .client(okHttpClient)
        .addConverterFactory(GsonConverterFactory.create())
        .build()
    
    val vbbApiService: VbbApiService = retrofit.create(VbbApiService::class.java)
    
    /**
     * Get API service for a specific endpoint
     */
    fun getApiServiceForEndpoint(baseUrl: String): VbbApiService {
        val retrofit = Retrofit.Builder()
            .baseUrl(baseUrl)
            .client(okHttpClient)
            .addConverterFactory(GsonConverterFactory.create())
            .build()
        return retrofit.create(VbbApiService::class.java)
    }
}
