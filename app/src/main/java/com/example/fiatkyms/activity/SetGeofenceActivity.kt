package com.example.fiatkyms.activity


import android.Manifest
import android.app.AlertDialog
import android.app.ProgressDialog
import android.content.pm.PackageManager
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.os.Bundle
import android.util.Log
import android.view.Gravity
import android.view.MenuItem
import android.view.View
import android.view.ViewGroup
import android.widget.AdapterView
import android.widget.ArrayAdapter
import android.widget.Button
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.app.AppCompatDelegate
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.databinding.DataBindingUtil
import androidx.lifecycle.ViewModelProvider
import com.example.fiatkyms.R
import com.example.fiatkyms.databinding.ActivitySetGeofenceBinding
import com.example.fiatkyms.helper.Constants
import com.example.fiatkyms.helper.Gps
import com.example.fiatkyms.helper.Resource
import com.example.fiatkyms.helper.SessionManager
import com.example.fiatkyms.model.geofence.AddGeofenceRequest
import com.example.fiatkyms.repository.KYMSRepository
import com.example.fiatkyms.viewmodel.Geofence.GeofenceVM
import com.example.fiatkyms.viewmodel.Geofence.GeofenceVMFactory
import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.OnMapReadyCallback
import com.google.android.gms.maps.model.*
import com.google.android.material.textfield.MaterialAutoCompleteTextView
import es.dmoral.toasty.Toasty
import java.util.*

class SetGeofenceActivity : AppCompatActivity(), View.OnClickListener, OnMapReadyCallback {

    lateinit var binding: ActivitySetGeofenceBinding

    private lateinit var viewModel: GeofenceVM

    private lateinit var progress: ProgressDialog

    var map: GoogleMap? = null

    var currentMarker: Marker? = null

    var lastLat = 0.0
    var lastLng = 0.0
    var lat = 0.0
    var lng = 0.0
    var newLat = 0.0
    var newLng = 0.0

    var t = Timer()
    var tt: TimerTask? = null

    var latLngList: ArrayList<LatLng> = ArrayList()
    var markerList: ArrayList<Marker> = ArrayList()

    var builder: AlertDialog.Builder? = null
    var dialog: AlertDialog? = null

    lateinit var spinnerArr: List<String>
    private var adapter: ArrayAdapter<String>? = null
    lateinit var adapterDialog: ArrayAdapter<String>

    val coordinatesMap = HashMap<String, ArrayList<LatLng>>()
    val idMap = HashMap<String, String>()

    private lateinit var session: SessionManager

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_NO)
        binding = DataBindingUtil.setContentView(this, R.layout.activity_set_geofence)
        binding.listener = this
        binding.mapview.onCreate(savedInstanceState)
        binding.mapview.getMapAsync(this)

        binding.setGeofenceToolbar.title = "Set/Delete Geofence"
        binding.setGeofenceToolbar.setTitleTextColor(ContextCompat.getColor(this, R.color.white))

        setSupportActionBar(binding.setGeofenceToolbar)

        supportActionBar?.setDisplayHomeAsUpEnabled(true)
        supportActionBar?.setDisplayShowHomeEnabled(true)

        Toasty.Config.getInstance()
            .setGravity(Gravity.CENTER)
            .apply()

        session = SessionManager(this)

        val kymsRepository = KYMSRepository()
        val viewModelProviderFactory =
            GeofenceVMFactory(application, kymsRepository)
        viewModel =
            ViewModelProvider(this, viewModelProviderFactory)[GeofenceVM::class.java]

        progress = ProgressDialog(this)
        progress.setMessage("Loading...")

        spinnerArr = ArrayList()
        (spinnerArr as ArrayList<String>).add("Select Geofence")
        adapter = ArrayAdapter<String>(this, R.layout.spinner_item, spinnerArr)
        binding.spinnerPolygon.adapter = adapter

        binding.spinnerPolygon.onItemSelectedListener =
            object : AdapterView.OnItemSelectedListener {
                override fun onItemSelected(
                    adapterView: AdapterView<*>?,
                    view: View?,
                    i: Int,
                    l: Long
                ) {
                    val selectedItem = adapterView?.selectedItem.toString()
                    val selectedItemPosi = adapterView?.selectedItemPosition

                    if (selectedItemPosi == 0) {
                        resetPolygon("")
                    } else {
                        resetPolygon(selectedItem)
                    }
                }

                override fun onNothingSelected(adapterView: AdapterView<*>?) {}
            }

        builder = AlertDialog.Builder(this)
        initDialog()

        tt = object : TimerTask() {
            override fun run() {
                getLocationNew()
            }
        }

        t.scheduleAtFixedRate(tt, 1000, 1000)

        viewModel.addGeofenceMLD.observe(this) { response ->
            when (response) {
                is Resource.Success -> {
                    hideProgressBar()
                    response.data?.let { resultResponse ->
                        if (resultResponse != null) {
                            clearAllMarker()
                            edName.setText("")
                            dialog!!.dismiss()
                            viewModel.getAllGeofence(
                                session.getToken(),
                                Constants.BASE_URL
                            )
                        }

                        /*     when (resultResponse.statusCode) {

                                 Constants.HTTP_CREATED, Constants.HTTP_UPDATED -> {
                                     clearAllMarker()
                                     edName.setText("")
                                     dialog!!.dismiss()
                                     viewModel.getAllGeofence(
                                         session.getToken(),
                                         "http://192.168.1.12:5000/api/"
                                     )
                                 }
                             }*/
                    }
                }
                is Resource.Error -> {
                    hideProgressBar()
                    response.message?.let { resultResponse ->
                        Toast.makeText(this, resultResponse, Toast.LENGTH_SHORT).show()

                        if (resultResponse == "Unauthorized" || resultResponse == "Authentication token expired" ||
                            resultResponse == Constants.CONFIG_ERROR
                        ) {
                            session.showCustomDialog(
                                "Session Expired",
                                "Please re-login to continue",
                                this@SetGeofenceActivity
                            )
                        }
                    }
                }
                is Resource.Loading -> {
                    showProgressBar()
                }
                else -> {}
            }
        }

        viewModel.getAllGeofenceMLD.observe(this) { response ->
            when (response) {
                is Resource.Success -> {
                    hideProgressBar()
                    response.data?.let { resultResponse ->
                        if (resultResponse.size > 0) {
                            map?.clear()
                            for (e in resultResponse) {
                                var coordinates: ArrayList<LatLng> =
                                    parseStringToList(e.coordinates)
                                if (!coordinatesMap.containsKey(e.displayName)) {
                                    coordinatesMap[e.displayName] = coordinates
                                    idMap[e.displayName] = "${e.locationId}"
                                    (spinnerArr as ArrayList<String>).add(e.displayName)
                                }
                                Log.d("coordinates", coordinates.toString())
                                val polygonOptions =
                                    PolygonOptions().addAll(coordinates).clickable(false)
                                        .strokeColor(
                                            ContextCompat.getColor(
                                                this,
                                                R.color.colorPrimaryLight
                                            )
                                        )
                                        .strokeWidth(3f)
                                map?.addPolygon(polygonOptions)
                            }
                            adapter?.notifyDataSetChanged()
                            adapterDialog.notifyDataSetChanged()

                        }
                    }
                }
                is Resource.Error -> {
                    hideProgressBar()
                    response.message?.let { resultResponse ->
                        Toast.makeText(this, resultResponse, Toast.LENGTH_SHORT).show()
                        if (resultResponse == "Unauthorized" || resultResponse == "Authentication token expired" ||
                            resultResponse == Constants.CONFIG_ERROR
                        ) {
                            session.showCustomDialog(
                                "Session Expired",
                                "Please re-login to continue",
                                this@SetGeofenceActivity
                            )
                        }
                    }
                }
                is Resource.Loading -> {
                    showProgressBar()
                }
            }
        }

    }

    private fun resetPolygon(polygonName: String) {
        map?.clear()
        for ((key, value) in coordinatesMap.entries) {
            if (polygonName.equals(key, true)) {
                val polygonOptions =
                    PolygonOptions().addAll(value).clickable(false)
                        .strokeColor(
                            ContextCompat.getColor(
                                this,
                                R.color.red
                            )
                        )
                        .strokeWidth(3f)
                map?.addPolygon(polygonOptions)
            } else if (polygonName.equals("")) {
                val polygonOptions =
                    PolygonOptions().addAll(value).clickable(false)
                        .strokeColor(
                            ContextCompat.getColor(
                                this,
                                R.color.colorPrimaryLight
                            )
                        )
                        .strokeWidth(3f)
                map?.addPolygon(polygonOptions)
            } else {
                val polygonOptions =
                    PolygonOptions().addAll(value).clickable(false)
                        .strokeColor(
                            ContextCompat.getColor(
                                this,
                                R.color.grey
                            )
                        )
                        .strokeWidth(3f)
                map?.addPolygon(polygonOptions)
            }
        }
    }

    override fun onClick(view: View?) {
        when (view!!.id) {
            R.id.add_geofence -> if (latLngList.size > 3) showAddPolygonNameDialog() else Toast.makeText(
                this,
                "Please draw atleast 4 points on map to add Geofence!",
                Toast.LENGTH_SHORT
            ).show()
            R.id.bt_clear -> clearAllMarker()
            R.id.update_geofence -> clearAllMarker()
            /*R.id.bt_delete -> showDeleteDialog()*/
            R.id.btnRecenter -> {
                if (currentMarker != null) currentMarker!!.remove()
                val currentPos = LatLng(newLat, newLng)
                map?.moveCamera(CameraUpdateFactory.newLatLng(currentPos))
                map?.animateCamera(CameraUpdateFactory.zoomTo(16f))
            }
        }
    }

    fun resetMap() {
        map?.moveCamera(CameraUpdateFactory.newLatLng(LatLng(20.5937, 78.9629)))
        map?.animateCamera(CameraUpdateFactory.zoomTo(3f))
    }

    override fun onMapReady(googleMap: GoogleMap) {
        map = googleMap
        map?.mapType = GoogleMap.MAP_TYPE_SATELLITE
        map?.uiSettings?.isMyLocationButtonEnabled = false
        //map?.mapType = GoogleMap.MAP_TYPE_SATELLITE
        resetMap()

        map?.setOnMapClickListener { latLng: LatLng? -> addMarker(latLng) }
    }

    fun addMarker(latLng: LatLng?) {
        val markerOptions = MarkerOptions().position(latLng!!).anchor(0.5f, 0.5f).icon(
            BitmapDescriptorFactory.fromBitmap(
                generateSmallIcon()!!
            )
        )
        val marker = map?.addMarker(markerOptions)
        latLngList.add(latLng)
        markerList.add(marker!!)
    }

    fun clearAllMarker() {
        for (marker in markerList) marker.remove()
        latLngList.clear()
        markerList.clear()
    }

    override fun onDestroy() {
        super.onDestroy()
        binding.mapview.onDestroy()
    }

    override fun onPause() {
        super.onPause()
        binding.mapview.onPause()
        if (t != null) {
            t.cancel()
            tt!!.cancel()
        }
    }

    override fun onResume() {
        super.onResume()
        binding.mapview.onResume()
        viewModel.getAllGeofence(
            session.getToken(),
            Constants.BASE_URL
        )
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

    override fun onLowMemory() {
        super.onLowMemory()
        binding.mapview.onLowMemory()
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
                if (currentMarker != null) currentMarker!!.remove()
                val markerOptions =
                    MarkerOptions().position(LatLng(newLat, newLng)).title("You are here!")
                        .icon(BitmapDescriptorFactory.fromBitmap(generateLocationIcon()!!))
                currentMarker = map?.addMarker(markerOptions)
            }
        })
    }


    fun generateLocationIcon(): Bitmap? {
        val height = 40
        val width = 40
        val bitmap = BitmapFactory.decodeResource(resources, R.drawable.ic_pin)
        return Bitmap.createScaledBitmap(bitmap, width, height, false)
    }

    fun generateSmallIcon(): Bitmap? {
        val height = 20
        val width = 20
        val bitmap = BitmapFactory.decodeResource(resources, R.drawable.location_marker_green)
        return Bitmap.createScaledBitmap(bitmap, width, height, false)
    }

    lateinit var customLayout: View
    lateinit var edName: MaterialAutoCompleteTextView
    lateinit var btnOk: Button
    lateinit var btnCancel: Button

    private fun initDialog() {
        adapterDialog = ArrayAdapter<String>(this, R.layout.spinner_item, spinnerArr)
        customLayout = layoutInflater.inflate(R.layout.set_polygon_name_dialog, null)
        edName = customLayout.findViewById(R.id.edName)
        edName.setAdapter(adapterDialog)
        btnOk = customLayout.findViewById(R.id.btnOk)
        btnCancel = customLayout.findViewById(R.id.btnCancel)
        builder!!.setView(customLayout)
    }

    private fun showAddPolygonNameDialog() {
        builder = AlertDialog.Builder(this)
        adapterDialog = ArrayAdapter<String>(this, R.layout.spinner_item, spinnerArr)
        customLayout = layoutInflater.inflate(R.layout.set_polygon_name_dialog, null)
        edName = customLayout.findViewById(R.id.edName)
        edName.setAdapter(adapterDialog)
        btnOk = customLayout.findViewById(R.id.btnOk)
        btnCancel = customLayout.findViewById(R.id.btnCancel)
        builder!!.setView(customLayout)
        dialog = builder!!.create()

        btnOk.setOnClickListener {
            val edTxValue = edName.text.toString().trim()
            var arr = latLngList.toString()
            if (edTxValue.isNotEmpty()) {
                Log.e("Geofence", arr)
                viewModel.addGeofence(
                    session.getToken(), Constants.BASE_URL, AddGeofenceRequest(
                        arr,
                        edName.text.toString()
                    )
                )
            } else {
                Toast.makeText(this, "Please fill the details", Toast.LENGTH_SHORT).show()
            }


        }
        btnCancel.setOnClickListener { dialog!!.dismiss() }
        dialog!!.setCancelable(false)
        dialog!!.show()
        val window = dialog!!.getWindow()
        window!!.setLayout(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT)
    }

    fun showProgressBar() {
        progress.show()
    }

    fun hideProgressBar() {
        progress.cancel()
    }

    fun parseStringToList(inputString: String): ArrayList<LatLng> {
        val regex = Regex("\\((-?\\d+\\.\\d+),(-?\\d+\\.\\d+)\\)")
        val matches = regex.findAll(inputString)

        val latLngList = ArrayList<LatLng>()
        for (match in matches) {
            val (latitudeStr, longitudeStr) = match.destructured
            val latitude = latitudeStr.toDouble()
            val longitude = longitudeStr.toDouble()
            val latLng = LatLng(latitude, longitude)
            latLngList.add(latLng)
        }
        return latLngList
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        when (item.itemId) {
            android.R.id.home -> {
                onBackPressed()
                return true
            }
            else -> return super.onOptionsItemSelected(item)
        }
    }
}