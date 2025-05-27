package com.example.health

import android.Manifest
import android.annotation.SuppressLint
import android.content.Intent
import android.location.LocationManager
import android.os.Bundle
import android.provider.Settings
import android.util.Log
import androidx.activity.ComponentActivity
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.compose.setContent
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Button
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import com.google.android.gms.location.FusedLocationProviderClient
import com.google.android.gms.location.LocationRequest
import com.google.android.gms.location.LocationServices
import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.model.BitmapDescriptorFactory
import com.google.android.gms.maps.model.CameraPosition
import com.google.android.gms.maps.model.LatLng
import com.google.android.gms.maps.model.LatLngBounds
import com.google.maps.android.compose.GoogleMap
import com.google.maps.android.compose.Marker
import com.google.maps.android.compose.MarkerState
import com.google.maps.android.compose.rememberCameraPositionState
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import org.json.JSONObject
import java.net.URL

// Data class to hold hospital information
data class Hospital(val name: String, val lat: Double, val lng: Double)

class LocationActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            val apiKey = "AIzaSyCgu05c7fh4V_l9WiGwMybgMuRe0t5Pdtc" // Replace with BuildConfig if set up
            HospitalFinderScreen(apiKey)
        }
    }

    override fun onStart() {
        super.onStart()
        Log.d("LocationActivity", "onStart: Activity started")
    }
}

@Composable
fun HospitalFinderScreen(apiKey: String) {
    val context = LocalContext.current
    var userLocation by remember { mutableStateOf<LatLng?>(null) }
    var hospitals by remember { mutableStateOf<List<Hospital>>(emptyList()) }
    var isLoading by remember { mutableStateOf(false) }
    var errorMessage by remember { mutableStateOf<String?>(null) }
    var isLocationServicesEnabled by remember { mutableStateOf(true) }

    val fusedLocationClient = remember { LocationServices.getFusedLocationProviderClient(context) }

    // Check if location services are enabled
    val locationManager = context.getSystemService(LocationManager::class.java)
    LaunchedEffect(Unit) {
        isLocationServicesEnabled = locationManager.isProviderEnabled(LocationManager.GPS_PROVIDER) ||
                locationManager.isProviderEnabled(LocationManager.NETWORK_PROVIDER)
        if (!isLocationServicesEnabled) {
            errorMessage = "Location services are disabled. Please enable them to find nearby hospitals."
        }
    }

    val enableLocationLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.StartActivityForResult(),
        onResult = {
            isLocationServicesEnabled = locationManager.isProviderEnabled(LocationManager.GPS_PROVIDER) ||
                    locationManager.isProviderEnabled(LocationManager.NETWORK_PROVIDER)
            if (!isLocationServicesEnabled) {
                errorMessage = "Location services are still disabled. Please enable them."
            }
        }
    )

    val locationPermissionLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestPermission(),
        onResult = { granted ->
            if (granted) {
                Log.d("HospitalFinder", "Location permission granted")
                if (isLocationServicesEnabled) {
                    getCurrentLocation(fusedLocationClient) { location ->
                        Log.d("HospitalFinder", "User location received: $location")
                        userLocation = location
                    }
                } else {
                    errorMessage = "Location services are disabled. Please enable them to find nearby hospitals."
                }
            } else {
                Log.d("HospitalFinder", "Location permission denied")
                errorMessage = "Location permission denied. Please enable it to find nearby hospitals."
            }
        }
    )

    // Request location permission on first composition
    LaunchedEffect(Unit) {
        Log.d("HospitalFinder", "Requesting location permission")
        locationPermissionLauncher.launch(Manifest.permission.ACCESS_FINE_LOCATION)
    }

    // Fetch nearby hospitals when user location is available
    LaunchedEffect(userLocation) {
        userLocation?.let { location ->
            Log.d("HospitalFinder", "Fetching hospitals for location: $location")
            isLoading = true
            errorMessage = null
            try {
                val fetchedHospitals = fetchNearbyHospitals(location.latitude, location.longitude, apiKey)
                Log.d("HospitalFinder", "Hospitals fetched: ${fetchedHospitals.size}")
                hospitals = fetchedHospitals
            } catch (e: Exception) {
                errorMessage = "Failed to load nearby hospitals: ${e.message}"
                Log.e("HospitalFinder", "Error fetching hospitals: ${e.message}", e)
            } finally {
                isLoading = false
            }
        } ?: run {
            Log.d("HospitalFinder", "User location is null, cannot fetch hospitals")
        }
    }

    Scaffold(modifier = Modifier.fillMaxSize()) { innerPadding ->
        Box(modifier = Modifier.padding(innerPadding)) {
            if (userLocation != null) {
                HospitalMap(userLocation!!, hospitals)
            } else {
                Box(
                    modifier = Modifier.fillMaxSize(),
                    contentAlignment = Alignment.Center
                ) {
                    if (errorMessage == null) {
                        Text("Waiting for location...")
                    } else {
                        // Display error message and options
                        Text(
                            text = errorMessage ?: "",
                            color = androidx.compose.ui.graphics.Color.Red,
                            modifier = Modifier.padding(bottom = 16.dp)
                        )
                        if (!isLocationServicesEnabled) {
                            Button(
                                onClick = {
                                    // Prompt user to enable location services
                                    val intent = Intent(Settings.ACTION_LOCATION_SOURCE_SETTINGS)
                                    enableLocationLauncher.launch(intent)
                                }
                            ) {
                                Text("Enable Location Services")
                            }
                        } else {
                            Button(
                                onClick = {
                                    // Retry location permission
                                    locationPermissionLauncher.launch(Manifest.permission.ACCESS_FINE_LOCATION)
                                }
                            ) {
                                Text("Retry Permission")
                            }
                        }
                    }
                }
            }

            if (isLoading) {
                CircularProgressIndicator(modifier = Modifier.align(Alignment.Center))
            }
        }
    }
}

@SuppressLint("MissingPermission")
fun getCurrentLocation(fusedLocationClient: FusedLocationProviderClient, onLocationReceived: (LatLng) -> Unit) {
    // First, try to get the last known location
    fusedLocationClient.lastLocation
        .addOnSuccessListener { location ->
            if (location != null) {
                Log.d("Location", "Last known location retrieved: ${location.latitude}, ${location.longitude}")
                onLocationReceived(LatLng(location.latitude, location.longitude))
            } else {
                Log.d("Location", "Last known location is null, requesting new location")
                // Request a fresh location
                val locationRequest = LocationRequest.create().apply {
                    priority = LocationRequest.PRIORITY_HIGH_ACCURACY
                    numUpdates = 1 // Request a single update
                    interval = 1000 // 1 second interval
                }
                fusedLocationClient.requestLocationUpdates(
                    locationRequest,
                    object : com.google.android.gms.location.LocationCallback() {
                        override fun onLocationResult(locationResult: com.google.android.gms.location.LocationResult) {
                            locationResult.lastLocation?.let { loc ->
                                Log.d("Location", "Fresh location retrieved: ${loc.latitude}, ${loc.longitude}")
                                onLocationReceived(LatLng(loc.latitude, loc.longitude))
                            } ?: run {
                                Log.e("Location", "Failed to get fresh location")
                            }
                            // Remove the callback after receiving the location
                            fusedLocationClient.removeLocationUpdates(this)
                        }

                        override fun onLocationAvailability(availability: com.google.android.gms.location.LocationAvailability) {
                            if (!availability.isLocationAvailable) {
                                Log.e("Location", "Location services are not available")
                            }
                        }
                    },
                    android.os.Looper.getMainLooper()
                ).addOnFailureListener { e ->
                    Log.e("Location", "Error requesting location updates: ${e.message}", e)
                }
            }
        }
        .addOnFailureListener { e ->
            Log.e("Location", "Error getting last location: ${e.message}", e)
        }
}

@Composable
fun HospitalMap(userLocation: LatLng, hospitals: List<Hospital>) {
    val cameraPositionState = rememberCameraPositionState {
        position = CameraPosition.fromLatLngZoom(userLocation, 14f)
    }

    LaunchedEffect(hospitals) {
        if (hospitals.isNotEmpty()) {
            val bounds = LatLngBounds.builder().apply {
                include(userLocation)
                hospitals.forEach { include(LatLng(it.lat, it.lng)) }
            }.build()
            cameraPositionState.move(CameraUpdateFactory.newLatLngBounds(bounds, 100))
            Log.d("HospitalMap", "Camera moved to include ${hospitals.size} hospitals")
        } else {
            Log.d("HospitalMap", "No hospitals to display, showing user location only")
        }
    }

    GoogleMap(
        modifier = Modifier.fillMaxSize(),
        cameraPositionState = cameraPositionState
    ) {
        Marker(
            state = MarkerState(position = userLocation),
            title = "You are here",
            icon = BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_BLUE)
        )

        hospitals.forEach { hospital ->
            Marker(
                state = MarkerState(position = LatLng(hospital.lat, hospital.lng)),
                title = hospital.name,
                icon = BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_RED)
            )
        }
        Log.d("HospitalMap", "Map rendered with ${hospitals.size} hospital markers")
    }
}

suspend fun fetchNearbyHospitals(lat: Double, lng: Double, apiKey: String): List<Hospital> {
    val url = "https://maps.googleapis.com/maps/api/place/nearbysearch/json" +
            "?location=$lat,$lng&radius=5000&type=hospital&key=$apiKey"

    try {
        val result = withContext(Dispatchers.IO) { URL(url).readText() }
        Log.d("HospitalFinder", "API response: $result")
        val json = JSONObject(result)

        if (!json.has("results")) {
            throw Exception("No results found in API response")
        }

        val hospitals = mutableListOf<Hospital>()
        val resultsArray = json.getJSONArray("results")

        for (i in 0 until resultsArray.length()) {
            val obj = resultsArray.getJSONObject(i)
            val name = obj.optString("name", "Unknown Hospital")
            val location = obj.optJSONObject("geometry")?.optJSONObject("location")
            val hospitalLat = location?.optDouble("lat") ?: continue
            val hospitalLng = location?.optDouble("lng") ?: continue
            hospitals.add(Hospital(name, hospitalLat, hospitalLng))
        }

        return hospitals
    } catch (e: Exception) {
        Log.e("HospitalFinder", "Failed to fetch hospitals: ${e.message}", e)
        throw e
    }
}