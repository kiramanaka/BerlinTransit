package com.berlintransit.app

import android.Manifest
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Bundle
import android.view.Menu
import android.view.MenuItem
import android.view.View
import android.widget.TextView
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import androidx.lifecycle.lifecycleScope
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.berlintransit.app.api.TransitRepository
import com.berlintransit.app.location.LocationService
import com.berlintransit.app.model.Departure
import com.berlintransit.app.model.Station
import com.berlintransit.app.widget.DepartureWidgetProvider
import com.berlintransit.app.widget.WidgetUpdateWorker
import com.google.android.material.appbar.MaterialToolbar
import com.google.android.material.progressindicator.CircularProgressIndicator
import kotlinx.coroutines.launch

class MainActivity : AppCompatActivity() {
    
    private lateinit var locationService: LocationService
    private lateinit var transitRepository: TransitRepository
    private lateinit var preferencesManager: PreferencesManager
    
    private lateinit var toolbar: MaterialToolbar
    private lateinit var swipeRefresh: SwipeRefreshLayout
    private lateinit var recyclerView: RecyclerView
    private lateinit var progressIndicator: CircularProgressIndicator
    private lateinit var statusText: TextView
    private lateinit var stationNameText: TextView
    
    private var currentStation: Station? = null
    private val departuresList = mutableListOf<Departure>()
    private lateinit var adapter: DepartureAdapter
    
    private val locationPermissionLauncher = registerForActivityResult(
        ActivityResultContracts.RequestPermission()
    ) { isGranted ->
        if (isGranted) {
            loadNearbyStations()
        } else {
            showStatus("Location permission required. Using Berlin central station.")
            loadBerlinCentral()
        }
    }
    
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        
        locationService = LocationService(this)
        transitRepository = TransitRepository()
        preferencesManager = PreferencesManager(this)
        
        // Schedule widget updates
        WidgetUpdateWorker.schedule(this)
        
        initViews()
        setupRecyclerView()
        checkLocationPermission()
    }
    
    private fun initViews() {
        toolbar = findViewById(R.id.toolbar)
        swipeRefresh = findViewById(R.id.swipeRefresh)
        recyclerView = findViewById(R.id.recyclerView)
        progressIndicator = findViewById(R.id.progressIndicator)
        statusText = findViewById(R.id.statusText)
        stationNameText = findViewById(R.id.stationNameText)
        
        setSupportActionBar(toolbar)
        supportActionBar?.setDisplayShowTitleEnabled(true)
        
        swipeRefresh.setOnRefreshListener {
            loadDepartures()
        }
    }
    
    override fun onCreateOptionsMenu(menu: Menu): Boolean {
        menuInflater.inflate(R.menu.menu_main, menu)
        return true
    }
    
    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        return when (item.itemId) {
            R.id.action_refresh -> {
                loadDepartures()
                true
            }
            R.id.action_location -> {
                checkLocationPermission()
                true
            }
            else -> super.onOptionsItemSelected(item)
        }
    }
    
    private fun setupRecyclerView() {
        adapter = DepartureAdapter(departuresList)
        recyclerView.layoutManager = LinearLayoutManager(this)
        recyclerView.adapter = adapter
    }
    
    private fun checkLocationPermission() {
        when {
            ContextCompat.checkSelfPermission(
                this,
                Manifest.permission.ACCESS_FINE_LOCATION
            ) == PackageManager.PERMISSION_GRANTED -> {
                loadNearbyStations()
            }
            shouldShowRequestPermissionRationale(Manifest.permission.ACCESS_FINE_LOCATION) -> {
                showStatus("Location permission needed to show nearby stations")
                locationPermissionLauncher.launch(Manifest.permission.ACCESS_FINE_LOCATION)
            }
            else -> {
                locationPermissionLauncher.launch(Manifest.permission.ACCESS_FINE_LOCATION)
            }
        }
    }
    
    private fun loadNearbyStations() {
        showLoading()
        
        lifecycleScope.launch {
            val location = locationService.getCurrentLocation()
            if (location != null) {
                // Show location in status
                showStatus("📍 ${location.latitude}, ${location.longitude}")
                
                val result = transitRepository.getNearbyStations(
                    location.latitude,
                    location.longitude
                )
                
                result.fold(
                    onSuccess = { stations ->
                        if (stations.isNotEmpty()) {
                            currentStation = stations[0]
                            stationNameText.text = "📍 ${stations[0].name}"
                            
                            // Save as preferred station
                            preferencesManager.savePreferredStation(
                                stations[0].id,
                                stations[0].name,
                                stations[0].latitude,
                                stations[0].longitude
                            )
                            
                            showStatus("✅ ${stations.size} stations found")
                            loadDepartures()
                        } else {
                            showStatus("⚠️ No stations. Using Berlin Hbf.")
                            loadBerlinCentral()
                        }
                    },
                    onFailure = { error ->
                        showStatus("⚠️ ${error.message}. Using Berlin stations.")
                        // Use hardcoded Berlin stations instead of just Hbf
                        loadBerlinStations()
                    }
                )
            } else {
                showStatus("⚠️ Location unavailable. Using Berlin stations.")
                loadBerlinStations()
            }
        }
    }
    
    private fun loadBerlinCentral() {
        // Berlin Hauptbahnhof as fallback
        currentStation = Station(
            id = "900000003201",
            name = "Berlin Hbf",
            latitude = 52.525589,
            longitude = 13.369548
        )
        stationNameText.text = "🚉 Berlin Hbf"
        
        // Save as preferred station
        preferencesManager.savePreferredStation(
            currentStation!!.id,
            currentStation!!.name,
            currentStation!!.latitude,
            currentStation!!.longitude
        )
        
        loadDepartures()
    }
    
    private fun loadBerlinStations() {
        // Use hardcoded Berlin stations list
        // Pick the first one (Hbf) or let user choose from list in future
        currentStation = transitRepository.BERLIN_STATIONS[0]
        stationNameText.text = "🚉 ${currentStation!!.name}"
        
        preferencesManager.savePreferredStation(
            currentStation!!.id,
            currentStation!!.name,
            currentStation!!.latitude,
            currentStation!!.longitude
        )
        
        loadDepartures()
    }
    
    private fun loadDepartures() {
        if (currentStation == null) {
            swipeRefresh.isRefreshing = false
            return
        }
        
        showLoading()
        
        lifecycleScope.launch {
            val result = transitRepository.getDepartures(currentStation!!.id)
            
            result.fold(
                onSuccess = { departures ->
                    departuresList.clear()
                    departuresList.addAll(departures)
                    adapter.notifyDataSetChanged()
                    
                    hideLoading()
                    swipeRefresh.isRefreshing = false
                    
                    if (departures.isEmpty()) {
                        showStatus("⚠️ No departures in the next 60 minutes")
                    } else {
                        showStatus("✅ ${departures.size} departures loaded")
                    }
                    
                    // Update widget
                    updateWidget()
                },
                onFailure = { error ->
                    hideLoading()
                    swipeRefresh.isRefreshing = false
                    val errorMsg = "❌ ${error.message}. Pull to refresh."
                    showStatus(errorMsg)
                    Toast.makeText(
                        this@MainActivity,
                        "Error: ${error.message}",
                        Toast.LENGTH_LONG
                    ).show()
                }
            )
        }
    }
    
    private fun updateWidget() {
        // Trigger immediate widget update via WorkManager
        WidgetUpdateWorker.triggerImmediate(this)
        
        // Also send broadcast for immediate update
        val intent = Intent(this, DepartureWidgetProvider::class.java).apply {
            action = "com.berlintransit.app.WIDGET_UPDATE"
        }
        sendBroadcast(intent)
    }
    
    private fun showLoading() {
        progressIndicator.visibility = View.VISIBLE
        statusText.visibility = View.GONE
    }
    
    private fun hideLoading() {
        progressIndicator.visibility = View.GONE
        statusText.visibility = View.VISIBLE
    }
    
    private fun showStatus(message: String) {
        statusText.text = message
        statusText.visibility = View.VISIBLE
    }
    
    override fun onResume() {
        super.onResume()
        // Auto-refresh when returning to app
        if (currentStation != null && departuresList.isEmpty()) {
            loadDepartures()
        }
    }
}
