package com.skincare.apitest.model

import com.google.gson.annotations.SerializedName
import java.io.Serializable

data class Product(
    @SerializedName("id")
    val id: Int,
    
    @SerializedName("product_name")
    val productName: String,
    
    @SerializedName("description")
    val description: String,
    
    @SerializedName("price")
    val price: Double,
    
    @SerializedName("image_data")
    val imageData: String? = null
)  : Serializable
{
    fun getFormattedPrice(): String {
        // Price is numeric from database, e.g., 110000 means 110.000
        val formatted = java.text.NumberFormat.getCurrencyInstance(java.util.Locale("in", "ID")).format(price)
        return formatted
    }
}

// API Response wrapper for REST
data class ProductResponse(
    @SerializedName("products")
    val products: List<Product>
)

// Wrapper for API responses
sealed class ApiResponse<out T> {
    data class Success<T>(val data: T) : ApiResponse<T>()
    data class Error(val message: String) : ApiResponse<Nothing>()
    object Loading : ApiResponse<Nothing>()
}

// API selection enum
enum class ApiType {
    RETROFIT,
    GRAPHQL
}
