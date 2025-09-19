package com.skincare.apitest.performance

import android.Manifest
import android.content.pm.PackageManager
import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.Button
import android.widget.ProgressBar
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.lifecycle.lifecycleScope
import com.skincare.apitest.R
import com.skincare.apitest.model.ApiType
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

import java.io.File

/**
 * Activity untuk menjalankan pengujian performa API
 */
class PerformanceTestActivity : AppCompatActivity() {
    
    private lateinit var performanceTestManager: PerformanceTestManager
    private lateinit var complexityAnalyzer: ComplexityAnalyzer
    
    // UI Components
    private lateinit var btnRunCompleteTest: Button
    private lateinit var btnRunQuickTest: Button
    private lateinit var btnAnalyzeComplexity: Button
    private lateinit var btnExportResults: Button
    private lateinit var btnTestDisplay: Button
    private lateinit var btnDebugApi: Button
    private lateinit var btnScrollTop: Button
    private lateinit var btnScrollBottom: Button
    private lateinit var progressBar: ProgressBar
    private lateinit var tvStatus: TextView
    private lateinit var tvResults: TextView
    private lateinit var scrollView: android.widget.ScrollView
    
    private val STORAGE_PERMISSION_REQUEST = 1001
    
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_performance_test)
        
        initializeComponents()
        setupPermissions()
        setupClickListeners()
    }
    
    private fun initializeComponents() {
        performanceTestManager = PerformanceTestManager(this)
        complexityAnalyzer = ComplexityAnalyzer(this)
        
        btnRunCompleteTest = findViewById(R.id.btnRunCompleteTest)
        btnRunQuickTest = findViewById(R.id.btnRunQuickTest)
        btnAnalyzeComplexity = findViewById(R.id.btnAnalyzeComplexity)
        btnExportResults = findViewById(R.id.btnExportResults)
        btnTestDisplay = findViewById(R.id.btnTestDisplay)
        btnDebugApi = findViewById(R.id.btnDebugApi)
        btnScrollTop = findViewById(R.id.btnScrollTop)
        btnScrollBottom = findViewById(R.id.btnScrollBottom)
        progressBar = findViewById(R.id.progressBar)
        tvStatus = findViewById(R.id.tvStatus)
        tvResults = findViewById(R.id.tvResults)
        scrollView = findViewById(R.id.scrollView)
        
        // Set initial state
        updateUIState(false)
    }
    
    private fun setupPermissions() {
        if (ContextCompat.checkSelfPermission(
                this,
                Manifest.permission.WRITE_EXTERNAL_STORAGE
            ) != PackageManager.PERMISSION_GRANTED
        ) {
            ActivityCompat.requestPermissions(
                this,
                arrayOf(Manifest.permission.WRITE_EXTERNAL_STORAGE),
                STORAGE_PERMISSION_REQUEST
            )
        }
    }
    
    private fun setupClickListeners() {
        btnRunCompleteTest.setOnClickListener {
            runCompletePerformanceTest()
        }
        
        btnRunQuickTest.setOnClickListener {
            runQuickTest()
        }
        
        btnAnalyzeComplexity.setOnClickListener {
            analyzeComplexity()
        }
        
        btnExportResults.setOnClickListener {
            exportResults()
        }
        
        btnTestDisplay.setOnClickListener {
            testResultsDisplay()
        }
        
        btnDebugApi.setOnClickListener {
            debugApiCalls()
        }
        
        btnScrollTop.setOnClickListener {
            scrollToTop()
        }
        
        btnScrollBottom.setOnClickListener {
            scrollToBottom()
        }
    }
    
    /**
     * Debug API calls untuk mengidentifikasi masalah
     */
    private fun debugApiCalls() {
        updateUIState(true)
        tvStatus.text = "Testing comprehensive database connectivity..."

        lifecycleScope.launch {
            try {
                // Use the new comprehensive database connectivity test
                val result = performanceTestManager.testDatabaseConnectivity()
                tvResults.text = result
                tvStatus.text = "Database connectivity test completed. Check results below."

                Log.d("PerformanceTest", "Database connectivity results:\n$result")

            } catch (e: Exception) {
                tvStatus.text = "Database connectivity test failed: ${e.message}"
                Log.e("PerformanceTest", "Database connectivity test error", e)
            } finally {
                updateUIState(false)
            }
        }
    }

    /**
     * Menjalankan pengujian performa lengkap
     */
    private fun runCompletePerformanceTest() {
        updateUIState(true)
        updateStatus("Memulai pengujian performa lengkap...")
        
        lifecycleScope.launch(Dispatchers.IO) {
            try {
                val results = performanceTestManager.runCompletePerformanceTest()
                
                withContext(Dispatchers.Main) {
                    displayResults(results)
                    updateStatus("Pengujian performa selesai!")
                    updateUIState(false)
                }
                
            } catch (e: Exception) {
                Log.e("PerformanceTest", "Error in complete test: ${e.message}")
                withContext(Dispatchers.Main) {
                    updateStatus("Error: ${e.message}")
                    updateUIState(false)
                }
            }
        }
    }
    
    /**
     * Menjalankan pengujian cepat
     */
    private fun runQuickTest() {
        updateUIState(true)
        updateStatus("Menjalankan pengujian cepat dengan protokol fair testing...")
        
        lifecycleScope.launch(Dispatchers.IO) {
            try {
                // 🎯 FAIR QUICK TEST PROTOCOL
                Log.d("PerformanceTest", "=== STARTING FAIR QUICK TEST PROTOCOL ===")
                
                // 1. Warm-up both APIs
                updateStatus("Phase 1: Warming up both APIs...")
                for (apiType in listOf(ApiType.RETROFIT, ApiType.GRAPHQL)) {
                    Log.d("PerformanceTest", "Warming up ${apiType.name}...")
                    repeat(3) { // 3 warm-up calls
                        try {
                            performanceTestManager.debugApiCall(apiType, "products")
                            kotlinx.coroutines.delay(100)
                        } catch (e: Exception) {
                            Log.w("PerformanceTest", "Warm-up failed for ${apiType.name}: ${e.message}")
                        }
                    }
                }
                
                // 2. Cooldown period
                updateStatus("Phase 2: Cooldown period...")
                kotlinx.coroutines.delay(1000)
                
                // 3. Test with alternating order
                updateStatus("Phase 3: Testing with alternating order...")
                
                // Round 1: Retrofit first, then Apollo
                updateStatus("Round 1: Testing Retrofit → Apollo...")
                val retrofitResult = performanceTestManager.runQuickTest(ApiType.RETROFIT)
                kotlinx.coroutines.delay(500) // Cooldown between APIs
                val apolloResult = performanceTestManager.runQuickTest(ApiType.GRAPHQL)
                
                // Round 2: Apollo first, then Retrofit (to eliminate order bias)
                updateStatus("Round 2: Testing Apollo → Retrofit...")
                kotlinx.coroutines.delay(1000) // Longer cooldown between rounds
                val apolloResult2 = performanceTestManager.runQuickTest(ApiType.GRAPHQL)
                kotlinx.coroutines.delay(500) // Cooldown between APIs
                val retrofitResult2 = performanceTestManager.runQuickTest(ApiType.RETROFIT)
                
                // Calculate average results to eliminate order bias
                val avgRetrofitResult = PerformanceTestManager.TestResult(
                    apiType = ApiType.RETROFIT,
                    testType = "QUICK_TEST_AVERAGE",
                    testScenario = "INDIVIDUAL_PRODUCTS_ONLY",
                    dataSize = 50,
                    loadLevel = 100,
                    networkTime = 0L,
                    parsingTime = 0L,
                    totalResponseTime = (retrofitResult.totalResponseTime + retrofitResult2.totalResponseTime) / 2,
                    timeToFirstRender = 0L,
                    memoryUsage = (retrofitResult.memoryUsage + retrofitResult2.memoryUsage) / 2,
                    peakMemoryUsage = (retrofitResult.peakMemoryUsage + retrofitResult2.peakMemoryUsage) / 2,
                    cpuUsage = (retrofitResult.cpuUsage + retrofitResult2.cpuUsage) / 2,
                    peakCpuUsage = (retrofitResult.peakCpuUsage + retrofitResult2.peakCpuUsage) / 2,
                    timestamp = System.currentTimeMillis(),
                    success = retrofitResult.success && retrofitResult2.success,
                    errorMessage = if (retrofitResult.success && retrofitResult2.success) null else "One or both rounds failed",
                    dataTransferSize = 0L,
                    queryComplexity = 0
                )
                
                val avgApolloResult = PerformanceTestManager.TestResult(
                    apiType = ApiType.GRAPHQL,
                    testType = "QUICK_TEST_AVERAGE",
                    testScenario = "INDIVIDUAL_PRODUCTS_ONLY",
                    dataSize = 50,
                    loadLevel = 100,
                    networkTime = 0L,
                    parsingTime = 0L,
                    totalResponseTime = (apolloResult.totalResponseTime + apolloResult2.totalResponseTime) / 2,
                    timeToFirstRender = 0L,
                    memoryUsage = (apolloResult.memoryUsage + apolloResult2.memoryUsage) / 2,
                    peakMemoryUsage = (apolloResult.peakMemoryUsage + apolloResult2.peakMemoryUsage) / 2,
                    cpuUsage = (apolloResult.cpuUsage + apolloResult2.cpuUsage) / 2,
                    peakCpuUsage = (apolloResult.peakCpuUsage + apolloResult2.peakCpuUsage) / 2,
                    timestamp = System.currentTimeMillis(),
                    success = apolloResult.success && apolloResult2.success,
                    errorMessage = if (apolloResult.success && apolloResult2.success) null else "One or both rounds failed",
                    dataTransferSize = 0L,
                    queryComplexity = 0
                )
                
                withContext(Dispatchers.Main) {
                    displayQuickTestResults(avgRetrofitResult, avgApolloResult)
                    updateStatus("Pengujian cepat selesai dengan protokol fair testing!")
                    updateUIState(false)
                }
                
                Log.d("PerformanceTest", "=== FAIR QUICK TEST PROTOCOL COMPLETED ===")
                
            } catch (e: Exception) {
                Log.e("PerformanceTest", "Error in quick test: ${e.message}")
                withContext(Dispatchers.Main) {
                    updateStatus("Error: ${e.message}")
                    updateUIState(false)
                }
            }
        }
    }
    
    /**
     * Menganalisis kompleksitas implementasi
     */
    private fun analyzeComplexity() {
        updateUIState(true)
        updateStatus("Menganalisis kompleksitas implementasi...")
        
        lifecycleScope.launch(Dispatchers.IO) {
            try {
                val results = complexityAnalyzer.analyzeComplexity()
                val summary = complexityAnalyzer.getComplexitySummaryFromResults(results)
                
                withContext(Dispatchers.Main) {
                    displayComplexityResults(results, summary)
                    updateStatus("Analisis kompleksitas selesai!")
                    updateUIState(false)
                }
                
            } catch (e: Exception) {
                Log.e("ComplexityAnalyzer", "Error in complexity analysis: ${e.message}")
                withContext(Dispatchers.Main) {
                    updateStatus("Error: ${e.message}")
                    updateUIState(false)
                }
            }
        }
    }
    
    /**
     * Export hasil pengujian
     */
    private fun exportResults() {
        updateStatus("Mengexport hasil pengujian...")
        
        lifecycleScope.launch(Dispatchers.IO) {
            try {
                // Check both possible locations
                val downloadsDir = android.os.Environment.getExternalStoragePublicDirectory(android.os.Environment.DIRECTORY_DOWNLOADS)
                val appDir = getExternalFilesDir(null)
                
                val resultsDir = File(downloadsDir, "SkincareApp_Results")
                val appResultsDir = File(appDir, "SkincareApp_Results")
                
                // Look for files in both locations
                val files = mutableListOf<File>()
                
                if (resultsDir.exists()) {
                    resultsDir.listFiles { file ->
                        file.name.startsWith("performance_test_") || file.name.startsWith("complexity_analysis_")
                    }?.let { files.addAll(it) }
                }
                
                if (appResultsDir.exists()) {
                    appResultsDir.listFiles { file ->
                        file.name.startsWith("performance_test_") || file.name.startsWith("complexity_analysis_")
                    }?.let { files.addAll(it) }
                }
                
                val sortedFiles = files.sortedByDescending { it.lastModified() }
                
                withContext(Dispatchers.Main) {
                    if (sortedFiles.isNotEmpty()) {
                        val sb = StringBuilder()
                        sb.append("📁 EXPORTED FILES LOCATION:\n\n")
                        
                        // Show primary location
                        if (resultsDir.exists()) {
                            sb.append("✅ Primary Location: ${resultsDir.absolutePath}\n")
                            sb.append("   (Downloads/SkincareApp_Results folder)\n\n")
                        }
                        
                        if (appResultsDir.exists()) {
                            sb.append("📱 Backup Location: ${appResultsDir.absolutePath}\n\n")
                        }
                        
                        sb.append("📄 Available Files:\n")
                        
                        sortedFiles.forEach { file ->
                            val fileSize = String.format("%.2f", file.length() / 1024.0)
                            val lastModified = java.text.SimpleDateFormat("yyyy-MM-dd HH:mm:ss", java.util.Locale.getDefault()).format(java.util.Date(file.lastModified()))
                            sb.append("  • ${file.name} (${fileSize} KB) - ${lastModified}\n")
                        }
                        
                        sb.append("\n💡 How to access these files:\n")
                        sb.append("  1. 📱 On Device: File Manager → Downloads → SkincareApp_Results\n")
                        sb.append("  2. 💻 Android Studio: Device File Explorer → Downloads → SkincareApp_Results\n")
                        sb.append("  3. 🔗 ADB Command: adb pull ${resultsDir.absolutePath} C:\\HASFI\\KULIAH\\SKRIPSI\\Hasil_Analisis\n")
                        sb.append("  4. 📂 Direct Copy: Copy from Downloads/SkincareApp_Results to your thesis folder\n")
                        
                        tvResults.text = sb.toString()
                        updateStatus("Export completed! Files saved to Downloads/SkincareApp_Results folder.")
                    } else {
                        tvResults.text = "❌ No exported files found. Run tests first to generate results."
                        updateStatus("No files to export")
                    }
                }
                
            } catch (e: Exception) {
                Log.e("PerformanceTest", "Error exporting results: ${e.message}")
                withContext(Dispatchers.Main) {
                    tvResults.text = "❌ Export error: ${e.message}"
                    updateStatus("Export failed")
                }
            }
        }
    }
    
    /**
     * Menampilkan hasil pengujian performa
     */
    private fun displayResults(results: List<PerformanceTestManager.TestStatistics>) {
        val sb = StringBuilder()
        sb.append("=== HASIL PENGUJIAN PERFORMA LENGKAP ===\n\n")
        
        // Group by API type
        val retrofitResults = results.filter { it.apiType == ApiType.RETROFIT }
        val apolloResults = results.filter { it.apiType == ApiType.GRAPHQL }
        
        // Retrofit Results
        sb.append("📊 RETROFIT RESULTS:\n")
        retrofitResults.forEach { result ->
            sb.append("${result.testType} (${result.dataSize} items, ${result.loadLevel} req/min):\n")
            sb.append("  • Avg: ${String.format("%.1f", result.averageResponseTime)}ms | Min: ${result.minResponseTime}ms | Max: ${result.maxResponseTime}ms\n")
            sb.append("  • Memory: ${String.format("%.2f", result.averageMemoryUsage / 1024 / 1024)}MB | CPU: ${String.format("%.1f", result.averageCpuUsage)}%\n")
            sb.append("  • Success: ${String.format("%.1f", result.successRate)}% | StdDev: ${String.format("%.1f", result.standardDeviation)}ms\n\n")
        }
        
        // Apollo Results
        sb.append("📊 APOLLO RESULTS:\n")
        apolloResults.forEach { result ->
            sb.append("${result.testType} (${result.dataSize} items, ${result.loadLevel} req/min):\n")
            sb.append("  • Avg: ${String.format("%.1f", result.averageResponseTime)}ms | Min: ${result.minResponseTime}ms | Max: ${result.maxResponseTime}ms\n")
            sb.append("  • Memory: ${String.format("%.2f", result.averageMemoryUsage / 1024 / 1024)}MB | CPU: ${String.format("%.1f", result.averageCpuUsage)}%\n")
            sb.append("  • Success: ${String.format("%.1f", result.successRate)}% | StdDev: ${String.format("%.1f", result.standardDeviation)}ms\n\n")
        }
        
        // Detailed Comparison
        sb.append("📈 DETAILED COMPARISON:\n")
        val retrofitAvgResponse = retrofitResults.map { it.averageResponseTime }.average()
        val apolloAvgResponse = apolloResults.map { it.averageResponseTime }.average()
        val retrofitAvgMemory = retrofitResults.map { it.averageMemoryUsage }.average()
        val apolloAvgMemory = apolloResults.map { it.averageMemoryUsage }.average()
        val retrofitAvgCpu = retrofitResults.map { it.averageCpuUsage }.average()
        val apolloAvgCpu = apolloResults.map { it.averageCpuUsage }.average()
        val retrofitAvgSuccess = retrofitResults.map { it.successRate }.average()
        val apolloAvgSuccess = apolloResults.map { it.successRate }.average()
        
        sb.append("  • Response Time: Retrofit ${String.format("%.1f", retrofitAvgResponse)}ms vs Apollo ${String.format("%.1f", apolloAvgResponse)}ms\n")
        sb.append("  • Memory Usage: Retrofit ${String.format("%.2f", retrofitAvgMemory / 1024 / 1024)}MB vs Apollo ${String.format("%.2f", apolloAvgMemory / 1024 / 1024)}MB\n")
        sb.append("  • CPU Usage: Retrofit ${String.format("%.1f", retrofitAvgCpu)}% vs Apollo ${String.format("%.1f", apolloAvgCpu)}%\n")
        sb.append("  • Success Rate: Retrofit ${String.format("%.1f", retrofitAvgSuccess)}% vs Apollo ${String.format("%.1f", apolloAvgSuccess)}%\n\n")
        
        // Performance Analysis
        sb.append("🔍 PERFORMANCE ANALYSIS:\n")
        val responseDiff = apolloAvgResponse - retrofitAvgResponse
        val memoryDiff = apolloAvgMemory - retrofitAvgMemory
        val cpuDiff = apolloAvgCpu - retrofitAvgCpu
        
        sb.append("  • Response Time: ${if (responseDiff > 0) "+" else ""}${String.format("%.1f", responseDiff)}ms (${if (responseDiff > 0) "Apollo slower" else "Apollo faster"})\n")
        sb.append("  • Memory Usage: ${if (memoryDiff > 0) "+" else ""}${String.format("%.2f", memoryDiff / 1024 / 1024)}MB (${if (memoryDiff > 0) "Apollo uses more" else "Apollo uses less"})\n")
        sb.append("  • CPU Usage: ${if (cpuDiff > 0) "+" else ""}${String.format("%.1f", cpuDiff)}% (${if (cpuDiff > 0) "Apollo uses more" else "Apollo uses less"})\n\n")
        
        // Test Coverage Summary
        sb.append("📋 TEST COVERAGE SUMMARY:\n")
        sb.append("  • Total Scenarios: ${results.size} | Retrofit: ${retrofitResults.size} | Apollo: ${apolloResults.size}\n")
        sb.append("  • Data Sizes: ${results.map { it.dataSize }.distinct().sorted()}\n")
        sb.append("  • Load Levels: ${results.map { it.loadLevel }.distinct().sorted()}\n")
        sb.append("  • Test Types: ${results.map { it.testType }.distinct()}\n")
        
        val resultText = sb.toString()
        Log.d("PerformanceTest", "Displaying results with ${resultText.length} characters")
        
        // Handle long results properly
        handleLongResults(resultText)
    }
    
    /**
     * Menampilkan hasil pengujian cepat
     */
    private fun displayQuickTestResults(
        retrofitResult: PerformanceTestManager.TestResult,
        apolloResult: PerformanceTestManager.TestResult
    ) {
        val sb = StringBuilder()
        sb.append("=== HASIL PENGUJIAN CEPAT ===\n\n")
        
        sb.append("📊 RETROFIT:\n")
        sb.append("  • Response Time: ${retrofitResult.totalResponseTime} ms\n")
        sb.append("  • Memory Usage: ${String.format("%.2f", retrofitResult.memoryUsage.toDouble() / 1024.0 / 1024.0)} MB\n")
        sb.append("  • CPU Usage: ${String.format("%.2f", retrofitResult.cpuUsage)}%\n")
        sb.append("  • Success: ${if (retrofitResult.success) "✅" else "❌"}\n\n")
        
        sb.append("📊 APOLLO:\n")
        sb.append("  • Response Time: ${apolloResult.totalResponseTime} ms\n")
        sb.append("  • Memory Usage: ${String.format("%.2f", apolloResult.memoryUsage.toDouble() / 1024.0 / 1024.0)} MB\n")
        sb.append("  • CPU Usage: ${String.format("%.2f", apolloResult.cpuUsage)}%\n")
        sb.append("  • Success: ${if (apolloResult.success) "✅" else "❌"}\n\n")
        
        // Quick comparison
        val responseDiff = apolloResult.totalResponseTime - retrofitResult.totalResponseTime
        val memoryDiff = apolloResult.memoryUsage - retrofitResult.memoryUsage
        val cpuDiff = apolloResult.cpuUsage - retrofitResult.cpuUsage
        
        sb.append("📈 QUICK COMPARISON:\n")
        sb.append("  • Response Time Diff: ${if (responseDiff > 0) "+" else ""}${responseDiff}ms\n")
        sb.append("  • Memory Diff: ${if (memoryDiff > 0) "+" else ""}${String.format("%.2f", memoryDiff.toDouble() / 1024.0 / 1024.0)}MB\n")
        sb.append("  • CPU Diff: ${if (cpuDiff > 0) "+" else ""}${String.format("%.2f", cpuDiff)}%\n")
        
        val resultText = sb.toString()
        Log.d("PerformanceTest", "Displaying quick test results with ${resultText.length} characters")
        
        // Handle long results properly
        handleLongResults(resultText)
    }
    
    /**
     * Menampilkan hasil analisis kompleksitas
     */
    private fun displayComplexityResults(
        results: List<ComplexityAnalyzer.ComplexityResult>,
        summary: ComplexityAnalyzer.ComplexitySummary
    ) {
        val sb = StringBuilder()
        sb.append("=== HASIL ANALISIS KOMPLEKSITAS ===\n\n")
        
        // Detailed results
        results.forEach { result ->
            sb.append("📊 ${result.apiType} - ${result.component}:\n")
            sb.append("  • Lines of Code: ${result.linesOfCode}\n")
            sb.append("  • Cyclomatic Complexity: ${result.cyclomaticComplexity}\n")
            sb.append("  • Cognitive Complexity: ${result.cognitiveComplexity}\n")
            sb.append("  • Error Handling Lines: ${result.errorHandlingLines}\n")
            sb.append("  • Documentation Lines: ${result.documentationLines}\n")
            sb.append("  • Implementation Difficulty: ${result.implementationDifficulty}\n")
            sb.append("  • Maintainability Score: ${String.format("%.2f", result.maintainabilityScore)}\n")
            sb.append("  • Readability Score: ${String.format("%.2f", result.readabilityScore)}\n\n")
        }
        
        // Summary
        sb.append("📈 COMPLEXITY SUMMARY:\n")
        sb.append("  • Total Lines: Retrofit ${summary.retrofitTotalLines} vs Apollo ${summary.apolloTotalLines}\n")
        sb.append("  • Avg Complexity: Retrofit ${String.format("%.1f", summary.retrofitAvgComplexity)} vs Apollo ${String.format("%.1f", summary.apolloAvgComplexity)}\n")
        sb.append("  • Maintainability: Retrofit ${String.format("%.2f", summary.retrofitMaintainability)} vs Apollo ${String.format("%.2f", summary.apolloMaintainability)}\n")
        sb.append("  • Readability: Retrofit ${String.format("%.2f", summary.retrofitReadability)} vs Apollo ${String.format("%.2f", summary.apolloReadability)}\n")
        
        val resultText = sb.toString()
        Log.d("PerformanceTest", "Displaying complexity results with ${resultText.length} characters")
        
        // Handle long results properly
        handleLongResults(resultText)
    }
    
    /**
     * Update UI state
     */
    private fun updateUIState(isRunning: Boolean) {
        btnRunCompleteTest.isEnabled = !isRunning
        btnRunQuickTest.isEnabled = !isRunning
        btnAnalyzeComplexity.isEnabled = !isRunning
        btnExportResults.isEnabled = !isRunning
        btnTestDisplay.isEnabled = !isRunning
        
        progressBar.visibility = if (isRunning) View.VISIBLE else View.GONE
    }
    
    /**
     * Update status text
     */
    private fun updateStatus(status: String) {
        tvStatus.text = status
        Log.d("PerformanceTest", status)
    }
    
    /**
     * Scroll to top of results
     */
    private fun scrollToTop() {
        scrollView.post {
            scrollView.smoothScrollTo(0, 0)
        }
    }
    
    /**
     * Scroll to bottom of results
     */
    private fun scrollToBottom() {
        scrollView.post {
            val scrollAmount = tvResults.height - scrollView.height
            if (scrollAmount > 0) {
                scrollView.smoothScrollTo(0, scrollAmount)
            }
        }
    }
    
    /**
     * Handle very long results by ensuring proper display
     */
    private fun handleLongResults(resultText: String) {
        Log.d("PerformanceTest", "Handling results with ${resultText.length} characters")
        
        // Check if text is too long for TextView
        if (resultText.length > 100000) { // 100KB limit
            Log.w("PerformanceTest", "Results are very long (${resultText.length} chars), truncating for display")
            
            // Show first part with indication
            val truncatedText = resultText.take(50000) + "\n\n... [RESULTS TRUNCATED - CHECK EXPORTED FILES FOR COMPLETE DATA] ...\n\n" + 
                               resultText.takeLast(50000)
            
            tvResults.post {
                tvResults.text = truncatedText
                tvResults.invalidate()
                scrollView.invalidate()
                scrollToTop()
            }
        } else {
            // Normal display
            tvResults.post {
                tvResults.text = resultText
                tvResults.invalidate()
                scrollView.invalidate()
                scrollToTop()
            }
        }
    }
    
    /**
     * Force refresh the results display
     */
    private fun forceRefreshResults() {
        tvResults.post {
            tvResults.requestLayout()
            scrollView.requestLayout()
            tvResults.invalidate()
            scrollView.invalidate()
            
            // Ensure the TextView is visible
            tvResults.visibility = View.VISIBLE
            scrollView.visibility = View.VISIBLE
            
            Log.d("PerformanceTest", "Forced refresh of results display")
        }
    }
    
    /**
     * Test the results display functionality
     */
    private fun testResultsDisplay() {
        val testText = StringBuilder()
        testText.append("🧪 TEST DISPLAY FUNCTIONALITY\n\n")
        testText.append("This is a test to verify that the results display is working properly.\n\n")
        
        // Add some sample data
        testText.append("📊 SAMPLE RETROFIT RESULTS:\n")
        testText.append("  • Response Time: 245 ms\n")
        testText.append("  • Memory Usage: 2.34 MB\n")
        testText.append("  • CPU Usage: 15.7%\n")
        testText.append("  • Success: ✅\n\n")
        
        testText.append("📊 SAMPLE APOLLO RESULTS:\n")
        testText.append("  • Response Time: 198 ms\n")
        testText.append("  • Memory Usage: 2.12 MB\n")
        testText.append("  • CPU Usage: 14.2%\n")
        testText.append("  • Success: ✅\n\n")
        
        testText.append("📈 SAMPLE COMPARISON:\n")
        testText.append("  • Response Time Diff: -47ms (Apollo faster)\n")
        testText.append("  • Memory Diff: -0.22MB (Apollo uses less)\n")
        testText.append("  • CPU Diff: -1.5% (Apollo uses less)\n\n")
        
        // Add more lines to test scrolling
        repeat(50) { i ->
            testText.append("Test line ${i + 1}: This is a long line to test scrolling functionality and ensure all content is visible. ")
            testText.append("Line ${i + 1} contains sample data to verify the TextView can handle long content properly.\n")
        }
        
        testText.append("\n✅ If you can see this message and scroll through all the test lines, the display is working correctly!\n")
        testText.append("✅ You can use the ⬆️ Top and ⬇️ Bottom buttons to navigate.\n")
        testText.append("✅ The text should be selectable for copying.\n")
        
        val testResultText = testText.toString()
        Log.d("PerformanceTest", "Testing display with ${testResultText.length} characters")
        
        // Use the handleLongResults method
        handleLongResults(testResultText)
        
        updateStatus("Test display completed - check results area")
    }
    
    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<out String>,
        grantResults: IntArray
    ) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        
        when (requestCode) {
            STORAGE_PERMISSION_REQUEST -> {
                if (grantResults.isNotEmpty() && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    Log.d("PerformanceTest", "Storage permission granted")
                } else {
                    Log.w("PerformanceTest", "Storage permission denied")
                    updateStatus("Storage permission diperlukan untuk menyimpan hasil pengujian")
                }
            }
        }
    }
} 