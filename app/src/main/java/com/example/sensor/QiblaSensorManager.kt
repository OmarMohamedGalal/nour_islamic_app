package com.example.sensor

import android.content.Context
import android.hardware.Sensor
import android.hardware.SensorEvent
import android.hardware.SensorEventListener
import android.hardware.SensorManager
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlin.math.*

data class QiblaState(
    val qiblaBearing: Float = 0f,
    val deviceAzimuth: Float = 0f,
    val relativeBearing: Float = 0f,
    val distanceKm: Double = 0.0,
    val isAligned: Boolean = false,
    val accuracy: Int = SensorManager.SENSOR_STATUS_ACCURACY_HIGH
)

class QiblaSensorManager(private val context: Context) : SensorEventListener {

    private val sensorManager = context.getSystemService(Context.SENSOR_SERVICE) as? SensorManager
    private val rotationSensor = sensorManager?.getDefaultSensor(Sensor.TYPE_ROTATION_VECTOR)
    private val accelerometer = sensorManager?.getDefaultSensor(Sensor.TYPE_ACCELEROMETER)
    private val magnetometer = sensorManager?.getDefaultSensor(Sensor.TYPE_MAGNETIC_FIELD)

    private val _qiblaState = MutableStateFlow(QiblaState())
    val qiblaState: StateFlow<QiblaState> = _qiblaState.asStateFlow()

    private var currentLatitude: Double = 21.4225
    private var currentLongitude: Double = 39.8262

    private val rotationMatrix = FloatArray(9)
    private val orientationAngles = FloatArray(3)
    private var lastAzimuth = 0f

    private val lastAccelerometer = FloatArray(3)
    private val lastMagnetometer = FloatArray(3)
    private var lastAccelerometerSet = false
    private var lastMagnetometerSet = false

    fun updateLocation(lat: Double, lng: Double) {
        currentLatitude = lat
        currentLongitude = lng
        val bearing = calculateQiblaBearing(lat, lng)
        val dist = calculateDistanceToKaaba(lat, lng)
        val rel = normalizeDegree(bearing - lastAzimuth)
        val aligned = abs(rel) <= 3.0f || abs(rel - 360f) <= 3.0f
        _qiblaState.value = _qiblaState.value.copy(
            qiblaBearing = bearing,
            distanceKm = dist,
            relativeBearing = rel,
            isAligned = aligned
        )
    }

    fun start() {
        if (rotationSensor != null) {
            sensorManager?.registerListener(this, rotationSensor, SensorManager.SENSOR_DELAY_UI)
        } else {
            accelerometer?.let { sensorManager?.registerListener(this, it, SensorManager.SENSOR_DELAY_UI) }
            magnetometer?.let { sensorManager?.registerListener(this, it, SensorManager.SENSOR_DELAY_UI) }
        }
    }

    fun stop() {
        sensorManager?.unregisterListener(this)
    }

    override fun onSensorChanged(event: SensorEvent) {
        if (event.sensor.type == Sensor.TYPE_ROTATION_VECTOR) {
            SensorManager.getRotationMatrixFromVector(rotationMatrix, event.values)
            SensorManager.getOrientation(rotationMatrix, orientationAngles)
            val azimuthRad = orientationAngles[0]
            var azimuthDeg = Math.toDegrees(azimuthRad.toDouble()).toFloat()
            if (azimuthDeg < 0) azimuthDeg += 360f

            // Low-pass filter smoothing
            lastAzimuth = smoothAngle(lastAzimuth, azimuthDeg, 0.15f)
            updateStateWithAzimuth(lastAzimuth)
        } else if (event.sensor.type == Sensor.TYPE_ACCELEROMETER) {
            System.arraycopy(event.values, 0, lastAccelerometer, 0, event.values.size)
            lastAccelerometerSet = true
            processFallbackSensors()
        } else if (event.sensor.type == Sensor.TYPE_MAGNETIC_FIELD) {
            System.arraycopy(event.values, 0, lastMagnetometer, 0, event.values.size)
            lastMagnetometerSet = true
            processFallbackSensors()
        }
    }

    private fun processFallbackSensors() {
        if (lastAccelerometerSet && lastMagnetometerSet) {
            if (SensorManager.getRotationMatrix(rotationMatrix, null, lastAccelerometer, lastMagnetometer)) {
                SensorManager.getOrientation(rotationMatrix, orientationAngles)
                val azimuthRad = orientationAngles[0]
                var azimuthDeg = Math.toDegrees(azimuthRad.toDouble()).toFloat()
                if (azimuthDeg < 0) azimuthDeg += 360f
                lastAzimuth = smoothAngle(lastAzimuth, azimuthDeg, 0.15f)
                updateStateWithAzimuth(lastAzimuth)
            }
        }
    }

    private fun updateStateWithAzimuth(azimuth: Float) {
        val qibla = _qiblaState.value.qiblaBearing
        val rel = normalizeDegree(qibla - azimuth)
        val aligned = abs(rel) <= 2.5f || abs(rel - 360f) <= 2.5f
        _qiblaState.value = _qiblaState.value.copy(
            deviceAzimuth = azimuth,
            relativeBearing = rel,
            isAligned = aligned
        )
    }

    override fun onAccuracyChanged(sensor: Sensor?, accuracy: Int) {
        _qiblaState.value = _qiblaState.value.copy(accuracy = accuracy)
    }

    companion object {
        const val KAABA_LAT = 21.422487
        const val KAABA_LNG = 39.826206

        fun calculateQiblaBearing(lat: Double, lng: Double): Float {
            val lat1 = Math.toRadians(lat)
            val lat2 = Math.toRadians(KAABA_LAT)
            val deltaLng = Math.toRadians(KAABA_LNG - lng)

            val y = sin(deltaLng) * cos(lat2)
            val x = cos(lat1) * sin(lat2) - sin(lat1) * cos(lat2) * cos(deltaLng)
            var bearing = Math.toDegrees(atan2(y, x)).toFloat()
            if (bearing < 0) bearing += 360f
            return bearing
        }

        fun calculateDistanceToKaaba(lat: Double, lng: Double): Double {
            val r = 6371.0 // Earth radius in km
            val dLat = Math.toRadians(KAABA_LAT - lat)
            val dLng = Math.toRadians(KAABA_LNG - lng)
            val a = sin(dLat / 2).pow(2) + cos(Math.toRadians(lat)) * cos(Math.toRadians(KAABA_LAT)) * sin(dLng / 2).pow(2)
            val c = 2 * atan2(sqrt(a), sqrt(1 - a))
            return r * c
        }

        private fun normalizeDegree(deg: Float): Float {
            var d = deg % 360f
            if (d < 0) d += 360f
            return d
        }

        private fun smoothAngle(current: Float, target: Float, factor: Float): Float {
            var diff = (target - current) % 360f
            if (diff > 180f) diff -= 360f
            if (diff < -180f) diff += 360f
            return (current + diff * factor + 360f) % 360f
        }
    }
}
