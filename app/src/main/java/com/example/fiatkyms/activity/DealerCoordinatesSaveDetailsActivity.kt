package com.example.fiatkyms.activity

import android.Manifest
import android.app.ProgressDialog
import android.content.pm.PackageManager
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.os.Bundle
import android.util.Log
import android.view.Gravity
import android.view.MenuItem
import android.view.View
import android.widget.AdapterView
import android.widget.ArrayAdapter
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.databinding.DataBindingUtil
import androidx.lifecycle.ViewModelProvider
import com.example.fiatkyms.R
import com.example.fiatkyms.databinding.ActivityDealerCoordinatesSaveDetailsBinding
import com.example.fiatkyms.helper.Constants
import com.example.fiatkyms.helper.Gps
import com.example.fiatkyms.helper.Resource
import com.example.fiatkyms.helper.SessionManager
import com.example.fiatkyms.model.dealercoordinates.AddDealerCoordinatesRequestResponse
import com.example.fiatkyms.repository.KYMSRepository
import com.example.fiatkyms.viewmodel.dealercoordinates.SaveDealerCoordinatesVM
import com.example.fiatkyms.viewmodel.dealercoordinates.SaveDealerCoordinatesVMPF
import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.OnMapReadyCallback
import com.google.android.gms.maps.model.*
import es.dmoral.toasty.Toasty
import java.util.*

class DealerCoordinatesSaveDetailsActivity : AppCompatActivity(), View.OnClickListener,
    OnMapReadyCallback {
    lateinit var binding: ActivityDealerCoordinatesSaveDetailsBinding
    private lateinit var viewModel: SaveDealerCoordinatesVM
    private lateinit var session: SessionManager
    private lateinit var progress: ProgressDialog


    var map: GoogleMap? = null
    var currentMarker: Marker? = null

    var newLat = 0.0
    var newLng = 0.0

    var t = Timer()
    var tt: TimerTask? = null


    var latLngList: ArrayList<LatLng> = ArrayList()
    var markerList: ArrayList<Marker> = ArrayList()
    val coordinatesMap = HashMap<String, ArrayList<LatLng>>()
    val idMap = HashMap<String, String>()
    lateinit var spinnerArr: List<String>
    private var adapter: ArrayAdapter<String>? = null

    var selectedDealer: String = ""
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding =
            DataBindingUtil.setContentView(this, R.layout.activity_dealer_coordinates_save_details)
        binding.listener = this
        binding.mapview.onCreate(savedInstanceState)
        binding.mapview.getMapAsync(this)
        binding.setGeofenceToolbar.setTitleTextColor(ContextCompat.getColor(this, R.color.white))
        setSupportActionBar(binding.setGeofenceToolbar)
        binding.setGeofenceToolbar.title = "Set Dealer Geofence"
        supportActionBar?.setDisplayHomeAsUpEnabled(true)
        supportActionBar?.setDisplayShowHomeEnabled(true)

        Toasty.Config.getInstance()
            .setGravity(Gravity.CENTER)
            .apply()

        session = SessionManager(this)
        progress = ProgressDialog(this)
        progress.setMessage("Loading...")
        spinnerArr = ArrayList()
        (spinnerArr as ArrayList<String>).add("Select Dealer")
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
                    selectedDealer = adapterView?.selectedItem.toString()
                    val selectedItemPosi = adapterView?.selectedItemPosition

                    if (selectedItemPosi == 0) {
                        resetPolygon("")
                    } else {
                        resetPolygon(selectedDealer)
                    }
                }

                override fun onNothingSelected(adapterView: AdapterView<*>?) {}
            }

        val kymsRepository = KYMSRepository()
        val viewModelProviderFactory =
            SaveDealerCoordinatesVMPF(application, kymsRepository)
        viewModel =
            ViewModelProvider(this, viewModelProviderFactory)[SaveDealerCoordinatesVM::class.java]

        getAllDealers()
        viewModel.addDealerGeofence.observe(this) { response ->
            when (response) {
                is Resource.Success -> {
                    hideProgressBar()
                    response.data?.let { resultResponse ->
                        Toasty.success(
                            this@DealerCoordinatesSaveDetailsActivity,
                            resultResponse.responseMessage.toString(),
                            Toasty.LENGTH_SHORT
                        ).show()
                        clearAllMarker()
                        //edName.setText("")
                        //dialog!!.dismiss()
                        getAllDealers()
                        /*   when (resultResponse.statusCode) {
                               Constants.HTTP_CREATED, Constants.HTTP_UPDATED -> {


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
                                this@DealerCoordinatesSaveDetailsActivity
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
        viewModel.dealerListLiveData.observe(this) { response ->
            when (response) {
                is Resource.Success -> {
                    hideProgressBar()
                    response.data?.let { resultResponse ->
                        if (resultResponse.size > 0) {
                            map?.clear()
                            (spinnerArr as ArrayList<String>).clear()
                            (spinnerArr as ArrayList<String>).add("Select Dealer")
                            for (e in resultResponse) {
                                (spinnerArr as ArrayList<String>).add(e.dealerName)
                                if (e.coordinates != null && !e.coordinates.equals("")) {
                                    var coordinates: ArrayList<LatLng> =
                                        parseStringToList(e.coordinates)
                                    if (!coordinatesMap.containsKey(e.dealerName)) {
                                        coordinatesMap[e.dealerName] = coordinates
                                        idMap[e.dealerName] = "${e.dealerCode}"

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
                            }
                            adapter?.notifyDataSetChanged()
                            //adapterDialog.notifyDataSetChanged()

                            binding.addGeofence.setOnClickListener {
                                Log.e("selectedDealer", selectedDealer.toString())
                                if (selectedDealer.isNotEmpty() && !selectedDealer.equals("") && !selectedDealer.equals(
                                        "Select Dealer"
                                    )
                                ) {
                                    val foundItem =
                                        resultResponse.filter { it.dealerName == selectedDealer }
                                    if (foundItem != null) {
                                        val Id = foundItem.first().id
                                        val dealerName = foundItem.first().dealerName
                                        val dealerCode = foundItem.first().dealerCode
                                        val dealerId = foundItem.first().dealerId
                                        submitGeofenceCoordinates(
                                            Id,
                                            dealerName,
                                            dealerCode,
                                            dealerId
                                        )
                                    }
                                } else {
                                    Toast.makeText(
                                        this@DealerCoordinatesSaveDetailsActivity,
                                        "Please select the dealer!!",
                                        Toast.LENGTH_SHORT
                                    ).show()
                                }
                            }


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
                                this@DealerCoordinatesSaveDetailsActivity
                            )
                        }
                    }
                }
                is Resource.Loading -> {
                    showProgressBar()
                }
            }
        }

        tt = object : TimerTask() {
            override fun run() {
                getLocationNew()
            }
        }

        t.scheduleAtFixedRate(tt, 1000, 1000)
    }

    fun submitGeofenceCoordinates(
        Id: Int,
        dealerName: String,
        dealerCode: String,
        dealerId1: Int
    ) {
        if (latLngList.size > 3) {
            var arr = latLngList.toString()
            Log.e("Geofence", arr)
            viewModel.addDealerGeofence(
                session.getToken(), Constants.BASE_URL,
                AddDealerCoordinatesRequestResponse(dealerId1, dealerCode, dealerName, arr, Id)
            )
        } else {
            Toast.makeText(
                this,
                "Please draw atleast 4 points on map to add Geofence!",
                Toast.LENGTH_SHORT
            ).show()
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

    override fun onMapReady(googleMap: GoogleMap) {
        map = googleMap
        map?.uiSettings?.isMyLocationButtonEnabled = false
        //map?.mapType = GoogleMap.MAP_TYPE_SATELLITE
        resetMap()

        map?.setOnMapClickListener { latLng: LatLng? -> addMarker(latLng) }
    }

    fun resetMap() {
        map?.moveCamera(CameraUpdateFactory.newLatLng(LatLng(20.5937, 78.9629)))
        map?.animateCamera(CameraUpdateFactory.zoomTo(3f))
    }

    fun clearAllMarker() {
        for (marker in markerList) marker.remove()
        latLngList.clear()
        markerList.clear()
    }

    override fun onClick(view: View?) {
        when (view!!.id) {
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
        getAllDealers()
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

    private fun getAllDealers() {
        try {
            viewModel.getAllDealers(
                //session.getToken(),
                session.getToken(),
                Constants.BASE_URL
            )
        } catch (e: Exception) {
            Toasty.success(
                this@DealerCoordinatesSaveDetailsActivity,
                e.toString(),
                Toasty.LENGTH_SHORT
            ).show()
        }
    }

    override fun onLowMemory() {
        super.onLowMemory()
        binding.mapview.onLowMemory()
    }

    fun showProgressBar() {
        progress.show()
    }

    fun hideProgressBar() {
        progress.cancel()
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