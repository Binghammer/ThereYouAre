package com.chadbingham.thereyouare.data

data class UserLocation(
    val userId: String,
    val latitude: Double,
    val longitude: Double,
    val timestamp: Long = System.currentTimeMillis())