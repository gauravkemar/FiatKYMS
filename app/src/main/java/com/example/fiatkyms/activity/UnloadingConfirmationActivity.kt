package com.example.fiatkyms.activity


import android.Manifest
import android.app.AlertDialog
import android.content.Context
import android.content.pm.PackageManager
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.Color
import android.location.Location
import android.location.LocationManager
import android.media.AudioManager
import android.media.ToneGenerator
import android.os.*
import android.util.Log
import android.view.MenuItem
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.databinding.DataBindingUtil
import com.example.fiatkyms.R
import com.example.fiatkyms.databinding.ActivityNavigateBinding
import com.example.fiatkyms.helper.Constants
import com.example.fiatkyms.helper.Gps
import com.example.fiatkyms.helper.toLatLong
import com.google.android.gms.location.FusedLocationProviderClient
import com.google.android.gms.location.LocationRequest
import com.google.android.gms.location.LocationServices
import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.OnMapReadyCallback
import com.google.android.gms.maps.SupportMapFragment
import com.google.android.gms.maps.model.*
import java.util.*


class UnloadingConfirmationActivity : AppCompatActivity(), OnMapReadyCallback {

    private var vehicleLocation: Location? = null
    private var currentLocation: Location? = null
    lateinit var binding: ActivityNavigateBinding
    var TamWorth = LatLng(-31.083332, 150.916672)
    var NewCastle = LatLng(-32.916668, 151.750000)
    var Brisbane = LatLng(-27.470125, 153.021072)

    var t = Timer()
    var tt: TimerTask? = null

    var currentMarker: Marker? = null
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = DataBindingUtil.setContentView(this, R.layout.activity_navigate)
        binding.searchVehicleToolbar.title = "Navigate"
        binding.searchVehicleToolbar.setTitleTextColor(ContextCompat.getColor(this, R.color.white))
        setSupportActionBar(binding.searchVehicleToolbar)
        supportActionBar?.setDisplayHomeAsUpEnabled(true)
        supportActionBar?.setDisplayShowHomeEnabled(true)
        fusedLocationClient = LocationServices.getFusedLocationProviderClient(this)
        val mapFragment =
            supportFragmentManager.findFragmentById(R.id.mapFragment) as SupportMapFragment
        mapFragment.getMapAsync(this)
        val log = intent.getDoubleExtra(Constants.LONGITUDE, 0.0)
        val lat = intent.getDoubleExtra(Constants.LATITUDE, 0.0)
        vehicleLocation = Location("Vehicle location")
        vehicleLocation?.latitude = lat
        vehicleLocation?.longitude = log
        requestLocationPermission()
        binding.btnRecenter.setOnClickListener {
            currentLocation?.let {
                it.toLatLong().let { latLng ->
                    CameraUpdateFactory.newLatLngZoom(
                        latLng,
                        22f
                    )
                }.let { cameraUpdate ->
                    googleMap.moveCamera(
                        cameraUpdate
                    )
                }
            }
            /* if (currentMarker != null) currentMarker!!.remove()
             val currentPos = LatLng(newLat, newLng)
             googleMap?.moveCamera(CameraUpdateFactory.newLatLng(currentPos))
             googleMap?.animateCamera(CameraUpdateFactory.zoomTo(16f))*/
        }
        tt = object : TimerTask() {
            override fun run() {
                getLocationNew()
            }
        }

        t.scheduleAtFixedRate(tt, 1000, 1000)
    }

    override fun onBackPressed() {
        super.onBackPressed()
        finish()
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        when (item.itemId) {
            android.R.id.home -> {
                onBackPressed()
                //startActivity(Intent(this@UnloadingConfirmationActivity,SearchVehicleActivity::class.java))
                //finish()
                return true
            }

            else -> return super.onOptionsItemSelected(item)
        }
    }

    fun getLocationNew() {
        if (ActivityCompat.checkSelfPermission(
                this,
                Manifest.permission.ACCESS_FINE_LOCATION
            ) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(
                this,
                Manifest.permission.ACCESS_COARSE_LOCATION
            ) != PackageManager.PERMISSION_GRANTED
        ) {
            return
        }
        runOnUiThread(Runnable {
            val gps = Gps(this)
            if (gps.canGetLocation()) {
                newLat = gps.getLatitude()
                newLng = gps.getLongitude()
                if (gps != null) {
                    currentLocation = gps.location
                    Log.e("currentLoc", currentLocation.toString())

                    if (::googleMap.isInitialized) {
                        drawLines()

                    }
                } else {
                    requestLocation()
                }
            }
        })
    }

    fun generateLocationIcon(): Bitmap? {
        val height = 40
        val width = 40
        val bitmap = BitmapFactory.decodeResource(resources, R.drawable.ic_pin)
        return Bitmap.createScaledBitmap(bitmap, width, height, false)
    }


    override fun onResume() {
        super.onResume()
        if (t == null) {
            t = Timer()
            tt = object : TimerTask() {
                override fun run() {
                    getLocationNew()
                }
            }
            t.scheduleAtFixedRate(tt, 1000, 1000)
        }
    }

    fun generateCarLocationIcon(): Bitmap? {
        val height = 40
        val width = 40
        val bitmap = BitmapFactory.decodeResource(resources, R.drawable.ic_car)
        return Bitmap.createScaledBitmap(bitmap, width, height, false)
    }

    private lateinit var googleMap: GoogleMap
    private val LOCATION_PERMISSION_REQUEST_CODE = 1001

    // Inside your activity or fragment
    private fun requestLocationPermission() {
        if (ContextCompat.checkSelfPermission(
                this,
                Manifest.permission.ACCESS_FINE_LOCATION
            ) != PackageManager.PERMISSION_GRANTED
        ) {
            ActivityCompat.requestPermissions(
                this,
                arrayOf(Manifest.permission.ACCESS_FINE_LOCATION),
                LOCATION_PERMISSION_REQUEST_CODE
            )
        } else {
            // Permission already granted, proceed with getting location
            getLocation()
        }
    }

    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<out String>,
        grantResults: IntArray
    ) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        if (requestCode == LOCATION_PERMISSION_REQUEST_CODE) {
            if (grantResults.isNotEmpty() && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                // Permission granted, get location
                getLocation()
            } else {
                // Permission denied, handle accordingly
            }
        }
    }

    private lateinit var fusedLocationClient: FusedLocationProviderClient
    private fun requestLocation() {
        val locationRequest = LocationRequest.create()
        locationRequest.priority = LocationRequest.PRIORITY_HIGH_ACCURACY
        locationRequest.interval = 10000 // 10 seconds

        if (ActivityCompat.checkSelfPermission(
                this,
                Manifest.permission.ACCESS_FINE_LOCATION
            ) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(
                this,
                Manifest.permission.ACCESS_COARSE_LOCATION
            ) != PackageManager.PERMISSION_GRANTED
        ) {
            return
        }
        fusedLocationClient.requestLocationUpdates(
            locationRequest,
            object : com.google.android.gms.location.LocationCallback() {
                override fun onLocationResult(locationResult: com.google.android.gms.location.LocationResult) {
                    val location = locationResult.lastLocation
                    // Handle the location update here
                    if (location != null) {
                        updateLocation(location)
                    }
                }
            },
            null
        )
    }

    private fun updateLocation(location: Location) {
        // Handle the location update here
        /*val latitude = location.latitude

        val longitude = location.longitude
        currentLocation?.latitude = latitude
        currentLocation?.longitude = longitude*/

        currentLocation = location
        if (::googleMap.isInitialized) {
            drawLines()
        }
    }


    private fun getLocation() {
        val locationManager = getSystemService(Context.LOCATION_SERVICE) as LocationManager

        if (locationManager.isProviderEnabled(LocationManager.GPS_PROVIDER)) {
            // Use LocationManager to get location
            if (ActivityCompat.checkSelfPermission(
                    this,
                    Manifest.permission.ACCESS_FINE_LOCATION
                ) == PackageManager.PERMISSION_GRANTED
            ) {
                val location = locationManager.getLastKnownLocation(LocationManager.GPS_PROVIDER)
                if (location != null) {
                    val latitude = location.latitude
                    val longitude = location.longitude
                    currentLocation = location
                    if (::googleMap.isInitialized) {
                        drawLines()
                    }
                } else {
                    requestLocation()
                }
            }
        } else {
            // GPS is not enabled, prompt the user to enable it
            /*val intent = Intent(Settings.ACTION_LOCATION_SOURCE_SETTINGS)
            startActivity(intent)*/
        }
    }

    override fun onMapReady(map: GoogleMap) {
        googleMap = map
        googleMap.mapType = GoogleMap.MAP_TYPE_SATELLITE
        googleMap.clear()
        toneGen1 = ToneGenerator(AudioManager.STREAM_MUSIC, 100)
        drawLines()

    }

    override fun onPause() {
        super.onPause()
        if (t != null) {
            t.cancel()
            tt!!.cancel()

        }
    }

    private fun drawLines() {
        if (!isFinishing()) {
            googleMap.clear()
            currentLocation?.let {
                val startPoint = LatLng(
                    currentLocation?.latitude!!,
                    currentLocation?.longitude!!
                ) // Example start point
                val endPoint = LatLng(
                    vehicleLocation?.latitude!!,
                    vehicleLocation?.longitude!!
                )// Example end point
                googleMap.addPolyline(
                    PolylineOptions().add(startPoint, endPoint)
                        .width
                            (5f)
                        .color(Color.RED)
                        .geodesic(true)
                )
                if (currentMarker != null) currentMarker!!.remove()
                val markerOptions =
                    MarkerOptions().position(
                        LatLng(
                            currentLocation?.latitude!!,
                            currentLocation?.longitude!!
                        )
                    ).title("You are here!")
                        .icon(BitmapDescriptorFactory.fromBitmap(generateLocationIcon()!!))
                currentMarker = googleMap?.addMarker(markerOptions)
                //googleMap.moveCamera(CameraUpdateFactory.newLatLngZoom(startPoint, 16f))

                val vehicleOption =
                    MarkerOptions().position(
                        LatLng(
                            vehicleLocation?.latitude!!,
                            vehicleLocation?.longitude!!
                        )
                    ).title("You are here!")
                        .icon(BitmapDescriptorFactory.fromBitmap(generateCarLocationIcon()!!))
                currentMarker = googleMap?.addMarker(vehicleOption)
                //googleMap.moveCamera(CameraUpdateFactory.newLatLngZoom(startPoint, 16f))

                val distance: Float = roundOff(
                    currentLocation?.latitude!!,
                    currentLocation?.longitude!!,
                    vehicleLocation?.latitude!!,
                    vehicleLocation?.longitude!!
                )?.toFloat()!!

                binding.tvTotalDistance.setText("$distance Meters Approx.")
                if (distance > 10f && distance < 15f) {
                    audio(1000, true)
                } else if (distance > 5f && distance < 10f) {
                    audio(500, true)
                } else if (distance > 2f && distance < 5f) {
                    audio(333, true)
                } else if (distance < 2f) {
                    audio(100, true)
                    if (showAlert) showAlertOnce()
                } else {
                    audio(0, false)
                }
            }
        }
    }


    override fun onDestroy() {
        super.onDestroy()
        toneGen1?.let {
            it.stopTone()
            handler.removeCallbacksAndMessages(null)
            it.release()
        }
        toneGen1 = null
        if (t != null) {
            t.cancel()
            tt!!.cancel()
        }
    }

    private fun isWithinRange(userLocation: Location, targetLocation: Location): Boolean {
        // Customize this function to define your "arrival" criteria
        val distance = userLocation.distanceTo(targetLocation)
        return distance <= TARGET_RADIUS_METERS
    }

    private fun vibrateDevice() {
        val vibrator = getSystemService(VIBRATOR_SERVICE) as Vibrator
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            vibrator.vibrate(VibrationEffect.createOneShot(1000, VibrationEffect.DEFAULT_AMPLITUDE))
        } else {
            // For devices running older Android versions
            vibrator.vibrate(1000)
        }
    }


    fun roundOff(lastLat: Double, lastLng: Double, lat: Double, lng: Double): String? {
        val results = FloatArray(1)
        Location.distanceBetween(
            lastLat, lastLng,
            lat, lng,
            results
        )
        //Toast.makeText(getActivity(), "Distance between Point A to B is : " + results[0], Toast.LENGTH_SHORT).show();
        return if (results[0] == 0f) "0.0" else String.format("%.2f", results[0])
    }

    fun audio(delay: Long, audio: Boolean) {
        handler.removeMessages(0)
        if (audio) {
            handler.postDelayed(object : Runnable {
                override fun run() {
                    if(!isFinishing())
                    {
                        toneGen1?.let {
                            toneGen1?.startTone(ToneGenerator.TONE_PROP_BEEP2, 100)
                            handler.postDelayed(this, delay)
                        }
                    }


                }
            }, 0)
        } else {
            handler.removeMessages(0)
        }
    }

    var newLat = 0.0
    var newLng = 0.0
    var toneGen1: ToneGenerator? = null

    val handler = Handler(Looper.getMainLooper())
    var showAlert = true
    fun showAlertOnce() {
        val alertDialog = AlertDialog.Builder(this)
        alertDialog.setTitle("Alert!")
        alertDialog.setMessage("You have reached the location!")
        alertDialog.setCancelable(false)
        alertDialog.setPositiveButton(
            "Okay"
        ) { dialog, which -> //tt.cancel();
            if (handler != null) handler.removeMessages(0)
        }
        alertDialog.show()
        showAlert = false
    }


    companion object {
        // Define the target location and arrival radius here
        private val targetLocation = Location("").apply {
            latitude = 12.34
            longitude = 56.78
        }
        private const val TARGET_RADIUS_METERS = 100.0
    }
}