package com.shaadow.tunes.utils

import android.content.Context
import android.hardware.Sensor
import android.hardware.SensorEvent
import android.hardware.SensorEventListener
import android.hardware.SensorManager
import androidx.compose.runtime.*
import androidx.compose.ui.platform.LocalContext
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
        private const val SHAKE_THRESHOLD = 2200
        private const val UPDATE_THRESHOLD = 40
        private const val MIN_DIRECTION_CHANGES = 3
    }
    
    private var directionChanges = 0
    private var lastDirection = 0 // -1 for left, 1 for right, 0 for neutral
    
    fun start(context: Context) {
        sensorManager = context.getSystemService(Context.SENSOR_SERVICE) as SensorManager
        accelerometer = sensorManager?.getDefaultSensor(Sensor.TYPE_ACCELEROMETER)
        sensorManager?.registerListener(this, accelerometer, SensorManager.SENSOR_DELAY_UI)
    }
    
    fun stop() {
        sensorManager?.unregisterListener(this)
    }
    
    override fun onSensorChanged(event: SensorEvent?) {
        if (event?.sensor?.type == Sensor.TYPE_ACCELEROMETER) {
            val currentTime = System.currentTimeMillis()
            
            if (currentTime - lastUpdate > UPDATE_THRESHOLD) {
                val diffTime = currentTime - lastUpdate
                lastUpdate = currentTime
                
                val x = event.values[0]
                val y = event.values[1]
                val z = event.values[2]
                
                val deltaX = x - lastX
                val deltaY = y - lastY
                val deltaZ = z - lastZ
                
                val speed = sqrt((deltaX * deltaX + deltaY * deltaY + deltaZ * deltaZ).toDouble()) / diffTime * 10000
                
                // Check for horizontal movement pattern (left-right or right-left)
                if (kotlin.math.abs(deltaX) > kotlin.math.abs(deltaY) && kotlin.math.abs(deltaX) > kotlin.math.abs(deltaZ)) {
                    val currentDirection = if (deltaX > 0.5f) 1 else if (deltaX < -0.5f) -1 else 0
                    
                    if (currentDirection != 0 && currentDirection != lastDirection && lastDirection != 0) {
                        directionChanges++
                    }
                    
                    if (currentDirection != 0) {
                        lastDirection = currentDirection
                    }
                }
                
                if (speed > SHAKE_THRESHOLD && directionChanges >= MIN_DIRECTION_CHANGES) {
                    onShake()
                    directionChanges = 0 // Reset after successful shake
                }
                
                // Reset direction changes if no significant movement for a while
                if (speed < 200) {
                    directionChanges = 0
                    lastDirection = 0
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