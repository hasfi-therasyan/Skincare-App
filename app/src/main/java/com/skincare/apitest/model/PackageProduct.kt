package com.skincare.apitest.model

import com.google.gson.annotations.SerializedName
import java.io.Serializable

data class PackageProduct(
    @SerializedName("id")
    val id: Int,

    @SerializedName("package_name")
    val packageName: String,

    @SerializedName("items")
    val items: List<String>,

    @SerializedName("price")
    val price: Double,

    @SerializedName("image_data")
    val imageData: String? = null
) : Serializable {
    fun getFormattedPrice(): String {
        val formatted = java.text.NumberFormat.getCurrencyInstance(java.util.Locale("in", "ID")).format(price)
        return formatted
    }
}

// API Response wrapper for REST
data class PackageProductResponse(
    @SerializedName("packages")
    val packages: List<PackageProduct>
)
