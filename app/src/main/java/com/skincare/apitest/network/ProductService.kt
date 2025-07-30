package com.skincare.apitest.network

import com.skincare.apitest.model.Product
import com.skincare.apitest.model.ProductResponse
import com.skincare.apitest.model.ResellerResponse
import retrofit2.Response
import retrofit2.http.GET
import retrofit2.http.Path

import com.apollographql.apollo3.ApolloClient
import retrofit2.Retrofit
import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.converter.gson.GsonConverterFactory

import com.skincare.apitest.model.PackageProductResponse

interface ProductService {
    @GET("products")
    suspend fun getProducts(): Response<ProductResponse>

    @GET("product/image/{id}")
    suspend fun getProductImage(@Path("id") id: Int): Response<String>

    @GET("packages")
    suspend fun getPackages(): Response<PackageProductResponse>

    @GET("resellers")
    suspend fun getResellers(): Response<ResellerResponse>

    @GET("resellers/limited")
    suspend fun getLimitedResellers(): Response<ResellerResponse>

    @GET("resellers/search/name/{query}")
    suspend fun searchResellersByName(@Path("query") query: String): Response<ResellerResponse>

    @GET("resellers/search/city/{query}")
    suspend fun searchResellersByCity(@Path("query") query: String): Response<ResellerResponse>

    @GET("package/image/{id}")
    suspend fun getPackageImage(@Path("id") id: Int): Response<String>

    companion object {
        const val BASE_URL = "http://10.0.2.2:4000/api/"
    }
}

// Apollo Client setup
object ApolloClientProvider {
    private var instance: ApolloClient? = null

    fun getApolloClient(): ApolloClient {
        if (instance == null) {
            instance = ApolloClient.Builder()
                .serverUrl("http://10.0.2.2:4000/graphql")
                .build()
        }
        return instance!!
    }
}

// Retrofit Client setup
object RetrofitClientProvider {
    private var instance: Retrofit? = null

    fun getRetrofitClient(): Retrofit {
        if (instance == null) {
            val loggingInterceptor = HttpLoggingInterceptor().apply {
                level = HttpLoggingInterceptor.Level.BODY
            }

            val client = OkHttpClient.Builder()
                .addInterceptor(loggingInterceptor)
                .build()

            instance = Retrofit.Builder()
                .baseUrl(ProductService.BASE_URL)
                .client(client)
                .addConverterFactory(GsonConverterFactory.create())
                .build()
        }
        return instance!!
    }
}
