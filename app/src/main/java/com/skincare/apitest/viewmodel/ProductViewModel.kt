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
import com.skincare.apitest.repository.CartRepository
import com.skincare.apitest.repository.CartItem
import com.skincare.apitest.SearchItem

class ProductViewModel : ViewModel() {
    private val repository = ProductRepository()

    private val _productsState = MutableStateFlow<ApiResponse<List<Product>>>(ApiResponse.Loading)
    val productsState: StateFlow<ApiResponse<List<Product>>> = _productsState

    private val _packageProductsState = MutableStateFlow<ApiResponse<List<PackageProduct>>>(ApiResponse.Loading)
    val packageProductsState: StateFlow<ApiResponse<List<PackageProduct>>> = _packageProductsState

    private val _selectedApiType = MutableStateFlow(ApiType.RETROFIT)
    val selectedApiType: StateFlow<ApiType> = _selectedApiType

    private val _currentImageState = MutableStateFlow<ApiResponse<String>?>(null)
    val currentImageState: StateFlow<ApiResponse<String>?> = _currentImageState

    // Cart state from shared repository
    val cartItems: StateFlow<List<CartItem>> = CartRepository.cartItems

    val cartItemCount: StateFlow<Int> = CartRepository.cartItemCount

    private val _searchResults = MutableStateFlow<List<SearchItem>>(emptyList())
    val searchResults: StateFlow<List<SearchItem>> get() = _searchResults

    private var productsLoaded = false
    private var packagesLoaded = false

    fun setApiType(type: ApiType) {
        _selectedApiType.value = type
    }

    fun fetchProducts(searchQuery: String? = null) {
        if (productsLoaded && searchQuery.isNullOrEmpty()) return
        viewModelScope.launch {
            repository.getProducts(_selectedApiType.value, searchQuery).collect { response ->
                if (response is ApiResponse.Success) {
                    productsLoaded = true
                }
                _productsState.value = response
            }
        }
    }

    fun fetchPackages() {
        if (packagesLoaded) return
        viewModelScope.launch {
            repository.getPackages(_selectedApiType.value).collect { response ->
                if (response is ApiResponse.Success) {
                    packagesLoaded = true
                }
                _packageProductsState.value = response
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

    fun addPackageToCart(packageProduct: PackageProduct) {
        CartRepository.addPackageToCart(packageProduct)
    }

    fun removePackageFromCart(packageProduct: PackageProduct) {
        CartRepository.removePackageFromCart(packageProduct)
    }

    fun removeFromCart(product: Product) {
        CartRepository.removeFromCart(product)
    }

    fun searchProducts(query: String) {
        viewModelScope.launch {
            val lowerQuery = query.lowercase().trim()
            android.util.Log.d("ProductViewModel", "Search query: '$lowerQuery'")

            if (lowerQuery.isEmpty()) {
                _searchResults.value = emptyList()
                return@launch
            }

            // Log the current state of products
            when (val response = _productsState.value) {
                is ApiResponse.Success -> {
                    android.util.Log.d("ProductViewModel", "Total products available: ${response.data.size}")
                    android.util.Log.d("ProductViewModel", "Sample product names: ${response.data.take(3).map { it.productName }}")
                }
                is ApiResponse.Error -> android.util.Log.e("ProductViewModel", "Products error: ${response.message}")
                is ApiResponse.Loading -> android.util.Log.d("ProductViewModel", "Products still loading")
            }

            // Log the current state of packages
            when (val response = _packageProductsState.value) {
                is ApiResponse.Success -> {
                    android.util.Log.d("ProductViewModel", "Total packages available: ${response.data.size}")
                    android.util.Log.d("ProductViewModel", "Sample package names: ${response.data.take(3).map { it.packageName }}")
                }
                is ApiResponse.Error -> android.util.Log.e("ProductViewModel", "Packages error: ${response.message}")
                is ApiResponse.Loading -> android.util.Log.d("ProductViewModel", "Packages still loading")
            }

            val individualResults = when (val response = _productsState.value) {
                is ApiResponse.Success -> response.data.filter { it.productName.lowercase().contains(lowerQuery) }
                else -> emptyList()
            }
            android.util.Log.d("ProductViewModel", "Matching individual products: ${individualResults.size}")

            val packageResults = when (val response = _packageProductsState.value) {
                is ApiResponse.Success -> response.data.filter { it.packageName.lowercase().contains(lowerQuery) }
                else -> emptyList()
            }
            android.util.Log.d("ProductViewModel", "Matching packages: ${packageResults.size}")

            // Combine and sort results by relevance or name
            val combinedResults = (individualResults.map { SearchItem.IndividualSearchItem(it) } +
                                   packageResults.map { SearchItem.PackageSearchItem(it) })
                .sortedBy { item ->
                    when (item) {
                        is SearchItem.IndividualSearchItem -> item.product.productName.lowercase().indexOf(lowerQuery)
                        is SearchItem.PackageSearchItem -> item.packageProduct.packageName.lowercase().indexOf(lowerQuery)
                    }
                }
            
            android.util.Log.d("ProductViewModel", "Total combined results: ${combinedResults.size}")
            _searchResults.value = combinedResults
        }
    }
}
