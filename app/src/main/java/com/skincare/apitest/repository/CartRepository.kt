package com.skincare.apitest.repository

import com.skincare.apitest.model.Product
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

object CartRepository {
    private val _cartItems = MutableStateFlow<List<Product>>(emptyList())
    val cartItems: StateFlow<List<Product>> = _cartItems.asStateFlow()

    val cartItemCount: StateFlow<Int> = MutableStateFlow(0)

    fun addToCart(product: Product) {
        val currentList = _cartItems.value.toMutableList()
        currentList.add(product)
        _cartItems.value = currentList
        (cartItemCount as MutableStateFlow).value = currentList.size
    }

    fun removeFromCart(product: Product) {
        val currentList = _cartItems.value.toMutableList()
        currentList.remove(product)
        _cartItems.value = currentList
        (cartItemCount as MutableStateFlow).value = currentList.size
    }
}
