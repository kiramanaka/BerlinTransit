package com.berlintransit.app.model

data class Station(
    val id: String,
    val name: String,
    val latitude: Double,
    val longitude: Double,
    val distance: Double? = null
)
