package com.example.fiatkyms.activity


import android.Manifest
import android.app.ProgressDialog
import android.content.Intent
import android.content.pm.PackageManager
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.os.Build
import android.os.Bundle
import android.util.Log
import android.view.Gravity
import android.view.MenuItem
import android.view.View
import android.widget.AdapterView
import android.widget.ArrayAdapter
import android.widget.Spinner
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.databinding.DataBindingUtil
import androidx.lifecycle.ViewModelProvider
import com.example.fiatkyms.R
import com.example.fiatkyms.databinding.ActivityParkInBinding
import com.example.fiatkyms.helper.*
import com.example.fiatkyms.model.parkinout.ParkInOutRequest
import com.example.fiatkyms.model.prdoutmodel.GetAllPrdOutVinList
import com.example.fiatkyms.repository.KYMSRepository
import com.example.fiatkyms.viewmodel.parkinout.ParkInOutViewModel
import com.example.fiatkyms.viewmodel.parkinout.ParkInOutViewModelFactory
import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.OnMapReadyCallback
import com.google.android.gms.maps.model.*
import com.google.maps.android.PolyUtil

import com.symbol.emdk.EMDKManager
import com.symbol.emdk.EMDKResults
import com.symbol.emdk.barcode.*
import com.symbol.emdk.barcode.Scanner
import com.symbol.emdk.barcode.StatusData.ScannerStates
import com.zebra.rfid.api3.TagData
import es.dmoral.toasty.Toasty
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.cancel
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import java.util.*

class ParkInActivity : AppCompatActivity(), View.OnClickListener,
    RFIDHandler.ResponseHandlerInterface,
    EMDKManager.EMDKListener, Scanner.StatusListener, Scanner.DataListener, OnMapReadyCallback {

    var emdkManager: EMDKManager? = null
    var barcodeManager: BarcodeManager? = null
    var scanner: Scanner? = null

    var resumeFlag = false
    var TAG = "ParkInActivity"

    lateinit var binding: ActivityParkInBinding

    var rfidHandler: RFIDHandler? = null

    var newLat = 0.0
    var newLng = 0.0

    var t = Timer()
    var tt: TimerTask? = null
    private lateinit var viewModel: ParkInOutViewModel

    private lateinit var session: SessionManager
    private lateinit var userDetails: HashMap<String, String?>
    private var token: String? = ""
    private var userName: String? = ""
    private lateinit var progress: ProgressDialog

    val coordinatesMap = HashMap<String, ArrayList<LatLng>>()
    val idMap = HashMap<String, String>()

    var isRFIDInit = false
    var isBarcodeInit = false

    var map: GoogleMap? = null
    var currentMarker: Marker? = null
    val vinPrdOutList: ArrayList<GetAllPrdOutVinList> = ArrayList()

    private var vinSelected: String = ""
    private fun initReader() {
        rfidHandler = RFIDHandler()
        rfidHandler!!.init(this)
    }

    override fun onPause() {
        super.onPause()
        binding.mapview.onPause()
        if (isRFIDInit) {
            rfidHandler!!.onPause()
        }
        if (isBarcodeInit) {
            deInitScanner()
        }
        if (t != null) {
            t.cancel()
            tt!!.cancel()
        }
        resumeFlag = true
    }



    override fun onPostResume() {
        super.onPostResume()
        binding.mapview.onResume()
        if (isRFIDInit) {
            val status = rfidHandler!!.onResume()
            Toast.makeText(this@ParkInActivity, status, Toast.LENGTH_SHORT).show()
        }

    }

    override fun onLowMemory() {
        super.onLowMemory()
        binding.mapview.onLowMemory()
    }

    override fun onDestroy() {
        super.onDestroy()
        binding.mapview.onDestroy()
        binding.unbind()
        if (isRFIDInit) {
            rfidHandler!!.onDestroy()
        }
        if (isBarcodeInit) {
            deInitScanner()
        }
    }

    fun performInventory() {
        rfidHandler!!.performInventory()
    }

    fun stopInventory() {
        rfidHandler!!.stopInventory()
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = DataBindingUtil.setContentView(this, R.layout.activity_park_in)
        binding.listener = this

        binding.parkInVehicleToolbar.setTitleTextColor(ContextCompat.getColor(this, R.color.white))

        setSupportActionBar(binding.parkInVehicleToolbar)

        supportActionBar?.setDisplayHomeAsUpEnabled(true)
        supportActionBar?.setDisplayShowHomeEnabled(true)
        binding.mapview.onCreate(savedInstanceState)
        binding.mapview.getMapAsync(this)
        Toasty.Config.getInstance()
            .setGravity(Gravity.CENTER)
            .apply()

        val kymsRepository = KYMSRepository()
        val viewModelProviderFactory =
            ParkInOutViewModelFactory(application, kymsRepository)
        viewModel =
            ViewModelProvider(this, viewModelProviderFactory)[ParkInOutViewModel::class.java]

        progress = ProgressDialog(this)
        progress.setMessage("Loading...")

        session = SessionManager(this)
        if (session.getRole().equals("Driver")) {
            binding.parkInVehicleToolbar.title = "Park IN"
        } else if (session.getRole().equals("supervisor") || session.getRole()
                .equals("superadmin")
        ) {

            binding.parkInVehicleToolbar.title = "Park OUT"
        }

        userDetails = session.getUserDetails()
        token = userDetails["jwtToken"]
        userName = userDetails["userName"]

        // binding.radioGroup.check(binding.radioBtn2.getId())

        if (Build.MANUFACTURER.contains("Zebra Technologies") || Build.MANUFACTURER.contains("Motorola Solutions")) {
            binding.scanBarcode.visibility = View.GONE
            //binding.radioGroup.visibility = View.VISIBLE
        } else {
            //binding.scanBarcode.visibility = View.VISIBLE
            // binding.radioGroup.visibility = View.GONE
        }

        setDefaultScanner()
        //defaultRFID()
        /*binding.radioGroup.setOnCheckedChangeListener { buttonView, selected ->
            if (Build.MANUFACTURER.contains("Zebra Technologies") || Build.MANUFACTURER.contains("Motorola Solutions")) {
                if (selected == binding.radioBtn1.getId()) {
                    isRFIDInit = true
                    isBarcodeInit = false
                    deInitScanner()
                    Thread.sleep(1000)
                    initReader()
                } else if (selected == binding.radioBtn2.getId()) {
                    isRFIDInit = false
                    isBarcodeInit = true
                    rfidHandler!!.onPause()
                    rfidHandler!!.onDestroy()
                    Thread.sleep(1000)
                    val results = EMDKManager.getEMDKManager(this@ParkInActivity, this)
                    if (results.statusCode != EMDKResults.STATUS_CODE.SUCCESS) {
                        Log.e(TAG, "EMDKManager object request failed!")
                    } else {
                        Log.e(
                            TAG,
                            "EMDKManager object initialization is   in   progress......."
                        )
                    }
                }


            }
        }*/

        getYardGeofence()
        getAllPrdOutVinList()


        viewModel.getAllGeofenceMLD.observe(this) { response ->
            when (response) {
                is Resource.Success -> {
                    hideProgressBar()
                    response.data?.let { resultResponse ->
                        //if (resultResponse.size > 0) {
                        //for (e in resultResponse) {
                        if (resultResponse != null) {
                            var coordinates: ArrayList<LatLng> =
                                parseStringToList(resultResponse.coordinates)
                            if (!coordinatesMap.containsKey(resultResponse.locationName)) {
                                coordinatesMap[resultResponse.locationName] = coordinates
                                idMap[resultResponse.locationName] = "${resultResponse.locationId}"
                            }

                            val polygonOptions =
                                PolygonOptions().addAll(coordinates).clickable(false)
                                    .strokeColor(
                                        ContextCompat.getColor(
                                            this,
                                            R.color.colorPrimaryLight
                                        )
                                    )
                                    .strokeWidth(3f)
                            map!!.addPolygon(polygonOptions)
                        }
                        // }

                    }
                    // }
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
                                this@ParkInActivity
                            )
                        }
                    }
                }
                is Resource.Loading -> {
                    showProgressBar()
                }
                else -> {

                }
            }
        }

        viewModel.getAllPrdOutVinListMutable .observe(this) { response ->
            when (response) {
                is Resource.Success -> {
                    hideProgressBar()
                    vinPrdOutList.clear()
                    response.data?.let { resultResponse ->
                        try {
                            if (resultResponse.size > 0) {
                                for (res in resultResponse) {
                                    vinPrdOutList.add(res)
                                    loadModelColorSpinner(vinPrdOutList)
                                }
                            }

                        } catch (e: Exception) {
                            Toasty.warning(
                                this@ParkInActivity,
                                e.printStackTrace().toString(),
                                Toasty.LENGTH_SHORT
                            ).show()
                        }

                    }
                    // }
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
                                this@ParkInActivity
                            )
                        }
                    }
                }
                is Resource.Loading -> {
                    showProgressBar()
                }
                else -> {

                }
            }
        }

        viewModel.parkInOutMutableLiveData.observe(this) { response ->
            when (response) {
                is Resource.Success -> {
                    hideProgressBar()
                    getAllPrdOutVinList()
                    response.data?.let { resultResponse ->
                        /*  Toast.makeText(this, resultResponse.responseMessage, Toast.LENGTH_SHORT)
                              .show()
  */                         if (resultResponse.responseMessage != null) {
                        Toasty.success(
                            this@ParkInActivity,
                            resultResponse.responseMessage.toString(),
                            Toasty.LENGTH_SHORT
                        ).show()

                    } else if (resultResponse.errorMessage != null) {
                        Toasty.warning(
                            this@ParkInActivity,
                            resultResponse.errorMessage.toString(),
                            Toasty.LENGTH_SHORT
                        ).show()

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
                                this@ParkInActivity
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

        tt = object : TimerTask() {
            override fun run() {
                getLocationNew()
            }
        }

        t.scheduleAtFixedRate(tt, 1000, 1000)
        binding.btnSubmitVin.setOnClickListener {
            parkInVin()
        }
    }
    private fun loadModelColorSpinner(arr: ArrayList<GetAllPrdOutVinList>) {
        var vinPrdOut = ArrayList<String>()
        for (i: GetAllPrdOutVinList in arr) {
            vinPrdOut.add(i.vin)
        }

        val spinner = findViewById<Spinner>(R.id.spinnerVin)

        if (spinner != null) {

            val adapter = ArrayAdapter(
                this,
                android.R.layout.simple_spinner_item, vinPrdOut
            )
            adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)

            spinner.adapter = adapter

            spinner.onItemSelectedListener = object :
                AdapterView.OnItemSelectedListener {
                override fun onItemSelected(
                    parent: AdapterView<*>,
                    view: View, position: Int, id: Long
                ) {
                    vinSelected = vinPrdOut[position]
                }

                override fun onNothingSelected(parent: AdapterView<*>) {
                    vinSelected = vinPrdOut[0]
                }

            }
        }
    }

    private fun parkInVin(){
        //val vinEd=binding.tvBarcode.text.toString().trim()
        if(vinSelected.isNotBlank())
            checkVehicleInsideGeofenceBarcode(vinSelected)
        else
            Toasty.error(
                this@ParkInActivity,"Vin Not Selected", Toasty.LENGTH_SHORT).show()

    }

    private fun getYardGeofence(){
        try {
            viewModel.getYardGeofence(
                session.getToken(),
                Constants.BASE_URL
            )
        }
        catch (e:Exception)
        {
            Toasty.error(
                this@ParkInActivity,
                e.message.toString(),
                Toasty.LENGTH_SHORT
            ).show()
        }

    }

    private fun getAllPrdOutVinList(){
        try {
            viewModel.getAllPrdOutVinList(
                session.getToken(),
                Constants.BASE_URL
            )
        }
        catch (e:Exception)
        {
            Toasty.error(
                this@ParkInActivity,
                e.message.toString(),
                Toasty.LENGTH_SHORT
            ).show()
        }

    }

    fun checkVehicleInsideGeofenceRFID(scanned: String) {
        for ((key, value) in coordinatesMap.entries) {
            if (containsLocation(LatLng(newLat, newLng), value, false)) {
                if (session.getRole().equals("Driver")) {
                    viewModel.parkInOut(
                        session.getToken(),
                        Constants.BASE_URL,
                        ParkInOutRequest(key, scanned, "$newLat,$newLng", "", "", userName!!)
                    )
                    runOnUiThread(Runnable {
                        Toasty.warning(
                            this@ParkInActivity,
                            "You are inside $key!!",
                            Toasty.LENGTH_SHORT
                        ).show()
                    })
                } else if (session.getRole().equals("supervisor") || session.getRole()
                        .equals("superadmin")
                ) {
                    viewModel.parkInOut(
                        session.getToken(),
                        Constants.BASE_URL,
                        ParkInOutRequest(
                            key,
                            scanned,
                            "$newLat,$newLng",
                            "",
                            "Park Out",
                            userName!!
                        )
                    )
                    runOnUiThread(Runnable {
                        Toasty.warning(
                            this@ParkInActivity,
                            "You are inside parking!!",
                            Toasty.LENGTH_SHORT
                        ).show()
                    })
                }

            } else {
                runOnUiThread(Runnable {
                    Toasty.warning(
                        this@ParkInActivity,
                        "You are not inside parking!!",
                        Toasty.LENGTH_SHORT
                    ).show()
                })
            }
        }
    }

    fun checkVehicleInsideGeofenceBarcode(scanned: String) {
        for ((key, value) in coordinatesMap.entries) {
            if (containsLocation(LatLng(newLat, newLng), value, false)) {
                if (session.getRole().equals("Driver")) {
                    viewModel.parkInOut(
                        session.getToken(),
                        Constants.BASE_URL,
                        ParkInOutRequest(key, "", "$newLat,$newLng", scanned, "", userName!!)
                    )
                    runOnUiThread(Runnable {
                        Toasty.warning(
                            this@ParkInActivity,
                            "You are inside parking!!",
                            Toasty.LENGTH_SHORT
                        ).show()
                    })
                } else if (session.getRole().equals("supervisor") || session.getRole()
                        .equals("superadmin")
                ) {
                    viewModel.parkInOut(
                        session.getToken(),
                        Constants.BASE_URL,
                        ParkInOutRequest(
                            key,
                            "",
                            "$newLat,$newLng",
                            scanned,
                            "Park Out",
                            userName!!
                        )
                    )
                    runOnUiThread(Runnable {
                        Toasty.warning(
                            this@ParkInActivity,
                            "You are inside parking!!",
                            Toasty.LENGTH_SHORT
                        ).show()
                    })

                }

            } else {
                runOnUiThread(Runnable {
                    Toasty.warning(
                        this@ParkInActivity,
                        "You are not inside parking!!",
                        Toasty.LENGTH_SHORT
                    ).show()
                })
            }
        }
    }

    fun containsLocation(point: LatLng, polygon: List<LatLng>, geodesic: Boolean): Boolean {
        return PolyUtil.containsLocation(point, polygon, geodesic)
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
                currentMarker = map!!.addMarker(markerOptions)
                Log.e(TAG, "Latitude/Longitude - $newLat,$newLng")
            }
        })
    }

    private fun defaultRFID() {
        isRFIDInit = true
        isBarcodeInit = false
        deInitScanner()
        Thread.sleep(1000)
        initReader()
    }
    private fun setDefaultScanner(){
        isRFIDInit = false
        isBarcodeInit = true
        //rfidHandler!!.onPause()
        //rfidHandler!!.onDestroy()
        Thread.sleep(1000)

        if (Build.MANUFACTURER.contains("Zebra Technologies") || Build.MANUFACTURER.contains("Motorola Solutions")) {
          /*  initBarcodeManager()
            initScanner()*/
            val results2 = EMDKManager.getEMDKManager(this@ParkInActivity, this)
            if (results2.statusCode != EMDKResults.STATUS_CODE.SUCCESS) {
                Log.e(TAG, "EMDKManager object request failed!")
            } else {
                Log.e(
                    TAG,
                    "EMDKManager object initialization is   in   progress......."
                )
            }
        }

    }

    fun generateLocationIcon(): Bitmap? {
        val height = 35
        val width = 35
        val bitmap = BitmapFactory.decodeResource(resources, R.drawable.ic_pin)
        return Bitmap.createScaledBitmap(bitmap, width, height, false)
    }

    override fun handleTagdata(tagData: Array<TagData>) {
        val sb = StringBuilder()
        sb.append(tagData[0].tagID)
        runOnUiThread {
            var tagDataFromScan = tagData[0].tagID
            binding.tvBarcode.setText(tagDataFromScan)
            Log.e(TAG, "RFID Data : $tagDataFromScan")

            stopInventory()
        }
        checkVehicleInsideGeofenceRFID(tagData[0].tagID.toString())
    }

    override fun handleTriggerPress(pressed: Boolean) {
        if (pressed) {
            performInventory()
        } else stopInventory()

    }

    override fun onOpened(emdkManager: EMDKManager?) {
        if (Build.MANUFACTURER.contains("Zebra Technologies") || Build.MANUFACTURER.contains("Motorola Solutions")) {
            this.emdkManager = emdkManager
            initBarcodeManager()
            initScanner()
        }
    }

    override fun onClosed() {
        if (emdkManager != null) {
            emdkManager!!.release()
            emdkManager = null
        }
    }

    override fun onResume() {
        super.onResume()
        binding.mapview.onResume()
        if (resumeFlag) {
            resumeFlag = false
            if (Build.MANUFACTURER.contains("Zebra Technologies") || Build.MANUFACTURER.contains("Motorola Solutions")) {
                initBarcodeManager()
                initScanner()
            }
        }
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

    fun initBarcodeManager() {
        barcodeManager =
            emdkManager!!.getInstance(EMDKManager.FEATURE_TYPE.BARCODE) as BarcodeManager
        if (barcodeManager == null) {
            Toast.makeText(
                this@ParkInActivity,
                "Barcode scanning is not supported.",
                Toast.LENGTH_LONG
            ).show()
            finish()
        }
    }

    fun initScanner() {
        if (scanner == null) {
            barcodeManager =
                emdkManager?.getInstance(EMDKManager.FEATURE_TYPE.BARCODE) as BarcodeManager
            scanner = barcodeManager!!.getDevice(BarcodeManager.DeviceIdentifier.DEFAULT)
            scanner?.addDataListener(this)
            scanner?.addStatusListener(this)
            scanner?.triggerType = Scanner.TriggerType.HARD
            try {
                scanner?.enable()
            } catch (e: ScannerException) {
                e.printStackTrace()
            }
        }
    }

    fun deInitScanner() {
        if (scanner != null) {
            try {
                scanner!!.release()
            } catch (e: Exception) {
            }
            scanner = null
        }
    }


    override fun onData(scanDataCollection: ScanDataCollection?) {
        var dataStr: String? = ""
        if (scanDataCollection != null && scanDataCollection.result == ScannerResults.SUCCESS) {
            val scanData = scanDataCollection.scanData
            for (data in scanData) {
                val barcodeData = data.data
                val labelType = data.labelType
                dataStr = barcodeData
            }
            runOnUiThread { binding.tvBarcode.setText(dataStr) }
            checkVehicleInsideGeofenceBarcode(dataStr.toString())

            Log.e(TAG, "Barcode Data : $dataStr")
        }
    }

    override fun onStatus(statusData: StatusData) {
        val state = statusData.state
        var statusStr = ""
        when (state) {
            ScannerStates.IDLE -> {
                statusStr = statusData.friendlyName + " is   enabled and idle..."
                setConfig()
                try {
                    scanner!!.read()
                } catch (e: ScannerException) {
                }
            }
            ScannerStates.WAITING -> statusStr = "Scanner is waiting for trigger press..."
            ScannerStates.SCANNING -> statusStr = "Scanning..."
            ScannerStates.DISABLED -> {}
            ScannerStates.ERROR -> statusStr = "An error has occurred."
            else -> {}
        }
        setStatusText(statusStr)
    }

    private fun setConfig() {
        if (scanner != null) {
            try {
                val config = scanner!!.config
                if (config.isParamSupported("config.scanParams.decodeHapticFeedback")) {
                    config.scanParams.decodeHapticFeedback = true
                }
                scanner!!.config = config
            } catch (e: ScannerException) {
                Log.e(TAG, e.message!!)
            }
        }
    }

    fun setStatusText(msg: String) {
        Log.e(TAG, "StatusText: $msg")
    }

    override fun onClick(view: View?) {
        when (view!!.id) {
            R.id.scanBarcode -> {
                val intent = Intent(this, ScanBarcodeActivity::class.java)
                startActivityForResult(intent, REQUEST_CODE)
            }
            R.id.btnClear -> {
                binding.tvBarcode.setText("")
            }
            R.id.btnRecenter -> {
                if (currentMarker != null) currentMarker!!.remove()
                val currentPos = LatLng(newLat, newLng)
                map!!.moveCamera(CameraUpdateFactory.newLatLng(currentPos))
                map!!.animateCamera(CameraUpdateFactory.zoomTo(16f))
                println("la-$newLat, $newLng")
            }
        }
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)

        if (requestCode == REQUEST_CODE) {
            if (resultCode == RESULT_OK) {
                val resultValue = data?.getStringExtra(ScanBarcodeActivity.EXTRA_RESULT)
                Log.e(TAG, resultValue ?: "Empty")
            } else {
                Log.e(TAG, "Result cancelled")
            }
        }
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

    companion object {
        const val REQUEST_CODE = 7777
    }

    override fun onMapReady(googleMap: GoogleMap) {
        map = googleMap
        map?.mapType = GoogleMap.MAP_TYPE_SATELLITE
        map!!.uiSettings.isMyLocationButtonEnabled = false
        map!!.mapType = GoogleMap.MAP_TYPE_NORMAL
    }

    override fun onBackPressed() {
        super.onBackPressed()
        startActivity(Intent(this@ParkInActivity,MainActivity::class.java))
        finish()
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