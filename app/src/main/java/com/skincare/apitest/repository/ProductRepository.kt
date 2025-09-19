package com.skincare.apitest.repository

import com.apollographql.apollo3.exception.ApolloException
import com.skincare.apitest.GetProductsQuery
import com.skincare.apitest.GetPackagesQuery
import com.skincare.apitest.GetResellersQuery
import com.skincare.apitest.GetLimitedResellersQuery
import com.skincare.apitest.SearchResellersByNameQuery
import com.skincare.apitest.SearchResellersByCityQuery
import com.skincare.apitest.model.ApiResponse
import com.skincare.apitest.model.ApiType
import com.skincare.apitest.model.PackageProduct
import com.skincare.apitest.model.PackageProductResponse
import com.skincare.apitest.model.Product
import com.skincare.apitest.model.Reseller
import com.skincare.apitest.model.ResellerResponse
import com.skincare.apitest.network.ApolloClientProvider
import com.skincare.apitest.network.ProductService
import com.skincare.apitest.network.RetrofitClientProvider
import com.skincare.apitest.network.await
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import com.apollographql.apollo3.api.Optional

class ProductRepository {
    private val retrofitService = RetrofitClientProvider.getRetrofitClient()
        .create(ProductService::class.java)
    private val apolloClient = ApolloClientProvider.getApolloClient()

    fun getProducts(apiType: ApiType, limit: Int? = null): Flow<ApiResponse<List<Product>>> = flow {
        emit(ApiResponse.Loading)
        try {
            when (apiType) {
                ApiType.RETROFIT -> {
                    val response = retrofitService.getProducts(limit)
                    if (response.isSuccessful) {
                        response.body()?.let { productResponse ->
                            emit(ApiResponse.Success(productResponse.products))
                        } ?: emit(ApiResponse.Error("Empty response body"))
                    } else {
                        emit(ApiResponse.Error("Error: ${response.code()}"))
                    }
                }
                ApiType.GRAPHQL -> {
                    val response = apolloClient.query(GetProductsQuery(limit?.let { Optional.present(it) } ?: Optional.absent())).execute()
                    if (response.hasErrors()) {
                        emit(ApiResponse.Error(response.errors?.first()?.message ?: "Unknown GraphQL error"))
                    } else {
                        val products = response.data?.products?.map { product ->
                            Product(
                                id = product.id.toInt(),
                                productName = product.product_name,
                                description = product.description,
                                price = product.price,
                                imageData = product.image_data // This is the image URL string now
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

    suspend fun getResellers(apiType: ApiType): Flow<ApiResponse<List<Reseller>>> = flow {
        emit(ApiResponse.Loading)
        try {
            when (apiType) {
                ApiType.RETROFIT -> {
                    val response = retrofitService.getResellers().await()
                    if (response.isSuccessful) {
                        response.body()?.let { resellerResponse ->
                            emit(ApiResponse.Success(resellerResponse.resellers))
                        } ?: emit(ApiResponse.Error("Empty response body"))
                    } else {
                        emit(ApiResponse.Error("Error: ${response.code()}"))
                    }
                }
                ApiType.GRAPHQL -> {
                    val response = apolloClient.query(GetResellersQuery()).execute()
                    if (response.hasErrors()) {
                        emit(ApiResponse.Error(response.errors?.first()?.message ?: "Unknown GraphQL error"))
                    } else {
                        val resellers = response.data?.resellers?.map { reseller ->
                            Reseller(
                                id = reseller.id.toInt(),
                                shopName = reseller.shop_name,
                                profilePictureUrl = reseller.profile_picture_url,
                                resellerName = reseller.reseller_name,
                                whatsappNumber = reseller.whatsapp_number,
                                facebook = reseller.facebook,
                                instagram = reseller.instagram,
                                city = reseller.city,
                                latitude = reseller.latitude,
                                longitude = reseller.longitude
                            )
                        } ?: emptyList()
                        emit(ApiResponse.Success(resellers))
                    }
                }
            }
        } catch (e: Exception) {
            emit(ApiResponse.Error(e.message ?: "Unknown error occurred"))
        }
    }

    suspend fun getLimitedResellers(apiType: ApiType): Flow<ApiResponse<List<Reseller>>> = flow {
        emit(ApiResponse.Loading)
        try {
            when (apiType) {
                ApiType.RETROFIT -> {
                    val response = retrofitService.getLimitedResellers().await()
                    if (response.isSuccessful) {
                        response.body()?.let { resellerResponse ->
                            emit(ApiResponse.Success(resellerResponse.resellers))
                        } ?: emit(ApiResponse.Error("Empty response body"))
                    } else {
                        emit(ApiResponse.Error("Error: ${response.code()}"))
                    }
                }
                ApiType.GRAPHQL -> {
                    val response = apolloClient.query(GetLimitedResellersQuery()).execute()
                    if (response.hasErrors()) {
                        emit(ApiResponse.Error(response.errors?.first()?.message ?: "Unknown GraphQL error"))
                    } else {
                        val resellers = response.data?.limitedResellers?.map { reseller ->
                            Reseller(
                                id = reseller.id.toInt(),
                                shopName = reseller.shop_name,
                                profilePictureUrl = reseller.profile_picture_url,
                                resellerName = reseller.reseller_name,
                                whatsappNumber = reseller.whatsapp_number,
                                facebook = reseller.facebook,
                                instagram = reseller.instagram,
                                city = reseller.city,
                                latitude = reseller.latitude,
                                longitude = reseller.longitude
                            )
                        } ?: emptyList()
                        emit(ApiResponse.Success(resellers))
                    }
                }
            }
        } catch (e: Exception) {
            emit(ApiResponse.Error(e.message ?: "Unknown error occurred"))
        }
    }

    suspend fun searchResellersByName(query: String, apiType: ApiType): Flow<ApiResponse<List<Reseller>>> = flow {
        emit(ApiResponse.Loading)
        try {
            when (apiType) {
                ApiType.RETROFIT -> {
                    val response = retrofitService.searchResellersByName(query).await()
                    if (response.isSuccessful) {
                        response.body()?.let { resellerResponse ->
                            emit(ApiResponse.Success(resellerResponse.resellers))
                        } ?: emit(ApiResponse.Error("Empty response body"))
                    } else {
                        emit(ApiResponse.Error("Error: ${response.code()}"))
                    }
                }
                ApiType.GRAPHQL -> {
                    val response = apolloClient.query(SearchResellersByNameQuery(query)).execute()
                    if (response.hasErrors()) {
                        emit(ApiResponse.Error(response.errors?.first()?.message ?: "Unknown GraphQL error"))
                    } else {
                        val resellers = response.data?.searchResellersByName?.map { reseller ->
                            Reseller(
                                id = reseller.id.toInt(),
                                shopName = reseller.shop_name,
                                profilePictureUrl = reseller.profile_picture_url,
                                resellerName = reseller.reseller_name,
                                whatsappNumber = reseller.whatsapp_number,
                                facebook = reseller.facebook,
                                instagram = reseller.instagram,
                                city = reseller.city,
                                latitude = reseller.latitude,
                                longitude = reseller.longitude
                            )
                        } ?: emptyList()
                        emit(ApiResponse.Success(resellers))
                    }
                }
            }
        } catch (e: Exception) {
            emit(ApiResponse.Error(e.message ?: "Unknown error occurred"))
        }
    }

    suspend fun searchResellersByCity(query: String, apiType: ApiType): Flow<ApiResponse<List<Reseller>>> = flow {
        emit(ApiResponse.Loading)
        try {
            when (apiType) {
                ApiType.RETROFIT -> {
                    val response = retrofitService.searchResellersByCity(query).await()
                    if (response.isSuccessful) {
                        response.body()?.let { resellerResponse ->
                            emit(ApiResponse.Success(resellerResponse.resellers))
                        } ?: emit(ApiResponse.Error("Empty response body"))
                    } else {
                        emit(ApiResponse.Error("Error: ${response.code()}"))
                    }
                }
                ApiType.GRAPHQL -> {
                    val response = apolloClient.query(SearchResellersByCityQuery(query)).execute()
                    if (response.hasErrors()) {
                        emit(ApiResponse.Error(response.errors?.first()?.message ?: "Unknown GraphQL error"))
                    } else {
                        val resellers = response.data?.searchResellersByCity?.map { reseller ->
                            Reseller(
                                id = reseller.id.toInt(),
                                shopName = reseller.shop_name,
                                profilePictureUrl = reseller.profile_picture_url,
                                resellerName = reseller.reseller_name,
                                whatsappNumber = reseller.whatsapp_number,
                                facebook = reseller.facebook,
                                instagram = reseller.instagram,
                                city = reseller.city,
                                latitude = reseller.latitude,
                                longitude = reseller.longitude
                            )
                        } ?: emptyList()
                        emit(ApiResponse.Success(resellers))
                    }
                }
            }
        } catch (e: Exception) {
            emit(ApiResponse.Error(e.message ?: "Unknown error occurred"))
        }
    }


    fun getPackages(apiType: ApiType, limit: Int? = null): Flow<ApiResponse<List<PackageProduct>>> = flow {
        emit(ApiResponse.Loading)
        try {
            when (apiType) {
                ApiType.RETROFIT -> {
                    val response = retrofitService.getPackages(limit)
                    if (response.isSuccessful) {
                        response.body()?.let { packageResponse ->
                            emit(ApiResponse.Success(packageResponse.packages))
                        } ?: emit(ApiResponse.Error("Empty response body"))
                    } else {
                        emit(ApiResponse.Error("Error: ${response.code()}"))
                    }
                }
                ApiType.GRAPHQL -> {
                    val response = apolloClient.query(GetPackagesQuery(limit?.let { Optional.present(it) } ?: Optional.absent())).execute()
                    if (response.hasErrors()) {
                        emit(ApiResponse.Error(response.errors?.first()?.message ?: "Unknown GraphQL error"))
                    } else {
                        val packages = response.data?.packages?.map { pkg ->
                            PackageProduct(
                                id = pkg.id.toInt(),
                                packageName = pkg.packageName,
                                items = pkg.items ?: emptyList(),
                                price = pkg.price,
                                imageData = pkg.image_data
                            )
                        } ?: emptyList()
                        emit(ApiResponse.Success(packages))
                    }
                }
            }
        } catch (e: Exception) {
            emit(ApiResponse.Error(e.message ?: "Unknown error occurred"))
        }
    }

}
