package com.berlintransit.app

import android.app.Application
import android.content.Context
import com.berlintransit.app.data.FavoritesStore
import com.berlintransit.app.data.TransitRepository
import com.berlintransit.app.data.api.ApiJson
import com.berlintransit.app.data.api.VbbApi
import com.berlintransit.app.location.LocationProvider
import com.berlintransit.app.widget.WidgetScheduler
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.OkHttpClient
import retrofit2.Retrofit
import retrofit2.converter.kotlinx.serialization.asConverterFactory
import java.util.concurrent.TimeUnit

class AppContainer(context: Context) {

    val favoritesStore = FavoritesStore(context)
    val locationProvider = LocationProvider(context)
    val repository: TransitRepository

    init {
        val okHttpClient = OkHttpClient.Builder()
            .connectTimeout(10, TimeUnit.SECONDS)
            .readTimeout(20, TimeUnit.SECONDS)
            .build()
        val retrofit = Retrofit.Builder()
            .baseUrl("https://v6.vbb.transport.rest/")
            .client(okHttpClient)
            .addConverterFactory(ApiJson.json.asConverterFactory("application/json".toMediaType()))
            .build()
        repository = TransitRepository(retrofit.create(VbbApi::class.java))
    }
}

class BerlinTransitApplication : Application() {

    lateinit var container: AppContainer
        private set

    override fun onCreate() {
        super.onCreate()
        container = AppContainer(this)
        WidgetScheduler.ensurePeriodicUpdates(this)
    }
}
