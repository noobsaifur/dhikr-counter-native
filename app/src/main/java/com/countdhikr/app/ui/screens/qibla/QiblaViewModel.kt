package com.countdhikr.app.ui.screens.qibla

import android.annotation.SuppressLint
import android.app.Application
import android.content.Context
import android.hardware.Sensor
import android.hardware.SensorEvent
import android.hardware.SensorEventListener
import android.hardware.SensorManager
import android.location.Location
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.countdhikr.app.util.QiblaCalculator
import com.google.android.gms.location.LocationServices
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import kotlin.math.abs

data class QiblaUiState(
    val qiblaDirection: Float? = null,
    val compassHeading: Float? = null,
    val distanceToKaaba: Double? = null,
    val isGyroscopeAvailable: Boolean = true,
    val loading: Boolean = false,
    val error: String? = null,
    val isAlignedWithQibla: Boolean = false,
    val latitude: Double? = null,
    val longitude: Double? = null
)

class QiblaViewModel(application: Application) : AndroidViewModel(application), SensorEventListener {
    private val sensorManager = application.getSystemService(Context.SENSOR_SERVICE) as SensorManager
    private val accelerometer = sensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER)
    private val magnetometer = sensorManager.getDefaultSensor(Sensor.TYPE_MAGNETIC_FIELD)
    private val rotationVector = sensorManager.getDefaultSensor(Sensor.TYPE_ROTATION_VECTOR)
    private val fusedLocationClient = LocationServices.getFusedLocationProviderClient(application)

    private val _uiState = MutableStateFlow(QiblaUiState())
    val uiState: StateFlow<QiblaUiState> = _uiState.asStateFlow()

    private var gravity: FloatArray? = null
    private var geomagnetic: FloatArray? = null
    private var lastHeading = 0f

    init {
        if (rotationVector == null && (accelerometer == null || magnetometer == null)) {
            _uiState.update { it.copy(isGyroscopeAvailable = false, error = "Compass sensors not available on this device") }
        }
    }

    fun startSensors() {
        if (rotationVector != null) {
            sensorManager.registerListener(this, rotationVector, SensorManager.SENSOR_DELAY_GAME)
        } else if (accelerometer != null && magnetometer != null) {
            sensorManager.registerListener(this, accelerometer, SensorManager.SENSOR_DELAY_GAME)
            sensorManager.registerListener(this, magnetometer, SensorManager.SENSOR_DELAY_GAME)
        }
        getLocation()
    }

    fun stopSensors() {
        sensorManager.unregisterListener(this)
    }

    @SuppressLint("MissingPermission")
    private fun getLocation() {
        _uiState.update { it.copy(loading = true, error = null) }
        try {
            fusedLocationClient.lastLocation.addOnSuccessListener { location: Location? ->
                if (location != null) {
                    val qibla = QiblaCalculator.calculateQiblaDirection(location.latitude, location.longitude).toFloat()
                    val distance = QiblaCalculator.calculateDistanceToKaaba(location.latitude, location.longitude)
                    _uiState.update { 
                        it.copy(
                            qiblaDirection = qibla,
                            distanceToKaaba = distance,
                            latitude = location.latitude,
                            longitude = location.longitude,
                            loading = false
                        )
                    }
                } else {
                    _uiState.update { it.copy(loading = false, error = "Could not determine location") }
                }
            }.addOnFailureListener {
                _uiState.update { state -> state.copy(loading = false, error = it.message) }
            }
        } catch (e: Exception) {
            _uiState.update { it.copy(loading = false, error = e.message) }
        }
    }

    override fun onSensorChanged(event: SensorEvent?) {
        if (event == null) return
        
        var success = false
        val R = FloatArray(9)
        
        if (event.sensor.type == Sensor.TYPE_ROTATION_VECTOR) {
            SensorManager.getRotationMatrixFromVector(R, event.values)
            success = true
        } else {
            if (event.sensor.type == Sensor.TYPE_ACCELEROMETER) {
                gravity = event.values.clone()
            } else if (event.sensor.type == Sensor.TYPE_MAGNETIC_FIELD) {
                geomagnetic = event.values.clone()
            }
            
            if (gravity != null && geomagnetic != null) {
                val I = FloatArray(9)
                success = SensorManager.getRotationMatrix(R, I, gravity, geomagnetic)
            }
        }

        if (success) {
            val orientation = FloatArray(3)
            SensorManager.getOrientation(R, orientation)
            
            val azimuthInRadians = orientation[0]
            var azimuthInDegrees = Math.toDegrees(azimuthInRadians.toDouble()).toFloat()
            
            if (azimuthInDegrees < 0) {
                azimuthInDegrees += 360f
            }
            
            // Higher responsiveness with rotation vector
            val alpha = if (event.sensor.type == Sensor.TYPE_ROTATION_VECTOR) 0.35f else 0.25f
            
            // Check for wrap-around crossing 0/360 degrees to prevent backward spinning lag
            var diff = azimuthInDegrees - (lastHeading % 360 + 360) % 360
            while (diff < -180) diff += 360
            while (diff > 180) diff -= 360
            
            val newHeading = lastHeading + alpha * diff
            lastHeading = newHeading

            val normalizedHeading = (newHeading % 360 + 360) % 360

            val qiblaDir = _uiState.value.qiblaDirection
            var aligned = false
            if (qiblaDir != null) {
                val bearingDiff = abs(normalizedHeading - qiblaDir)
                aligned = bearingDiff < 5 || bearingDiff > 355 // ±5 degrees tolerance
            }

            _uiState.update { 
                it.copy(
                    compassHeading = newHeading, // Pass continuous heading for butter-smooth animation
                    isAlignedWithQibla = aligned
                )
            }
        }
    }

    override fun onAccuracyChanged(sensor: Sensor?, accuracy: Int) {}
    
    override fun onCleared() {
        super.onCleared()
        stopSensors()
    }
}
