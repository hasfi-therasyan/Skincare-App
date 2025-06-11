package com.skincare.apitest.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.skincare.apitest.model.ApiResponse
import com.skincare.apitest.model.ApiType
import com.skincare.apitest.model.Product
import com.skincare.apitest.repository.ProductRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.flow.asStateFlow
import com.skincare.apitest.model.PackageProduct
import com.skincare.apitest.model.Reseller
import com.skincare.apitest.repository.CartRepository
import com.skincare.apitest.repository.CartItem

class ProductViewModel : ViewModel() {
    private val repository = ProductRepository()

    private val _productsState = MutableStateFlow<ApiResponse<List<Product>>>(ApiResponse.Loading)
    val productsState: StateFlow<ApiResponse<List<Product>>> = _productsState

    private val _packageProductsState = MutableStateFlow<ApiResponse<List<PackageProduct>>>(ApiResponse.Loading)
    val packageProductsState: StateFlow<ApiResponse<List<PackageProduct>>> = _packageProductsState

    private val _resellersState = MutableStateFlow<ApiResponse<List<Reseller>>>(ApiResponse.Loading)
    val resellersState: StateFlow<ApiResponse<List<Reseller>>> = _resellersState

    private val _selectedApiType = MutableStateFlow(ApiType.RETROFIT)
    val selectedApiType: StateFlow<ApiType> = _selectedApiType

    private val _currentImageState = MutableStateFlow<ApiResponse<String>?>(null)
    val currentImageState: StateFlow<ApiResponse<String>?> = _currentImageState

    // Cart state from shared repository
    val cartItems: StateFlow<List<CartItem>> = CartRepository.cartItems

    val cartItemCount: StateFlow<Int> = CartRepository.cartItemCount

    fun setApiType(type: ApiType) {
        _selectedApiType.value = type
    }

    fun fetchProducts() {
        viewModelScope.launch {
            repository.getProducts(_selectedApiType.value).collect { response ->
                _productsState.value = response
            }
        }
    }

    fun fetchPackages() {
        viewModelScope.launch {
            repository.getPackages(_selectedApiType.value).collect { response ->
                _packageProductsState.value = response
            }
        }
    }

    fun fetchResellers() {
        viewModelScope.launch {
            repository.getResellers(_selectedApiType.value).collect { response ->
                _resellersState.value = response
            }
        }
    }

    fun fetchProductImage(productId: Int) {
        viewModelScope.launch {
            repository.getProductImage(productId, _selectedApiType.value).collect { response ->
                _currentImageState.value = response
            }
        }
    }

    fun clearCurrentImage() {
        _currentImageState.value = null
    }

    fun addToCart(product: Product) {
        CartRepository.addToCart(product)
    }

    fun addPackageToCart(packageProduct: PackageProduct, selectedItems: List<String>) {
        CartRepository.addPackageToCart(packageProduct, selectedItems)
    }

    fun removePackageFromCart(packageProduct: PackageProduct) {
        CartRepository.removePackageFromCart(packageProduct)
    }

    fun removeFromCart(product: Product) {
        CartRepository.removeFromCart(product)
    }
}
