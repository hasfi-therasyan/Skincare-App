package com.skincare.apitest.performance

import android.app.ActivityManager
import android.content.Context
import android.os.Debug
import android.os.Process
import android.util.Log
import java.io.BufferedReader
import java.io.FileReader
import java.io.IOException
import kotlin.math.ln

/**
 * Memory Tracker untuk memantau penggunaan memori selama pengujian API
 */
class MemoryTracker {
    
    private var initialMemory: Long = 0
    private var peakMemory: Long = 0
    private var isTracking: Boolean = false
    
    /**
     * Memulai tracking memory
     */
    fun startTracking() {
        if (isTracking) {
            Log.w("MemoryTracker", "Memory tracking already started")
            return
        }
        
        isTracking = true
        initialMemory = getCurrentMemoryUsage()
        peakMemory = initialMemory
        
        Log.d("MemoryTracker", "Started memory tracking. Initial: ${formatBytes(initialMemory)}")
    }
    
    /**
     * Menghentikan tracking memory
     */
    fun stopTracking() {
        if (!isTracking) {
            Log.w("MemoryTracker", "Memory tracking not started")
            return
        }
        
        isTracking = false
        val finalMemory = getCurrentMemoryUsage()
        peakMemory = if (peakMemory > finalMemory) peakMemory else finalMemory
        
        Log.d("MemoryTracker", "Stopped memory tracking. Final: ${formatBytes(finalMemory)}, Peak: ${formatBytes(peakMemory)}")
    }
    
    /**
     * Mendapatkan penggunaan memory saat ini
     */
    fun getCurrentMemoryUsage(): Long {
        return try {
            // Method 1: Using Runtime
            val runtime = Runtime.getRuntime()
            val usedMemory = runtime.totalMemory() - runtime.freeMemory()
            
            // Method 2: Using Debug (if available)
            val debugMemory: Long = if (Debug.isDebuggerConnected()) {
                Debug.getGlobalAllocSize().toLong()
            } else {
                usedMemory
            }
            
            // Return the more accurate measurement
            return if (usedMemory > debugMemory) usedMemory else debugMemory
            
        } catch (e: Exception) {
            Log.e("MemoryTracker", "Error getting memory usage: ${e.message}")
            0L
        }
    }
    
    /**
     * Mendapatkan peak memory usage selama tracking
     */
    fun getPeakMemoryUsage(): Long {
        return peakMemory
    }
    
    /**
     * Mendapatkan memory delta (selisih antara peak dan initial)
     */
    fun getMemoryDelta(): Long {
        return peakMemory - initialMemory
    }
    
    /**
     * Mendapatkan informasi memory dari /proc/meminfo (Linux system)
     */
    fun getSystemMemoryInfo(): SystemMemoryInfo {
        return try {
            val reader = BufferedReader(FileReader("/proc/meminfo"))
            var totalMemory = 0L
            var availableMemory = 0L
            var freeMemory = 0L
            
            reader.useLines { lines ->
                lines.forEach { line ->
                    when {
                        line.startsWith("MemTotal:") -> {
                            totalMemory = line.split("\\s+".toRegex())[1].toLong() * 1024
                        }
                        line.startsWith("MemAvailable:") -> {
                            availableMemory = line.split("\\s+".toRegex())[1].toLong() * 1024
                        }
                        line.startsWith("MemFree:") -> {
                            freeMemory = line.split("\\s+".toRegex())[1].toLong() * 1024
                        }
                    }
                }
            }
            
            SystemMemoryInfo(
                totalMemory = totalMemory,
                availableMemory = availableMemory,
                freeMemory = freeMemory,
                usedMemory = totalMemory - availableMemory
            )
            
        } catch (e: IOException) {
            Log.e("MemoryTracker", "Error reading /proc/meminfo: ${e.message}")
            SystemMemoryInfo(0, 0, 0, 0)
        }
    }
    
    /**
     * Mendapatkan memory info untuk process saat ini
     */
    fun getProcessMemoryInfo(): ProcessMemoryInfo {
        return try {
            val pid = Process.myPid()
            val reader = BufferedReader(FileReader("/proc/$pid/status"))
            var vmRss = 0L
            var vmSize = 0L
            
            reader.useLines { lines ->
                lines.forEach { line ->
                    when {
                        line.startsWith("VmRSS:") -> {
                            vmRss = line.split("\\s+".toRegex())[1].toLong() * 1024
                        }
                        line.startsWith("VmSize:") -> {
                            vmSize = line.split("\\s+".toRegex())[1].toLong() * 1024
                        }
                    }
                }
            }
            
            ProcessMemoryInfo(
                pid = pid,
                vmRss = vmRss, // Resident Set Size (physical memory)
                vmSize = vmSize // Virtual Memory Size
            )
            
        } catch (e: IOException) {
            Log.e("MemoryTracker", "Error reading process memory info: ${e.message}")
            ProcessMemoryInfo(Process.myPid(), 0, 0)
        }
    }
    
    /**
     * Format bytes ke human readable string
     */
    private fun formatBytes(bytes: Long): String {
        if (bytes < 1024) return "$bytes B"
        val exp = (ln(bytes.toDouble()) / ln(1024.0)).toInt()
        val pre = "KMGTPE"[exp - 1].toString()
        return String.format("%.1f %sB", bytes / Math.pow(1024.0, exp.toDouble()), pre)
    }
    
    /**
     * Reset tracking state
     */
    fun reset() {
        isTracking = false
        initialMemory = 0
        peakMemory = 0
    }
    
    /**
     * Data class untuk system memory info
     */
    data class SystemMemoryInfo(
        val totalMemory: Long,
        val availableMemory: Long,
        val freeMemory: Long,
        val usedMemory: Long
    ) {
        fun getUsagePercentage(): Double {
            return if (totalMemory > 0) {
                (usedMemory.toDouble() / totalMemory) * 100
            } else {
                0.0
            }
        }
    }
    
    /**
     * Data class untuk process memory info
     */
    data class ProcessMemoryInfo(
        val pid: Int,
        val vmRss: Long, // Physical memory usage
        val vmSize: Long // Virtual memory usage
    )
} 