package com.skincare.apitest.model

import java.io.Serializable

data class CustomPackageProduct(
    val basePackage: PackageProduct,
    val selectedItems: List<String>
) : Serializable {
    val id: Int get() = basePackage.id
    val packageName: String get() = basePackage.packageName
    val price: Double get() = basePackage.price
    val imageData: String? get() = basePackage.imageData

    fun getFormattedPrice(): String {
        return basePackage.getFormattedPrice()
    }
}