package com.skincare.apitest.performance

import android.os.Process
import android.util.Log
import java.io.BufferedReader
import java.io.FileReader
import java.io.IOException
import kotlin.math.abs

/**
 * CPU Tracker untuk memantau penggunaan CPU selama pengujian API
 */
class CPUTracker {
    
    private var initialCpuTime: Long = 0
    private var initialSystemTime: Long = 0
    private var isTracking: Boolean = false
    private var peakCpuUsage: Double = 0.0
    private var totalCpuUsage: Double = 0.0
    private var measurementCount: Int = 0
    
    /**
     * Memulai tracking CPU
     */
    fun startTracking() {
        if (isTracking) {
            Log.w("CPUTracker", "CPU tracking already started")
            return
        }
        
        isTracking = true
        initialCpuTime = getProcessCpuTime()
        initialSystemTime = System.currentTimeMillis()
        peakCpuUsage = 0.0
        totalCpuUsage = 0.0
        measurementCount = 0
        
        Log.d("CPUTracker", "Started CPU tracking")
    }
    
    /**
     * Menghentikan tracking CPU
     */
    fun stopTracking() {
        if (!isTracking) {
            Log.w("CPUTracker", "CPU tracking not started")
            return
        }
        
        isTracking = false
        Log.d("CPUTracker", "Stopped CPU tracking. Peak: ${String.format("%.2f", peakCpuUsage)}%, Avg: ${String.format("%.2f", getAverageCpuUsage())}%")
    }
    
    /**
     * Mendapatkan penggunaan CPU saat ini
     */
    fun getCurrentCpuUsage(): Double {
        return try {
            val currentCpuTime = getProcessCpuTime()
            val currentSystemTime = System.currentTimeMillis()
            
            val cpuDelta = currentCpuTime - initialCpuTime
            val timeDelta = currentSystemTime - initialSystemTime
            
            val cpuUsage = if (timeDelta > 0) {
                (cpuDelta.toDouble() / timeDelta) * 100.0
            } else {
                0.0
            }
            
            // Update peak and average
            peakCpuUsage = maxOf(peakCpuUsage, cpuUsage)
            totalCpuUsage += cpuUsage
            measurementCount++
            
            // Update initial values for next measurement
            initialCpuTime = currentCpuTime
            initialSystemTime = currentSystemTime
            
            cpuUsage
            
        } catch (e: Exception) {
            Log.e("CPUTracker", "Error getting CPU usage: ${e.message}")
            0.0
        }
    }
    
    /**
     * Mendapatkan peak CPU usage selama tracking
     */
    fun getPeakCpuUsage(): Double {
        return peakCpuUsage
    }
    
    /**
     * Mendapatkan average CPU usage selama tracking
     */
    fun getAverageCpuUsage(): Double {
        return if (measurementCount > 0) {
            totalCpuUsage / measurementCount
        } else {
            0.0
        }
    }
    
    /**
     * Mendapatkan CPU time untuk process saat ini
     */
    private fun getProcessCpuTime(): Long {
        return try {
            val pid = Process.myPid()
            val reader = BufferedReader(FileReader("/proc/$pid/stat"))
            val line = reader.readLine()
            reader.close()
            
            if (line != null) {
                val parts = line.split(" ")
                // CPU time = utime + stime (user time + system time)
                val utime = parts[13].toLong()
                val stime = parts[14].toLong()
                utime + stime
            } else {
                0L
            }
            
        } catch (e: IOException) {
            Log.e("CPUTracker", "Error reading process CPU time: ${e.message}")
            0L
        }
    }
    
    /**
     * Mendapatkan informasi CPU system
     */
    fun getSystemCpuInfo(): SystemCpuInfo {
        return try {
            val reader = BufferedReader(FileReader("/proc/stat"))
            val line = reader.readLine()
            reader.close()
            
            if (line != null && line.startsWith("cpu ")) {
                val parts = line.split("\\s+".toRegex())
                val user = parts[1].toLong()
                val nice = parts[2].toLong()
                val system = parts[3].toLong()
                val idle = parts[4].toLong()
                val iowait = parts[5].toLong()
                val irq = parts[6].toLong()
                val softirq = parts[7].toLong()
                val steal = parts[8].toLong()
                
                val total = user + nice + system + idle + iowait + irq + softirq + steal
                val nonIdle = user + nice + system + irq + softirq + steal
                
                SystemCpuInfo(
                    total = total,
                    idle = idle,
                    nonIdle = nonIdle,
                    usagePercentage = if (total > 0) (nonIdle.toDouble() / total) * 100 else 0.0
                )
            } else {
                SystemCpuInfo(0, 0, 0, 0.0)
            }
            
        } catch (e: IOException) {
            Log.e("CPUTracker", "Error reading system CPU info: ${e.message}")
            SystemCpuInfo(0, 0, 0, 0.0)
        }
    }
    
    /**
     * Mendapatkan informasi CPU per core
     */
    fun getPerCoreCpuInfo(): List<CoreCpuInfo> {
        val cores = mutableListOf<CoreCpuInfo>()
        
        try {
            var coreIndex = 0
            while (true) {
                val reader = BufferedReader(FileReader("/proc/stat"))
                var found = false
                
                reader.useLines { lines ->
                    lines.forEach { line ->
                        if (line.startsWith("cpu$coreIndex ")) {
                            val parts = line.split("\\s+".toRegex())
                            val user = parts[1].toLong()
                            val nice = parts[2].toLong()
                            val system = parts[3].toLong()
                            val idle = parts[4].toLong()
                            val iowait = parts[5].toLong()
                            val irq = parts[6].toLong()
                            val softirq = parts[7].toLong()
                            val steal = parts[8].toLong()
                            
                            val total = user + nice + system + idle + iowait + irq + softirq + steal
                            val nonIdle = user + nice + system + irq + softirq + steal
                            
                            cores.add(CoreCpuInfo(
                                coreIndex = coreIndex,
                                total = total,
                                idle = idle,
                                nonIdle = nonIdle,
                                usagePercentage = if (total > 0) (nonIdle.toDouble() / total) * 100 else 0.0
                            ))
                            
                            found = true
                        }
                    }
                }
                
                if (!found) break
                coreIndex++
            }
            
        } catch (e: IOException) {
            Log.e("CPUTracker", "Error reading per-core CPU info: ${e.message}")
        }
        
        return cores
    }
    
    /**
     * Mendapatkan informasi CPU frequency
     */
    fun getCpuFrequency(): CpuFrequencyInfo {
        return try {
            val reader = BufferedReader(FileReader("/sys/devices/system/cpu/cpu0/cpufreq/scaling_cur_freq"))
            val currentFreq = reader.readLine()?.toLong() ?: 0
            reader.close()
            
            val reader2 = BufferedReader(FileReader("/sys/devices/system/cpu/cpu0/cpufreq/scaling_max_freq"))
            val maxFreq = reader2.readLine()?.toLong() ?: 0
            reader2.close()
            
            val reader3 = BufferedReader(FileReader("/sys/devices/system/cpu/cpu0/cpufreq/scaling_min_freq"))
            val minFreq = reader3.readLine()?.toLong() ?: 0
            reader3.close()
            
            CpuFrequencyInfo(
                currentFrequency = currentFreq,
                maxFrequency = maxFreq,
                minFrequency = minFreq,
                usagePercentage = if (maxFreq > 0) (currentFreq.toDouble() / maxFreq) * 100 else 0.0
            )
            
        } catch (e: IOException) {
            Log.e("CPUTracker", "Error reading CPU frequency: ${e.message}")
            CpuFrequencyInfo(0, 0, 0, 0.0)
        }
    }
    
    /**
     * Reset tracking state
     */
    fun reset() {
        isTracking = false
        initialCpuTime = 0
        initialSystemTime = 0
        peakCpuUsage = 0.0
        totalCpuUsage = 0.0
        measurementCount = 0
    }
    
    /**
     * Data class untuk system CPU info
     */
    data class SystemCpuInfo(
        val total: Long,
        val idle: Long,
        val nonIdle: Long,
        val usagePercentage: Double
    )
    
    /**
     * Data class untuk per-core CPU info
     */
    data class CoreCpuInfo(
        val coreIndex: Int,
        val total: Long,
        val idle: Long,
        val nonIdle: Long,
        val usagePercentage: Double
    )
    
    /**
     * Data class untuk CPU frequency info
     */
    data class CpuFrequencyInfo(
        val currentFrequency: Long,
        val maxFrequency: Long,
        val minFrequency: Long,
        val usagePercentage: Double
    ) {
        fun getCurrentFrequencyMHz(): Double = currentFrequency / 1000.0
        fun getMaxFrequencyMHz(): Double = maxFrequency / 1000.0
        fun getMinFrequencyMHz(): Double = minFrequency / 1000.0
    }
} 