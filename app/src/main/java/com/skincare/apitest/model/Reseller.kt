package com.skincare.apitest.model

import com.google.gson.annotations.SerializedName

data class Reseller(
    @SerializedName("id")
    val id: Int,

    @SerializedName("shop_name")
    val shopName: String = "",

    @SerializedName("profile_picture_url")
    val profilePictureUrl: String? = null,

    @SerializedName("reseller_name")
    val resellerName: String = "",

    @SerializedName("whatsapp_number")
    val whatsappNumber: String? = null,

    @SerializedName("facebook")
    val facebook: String? = null,

    @SerializedName("instagram")
    val instagram: String? = null,

    @SerializedName("city")
    val city: String? = null,

    @SerializedName("latitude")
    val latitude: Double,

    @SerializedName("longitude")
    val longitude: Double
)
