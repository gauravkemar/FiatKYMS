package com.example.fiatkyms.activity


import android.app.ProgressDialog
import android.content.Intent
import android.os.Build
import android.os.Bundle
import android.util.Log
import android.view.Gravity
import android.view.MenuItem
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import androidx.databinding.DataBindingUtil
import androidx.lifecycle.ViewModelProvider
import com.example.fiatkyms.R
import com.example.fiatkyms.databinding.ActivityVinRfidMappingBinding
import com.example.fiatkyms.helper.*
import com.example.fiatkyms.model.vinrfidmapping.AddVehicle
import com.example.fiatkyms.model.vinrfidmapping.VinRfidRequest
import com.example.fiatkyms.repository.KYMSRepository
import com.example.fiatkyms.viewmodel.vinrfidmapping.VinRfidMappingViewModel
import com.example.fiatkyms.viewmodel.vinrfidmapping.VinRfidMappingViewModelFactory
import com.symbol.emdk.EMDKManager
import com.symbol.emdk.EMDKResults
import com.symbol.emdk.barcode.*
import com.zebra.rfid.api3.TagData
import es.dmoral.toasty.Toasty

class VinRfidMappingActivity : AppCompatActivity(), EMDKManager.EMDKListener,
    Scanner.StatusListener, Scanner.DataListener, RFIDHandler.ResponseHandlerInterface {
    lateinit var binding: ActivityVinRfidMappingBinding
    private lateinit var viewModel: VinRfidMappingViewModel
    private lateinit var progress: ProgressDialog
    private lateinit var session: SessionManager

    /////implementation of RFID AND BARCODE
    var TAG = "MainActivity"
    var rfidHandler: RFIDHandlerForVinMapping? = null
    private fun initReader() {
        rfidHandler = RFIDHandlerForVinMapping()
        rfidHandler!!.init(this)
    }
    var isRFIDInit = false
    var isBarcodeInit = false
    var resumeFlag = false
    var emdkManager: EMDKManager? = null
    var barcodeManager: BarcodeManager? = null
    var scanner: Scanner? = null

    override fun onPause() {
        super.onPause()
        if (isRFIDInit) {
            rfidHandler!!.onPause()
        }
        if (isBarcodeInit) {
            deInitScanner()
        }
        resumeFlag = true
    }

    override fun onPostResume() {
        super.onPostResume()

        if (isRFIDInit) {
            val status = rfidHandler!!.onResume()
            Toast.makeText(this@VinRfidMappingActivity, status, Toast.LENGTH_SHORT).show()
        }

    }

    override fun onDestroy() {
        super.onDestroy()
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
        binding = DataBindingUtil.setContentView(this, R.layout.activity_vin_rfid_mapping)
        progress = ProgressDialog(this)
        progress.setMessage("Please Wait...")

        binding.vinRfidMappingToolbar.title = "VIN/RFID Mapping"
        binding.vinRfidMappingToolbar.setTitleTextColor(ContextCompat.getColor(this, R.color.white))

        setSupportActionBar(binding.vinRfidMappingToolbar)

        supportActionBar?.setDisplayHomeAsUpEnabled(true)
        supportActionBar?.setDisplayShowHomeEnabled(true)
        Toasty.Config.getInstance().setGravity(Gravity.CENTER).apply()

        session = SessionManager(this)

        val kymsRepository = KYMSRepository()
        val viewModelProviderFactory = VinRfidMappingViewModelFactory(application, kymsRepository)
        viewModel =
            ViewModelProvider(this, viewModelProviderFactory)[VinRfidMappingViewModel::class.java]
        binding.btnSubmit.setOnClickListener {
            //validateVinRfid()
            validateVinAddVehicleMapping()
        }
        binding.btnValidate.setOnClickListener {
            validateVinRfidMapping()
        }
        /*binding.btnSetGeofence.setOnClickListener {
            startActivity(Intent(this@VinRfidMappingActivity,SetGeofenceActivity::class.java))
        }*/

        binding.btnClearTx.setOnClickListener {
            clearTx()
        }
        binding.radioGroup.check(binding.radioBtn2.getId())

        if (Build.MANUFACTURER.contains("Zebra Technologies") || Build.MANUFACTURER.contains("Motorola Solutions")) {
            val results = EMDKManager.getEMDKManager(this@VinRfidMappingActivity, this)
            if (results.statusCode != EMDKResults.STATUS_CODE.SUCCESS) {
                Log.e(TAG, "EMDKManager object request failed!")
            } else {
                Log.e(TAG, "EMDKManager object initialization is   in   progress.......")
            }
        }
        viewModel.vinRfidMutableLiveData.observe(this) { response ->
            when (response) {
                is Resource.Success -> {
                    hideProgressBar()
                    response.data?.let { resultResponse ->
                        try {
                            if (resultResponse.responseMessage != null && resultResponse.responseMessage != "") {
                                Toasty.success(
                                    this@VinRfidMappingActivity,
                                    resultResponse.responseMessage.toString(),
                                    Toasty.LENGTH_SHORT
                                ).show()

                            } else if (resultResponse.errorMessage != null) {
                                Toasty.warning(
                                    this@VinRfidMappingActivity,
                                    resultResponse.errorMessage.toString(),
                                    Toasty.LENGTH_SHORT
                                ).show()

                            }
                        } catch (e: Exception) {
                            Toasty.warning(
                                this@VinRfidMappingActivity,
                                e.printStackTrace().toString(),
                                Toasty.LENGTH_SHORT
                            ).show()
                        }
                    }
                }
                is Resource.Error -> {
                    hideProgressBar()
                    /*  Toasty.error(
                          this@VinRfidMappingActivity,
                          "Error Message: "
                      ).show()*/
                    response.message?.let { resultResponse ->
                        Toasty.error(
                            this@VinRfidMappingActivity, "Error Message: $resultResponse"
                        ).show()
                        if (resultResponse == "Unauthorized" || resultResponse == "Authentication token expired" || resultResponse == Constants.CONFIG_ERROR) {
                            session.showCustomDialog(
                                "Session Expired",
                                "Please re-login to continue",
                                this@VinRfidMappingActivity
                            )
                        }

                    }
                }
                is Resource.Loading -> {
                    showProgressBar()
                }
            }
        }

        viewModel.vinAddVehicleMutableLiveData.observe(this) { response ->
            when (response) {
                is Resource.Success -> {
                    hideProgressBar()
                    response.data?.let { resultResponse ->
                        try {
                            if (resultResponse.responseMessage != null && resultResponse.responseMessage != "") {
                                Toasty.success(
                                    this@VinRfidMappingActivity,
                                    resultResponse.responseMessage.toString(),
                                    Toasty.LENGTH_SHORT
                                ).show()

                            } else if (resultResponse.errorMessage != null) {
                                Toasty.warning(
                                    this@VinRfidMappingActivity,
                                    resultResponse.errorMessage.toString(),
                                    Toasty.LENGTH_SHORT
                                ).show()

                            }
                        } catch (e: Exception) {
                            Toasty.warning(
                                this@VinRfidMappingActivity,
                                e.printStackTrace().toString(),
                                Toasty.LENGTH_SHORT
                            ).show()
                        }
                    }
                }
                is Resource.Error -> {
                    hideProgressBar()
                    /*  Toasty.error(
                          this@VinRfidMappingActivity,
                          "Error Message: "
                      ).show()*/
                    response.message?.let { resultResponse ->
                        Toasty.error(
                            this@VinRfidMappingActivity, "Error Message: $resultResponse"
                        ).show()
                        if (resultResponse == "Unauthorized" || resultResponse == "Authentication token expired" || resultResponse == Constants.CONFIG_ERROR) {
                            session.showCustomDialog(
                                "Session Expired",
                                "Please re-login to continue",
                                this@VinRfidMappingActivity
                            )
                        }

                    }
                }
                is Resource.Loading -> {
                    showProgressBar()
                }
            }
        }
        viewModel.validateVinRfidMutableLiveData.observe(this) { response ->
            when (response) {
                is Resource.Success -> {
                    hideProgressBar()
                    response.data?.let { resultResponse ->
                        try {
                            if (resultResponse.responseMessage.equals("Success")) {
                                Toasty.success(
                                    this@VinRfidMappingActivity,
                                    resultResponse.responseMessage.toString(),
                                    Toasty.LENGTH_LONG
                                ).show()

                            } else {
                                Toasty.warning(
                                    this@VinRfidMappingActivity,
                                    resultResponse.responseMessage.toString(),
                                    Toasty.LENGTH_LONG
                                ).show()

                            }
                        } catch (e: Exception) {
                            Toasty.warning(
                                this@VinRfidMappingActivity,
                                e.printStackTrace().toString(),
                                Toasty.LENGTH_SHORT
                            ).show()
                        }
                    }
                }
                is Resource.Error -> {
                    hideProgressBar()
                    response.message?.let { resultResponse ->
                        Toasty.error(
                            this@VinRfidMappingActivity, "Error Message: $resultResponse"
                        ).show()
                        if (resultResponse == "Unauthorized" || resultResponse == "Authentication token expired" || resultResponse == Constants.CONFIG_ERROR) {
                            session.showCustomDialog(
                                "Session Expired",
                                "Please re-login to continue",
                                this@VinRfidMappingActivity
                            )
                        }
                    }
                }
                is Resource.Loading -> {
                    showProgressBar()
                }
            }
        }
       // setDefaultScanner()
        binding.radioGroup.setOnCheckedChangeListener { buttonView, selected ->
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
                    val results = EMDKManager.getEMDKManager(this@VinRfidMappingActivity, this)
                    if (results.statusCode != EMDKResults.STATUS_CODE.SUCCESS) {
                        Log.e(TAG, "EMDKManager object request failed!")
                    } else {
                        Log.e(
                            TAG, "EMDKManager object initialization is   in   progress......."
                        )
                    }
                }

            }
        }

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
        val results2 = EMDKManager.getEMDKManager(this@VinRfidMappingActivity, this)
        if (results2.statusCode != EMDKResults.STATUS_CODE.SUCCESS) {
            Log.e(TAG, "EMDKManager object request failed!")
        } else {
            Log.e(
                TAG,
                "EMDKManager object initialization is   in   progress......."
            )
        }
    }

    fun clearTx() {
        binding.edVinScan.setText("")
        binding.edRfidScan.setText("")
    }

    fun validateVinRfid() {
        try {
            val vinScan = binding.edVinScan.text.toString()
            val rfidScan = binding.edRfidScan.text.toString()
            if (vinScan.isNotEmpty() && rfidScan.isNotEmpty()) {
                val vinRfidMappingRequest = VinRfidRequest(vinScan, rfidScan)
                viewModel.vinRfidMapping(
                    session.getToken(), Constants.BASE_URL, vinRfidMappingRequest
                )
            } else {
                Toasty.warning(
                    this@VinRfidMappingActivity, "Please Fill the details", Toasty.LENGTH_SHORT
                ).show()
            }
        } catch (e: Exception) {
            Toasty.warning(
                this@VinRfidMappingActivity, e.printStackTrace().toString(), Toasty.LENGTH_SHORT
            ).show()
        }
    }

    fun validateVinRfidMapping() {
        try {
            val vinScan = binding.edVinScan.text.toString()
            val rfidScan = binding.edRfidScan.text.toString()
            if (vinScan.isNotEmpty() && rfidScan.isNotEmpty()) {
                val vinRfidMappingRequest = VinRfidRequest(vinScan, rfidScan)
                viewModel.validateVinRfidMapping(
                    session.getToken(), Constants.BASE_URL, vinRfidMappingRequest
                )
            } else {
                Toasty.warning(
                    this@VinRfidMappingActivity, "Please Fill the details", Toasty.LENGTH_SHORT
                ).show()
            }
        } catch (e: Exception) {
            Toasty.warning(
                this@VinRfidMappingActivity, e.printStackTrace().toString(), Toasty.LENGTH_SHORT
            ).show()
        }
    }

    fun validateVinAddVehicleMapping() {
        try {
            val vinScan = binding.edVinScan.text.toString()
            if (vinScan.isNotEmpty()  ) {
                val vinAddVehicleRequest = AddVehicle("White","EN1023","XUV700",vinScan,"Yard In")
                viewModel.vinAddVehicle(
                    session.getToken(), Constants.BASE_URL, vinAddVehicleRequest
                )
            } else {
                Toasty.warning(
                    this@VinRfidMappingActivity, "Please Fill the details", Toasty.LENGTH_SHORT
                ).show()
            }
        } catch (e: Exception) {
            Toasty.warning(
                this@VinRfidMappingActivity, e.printStackTrace().toString(), Toasty.LENGTH_SHORT
            ).show()
        }
    }

    private fun showProgressBar() {
        progress.show()
    }

    private fun hideProgressBar() {
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


    ///implementation of RFID / BARCODE
    override fun handleTagdata(tagData: Array<TagData>) {
        val sb = StringBuilder()
        sb.append(tagData[0].tagID)
        runOnUiThread {
            var tagDataFromScan = tagData[0].tagID
            binding.edRfidScan.setText(tagDataFromScan)
            Log.e(TAG, "RFID Data : $tagDataFromScan")

            stopInventory()
        }
        //checkVehicleInsideGeofenceRFID(tagData[0].tagID.toString())
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

        if (resumeFlag) {
            resumeFlag = false
            if (Build.MANUFACTURER.contains("Zebra Technologies") || Build.MANUFACTURER.contains("Motorola Solutions")) {
                initBarcodeManager()
                initScanner()
            }
        }

    }

    fun initBarcodeManager() {
        barcodeManager =
            emdkManager!!.getInstance(EMDKManager.FEATURE_TYPE.BARCODE) as BarcodeManager
        if (barcodeManager == null) {
            Toast.makeText(
                this@VinRfidMappingActivity, "Barcode scanning is not supported.", Toast.LENGTH_LONG
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
            runOnUiThread { binding.edVinScan.setText(dataStr) }
            // checkVehicleInsideGeofenceBarcode(dataStr.toString())

            Log.e(TAG, "Barcode Data : $dataStr")
        }
    }

    override fun onStatus(statusData: StatusData) {
        val state = statusData.state
        var statusStr = ""
        when (state) {
            StatusData.ScannerStates.IDLE -> {
                statusStr = statusData.friendlyName + " is   enabled and idle..."
                setConfig()
                try {
                    scanner!!.read()
                } catch (e: ScannerException) {
                }
            }
            StatusData.ScannerStates.WAITING -> statusStr =
                "Scanner is waiting for trigger press..."
            StatusData.ScannerStates.SCANNING -> statusStr = "Scanning..."
            StatusData.ScannerStates.DISABLED -> {}
            StatusData.ScannerStates.ERROR -> statusStr = "An error has occurred."
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

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)

        if (requestCode == ParkInActivity.REQUEST_CODE) {
            if (resultCode == RESULT_OK) {
                val resultValue = data?.getStringExtra(ScanBarcodeActivity.EXTRA_RESULT)
                Log.e(TAG, resultValue ?: "Empty")
            } else {
                Log.e(TAG, "Result cancelled")
            }
        }
    }
}