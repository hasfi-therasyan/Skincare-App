package com.skincare.apitest.repository

import com.apollographql.apollo3.exception.ApolloException
import com.skincare.apitest.GetProductImageQuery
import com.skincare.apitest.GetProductsQuery
import com.skincare.apitest.model.ApiResponse
import com.skincare.apitest.model.ApiType
import com.skincare.apitest.model.Product
import com.skincare.apitest.network.ApolloClientProvider
import com.skincare.apitest.network.ProductService
import com.skincare.apitest.network.RetrofitClientProvider
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow

class ProductRepository {
    private val retrofitService = RetrofitClientProvider.getRetrofitClient()
        .create(ProductService::class.java)
    private val apolloClient = ApolloClientProvider.getApolloClient()

    fun getProducts(apiType: ApiType): Flow<ApiResponse<List<Product>>> = flow {
        emit(ApiResponse.Loading)
        try {
            when (apiType) {
                ApiType.RETROFIT -> {
                    val response = retrofitService.getProducts()
                    if (response.isSuccessful) {
                        response.body()?.let { productResponse ->
                            emit(ApiResponse.Success(productResponse.products))
                        } ?: emit(ApiResponse.Error("Empty response body"))
                    } else {
                        emit(ApiResponse.Error("Error: ${response.code()}"))
                    }
                }
                ApiType.GRAPHQL -> {
                    val response = apolloClient.query(GetProductsQuery()).execute()
                    if (response.hasErrors()) {
                        emit(ApiResponse.Error(response.errors?.first()?.message ?: "Unknown GraphQL error"))
                    } else {
                        val products = response.data?.products?.map { product ->
                            Product(
                                id = product.id.toInt(),
                                productName = product.product_name,
                                description = product.description,
                                price = product.price,
                                imageData = product.image_data
                            )
                        } ?: emptyList()
                        emit(ApiResponse.Success(products))
                    }
                }
            }
        } catch (e: Exception) {
            emit(ApiResponse.Error(e.message ?: "Unknown error occurred"))
        }
    }

    fun getProductImage(id: Int, apiType: ApiType): Flow<ApiResponse<String>> = flow {
        emit(ApiResponse.Loading)
        try {
            when (apiType) {
                ApiType.RETROFIT -> {
                    val response = retrofitService.getProductImage(id)
                    if (response.isSuccessful) {
                        response.body()?.let { imageData ->
                            emit(ApiResponse.Success(imageData))
                        } ?: emit(ApiResponse.Error("Empty image data"))
                    } else {
                        emit(ApiResponse.Error("Error: ${response.code()}"))
                    }
                }
                ApiType.GRAPHQL -> {
                    val response = apolloClient.query(GetProductImageQuery(id.toString())).execute()
                    if (response.hasErrors()) {
                        emit(ApiResponse.Error(response.errors?.first()?.message ?: "Unknown GraphQL error"))
                    } else {
                        val imageData = response.data?.productImage?.image_data
                        if (imageData != null) {
                            emit(ApiResponse.Success(imageData))
                        } else {
                            emit(ApiResponse.Error("Image not found"))
                        }
                    }
                }
            }
        } catch (e: Exception) {
            emit(ApiResponse.Error(e.message ?: "Unknown error occurred"))
        }
    }
}
