package com.example.sensors

import android.content.Context
import android.hardware.GeomagneticField
import android.hardware.Sensor
import android.hardware.SensorEvent
import android.hardware.SensorEventListener
import android.hardware.SensorManager
import android.os.Build
import android.view.Surface
import android.view.WindowManager
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
import kotlin.math.atan2
import kotlin.math.cos
import kotlin.math.sin

enum class CompassAccuracy {
    HIGH, MEDIUM, LOW, UNRELIABLE
}

data class CompassSensorState(
    val headingDegrees: Float = 0f,
    val accuracy: CompassAccuracy = CompassAccuracy.MEDIUM,
    val sensorAvailable: Boolean = true,
    val usingRotationVector: Boolean = true
)

class QiblaSensorManager(private val context: Context) {

    fun observeCompass(latitude: Double, longitude: Double): Flow<CompassSensorState> = callbackFlow {
        val sensorManager = context.getSystemService(Context.SENSOR_SERVICE) as? SensorManager
        if (sensorManager == null) {
            trySend(CompassSensorState(sensorAvailable = false))
            awaitClose { }
            return@callbackFlow
        }

        val rotationSensor = sensorManager.getDefaultSensor(Sensor.TYPE_ROTATION_VECTOR)
        val accelSensor = sensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER)
        val magnetSensor = sensorManager.getDefaultSensor(Sensor.TYPE_MAGNETIC_FIELD)

        if (rotationSensor == null && (accelSensor == null || magnetSensor == null)) {
            trySend(CompassSensorState(sensorAvailable = false, usingRotationVector = false))
            awaitClose { }
            return@callbackFlow
        }

        val usingRotationVector = rotationSensor != null
        val geomagneticField = runCatching {
            GeomagneticField(
                latitude.toFloat(),
                longitude.toFloat(),
                0f,
                System.currentTimeMillis()
            )
        }.getOrNull()
        val declinationDeg = geomagneticField?.declination ?: 0f

        val rotationMatrix = FloatArray(9)
        val remappedMatrix = FloatArray(9)
        val orientationAngles = FloatArray(3)
        val gravity = FloatArray(3)
        val geomagnetic = FloatArray(3)
        var hasGravity = false
        var hasGeomagnetic = false

        // Circular smoothing using sin/cos components prevents 0/360 wraparound jitter
        var smoothedSin = 0.0
        var smoothedCos = 1.0
        var initialized = false
        var currentAccuracy = CompassAccuracy.MEDIUM

        fun emitHeading(rawMagneticDegrees: Float) {
            val trueNorth = ((rawMagneticDegrees + declinationDeg) % 360f + 360f) % 360f
            val rad = Math.toRadians(trueNorth.toDouble())
            val alpha = 0.16
            if (!initialized) {
                smoothedSin = sin(rad)
                smoothedCos = cos(rad)
                initialized = true
            } else {
                smoothedSin = smoothedSin + alpha * (sin(rad) - smoothedSin)
                smoothedCos = smoothedCos + alpha * (cos(rad) - smoothedCos)
            }
            val smoothDeg = ((Math.toDegrees(atan2(smoothedSin, smoothedCos)) % 360.0) + 360.0) % 360.0
            trySend(
                CompassSensorState(
                    headingDegrees = smoothDeg.toFloat(),
                    accuracy = currentAccuracy,
                    sensorAvailable = true,
                    usingRotationVector = usingRotationVector
                )
            )
        }

        fun currentDisplayRotation(): Int {
            return runCatching {
                val dm = context.getSystemService(Context.DISPLAY_SERVICE) as? android.hardware.display.DisplayManager
                dm?.getDisplay(android.view.Display.DEFAULT_DISPLAY)?.rotation ?: Surface.ROTATION_0
            }.getOrDefault(Surface.ROTATION_0)
        }

        fun remapForDisplay(matrix: FloatArray): FloatArray {
            val (axisX, axisY) = when (currentDisplayRotation()) {
                Surface.ROTATION_90 -> SensorManager.AXIS_Y to SensorManager.AXIS_MINUS_X
                Surface.ROTATION_180 -> SensorManager.AXIS_MINUS_X to SensorManager.AXIS_MINUS_Y
                Surface.ROTATION_270 -> SensorManager.AXIS_MINUS_Y to SensorManager.AXIS_X
                else -> SensorManager.AXIS_X to SensorManager.AXIS_Y
            }
            SensorManager.remapCoordinateSystem(matrix, axisX, axisY, remappedMatrix)
            return remappedMatrix
        }

        val listener = object : SensorEventListener {
            override fun onSensorChanged(event: SensorEvent) {
                runCatching {
                    when (event.sensor.type) {
                        Sensor.TYPE_ROTATION_VECTOR -> {
                            SensorManager.getRotationMatrixFromVector(rotationMatrix, event.values)
                            val remapped = remapForDisplay(rotationMatrix)
                            SensorManager.getOrientation(remapped, orientationAngles)
                            val azimuthDeg = Math.toDegrees(orientationAngles[0].toDouble()).toFloat()
                            emitHeading(azimuthDeg)
                        }
                        Sensor.TYPE_ACCELEROMETER -> {
                            System.arraycopy(event.values, 0, gravity, 0, 3)
                            hasGravity = true
                            if (hasGeomagnetic && SensorManager.getRotationMatrix(rotationMatrix, null, gravity, geomagnetic)) {
                                val remapped = remapForDisplay(rotationMatrix)
                                SensorManager.getOrientation(remapped, orientationAngles)
                                emitHeading(Math.toDegrees(orientationAngles[0].toDouble()).toFloat())
                            }
                        }
                        Sensor.TYPE_MAGNETIC_FIELD -> {
                            System.arraycopy(event.values, 0, geomagnetic, 0, 3)
                            hasGeomagnetic = true
                            if (hasGravity && SensorManager.getRotationMatrix(rotationMatrix, null, gravity, geomagnetic)) {
                                val remapped = remapForDisplay(rotationMatrix)
                                SensorManager.getOrientation(remapped, orientationAngles)
                                emitHeading(Math.toDegrees(orientationAngles[0].toDouble()).toFloat())
                            }
                        }
                    }
                }
            }

            override fun onAccuracyChanged(sensor: Sensor?, accuracy: Int) {
                if (sensor?.type == Sensor.TYPE_MAGNETIC_FIELD || sensor?.type == Sensor.TYPE_ROTATION_VECTOR) {
                    currentAccuracy = when (accuracy) {
                        SensorManager.SENSOR_STATUS_ACCURACY_HIGH -> CompassAccuracy.HIGH
                        SensorManager.SENSOR_STATUS_ACCURACY_MEDIUM -> CompassAccuracy.MEDIUM
                        SensorManager.SENSOR_STATUS_ACCURACY_LOW -> CompassAccuracy.LOW
                        else -> CompassAccuracy.UNRELIABLE
                    }
                }
            }
        }

        if (rotationSensor != null) {
            sensorManager.registerListener(listener, rotationSensor, SensorManager.SENSOR_DELAY_UI)
        } else {
            sensorManager.registerListener(listener, accelSensor, SensorManager.SENSOR_DELAY_UI)
            sensorManager.registerListener(listener, magnetSensor, SensorManager.SENSOR_DELAY_UI)
        }

        awaitClose {
            sensorManager.unregisterListener(listener)
        }
    }
}
