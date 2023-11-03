package com.example.fiatkyms.activity


import android.app.ProgressDialog
import android.content.Intent
import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.view.Gravity
import android.view.MenuItem
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.app.AppCompatDelegate
import androidx.core.content.ContextCompat
import androidx.databinding.DataBindingUtil
import androidx.lifecycle.ViewModelProvider
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.fiatkyms.R
import com.example.fiatkyms.adapter.SearchVehicleAdapter
import com.example.fiatkyms.databinding.ActivitySearchVehicleBinding
import com.example.fiatkyms.helper.Constants
import com.example.fiatkyms.helper.Resource
import com.example.fiatkyms.helper.SessionManager
import com.example.fiatkyms.model.searchvehicle.VehicleListResponseItem
import com.example.fiatkyms.repository.KYMSRepository
import com.example.fiatkyms.viewmodel.searchvehicle.SearchVehicleViewModel
import com.example.fiatkyms.viewmodel.searchvehicle.SearchVehicleViewModelFactory
import es.dmoral.toasty.Toasty
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.cancel
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

class SearchVehicleActivity : AppCompatActivity() {
    private var listItems: MutableList<VehicleListResponseItem>? = null
    lateinit var binding: ActivitySearchVehicleBinding
    private lateinit var viewModel: SearchVehicleViewModel
    private lateinit var progress: ProgressDialog
    private lateinit var session: SessionManager
    private var vehicleListRecycler: RecyclerView? = null
    private var vehicleRecyclerAdapter: SearchVehicleAdapter? = null

    private val coroutineScope = CoroutineScope(Dispatchers.IO)
    private var job: Job? = null
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_NO)
        binding = DataBindingUtil.setContentView(this, R.layout.activity_search_vehicle)
        binding.searchVehicleToolbar.title = "Find/Search Vehicle"
        binding.searchVehicleToolbar.setTitleTextColor(ContextCompat.getColor(this, R.color.white))
        setSupportActionBar(binding.searchVehicleToolbar)
        supportActionBar?.setDisplayHomeAsUpEnabled(true)
        supportActionBar?.setDisplayShowHomeEnabled(true)


        Toasty.Config.getInstance()
            .setGravity(Gravity.CENTER)
            .apply()
        progress = ProgressDialog(this)
        progress.setMessage("Please Wait...")

        val kymsRepository = KYMSRepository()
        val viewModelProviderFactory = SearchVehicleViewModelFactory(application, kymsRepository)
        viewModel =
            ViewModelProvider(this, viewModelProviderFactory)[SearchVehicleViewModel::class.java]
        session = SessionManager(this)
        viewModel.searchVehicleListMutableLiveData.observe(this) { response ->
            when (response) {
                is Resource.Success -> {
                    //hideProgressBar()
                    listItems?.clear()
                    binding.searchSwipeRefresh.isRefreshing = false
                    response.data?.let { resultResponse ->
                        try {
                            listItems = resultResponse
                            setVehicleList(resultResponse)
                            binding.layoutParked.setOnClickListener {
                                selectParkedReparked("Park In")

                            }
                            binding.layoutReparked.setOnClickListener {
                                selectParkedReparked("Reparking")
                            }
                        } catch (e: Exception) {
                            Toasty.warning(
                                this@SearchVehicleActivity,
                                e.printStackTrace().toString(),
                                Toasty.LENGTH_SHORT
                            ).show()
                        }
                    }
                }

                is Resource.Error -> {
                    //hideProgressBar()
                    binding.searchSwipeRefresh.isRefreshing = false
                    response.message?.let { resultResponse ->
                        Toasty.error(
                            this@SearchVehicleActivity,
                            "Error Message: $resultResponse"
                        ).show()
                        if (resultResponse == "Unauthorized" || resultResponse == "Authentication token expired" ||
                            resultResponse == Constants.CONFIG_ERROR
                        ) {
                            session.showCustomDialog(
                                "Session Expired",
                                "Please re-login to continue",
                                this@SearchVehicleActivity
                            )
                        }
                    }
                }

                is Resource.Loading -> {
                   // showProgressBar()
                }
            }
        }
        searchVehicleDetails()
     /*   viewModel.generateTokenMutableLiveData.observe(this) { response ->
            when (response) {
                is Resource.Success -> {
                    response.data?.let { resultResponse ->
                        try {
                            viewModel.searchVehicleList(
                                session.getToken(),
                                vehicleListRequest = SearchVehiclePostRequest(
                                    UserName = "Bhola",
                                    Password = "Bhola@123"
                                )
                            )
                        } catch (e: Exception) {
                            Toasty.warning(
                                this@SearchVehicleActivity,
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
                            this@SearchVehicleActivity,
                            "Error Message: $resultResponse"
                        ).show()
                        if (resultResponse == "Unauthorized" || resultResponse == "Authentication token expired" ||
                            resultResponse == Constants.CONFIG_ERROR
                        ) {
                            session.showCustomDialog(
                                "Session Expired",
                                "Please re-login to continue",
                                this@SearchVehicleActivity
                            )
                        }
                    }
                }

                is Resource.Loading -> {
                    showProgressBar()
                }
            }
        }*/
        binding.mcvBtnClear.setOnClickListener {
            binding.etSearch.setText("")
        }
        binding.etSearch.addTextChangedListener(object : TextWatcher {
            override fun onTextChanged(s: CharSequence, start: Int, before: Int, count: Int) {
                filterItems(s.toString())
            }

            override fun beforeTextChanged(s: CharSequence, start: Int, count: Int, after: Int) {}
            override fun afterTextChanged(s: Editable) {}
        })

        binding.searchSwipeRefresh.setOnRefreshListener { searchVehicleDetails() }
    }
    private fun searchVehicleDetails(){
        try {
            viewModel.searchVehicleList(
                session.getToken(), Constants.BASE_URL
                /*   token = "bearer ${resultResponse.jwtToken}",
                   vehicleListRequest = SearchVehiclePostRequest(
                       UserName = "Bhola",
                       Password = "Bhola@123"
                   )*/
            )
        } catch (e: Exception) {
            Toasty.warning(
                this@SearchVehicleActivity,
                e.printStackTrace().toString(),
                Toasty.LENGTH_SHORT
            ).show()
        }
    }
    private fun filterItems(search: String) {
        listItems?.let {
            val searchItesm = it.filter { it.vehicleResponse?.vin?.contains(search) == true }
            setVehicleList(searchItesm)
        }
    }

    private fun selectParkedReparked(status: String) {
        listItems?.let {
            val searchItesm = it.filter{it.status?.equals(status)==true}
            setVehicleList(searchItesm)
        }
    }


    private fun setVehicleList(vehicleListModel: List<VehicleListResponseItem>) {
        vehicleListRecycler = findViewById(R.id.vehicle_list_rc)
        vehicleRecyclerAdapter = SearchVehicleAdapter({
            startActivity(
                Intent(
                    this@SearchVehicleActivity,
                    VehicleDetailsScreen::class.java
                ).apply {
                    putExtra("model", it)
                })
        }) {
            startActivity(
                Intent(
                    this@SearchVehicleActivity,
                    UnloadingConfirmationActivity::class.java
                ).apply {
                    /*       it.coordinates?.let { it1 -> Utils.parseString(it1) }?.get(0)
                               ?.let { it1 -> putExtra(LATITUDE, it1.latitude) }
                           it.coordinates?.let { it1 -> Utils.parseString(it1) }?.get(0)
                               ?.let { it1 -> putExtra(LONGITUDE, it1.longitude) }*/
                    val (latitude: Double?, longitude) = it.coordinates?.split(",")!!
                        .map { it.toDoubleOrNull() }
                    if (latitude != null && longitude != null) {
                        val intent = Intent(
                            this@SearchVehicleActivity,
                            UnloadingConfirmationActivity::class.java
                        )
                        intent.putExtra(Constants.LATITUDE, latitude)
                        intent.putExtra(Constants.LONGITUDE, longitude)
                        startActivity(intent)
                    } else {
                        // Handle invalid coordinates here if needed.
                    }
                })
        }


        vehicleRecyclerAdapter?.setVehicleList(vehicleListModel, this@SearchVehicleActivity)
        vehicleListRecycler!!.adapter = vehicleRecyclerAdapter
        binding.vehicleListRc.layoutManager = LinearLayoutManager(this)
    }

    override fun onBackPressed() {
        super.onBackPressed()
         var intent=Intent(this@SearchVehicleActivity,MainActivity::class.java)
         intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
        startActivity(intent)
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

    private fun showProgressBar() {
        progress.show()
    }

    private fun hideProgressBar() {
        progress.cancel()
    }

    private fun performPeriodicTasks() {
        job = coroutineScope.launch {
            while (true) {
                try {
                    searchVehicleDetails()
                } catch (e: Exception) {
                    // Handle errors as needed
                }

                // Delay for 5 seconds before running the tasks again
                delay(35000)
            }
        }
    }
    override fun onStart() {
        super.onStart()
        performPeriodicTasks()

    }

    override fun onDestroy() {
        super.onDestroy()
        coroutineScope.cancel()
    }
    override fun onStop() {
        super.onStop()
        stopPeriodicTasks()
    }
    private fun stopPeriodicTasks() {
        job?.cancel() // Cancel the periodic task job when the activity is not visible
    }
}