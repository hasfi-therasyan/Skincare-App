package com.skincare.apitest.repository

import com.skincare.apitest.model.Product
import com.skincare.apitest.model.PackageProduct
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

sealed class CartItem {
    data class IndividualProduct(val product: Product) : CartItem()
    data class PackageProductItem(val packageProduct: PackageProduct) : CartItem()
}

object CartRepository {
    private val _cartItems = MutableStateFlow<List<CartItem>>(emptyList())
    val cartItems: StateFlow<List<CartItem>> = _cartItems.asStateFlow()

    val cartItemCount: StateFlow<Int> = MutableStateFlow(0)

    fun addToCart(product: Product) {
        val currentList = _cartItems.value.toMutableList()
        currentList.add(CartItem.IndividualProduct(product))
        _cartItems.value = currentList
        (cartItemCount as MutableStateFlow).value = currentList.size
    }

    fun addPackageToCart(packageProduct: PackageProduct) {
        val currentList = _cartItems.value.toMutableList()
        currentList.add(CartItem.PackageProductItem(packageProduct))
        _cartItems.value = currentList
        (cartItemCount as MutableStateFlow).value = currentList.size
    }

    fun removeFromCart(product: Product) {
        val currentList = _cartItems.value.toMutableList()
        currentList.removeAll { it is CartItem.IndividualProduct && it.product == product }
        _cartItems.value = currentList
        (cartItemCount as MutableStateFlow).value = currentList.size
    }

    fun removePackageFromCart(packageProduct: PackageProduct) {
        val currentList = _cartItems.value.toMutableList()
        currentList.removeAll { it is CartItem.PackageProductItem && it.packageProduct == packageProduct }
        _cartItems.value = currentList
        (cartItemCount as MutableStateFlow).value = currentList.size
    }
}
