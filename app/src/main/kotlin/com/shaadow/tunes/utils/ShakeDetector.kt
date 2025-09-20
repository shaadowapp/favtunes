package com.shaadow.tunes.utils

import android.content.Context
import android.hardware.Sensor
import android.hardware.SensorEvent
import android.hardware.SensorEventListener
import android.hardware.SensorManager
import androidx.compose.runtime.*
import androidx.compose.ui.platform.LocalContext
import kotlin.math.abs
import kotlin.math.sqrt

class ShakeDetector(
    private val onShake: () -> Unit
) : SensorEventListener {
    
    private var sensorManager: SensorManager? = null
    private var accelerometer: Sensor? = null
    private var lastUpdate: Long = 0
    private var lastX = 0f
    private var lastY = 0f
    private var lastZ = 0f
    
    companion object {
        // Properly balanced thresholds
        private const val SHAKE_THRESHOLD = 12.0f // Higher threshold to prevent light touches
        private const val UPDATE_THRESHOLD = 100 // Reasonable update rate
        private const val MIN_DIRECTION_CHANGES = 3 // Need proper back-and-forth motion
        private const val COOLDOWN_PERIOD = 3000 // 3 second cooldown between shakes
        private const val MIN_SHAKE_DURATION = 500 // Minimum 500ms of sustained shaking
        private const val MIN_CONSECUTIVE_READINGS = 5 // Need sustained motion
    }
    
    private var directionChanges = 0
    private var lastDirection = 0 // -1 for negative, 1 for positive, 0 for neutral
    private var shakeStartTime = 0L
    private var lastShakeTime = 0L
    private var consecutiveShakeReadings = 0
    
    fun start(context: Context) {
        sensorManager = context.getSystemService(Context.SENSOR_SERVICE) as SensorManager
        accelerometer = sensorManager?.getDefaultSensor(Sensor.TYPE_ACCELEROMETER)
        sensorManager?.registerListener(this, accelerometer, SensorManager.SENSOR_DELAY_UI)
    }
    
    fun stop() {
        sensorManager?.unregisterListener(this)
        resetShakeDetection()
    }
    
    private fun resetShakeDetection() {
        directionChanges = 0
        lastDirection = 0
        shakeStartTime = 0L
        consecutiveShakeReadings = 0
    }
    
    private fun calculateAcceleration(x: Float, y: Float, z: Float): Float {
        // Calculate total acceleration magnitude
        return sqrt(x * x + y * y + z * z)
    }
    
    override fun onSensorChanged(event: SensorEvent?) {
        if (event?.sensor?.type == Sensor.TYPE_ACCELEROMETER) {
            val currentTime = System.currentTimeMillis()
            
            if (currentTime - lastUpdate > UPDATE_THRESHOLD) {
                lastUpdate = currentTime
                
                val x = event.values[0]
                val y = event.values[1] 
                val z = event.values[2]
                
                // Calculate total acceleration
                val acceleration = calculateAcceleration(x, y, z)
                
                // Check if we're in cooldown period
                if (lastShakeTime > 0 && (currentTime - lastShakeTime) < COOLDOWN_PERIOD) {
                    lastX = x
                    lastY = y
                    lastZ = z
                    return
                }
                
                // Detect shake motion
                if (acceleration > SHAKE_THRESHOLD) {
                    consecutiveShakeReadings++
                    
                    if (shakeStartTime == 0L) {
                        shakeStartTime = currentTime
                    }
                    
                    // Detect direction changes for shake pattern
                    val deltaX = x - lastX
                    val deltaY = y - lastY
                    val deltaZ = z - lastZ
                    
                    // Focus on horizontal movement (X-axis) as it's most common for shaking
                    val currentDirection = when {
                        abs(deltaX) > abs(deltaY) && abs(deltaX) > abs(deltaZ) -> {
                            if (deltaX > 2.0f) 1 else if (deltaX < -2.0f) -1 else 0
                        }
                        abs(deltaY) > abs(deltaZ) -> {
                            if (deltaY > 2.0f) 2 else if (deltaY < -2.0f) -2 else 0
                        }
                        else -> {
                            if (deltaZ > 2.0f) 3 else if (deltaZ < -2.0f) -3 else 0
                        }
                    }
                    
                    // Count direction changes (back-and-forth motion)
                    if (currentDirection != 0 && lastDirection != 0 && 
                        ((currentDirection > 0 && lastDirection < 0) || (currentDirection < 0 && lastDirection > 0)) &&
                        abs(currentDirection) == abs(lastDirection)) {
                        directionChanges++
                    }
                    
                    if (currentDirection != 0) {
                        lastDirection = currentDirection
                    }
                    
                    // Check if we have a valid shake
                    val shakeDuration = currentTime - shakeStartTime
                    if (directionChanges >= MIN_DIRECTION_CHANGES && 
                        consecutiveShakeReadings >= 3 && 
                        shakeDuration >= MIN_SHAKE_DURATION) {
                        onShake()
                        lastShakeTime = currentTime
                        resetShakeDetection()
                    }
                } else {
                    // Reset if no shake motion detected for a while
                    if (consecutiveShakeReadings > 0) {
                        consecutiveShakeReadings--
                    }
                    
                    if (acceleration < 5.0f) {
                        resetShakeDetection()
                    }
                }
                
                // Auto-reset if shake attempt takes too long (2 seconds)
                if (shakeStartTime > 0 && (currentTime - shakeStartTime) > 2000) {
                    resetShakeDetection()
                }
                
                lastX = x
                lastY = y
                lastZ = z
            }
        }
    }
    
    override fun onAccuracyChanged(sensor: Sensor?, accuracy: Int) {}
}

@Composable
fun rememberShakeDetector(onShake: () -> Unit): ShakeDetector {
    val context = LocalContext.current
    val shakeDetector = remember { ShakeDetector(onShake) }
    
    DisposableEffect(context) {
        shakeDetector.start(context)
        onDispose {
            shakeDetector.stop()
        }
    }
    
    return shakeDetector
}