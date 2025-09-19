package com.skincare.apitest.performance

import android.content.Context
import android.os.SystemClock
import android.util.Log
import com.skincare.apitest.model.ApiType
import com.skincare.apitest.model.Product
import com.skincare.apitest.model.PackageProduct
import com.skincare.apitest.model.Reseller
import com.skincare.apitest.repository.ProductRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.flow
import java.io.File
import java.io.FileWriter
import java.text.SimpleDateFormat
import java.util.*

/**
 * Performance Test Manager untuk menguji performa API Retrofit vs Apollo
 * Mengukur: Response Time, Memory Usage, CPU Usage, dan Kompleksitas Implementasi
 */
class PerformanceTestManager(private val context: Context) {
    
    private val repository = ProductRepository()
    private val dateFormat = SimpleDateFormat("yyyy-MM-dd_HH-mm-ss", Locale.getDefault())
    private val memoryTracker = MemoryTracker()
    private val cpuTracker = CPUTracker()
    
    // Data untuk pengujian - Enhanced untuk penelitian yang lebih komprehensif
    private val testDataSizes = listOf(50, 100, 121, 184) // Include full dataset sizes
    private val testLoadLevels = listOf(100, 500, 1000) // Request per menit
    private val testIterations = 100 // Jumlah pengulangan untuk akurasi
    
    // Enhanced test scenarios for fair comparison
    private val testScenarios = listOf(
        "INDIVIDUAL_PRODUCTS_ONLY",    // 121 products only
        "PACKAGE_PRODUCTS_ONLY",       // 63 packages only  
        "MIXED_PRODUCTS",              // 121 + 63 = 184 total
        "SEARCH_OPERATIONS",           // Reseller search
        "REAL_WORLD_SCENARIO"          // Simulate actual app usage
    )
    
    /**
     * Hasil pengujian untuk setiap metrik - Enhanced untuk penelitian
     */
    data class TestResult(
        val apiType: ApiType,
        val testType: String,
        val testScenario: String,
        val dataSize: Int,
        val loadLevel: Int,
        val networkTime: Long, // Network request time only
        val parsingTime: Long, // JSON/GraphQL parsing time
        val totalResponseTime: Long, // Total time (network + parsing)
        val timeToFirstRender: Long, // Time to first UI render
        val memoryUsage: Long, // bytes
        val peakMemoryUsage: Long, // Peak memory during test
        val cpuUsage: Double, // percentage
        val peakCpuUsage: Double, // Peak CPU during test
        val timestamp: Long,
        val success: Boolean,
        val errorMessage: String? = null,
        val dataTransferSize: Long = 0, // Size of data transferred
        val queryComplexity: Int = 0 // GraphQL query complexity or REST endpoint count
    )
    
    /**
     * Statistik hasil pengujian
     */
    data class TestStatistics(
        val apiType: ApiType,
        val testType: String,
        val dataSize: Int,
        val loadLevel: Int,
        val averageResponseTime: Double,
        val minResponseTime: Long,
        val maxResponseTime: Long,
        val averageMemoryUsage: Double,
        val averageCpuUsage: Double,
        val successRate: Double,
        val standardDeviation: Double
    )
    
    /**
     * Debug test untuk menguji API call individual - Enhanced untuk testing konektivitas database
     */
    suspend fun debugApiCall(apiType: ApiType, testType: String): String {
        Log.d("PerformanceTest", "Debug API call - Type: $testType, API: ${apiType.name}")
        
        return try {
            when (testType) {
                "products" -> {
                    var result = "No response"
                    // Fetch all products (121 total) to test database connectivity
                    repository.getProducts(apiType, 121).collect { response ->
                        result = when (response) {
                            is com.skincare.apitest.model.ApiResponse.Success -> "SUCCESS: ${response.data.size}/121 products loaded"
                            is com.skincare.apitest.model.ApiResponse.Error -> "ERROR: ${response.message}"
                            is com.skincare.apitest.model.ApiResponse.Loading -> "LOADING"
                        }
                    }
                    result
                }
                "packages" -> {
                    var result = "No response"
                    // Fetch all packages (63 total) to test database connectivity
                    repository.getPackages(apiType, 63).collect { response ->
                        result = when (response) {
                            is com.skincare.apitest.model.ApiResponse.Success -> "SUCCESS: ${response.data.size}/63 packages loaded"
                            is com.skincare.apitest.model.ApiResponse.Error -> "ERROR: ${response.message}"
                            is com.skincare.apitest.model.ApiResponse.Loading -> "LOADING"
                        }
                    }
                    result
                }
                "resellers" -> {
                    var result = "No response"
                    // Use getResellers to get all resellers instead of searchResellersByName with empty string
                    repository.getResellers(apiType).collect { response ->
                        result = when (response) {
                            is com.skincare.apitest.model.ApiResponse.Success -> "SUCCESS: ${response.data.size} resellers loaded"
                            is com.skincare.apitest.model.ApiResponse.Error -> "ERROR: ${response.message}"
                            is com.skincare.apitest.model.ApiResponse.Loading -> "LOADING"
                        }
                    }
                    result
                }
                else -> "Unknown test type: $testType"
            }
        } catch (e: Exception) {
            "EXCEPTION: ${e.message}"
        }
    }

    /**
     * Test konektivitas API untuk memastikan backend berjalan
     */
    suspend fun testApiConnectivity(apiType: ApiType): Boolean {
        Log.d("PerformanceTest", "Testing API connectivity for ${apiType.name}")
        Log.d("PerformanceTest", "Backend URL - Retrofit: http://10.0.2.2:4000/api/, GraphQL: http://10.0.2.2:4000/graphql")
        
        return try {
            var success = false
            var responseCount = 0
            
            // Set a timeout of 10 seconds
            kotlinx.coroutines.withTimeout(10000) {
                repository.getProducts(apiType, 1).collect { response ->
                    responseCount++
                    Log.d("PerformanceTest", "Response $responseCount for ${apiType.name}: ${response.javaClass.simpleName}")
                    
                    when (response) {
                        is com.skincare.apitest.model.ApiResponse.Success -> {
                            Log.d("PerformanceTest", "API connectivity test SUCCESS for ${apiType.name} - Got ${response.data.size} products")
                            success = true
                        }
                        is com.skincare.apitest.model.ApiResponse.Error -> {
                            Log.e("PerformanceTest", "API connectivity test FAILED for ${apiType.name} - ${response.message}")
                            success = false
                        }
                        is com.skincare.apitest.model.ApiResponse.Loading -> {
                            Log.d("PerformanceTest", "API connectivity test LOADING for ${apiType.name}")
                            // Continue waiting
                        }
                    }
                }
            }
            
            if (responseCount == 0) {
                Log.e("PerformanceTest", "No responses received for ${apiType.name}")
                return false
            }
            
            success
        } catch (e: kotlinx.coroutines.TimeoutCancellationException) {
            Log.e("PerformanceTest", "API connectivity test TIMEOUT for ${apiType.name} after 10 seconds")
            false
        } catch (e: Exception) {
            Log.e("PerformanceTest", "API connectivity test EXCEPTION for ${apiType.name} - ${e.message}")
            e.printStackTrace()
            false
        }
    }

    /**
     * Comprehensive database connectivity test - Tests all data types and provides detailed results
     */
    suspend fun testDatabaseConnectivity(): String {
        Log.d("PerformanceTest", "Testing comprehensive database connectivity...")
        
        val results = mutableListOf<String>()
        
        // Test both APIs
        for (apiType in listOf(ApiType.RETROFIT, ApiType.GRAPHQL)) {
            results.add("=== ${apiType.name} API ===")
            
            // Test Products
            try {
                var productCount = 0
                repository.getProducts(apiType, 121).collect { response ->
                    when (response) {
                        is com.skincare.apitest.model.ApiResponse.Success -> {
                            productCount = response.data.size
                            results.add("✅ Products: $productCount/121 loaded")
                        }
                        is com.skincare.apitest.model.ApiResponse.Error -> {
                            results.add("❌ Products: ERROR - ${response.message}")
                        }
                        is com.skincare.apitest.model.ApiResponse.Loading -> {
                            results.add("⏳ Products: Loading...")
                        }
                    }
                }
            } catch (e: Exception) {
                results.add("❌ Products: EXCEPTION - ${e.message}")
            }
            
            // Test Packages
            try {
                var packageCount = 0
                repository.getPackages(apiType, 63).collect { response ->
                    when (response) {
                        is com.skincare.apitest.model.ApiResponse.Success -> {
                            packageCount = response.data.size
                            results.add("✅ Packages: $packageCount/63 loaded")
                        }
                        is com.skincare.apitest.model.ApiResponse.Error -> {
                            results.add("❌ Packages: ERROR - ${response.message}")
                        }
                        is com.skincare.apitest.model.ApiResponse.Loading -> {
                            results.add("⏳ Packages: Loading...")
                        }
                    }
                }
            } catch (e: Exception) {
                results.add("❌ Packages: EXCEPTION - ${e.message}")
            }
            
            // Test Resellers (fetch all resellers directly)
            try {
                var resellerCount = 0
                repository.getResellers(apiType).collect { response ->
                    when (response) {
                        is com.skincare.apitest.model.ApiResponse.Success -> {
                            resellerCount = response.data.size
                            results.add("✅ Resellers: $resellerCount resellers loaded")
                        }
                        is com.skincare.apitest.model.ApiResponse.Error -> {
                            results.add("❌ Resellers: ERROR - ${response.message}")
                        }
                        is com.skincare.apitest.model.ApiResponse.Loading -> {
                            results.add("⏳ Resellers: Loading...")
                        }
                    }
                }
            } catch (e: Exception) {
                results.add("❌ Resellers: EXCEPTION - ${e.message}")
            }
            
            results.add("") // Empty line for separation
        }
        
        // Summary
        results.add("=== CONNECTIVITY SUMMARY ===")
        val successCount = results.count { it.startsWith("✅") }
        val errorCount = results.count { it.startsWith("❌") }
        val totalTests = successCount + errorCount
        
        results.add("Total Tests: $totalTests")
        results.add("Successful: $successCount")
        results.add("Failed: $errorCount")
        results.add("Success Rate: ${if (totalTests > 0) (successCount * 100 / totalTests) else 0}%")
        
        return results.joinToString("\n")
    }

    /**
     * Menjalankan pengujian lengkap untuk semua skenario - Enhanced untuk penelitian
     */
    suspend fun runCompletePerformanceTest(): List<TestStatistics> {
        val allResults = mutableListOf<TestResult>()
        val statistics = mutableListOf<TestStatistics>()
        
        Log.d("PerformanceTest", "Memulai pengujian performa lengkap untuk penelitian...")
        
        // Test API connectivity first
        for (apiType in listOf(ApiType.RETROFIT, ApiType.GRAPHQL)) {
            val isConnected = testApiConnectivity(apiType)
            if (!isConnected) {
                Log.e("PerformanceTest", "API connectivity test failed for ${apiType.name}. Skipping tests.")
                continue
            }
        }
        
        // 🎯 FAIR TESTING PROTOCOL - Ensure both APIs start from same baseline
        Log.d("PerformanceTest", "=== STARTING FAIR TESTING PROTOCOL ===")
        
        // 1. Warm-up phase for both APIs (establish baseline)
        Log.d("PerformanceTest", "Phase 1: Warming up both APIs...")
        for (apiType in listOf(ApiType.RETROFIT, ApiType.GRAPHQL)) {
            Log.d("PerformanceTest", "Warming up ${apiType.name}...")
            repeat(5) { // 5 warm-up calls
                try {
                    repository.getProducts(apiType, 10).collect { }
                    kotlinx.coroutines.delay(100)
                } catch (e: Exception) {
                    Log.w("PerformanceTest", "Warm-up call failed for ${apiType.name}: ${e.message}")
                }
            }
        }
        
        // 2. Cooldown period to reset any advantages
        Log.d("PerformanceTest", "Phase 2: Cooldown period (2 seconds)...")
        kotlinx.coroutines.delay(2000)
        
        // 3. Alternating test order to eliminate first-mover advantage
        val apiOrder = listOf(ApiType.RETROFIT, ApiType.GRAPHQL)
        val reversedApiOrder = listOf(ApiType.GRAPHQL, ApiType.RETROFIT)
        
        // Use alternating order for different test types
        var useReversedOrder = false
        
        Log.d("PerformanceTest", "Phase 3: Starting alternating test sequence...")
        
        // Pengujian untuk setiap jenis API dengan alternating order
        for (testRound in 0..1) { // 2 rounds with alternating order
            val currentApiOrder = if (useReversedOrder) reversedApiOrder else apiOrder
            Log.d("PerformanceTest", "Test Round ${testRound + 1}: ${currentApiOrder.map { it.name }}")
            
            for (apiType in currentApiOrder) {
                Log.d("PerformanceTest", "Testing API: ${apiType.name} (Round ${testRound + 1})")
                
                // Reset trackers before each API test
                memoryTracker.reset()
                cpuTracker.reset()
                
                // Small cooldown between APIs
                kotlinx.coroutines.delay(500)
                
                // 1. Pengujian produk individual dengan berbagai ukuran data
                for (dataSize in testDataSizes) {
                    for (loadLevel in testLoadLevels) {
                        val results = runProductTest(apiType, dataSize, loadLevel)
                        allResults.addAll(results)
                        
                        val stats = calculateStatistics(results)
                        statistics.add(stats)
                        
                        Log.d("PerformanceTest", "Completed: ${apiType.name} - Products - Size: $dataSize - Load: $loadLevel")
                        
                        // Small delay between test configurations
                        kotlinx.coroutines.delay(200)
                    }
                }
                
                // 2. Pengujian package products dengan berbagai ukuran data
                for (dataSize in testDataSizes) {
                    for (loadLevel in testLoadLevels) {
                        val results = runPackageTest(apiType, dataSize, loadLevel)
                        allResults.addAll(results)
                        
                        val stats = calculateStatistics(results)
                        statistics.add(stats)
                        
                        Log.d("PerformanceTest", "Completed: ${apiType.name} - Packages - Size: $dataSize - Load: $loadLevel")
                        
                        // Small delay between test configurations
                        kotlinx.coroutines.delay(200)
                    }
                }
                
                // 3. Pengujian reseller search
                val searchResults = runResellerSearchTest(apiType)
                allResults.addAll(searchResults)
                
                val searchStats = calculateStatistics(searchResults)
                statistics.add(searchStats)
                
                Log.d("PerformanceTest", "Completed: ${apiType.name} - Reseller Search")
                
                // 4. Pengujian skenario dunia nyata - KUNCI untuk penelitian yang adil
                val realWorldResults = runRealWorldScenarioTest(apiType)
                allResults.addAll(realWorldResults)
                
                val realWorldStats = calculateStatistics(realWorldResults)
                statistics.add(realWorldStats)
                
                Log.d("PerformanceTest", "Completed: ${apiType.name} - Real World Scenario")
                
                // Cooldown between APIs
                kotlinx.coroutines.delay(1000)
            }
            
            // Switch order for next round
            useReversedOrder = !useReversedOrder
            
            // Longer cooldown between rounds
            if (testRound == 0) {
                Log.d("PerformanceTest", "Cooldown between test rounds (3 seconds)...")
                kotlinx.coroutines.delay(3000)
            }
        }
        
        // Simpan hasil ke file
        saveResultsToFile(allResults, statistics)
        
        // Log summary for research analysis
        logResearchSummary(allResults)
        
        Log.d("PerformanceTest", "=== FAIR TESTING PROTOCOL COMPLETED ===")
        
        return statistics
    }
    
    /**
     * Mengukur waktu parsing terpisah untuk perbandingan yang lebih akurat
     */
    private suspend fun measureParsingTime(apiType: ApiType, dataSize: Int): Long {
        return try {
            val startTime = SystemClock.elapsedRealtime()
            
            when (apiType) {
                ApiType.RETROFIT -> {
                    // Measure JSON parsing time
                    repository.getProducts(apiType, dataSize).collect { apiResponse ->
                        // Force parsing by accessing data
                        if (apiResponse is com.skincare.apitest.model.ApiResponse.Success) {
                            apiResponse.data.forEach { product ->
                                // Access all fields to ensure parsing
                                product.id
                                product.productName
                                product.description
                                product.price
                                product.imageData
                            }
                        }
                    }
                }
                ApiType.GRAPHQL -> {
                    // Measure GraphQL parsing time
                    repository.getProducts(apiType, dataSize).collect { apiResponse ->
                        // Force parsing by accessing data
                        if (apiResponse is com.skincare.apitest.model.ApiResponse.Success) {
                            apiResponse.data.forEach { product ->
                                // Access all fields to ensure parsing
                                product.id
                                product.productName
                                product.description
                                product.price
                                product.imageData
                            }
                        }
                    }
                }
            }
            
            val endTime = SystemClock.elapsedRealtime()
            endTime - startTime
            
        } catch (e: Exception) {
            Log.e("PerformanceTest", "Error measuring parsing time: ${e.message}")
            0L
        }
    }

    /**
     * Pengujian untuk produk individual
     */
    private suspend fun runProductTest(
        apiType: ApiType,
        dataSize: Int,
        loadLevel: Int
    ): List<TestResult> {
        val results = mutableListOf<TestResult>()
        
        val requestsPerSecond = loadLevel / 60.0
        val delayBetweenRequests = (1000.0 / requestsPerSecond).toLong()
        
        repeat(testIterations) { iteration ->
            try {
                // Reset trackers before each test
                memoryTracker.reset()
                cpuTracker.reset()
                
                memoryTracker.startTracking()
                cpuTracker.startTracking()
                
                val startTime = SystemClock.elapsedRealtime()
                
                // Use collect instead of first() to ensure proper Flow handling
                var response: com.skincare.apitest.model.ApiResponse<List<Product>>? = null
                repository.getProducts(apiType, dataSize).collect { apiResponse ->
                    response = apiResponse
                }
                
                val endTime = SystemClock.elapsedRealtime()
                val responseTime = endTime - startTime
                
                val memoryUsage = memoryTracker.getCurrentMemoryUsage()
                val peakMemoryUsage = memoryTracker.getPeakMemoryUsage()
                val cpuUsage = cpuTracker.getCurrentCpuUsage()
                val peakCpuUsage = cpuTracker.getPeakCpuUsage()
                
                memoryTracker.stopTracking()
                cpuTracker.stopTracking()
                
                val success = response is com.skincare.apitest.model.ApiResponse.Success
                val errorMessage = (response as? com.skincare.apitest.model.ApiResponse.Error)?.message
                
                // Calculate data transfer size using safe casting
                val dataTransferSize = when {
                    response is com.skincare.apitest.model.ApiResponse.Success -> {
                        (response as com.skincare.apitest.model.ApiResponse.Success<List<Product>>).data.sumOf { it.productName.length + it.description.length }.toLong() * 2
                    }
                    else -> 0L
                }
                
                val result = TestResult(
                    apiType = apiType,
                    testType = "PRODUCTS",
                    testScenario = "INDIVIDUAL_PRODUCTS_ONLY",
                    dataSize = dataSize,
                    loadLevel = loadLevel,
                    networkTime = 0, // Placeholder, needs actual measurement
                    parsingTime = 0, // Placeholder, needs actual measurement
                    totalResponseTime = responseTime,
                    timeToFirstRender = 0, // Placeholder, needs actual measurement
                    memoryUsage = memoryUsage,
                    peakMemoryUsage = peakMemoryUsage,
                    cpuUsage = cpuUsage,
                    peakCpuUsage = peakCpuUsage,
                    timestamp = System.currentTimeMillis(),
                    success = success,
                    errorMessage = errorMessage,
                    dataTransferSize = dataTransferSize,
                    queryComplexity = 1 // Single API call
                )
                
                results.add(result)
                
                Log.d("PerformanceTest", "Product test ${iteration + 1}/$testIterations - API: ${apiType.name}, Size: $dataSize, Time: ${responseTime}ms, Memory: ${memoryUsage}bytes, CPU: ${cpuUsage}%")
                
                kotlinx.coroutines.delay(delayBetweenRequests)
                
            } catch (e: Exception) {
                Log.e("PerformanceTest", "Error in product test: ${e.message}")
                
                memoryTracker.stopTracking()
                cpuTracker.stopTracking()
                
                val errorResult = TestResult(
                    apiType = apiType,
                    testType = "PRODUCTS",
                    testScenario = "INDIVIDUAL_PRODUCTS_ONLY", // Assuming this is the scenario
                    dataSize = dataSize,
                    loadLevel = loadLevel,
                    networkTime = 0, // Placeholder
                    parsingTime = 0, // Placeholder
                    totalResponseTime = -1,
                    timeToFirstRender = 0, // Placeholder
                    memoryUsage = 0,
                    peakMemoryUsage = 0, // Placeholder
                    cpuUsage = 0.0,
                    peakCpuUsage = 0.0, // Placeholder
                    timestamp = System.currentTimeMillis(),
                    success = false,
                    errorMessage = e.message,
                    dataTransferSize = 0, // Placeholder
                    queryComplexity = 0 // Placeholder
                )
                
                results.add(errorResult)
            }
        }
        
        return results
    }
    
    /**
     * Pengujian untuk package products
     */
    private suspend fun runPackageTest(
        apiType: ApiType, 
        dataSize: Int, 
        loadLevel: Int
    ): List<TestResult> {
        val results = mutableListOf<TestResult>()
        
        val requestsPerSecond = loadLevel / 60.0
        val delayBetweenRequests = (1000.0 / requestsPerSecond).toLong()
        
        repeat(testIterations) { iteration ->
            try {
                // Reset trackers before each test
                memoryTracker.reset()
                cpuTracker.reset()
                
                memoryTracker.startTracking()
                cpuTracker.startTracking()
                
                val startTime = SystemClock.elapsedRealtime()
                
                // Use collect instead of first() to ensure proper Flow handling
                var response: com.skincare.apitest.model.ApiResponse<List<PackageProduct>>? = null
                repository.getPackages(apiType, dataSize).collect { apiResponse ->
                    response = apiResponse
                }
                
                val endTime = SystemClock.elapsedRealtime()
                val responseTime = endTime - startTime
                
                val memoryUsage = memoryTracker.getCurrentMemoryUsage()
                val peakMemoryUsage = memoryTracker.getPeakMemoryUsage()
                val cpuUsage = cpuTracker.getCurrentCpuUsage()
                val peakCpuUsage = cpuTracker.getPeakCpuUsage()
                
                memoryTracker.stopTracking()
                cpuTracker.stopTracking()
                
                val success = response is com.skincare.apitest.model.ApiResponse.Success
                val errorMessage = (response as? com.skincare.apitest.model.ApiResponse.Error)?.message
                
                // Calculate data transfer size using safe casting
                val dataTransferSize = when {
                    response is com.skincare.apitest.model.ApiResponse.Success -> {
                        (response as com.skincare.apitest.model.ApiResponse.Success<List<PackageProduct>>).data.sumOf { it.packageName.length + it.items.sumOf { item -> item.length } }.toLong() * 2
                    }
                    else -> 0L
                }
                
                val result = TestResult(
                    apiType = apiType,
                    testType = "PACKAGES",
                    testScenario = "PACKAGE_PRODUCTS_ONLY", // Assuming this is the scenario
                    dataSize = dataSize,
                    loadLevel = loadLevel,
                    networkTime = 0, // Placeholder
                    parsingTime = 0, // Placeholder
                    totalResponseTime = responseTime,
                    timeToFirstRender = 0, // Placeholder
                    memoryUsage = memoryUsage,
                    peakMemoryUsage = peakMemoryUsage,
                    cpuUsage = cpuUsage,
                    peakCpuUsage = peakCpuUsage,
                    timestamp = System.currentTimeMillis(),
                    success = success,
                    errorMessage = errorMessage,
                    dataTransferSize = dataTransferSize,
                    queryComplexity = 1 // Single API call
                )
                
                results.add(result)
                
                Log.d("PerformanceTest", "Package test ${iteration + 1}/$testIterations - API: ${apiType.name}, Size: $dataSize, Time: ${responseTime}ms, Memory: ${memoryUsage}bytes, CPU: ${cpuUsage}%")
                
                kotlinx.coroutines.delay(delayBetweenRequests)
                
            } catch (e: Exception) {
                Log.e("PerformanceTest", "Error in package test: ${e.message}")
                
                memoryTracker.stopTracking()
                cpuTracker.stopTracking()
                
                val errorResult = TestResult(
                    apiType = apiType,
                    testType = "PACKAGES",
                    testScenario = "PACKAGE_PRODUCTS_ONLY", // Assuming this is the scenario
                    dataSize = dataSize,
                    loadLevel = loadLevel,
                    networkTime = 0, // Placeholder
                    parsingTime = 0, // Placeholder
                    totalResponseTime = -1,
                    timeToFirstRender = 0, // Placeholder
                    memoryUsage = 0,
                    peakMemoryUsage = 0, // Placeholder
                    cpuUsage = 0.0,
                    peakCpuUsage = 0.0, // Placeholder
                    timestamp = System.currentTimeMillis(),
                    success = false,
                    errorMessage = e.message,
                    dataTransferSize = 0, // Placeholder
                    queryComplexity = 0 // Placeholder
                )
                
                results.add(errorResult)
            }
        }
        
        return results
    }
    
    /**
     * Pengujian untuk reseller search (simulasi pencarian)
     */
    private suspend fun runResellerSearchTest(apiType: ApiType): List<TestResult> {
        val results = mutableListOf<TestResult>()
        val searchQueries = listOf("Ayu", "Malang", "Jakarta", "Surabaya", "Bandung")
        
        repeat(testIterations) { iteration ->
            try {
                val query = searchQueries[iteration % searchQueries.size]
                
                // Reset trackers before each test
                memoryTracker.reset()
                cpuTracker.reset()
                
                memoryTracker.startTracking()
                cpuTracker.startTracking()
                
                val startTime = SystemClock.elapsedRealtime()
                
                // Test search by name
                var response: com.skincare.apitest.model.ApiResponse<List<Reseller>>? = null
                repository.searchResellersByName(query, apiType).collect { apiResponse ->
                    response = apiResponse
                }
                
                val endTime = SystemClock.elapsedRealtime()
                val responseTime = endTime - startTime
                
                val memoryUsage = memoryTracker.getCurrentMemoryUsage()
                val peakMemoryUsage = memoryTracker.getPeakMemoryUsage()
                val cpuUsage = cpuTracker.getCurrentCpuUsage()
                val peakCpuUsage = cpuTracker.getPeakCpuUsage()
                
                memoryTracker.stopTracking()
                cpuTracker.stopTracking()
                
                val success = response is com.skincare.apitest.model.ApiResponse.Success
                val errorMessage = (response as? com.skincare.apitest.model.ApiResponse.Error)?.message
                
                // Calculate data transfer size using safe casting
                val dataTransferSize = when {
                    response is com.skincare.apitest.model.ApiResponse.Success -> {
                        (response as com.skincare.apitest.model.ApiResponse.Success<List<Reseller>>).data.sumOf { it.shopName.length + it.resellerName.length + (it.city?.length ?: 0) }.toLong() * 2
                    }
                    else -> 0L
                }
                
                val result = TestResult(
                    apiType = apiType,
                    testType = "RESELLER_SEARCH",
                    testScenario = "SEARCH_OPERATIONS", // Assuming this is the scenario
                    dataSize = 50, // Default search limit
                    loadLevel = 100, // Default load
                    networkTime = 0, // Placeholder
                    parsingTime = 0, // Placeholder
                    totalResponseTime = responseTime,
                    timeToFirstRender = 0, // Placeholder
                    memoryUsage = memoryUsage,
                    peakMemoryUsage = peakMemoryUsage,
                    cpuUsage = cpuUsage,
                    peakCpuUsage = peakCpuUsage,
                    timestamp = System.currentTimeMillis(),
                    success = success,
                    errorMessage = errorMessage,
                    dataTransferSize = dataTransferSize,
                    queryComplexity = 1 // Single API call
                )
                
                results.add(result)
                
                Log.d("PerformanceTest", "Reseller search test ${iteration + 1}/$testIterations - API: ${apiType.name}, Query: $query, Time: ${responseTime}ms, Memory: ${memoryUsage}bytes, CPU: ${cpuUsage}%")
                
                kotlinx.coroutines.delay(100) // Delay 100ms between searches
                
            } catch (e: Exception) {
                Log.e("PerformanceTest", "Error in reseller search test: ${e.message}")
                
                memoryTracker.stopTracking()
                cpuTracker.stopTracking()
                
                val errorResult = TestResult(
                    apiType = apiType,
                    testType = "RESELLER_SEARCH",
                    testScenario = "SEARCH_OPERATIONS", // Assuming this is the scenario
                    dataSize = 50,
                    loadLevel = 100,
                    networkTime = 0, // Placeholder
                    parsingTime = 0, // Placeholder
                    totalResponseTime = -1,
                    timeToFirstRender = 0, // Placeholder
                    memoryUsage = 0,
                    peakMemoryUsage = 0, // Placeholder
                    cpuUsage = 0.0,
                    peakCpuUsage = 0.0, // Placeholder
                    timestamp = System.currentTimeMillis(),
                    success = false,
                    errorMessage = e.message,
                    dataTransferSize = 0, // Placeholder
                    queryComplexity = 0 // Placeholder
                )
                
                results.add(errorResult)
            }
        }
        
        return results
    }
    
    /**
     * Pengujian skenario dunia nyata - Simulasi penggunaan aplikasi yang sebenarnya
     * Ini memberikan perbandingan yang lebih adil antara Retrofit dan Apollo
     */
    private suspend fun runRealWorldScenarioTest(apiType: ApiType): List<TestResult> {
        val results = mutableListOf<TestResult>()
        
        Log.d("PerformanceTest", "Running real-world scenario test for ${apiType.name}")
        
        repeat(testIterations) { iteration ->
            try {
                // Reset trackers
                memoryTracker.reset()
                cpuTracker.reset()
                memoryTracker.startTracking()
                cpuTracker.startTracking()
                
                val scenarioStartTime = SystemClock.elapsedRealtime()
                
                // Simulate real app usage: Load products, then packages, then load resellers, then search
                val productStartTime = SystemClock.elapsedRealtime()
                var productResponse: com.skincare.apitest.model.ApiResponse<List<Product>>? = null
                repository.getProducts(apiType, 121).collect { response ->
                    productResponse = response
                }
                val productEndTime = SystemClock.elapsedRealtime()
                val productTime = productEndTime - productStartTime
                
                // Small delay to simulate user interaction
                kotlinx.coroutines.delay(100)
                
                val packageStartTime = SystemClock.elapsedRealtime()
                var packageResponse: com.skincare.apitest.model.ApiResponse<List<PackageProduct>>? = null
                repository.getPackages(apiType, 63).collect { response ->
                    packageResponse = response
                }
                val packageEndTime = SystemClock.elapsedRealtime()
                val packageTime = packageEndTime - packageStartTime
                
                // Small delay to simulate user interaction
                kotlinx.coroutines.delay(100)
                
                // Load all resellers (external API test)
                val resellerLoadStartTime = SystemClock.elapsedRealtime()
                var resellerLoadResponse: com.skincare.apitest.model.ApiResponse<List<Reseller>>? = null
                repository.getResellers(apiType).collect { response -> // Use getResellers instead of searchResellersByName with empty string
                    resellerLoadResponse = response
                }
                val resellerLoadEndTime = SystemClock.elapsedRealtime()
                val resellerLoadTime = resellerLoadEndTime - resellerLoadStartTime
                
                // Small delay to simulate user interaction
                kotlinx.coroutines.delay(100)
                
                // Search specific reseller (filtered search)
                val searchStartTime = SystemClock.elapsedRealtime()
                var searchResponse: com.skincare.apitest.model.ApiResponse<List<Reseller>>? = null
                repository.searchResellersByName("Jakarta", apiType).collect { response ->
                    searchResponse = response
                }
                val searchEndTime = SystemClock.elapsedRealtime()
                val searchTime = searchEndTime - searchStartTime
                
                val scenarioEndTime = SystemClock.elapsedRealtime()
                val totalScenarioTime = scenarioEndTime - scenarioStartTime
                
                val memoryUsage = memoryTracker.getCurrentMemoryUsage()
                val peakMemoryUsage = memoryTracker.getPeakMemoryUsage()
                val cpuUsage = cpuTracker.getCurrentCpuUsage()
                val peakCpuUsage = cpuTracker.getPeakCpuUsage()
                
                memoryTracker.stopTracking()
                cpuTracker.stopTracking()
                
                val success = productResponse is com.skincare.apitest.model.ApiResponse.Success &&
                             packageResponse is com.skincare.apitest.model.ApiResponse.Success &&
                             resellerLoadResponse is com.skincare.apitest.model.ApiResponse.Success &&
                             searchResponse is com.skincare.apitest.model.ApiResponse.Success
                
                val errorMessage = when {
                    productResponse is com.skincare.apitest.model.ApiResponse.Error -> "Product error: ${(productResponse as com.skincare.apitest.model.ApiResponse.Error).message}"
                    packageResponse is com.skincare.apitest.model.ApiResponse.Error -> "Package error: ${(packageResponse as com.skincare.apitest.model.ApiResponse.Error).message}"
                    resellerLoadResponse is com.skincare.apitest.model.ApiResponse.Error -> "Reseller load error: ${(resellerLoadResponse as com.skincare.apitest.model.ApiResponse.Error).message}"
                    searchResponse is com.skincare.apitest.model.ApiResponse.Error -> "Search error: ${(searchResponse as com.skincare.apitest.model.ApiResponse.Error).message}"
                    else -> null
                }
                
                // Calculate data transfer size (approximate) using safe casting
                val productDataSize = when {
                    productResponse is com.skincare.apitest.model.ApiResponse.Success -> {
                        (productResponse as com.skincare.apitest.model.ApiResponse.Success<List<Product>>).data.sumOf { it.productName.length + it.description.length }.toLong() * 2
                    }
                    else -> 0L
                }
                
                val packageDataSize = when {
                    packageResponse is com.skincare.apitest.model.ApiResponse.Success -> {
                        (packageResponse as com.skincare.apitest.model.ApiResponse.Success<List<PackageProduct>>).data.sumOf { it.packageName.length + it.items.sumOf { item -> item.length } }.toLong() * 2
                    }
                    else -> 0L
                }
                
                val resellerLoadDataSize = when {
                    resellerLoadResponse is com.skincare.apitest.model.ApiResponse.Success -> {
                        (resellerLoadResponse as com.skincare.apitest.model.ApiResponse.Success<List<Reseller>>).data.sumOf { it.shopName.length + it.resellerName.length + (it.city?.length ?: 0) }.toLong() * 2
                    }
                    else -> 0L
                }
                
                val searchDataSize = when {
                    searchResponse is com.skincare.apitest.model.ApiResponse.Success -> {
                        (searchResponse as com.skincare.apitest.model.ApiResponse.Success<List<Reseller>>).data.sumOf { it.shopName.length + it.resellerName.length + (it.city?.length ?: 0) }.toLong() * 2
                    }
                    else -> 0L
                }
                
                val totalDataTransferSize = productDataSize + packageDataSize + resellerLoadDataSize + searchDataSize
                
                // Calculate query complexity - Updated for 4 operations
                val queryComplexity = when (apiType) {
                    ApiType.RETROFIT -> 4 // 4 separate REST calls (products, packages, resellers load, search)
                    ApiType.GRAPHQL -> 4 // 4 separate GraphQL queries (products, packages, resellers load, search)
                }
                
                val result = TestResult(
                    apiType = apiType,
                    testType = "REAL_WORLD_SCENARIO",
                    testScenario = "REAL_WORLD_SCENARIO",
                    dataSize = 184, // Total data size
                    loadLevel = 100,
                    networkTime = productTime + packageTime + resellerLoadTime + searchTime, // Total network time
                    parsingTime = 0, // Would need to be measured separately
                    totalResponseTime = totalScenarioTime,
                    timeToFirstRender = productTime, // Time to first product render
                    memoryUsage = memoryUsage,
                    peakMemoryUsage = peakMemoryUsage,
                    cpuUsage = cpuUsage,
                    peakCpuUsage = peakCpuUsage,
                    timestamp = System.currentTimeMillis(),
                    success = success,
                    errorMessage = errorMessage,
                    dataTransferSize = totalDataTransferSize,
                    queryComplexity = queryComplexity
                )
                
                results.add(result)
                
                Log.d("PerformanceTest", "Real-world scenario ${iteration + 1}/$testIterations - API: ${apiType.name}, " +
                    "Total Time: ${totalScenarioTime}ms, Memory: ${memoryUsage}bytes, CPU: ${cpuUsage}%, " +
                    "Data Transfer: ${totalDataTransferSize}bytes, Query Complexity: $queryComplexity")
                
                kotlinx.coroutines.delay(200) // Delay between scenarios
                
            } catch (e: Exception) {
                Log.e("PerformanceTest", "Error in real-world scenario test: ${e.message}")
                
                memoryTracker.stopTracking()
                cpuTracker.stopTracking()
                
                val errorResult = TestResult(
                    apiType = apiType,
                    testType = "REAL_WORLD_SCENARIO",
                    testScenario = "REAL_WORLD_SCENARIO",
                    dataSize = 184,
                    loadLevel = 100,
                    networkTime = -1,
                    parsingTime = -1,
                    totalResponseTime = -1,
                    timeToFirstRender = -1,
                    memoryUsage = 0,
                    peakMemoryUsage = 0,
                    cpuUsage = 0.0,
                    peakCpuUsage = 0.0,
                    timestamp = System.currentTimeMillis(),
                    success = false,
                    errorMessage = e.message,
                    dataTransferSize = 0,
                    queryComplexity = 0
                )
                
                results.add(errorResult)
            }
        }
        
        return results
    }

    /**
     * Menghitung statistik dari hasil pengujian
     */
    private fun calculateStatistics(results: List<TestResult>): TestStatistics {
        val successfulResults = results.filter { it.success && it.totalResponseTime > 0 }
        
        if (successfulResults.isEmpty()) {
            return TestStatistics(
                apiType = results.firstOrNull()?.apiType ?: ApiType.RETROFIT,
                testType = results.firstOrNull()?.testType ?: "",
                dataSize = results.firstOrNull()?.dataSize ?: 0,
                loadLevel = results.firstOrNull()?.loadLevel ?: 0,
                averageResponseTime = 0.0,
                minResponseTime = 0,
                maxResponseTime = 0,
                averageMemoryUsage = 0.0,
                averageCpuUsage = 0.0,
                successRate = 0.0,
                standardDeviation = 0.0
            )
        }
        
        val responseTimes = successfulResults.map { it.totalResponseTime }
        val memoryUsages = successfulResults.map { it.memoryUsage }
        val cpuUsages = successfulResults.map { it.cpuUsage }
        
        val avgResponseTime = responseTimes.average()
        val minResponseTime = responseTimes.minOrNull() ?: 0
        val maxResponseTime = responseTimes.maxOrNull() ?: 0
        val avgMemoryUsage = memoryUsages.average()
        val avgCpuUsage = cpuUsages.average()
        val successRate = (successfulResults.size.toDouble() / results.size) * 100
        
        // Calculate standard deviation
        val variance = responseTimes.map { (it - avgResponseTime) * (it - avgResponseTime) }.average()
        val standardDeviation = kotlin.math.sqrt(variance)
        
        return TestStatistics(
            apiType = results.first().apiType,
            testType = results.first().testType,
            dataSize = results.first().dataSize,
            loadLevel = results.first().loadLevel,
            averageResponseTime = avgResponseTime,
            minResponseTime = minResponseTime,
            maxResponseTime = maxResponseTime,
            averageMemoryUsage = avgMemoryUsage,
            averageCpuUsage = avgCpuUsage,
            successRate = successRate,
            standardDeviation = standardDeviation
        )
    }
    
    /**
     * Menyimpan hasil pengujian ke file CSV
     */
    private fun saveResultsToFile(
        results: List<TestResult>, 
        statistics: List<TestStatistics>
    ) {
        try {
            val timestamp = dateFormat.format(Date())
            
            // Try to save to Downloads folder first, fallback to app directory
            val downloadsDir = android.os.Environment.getExternalStoragePublicDirectory(android.os.Environment.DIRECTORY_DOWNLOADS)
            val appDir = context.getExternalFilesDir(null)
            val targetDir = if (downloadsDir.exists() && downloadsDir.canWrite()) downloadsDir else appDir
            
            // Create SkincareApp_Results subfolder
            val resultsDir = File(targetDir, "SkincareApp_Results")
            if (!resultsDir.exists()) {
                resultsDir.mkdirs()
            }
            
            // Save raw results
            val rawResultsFile = File(resultsDir, "performance_test_raw_$timestamp.csv")
            FileWriter(rawResultsFile).use { writer ->
                writer.append("API_TYPE,TEST_TYPE,TEST_SCENARIO,DATA_SIZE,LOAD_LEVEL,NETWORK_TIME,PARSING_TIME,TOTAL_RESPONSE_TIME,TIME_TO_FIRST_RENDER,MEMORY_USAGE,PEAK_MEMORY_USAGE,CPU_USAGE,PEAK_CPU_USAGE,TIMESTAMP,SUCCESS,ERROR_MESSAGE,DATA_TRANSFER_SIZE,QUERY_COMPLEXITY\n")
                
                results.forEach { result ->
                    writer.append("${result.apiType},${result.testType},${result.testScenario},${result.dataSize},${result.loadLevel},")
                    writer.append("${result.networkTime},${result.parsingTime},${result.totalResponseTime},${result.timeToFirstRender},")
                    writer.append("${result.memoryUsage},${result.peakMemoryUsage},${result.cpuUsage},${result.peakCpuUsage},")
                    writer.append("${result.timestamp},${result.success},${result.errorMessage ?: ""},${result.dataTransferSize},${result.queryComplexity}\n")
                }
            }
            
            // Save statistics
            val statisticsFile = File(resultsDir, "performance_test_stats_$timestamp.csv")
            FileWriter(statisticsFile).use { writer ->
                writer.append("API_TYPE,TEST_TYPE,DATA_SIZE,LOAD_LEVEL,AVG_RESPONSE_TIME,MIN_RESPONSE_TIME,MAX_RESPONSE_TIME,")
                writer.append("AVG_MEMORY_USAGE,AVG_CPU_USAGE,SUCCESS_RATE,STANDARD_DEVIATION\n")
                
                statistics.forEach { stat ->
                    writer.append("${stat.apiType},${stat.testType},${stat.dataSize},${stat.loadLevel},")
                    writer.append("${stat.averageResponseTime},${stat.minResponseTime},${stat.maxResponseTime},")
                    writer.append("${stat.averageMemoryUsage},${stat.averageCpuUsage},${stat.successRate},${stat.standardDeviation}\n")
                }
            }
            
            Log.d("PerformanceTest", "Results saved to: ${rawResultsFile.absolutePath}")
            Log.d("PerformanceTest", "Statistics saved to: ${statisticsFile.absolutePath}")
            
        } catch (e: Exception) {
            Log.e("PerformanceTest", "Error saving results: ${e.message}")
        }
    }
    
    /**
     * Menjalankan pengujian cepat untuk debugging
     */
    suspend fun runQuickTest(apiType: ApiType): TestResult {
        Log.d("PerformanceTest", "Running quick test for ${apiType.name}")
        
        try {
            // Reset trackers before test
            memoryTracker.reset()
            cpuTracker.reset()
            
            memoryTracker.startTracking()
            cpuTracker.startTracking()
            
            val startTime = SystemClock.elapsedRealtime()
            
            // Use collect instead of first() to ensure proper Flow handling
            var response: com.skincare.apitest.model.ApiResponse<List<Product>>? = null
            repository.getProducts(apiType, 50).collect { apiResponse ->
                response = apiResponse
            }
            
            val endTime = SystemClock.elapsedRealtime()
            val responseTime = endTime - startTime
            
            val memoryUsage = memoryTracker.getCurrentMemoryUsage()
            val peakMemoryUsage = memoryTracker.getPeakMemoryUsage()
            val cpuUsage = cpuTracker.getCurrentCpuUsage()
            val peakCpuUsage = cpuTracker.getPeakCpuUsage()
            
            memoryTracker.stopTracking()
            cpuTracker.stopTracking()
            
            val success = response is com.skincare.apitest.model.ApiResponse.Success
            val errorMessage = (response as? com.skincare.apitest.model.ApiResponse.Error)?.message
            
            val result = TestResult(
                apiType = apiType,
                testType = "QUICK_TEST",
                testScenario = "INDIVIDUAL_PRODUCTS_ONLY",
                dataSize = 50,
                loadLevel = 100,
                networkTime = 0L,
                parsingTime = 0L,
                totalResponseTime = responseTime,
                timeToFirstRender = 0L,
                memoryUsage = memoryUsage,
                peakMemoryUsage = peakMemoryUsage,
                cpuUsage = cpuUsage,
                peakCpuUsage = peakCpuUsage,
                timestamp = System.currentTimeMillis(),
                success = success,
                errorMessage = errorMessage,
                dataTransferSize = 0L,
                queryComplexity = 0
            )
            
            Log.d("PerformanceTest", "Quick test completed - API: ${apiType.name}, Time: ${responseTime}ms, Memory: ${memoryUsage}bytes, CPU: ${cpuUsage}%, Success: $success")
            
            return result
            
        } catch (e: Exception) {
            Log.e("PerformanceTest", "Error in quick test: ${e.message}")
            Log.e("PerformanceTest", "Error stack trace: ${e.stackTraceToString()}")
            throw e
        }
    }

    /**
     * Log summary for research analysis
     */
    private fun logResearchSummary(results: List<TestResult>) {
        val retrofitResults = results.filter { it.apiType == ApiType.RETROFIT }
        val graphqlResults = results.filter { it.apiType == ApiType.GRAPHQL }

        val avgRetrofitResponseTime = retrofitResults.filter { it.success && it.totalResponseTime > 0 }.map { it.totalResponseTime }.average()
        val avgGraphqlResponseTime = graphqlResults.filter { it.success && it.totalResponseTime > 0 }.map { it.totalResponseTime }.average()

        val avgRetrofitMemoryUsage = retrofitResults.filter { it.success }.map { it.memoryUsage }.average()
        val avgGraphqlMemoryUsage = graphqlResults.filter { it.success }.map { it.memoryUsage }.average()

        val avgRetrofitCpuUsage = retrofitResults.filter { it.success }.map { it.cpuUsage }.average()
        val avgGraphqlCpuUsage = graphqlResults.filter { it.success }.map { it.cpuUsage }.average()

        val avgRetrofitSuccessRate = (retrofitResults.filter { it.success }.size.toDouble() / retrofitResults.size) * 100
        val avgGraphqlSuccessRate = (graphqlResults.filter { it.success }.size.toDouble() / graphqlResults.size) * 100

        Log.d("PerformanceTest", "=== Research Summary ===")
        Log.d("PerformanceTest", "Average Response Time - Retrofit: $avgRetrofitResponseTime ms, GraphQL: $avgGraphqlResponseTime ms")
        Log.d("PerformanceTest", "Average Memory Usage - Retrofit: $avgRetrofitMemoryUsage bytes, GraphQL: $avgGraphqlMemoryUsage bytes")
        Log.d("PerformanceTest", "Average CPU Usage - Retrofit: $avgRetrofitCpuUsage%, GraphQL: $avgGraphqlCpuUsage%")
        Log.d("PerformanceTest", "Average Success Rate - Retrofit: $avgRetrofitSuccessRate%, GraphQL: $avgGraphqlSuccessRate%")
        Log.d("PerformanceTest", "=========================")
    }
} 