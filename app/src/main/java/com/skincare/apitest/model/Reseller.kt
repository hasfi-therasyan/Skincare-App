package com.skincare.apitest.model

data class Reseller(
    val id: Int,
    val shopName: String,
    val profilePictureUrl: String?,
    val resellerName: String,
    val whatsappNumber: String?,
    val facebook: String?,
    val instagram: String?,
    val city: String?,
    val latitude: Double,
    val longitude: Double
)
