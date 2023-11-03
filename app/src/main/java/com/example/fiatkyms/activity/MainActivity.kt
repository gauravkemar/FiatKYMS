package com.example.fiatkyms.activity


import android.app.ProgressDialog
import android.content.Intent
import android.graphics.Color
import android.graphics.PorterDuff
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.MenuItem
import android.view.View
import android.webkit.WebViewClient
import android.widget.ProgressBar
import androidx.appcompat.app.ActionBarDrawerToggle
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import androidx.databinding.DataBindingUtil
import androidx.lifecycle.ViewModelProvider
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.fiatkyms.R
import com.example.fiatkyms.adapter.DashboardTableAdapter
import com.example.fiatkyms.adapter.MyDialogAdapter
import com.example.fiatkyms.databinding.ActivityMainBinding
import com.example.fiatkyms.helper.Constants
import com.example.fiatkyms.helper.Resource
import com.example.fiatkyms.helper.SessionManager
import com.example.fiatkyms.helper.Utils
import com.example.fiatkyms.model.dashboard.DashboardDataResponseItem
import com.example.fiatkyms.model.dashboard.DashboardGraphResponseItem
import com.example.fiatkyms.model.dashboard.DashboardPostRequest
import com.example.fiatkyms.model.dashboard.GetVehicleDashboardDataAdminResponse
import com.example.fiatkyms.repository.KYMSRepository
import com.example.fiatkyms.viewmodel.dashboard.DashboardViewModel
import com.example.fiatkyms.viewmodel.dashboard.DashboardViewModelFactory
import com.github.mikephil.charting.charts.BarChart
import com.github.mikephil.charting.components.AxisBase
import com.github.mikephil.charting.components.XAxis
import com.github.mikephil.charting.data.BarData
import com.github.mikephil.charting.data.BarDataSet
import com.github.mikephil.charting.data.BarEntry
import com.github.mikephil.charting.formatter.IndexAxisValueFormatter
import com.github.mikephil.charting.formatter.ValueFormatter
import es.dmoral.toasty.Toasty
import java.text.SimpleDateFormat
import java.util.*


class MainActivity : AppCompatActivity() {

    lateinit var toggle: ActionBarDrawerToggle
    private lateinit var session: SessionManager

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        return if (toggle.onOptionsItemSelected(item)) {
            true
        } else super.onOptionsItemSelected(item)
    }

    private var userID: String = ""
    private var vehicleRecyclerAdapter: DashboardTableAdapter? = null
    lateinit var binding: ActivityMainBinding
    private lateinit var viewModel: DashboardViewModel
    private lateinit var progress: ProgressDialog


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = DataBindingUtil.setContentView(this, R.layout.activity_main)
        binding.toolbar.title = "Dashboard"
        binding.toolbar.setTitleTextColor(ContextCompat.getColor(this, R.color.white))
        setSupportActionBar(binding.toolbar)
        supportActionBar?.setDisplayHomeAsUpEnabled(true)
        supportActionBar?.setDisplayShowHomeEnabled(true)
        session = SessionManager(this)
        userID = Utils.getSharedPrefsInteger(this, Constants.KEY_USER_ID, 0).toString()
        val kymsRepository = KYMSRepository()
        val viewModelProviderFactory = DashboardViewModelFactory(application, kymsRepository)
        viewModel =
            ViewModelProvider(this, viewModelProviderFactory)[DashboardViewModel::class.java]


        //binding.toolbar.setNavigationIcon(R.drawable.ic_hamburger_white)
        setSupportActionBar(binding.toolbar)
        //getSupportActionBar()?.setDisplayHomeAsUpEnabled(true)

        toggle = ActionBarDrawerToggle(
            this,
            binding.drawerLayout,
            R.string.navigation_drawer_open,
            R.string.navigation_drawer_close
        )
        binding.drawerLayout.addDrawerListener(toggle)
        toggle.isDrawerIndicatorEnabled = true
        toggle.syncState()
        binding.navigationView.getMenu().clear()

        if (session.getRole().equals("") || Utils.getSharedPrefsBoolean(
                this@MainActivity,
                Constants.LOGGEDIN,
                false
            ) == false
        ) {
            Utils.setSharedPrefsBoolean(this@MainActivity, Constants.LOGGEDIN, false)
            startActivity(Intent(this@MainActivity, LoginActivity::class.java))
            finish()
        }
        setMenuAndDashboard()
        callDefaultGraphApi()


        /* if(session.getRole().equals(""))
         {
             Utils.setSharedPrefsBoolean(this@MainActivity, Constants.LOGGEDIN, false)
             startActivity(Intent(this@MainActivity, LoginActivity::class.java))
             finish()
         }
         //binding.navigationView.inflateMenu(R.menu.driver_menu)
        if(session.getRole().equals("Driver"))
         {
             binding.navigationView.inflateMenu(R.menu.driver_menu)
         }
         if(session.getRole().equals("SuperAdmin"))
         {
             binding.navigationView.inflateMenu(R.menu.supervisor_menu)
             //binding.navigationView.inflateMenu(R.menu.driver_menu)
         }*/
        val drawable = ContextCompat.getDrawable(this, R.drawable.ic_hamburger_white)
        drawable?.setColorFilter(Color.WHITE, PorterDuff.Mode.SRC_ATOP)
        binding.toolbar.navigationIcon = drawable


        binding.navigationView.setNavigationItemSelectedListener { menuItem ->
            // Handle item selection here
            when (menuItem.itemId) {
                R.id.vin_rfid_map -> {
                    startActivity(Intent(this@MainActivity, VinRfidMappingActivity::class.java))
                }

                R.id.add_geofence -> {
                    startActivity(Intent(this@MainActivity, SetGeofenceActivity::class.java))
                }
                R.id.park_in -> {
                    startActivity(Intent(this@MainActivity, ParkInActivity::class.java))
                }
                R.id.search_vehicle -> {
                    startActivity(Intent(this@MainActivity, SearchVehicleActivity::class.java))
                }
               /* R.id.set_dealer_coordinates -> {
                    startActivity(
                        Intent(
                            this@MainActivity,
                            DealerCoordinatesSaveDetailsActivity::class.java
                        )
                    )
                }*/
                R.id.logout -> {
                    showLogoutDialog()
                }
                // Add more cases for other menu items if needed
            }

            // Close the drawer after item selection
            binding.drawerLayout.closeDrawers()
            true
        }

        binding.tvSelectPeriod2.setOnClickListener {
            showRecyclerDialog(barChart = false)
        }
        binding.tvSelectPeriodBarchart1.setOnClickListener {
            showRecyclerDialog(barChart = true)
        }


        progress = ProgressDialog(this)
        progress.setMessage("Please Wait...")

        viewModel.dashboardDataMutableLiveData.observe(this) { response ->
            when (response) {
                is Resource.Success -> {
                    binding.homeSwipeRefresh.isRefreshing = false
                    hideTableProgressBar()
                    response.data?.let { resultResponse ->
                        try {
                            setDashboardTableList(resultResponse,null)
                        } catch (e: Exception) {
                            Toasty.warning(
                                this@MainActivity,
                                e.printStackTrace().toString(),
                                Toasty.LENGTH_SHORT
                            ).show()
                        }
                    }
                }
                is Resource.Error -> {
                    binding.homeSwipeRefresh.isRefreshing = false
                    hideTableProgressBar()
                    response.message?.let { resultResponse ->
                        Toasty.error(
                            this@MainActivity,
                            "Error Message: $resultResponse"
                        ).show()
                        if (resultResponse == "Unauthorized" || resultResponse == "Authentication token expired" ||
                            resultResponse == Constants.CONFIG_ERROR
                        ) {
                            session.showCustomDialog(
                                "Session Expired",
                                "Please re-login to continue",
                                this@MainActivity
                            )
                        }
                    }
                }
                is Resource.Loading -> {
                    // showTableProgressBar()
                }
            }
        }
        viewModel.dashboardDataAdminMutableLiveData.observe(this) { response ->
            when (response) {
                is Resource.Success -> {
                    binding.homeSwipeRefresh.isRefreshing = false
                    hideTableProgressBar()
                    response.data?.let { resultResponse ->
                        try {
                            setDashboardTableList(null,resultResponse)
                            binding.tvTotalVehicleScanValue.setText(resultResponse.size.toString())
                        } catch (e: Exception) {
                            Toasty.warning(
                                this@MainActivity,
                                e.printStackTrace().toString(),
                                Toasty.LENGTH_SHORT
                            ).show()
                        }
                    }
                }
                is Resource.Error -> {
                    binding.homeSwipeRefresh.isRefreshing = false
                    hideTableProgressBar()
                    response.message?.let { resultResponse ->
                        Toasty.error(
                            this@MainActivity,
                            "Error Message: $resultResponse"
                        ).show()
                        if (resultResponse == "Unauthorized" || resultResponse == "Authentication token expired" ||
                            resultResponse == Constants.CONFIG_ERROR
                        ) {
                            session.showCustomDialog(
                                "Session Expired",
                                "Please re-login to continue",
                                this@MainActivity
                            )
                        }
                    }
                }
                is Resource.Loading -> {
                    // showTableProgressBar()
                }
            }
        }
        viewModel.dashboardGraphMutableLiveData.observe(this) { response ->
            when (response) {
                is Resource.Success -> {
                    binding.homeSwipeRefresh.isRefreshing = false
                    hideGraphProgressBar()
                    response.data?.let { resultResponse ->
                        try {
                            Log.e("graphData", resultResponse.toString())
                            setDashboardGraphList(resultResponse)
                        } catch (e: Exception) {
                            Toasty.warning(
                                this@MainActivity,
                                e.printStackTrace().toString(),
                                Toasty.LENGTH_SHORT
                            ).show()
                        }
                    }
                }
                is Resource.Error -> {
                    binding.homeSwipeRefresh.isRefreshing = false
                    hideGraphProgressBar()
                    response.message?.let { resultResponse ->
                        Toasty.error(
                            this@MainActivity,
                            "Error Message: $resultResponse"
                        ).show()
                        if (resultResponse == "Unauthorized" || resultResponse == "Authentication token expired" ||
                            resultResponse == Constants.CONFIG_ERROR
                        ) {
                            session.showCustomDialog(
                                "Session Expired",
                                "Please re-login to continue",
                                this@MainActivity
                            )
                        }
                    }
                }
                is Resource.Loading -> {
                    //showGraphProgressBar()
                }
            }
        }

        binding.homeSwipeRefresh.setOnRefreshListener {
            callListApi()
        }

    }

    private fun callListApi() {
        callDefaultGraphApi()
    }

    private fun setMenuAndDashboard() {
        if (session.getRole().equals("supervisor")) {
            binding.navigationView.inflateMenu(R.menu.supervisor_menu)
            //binding.graphLlc.visibility = View.INVISIBLE
            binding.tvSelectPeriodBarchart1.visibility=View.INVISIBLE
            binding.tvSelectPeriod2.visibility=View.INVISIBLE
            binding.homeSwipeRefresh.visibility = View.VISIBLE
            binding.adminDashboard.visibility = View.VISIBLE
            binding.idBarChart.visibility=View.GONE
     /*
            binding.homeSwipeRefresh.visibility = View.INVISIBLE*/
            // binding.wevScroll.visibility=View.VISIBLE
        /*    binding.wvDashboard.visibility = View.VISIBLE
            binding.wvDashboard.settings.javaScriptEnabled = true
            binding.wvDashboard.webViewClient = WebViewClient()
            val url = "http://103.240.90.141:5050/"
            binding.wvDashboard.loadUrl(url)*/
        } else if (session.getRole().equals("superadmin")) {
            binding.navigationView.inflateMenu(R.menu.super_admin_menu)
           /* binding.graphLlc.visibility = View.INVISIBLE
            //binding.wevScroll.visibility=View.VISIBLE
            binding.wvDashboard.visibility = View.INVISIBLE
            binding.homeSwipeRefresh.visibility = View.INVISIBLE
            binding.wvDashboard.settings.javaScriptEnabled = true
            binding.wvDashboard.webViewClient = WebViewClient()
            val url = "http://103.240.90.141:5050/"
            binding.wvDashboard.loadUrl(url)*/
            //binding.graphLlc.visibility = View.INVISIBLE
            binding.idBarChart.visibility=View.GONE
            binding.adminDashboard.visibility = View.VISIBLE
            binding.tvSelectPeriodBarchart1.visibility=View.INVISIBLE
            binding.tvSelectPeriod2.visibility=View.INVISIBLE
            binding.homeSwipeRefresh.visibility = View.VISIBLE
        } else if (session.getRole().equals("Driver")) {
            binding.navigationView.inflateMenu(R.menu.driver_menu)
            binding.idBarChart.visibility=View.VISIBLE
            //binding.graphLlc.visibility = View.VISIBLE
            //binding.wevScroll.visibility=View.INVISIBLE
            binding.wvDashboard.visibility = View.INVISIBLE
            binding.homeSwipeRefresh.visibility = View.VISIBLE
            binding.adminDashboard.visibility = View.INVISIBLE
        }
    }

    private fun callDefaultGraphApi() {
      /*  if (binding.graphLlc.visibility == View.VISIBLE && binding.wvDashboard.visibility == View.INVISIBLE) {
            viewModel.getDashboardData(
                request = DashboardPostRequest(
                    UserId = userID, period = 20
                )

            )

            viewModel.getDashboardGraphData(
                request = DashboardPostRequest(
                    UserId = userID, period = 20
                )
            )
        }*/

        when(session.getRole()){
            "supervisor" ->  viewModel.getDashboardDataAdmin(request = DashboardPostRequest(
                UserId = null, period = 1
            ))
            "superadmin" ->   viewModel.getDashboardDataAdmin(request = DashboardPostRequest(
                UserId = null, period = 1
            ))
            "Driver" ->  {
                viewModel.getDashboardData(
                    request = DashboardPostRequest(
                        UserId = userID, period = 20
                    )

                )
                viewModel.getDashboardGraphData(
                    request = DashboardPostRequest(
                        UserId = userID, period = 20
                    )
                )
            }
        }
     /*   if (session.getRole().equals("supervisor")) {
            viewModel.getDashboardDataAdmin()
        }
        else if (session.getRole().equals("superadmin")) {
            viewModel.getDashboardDataAdmin()
        }
        else if (session.getRole().equals("Driver")) {

        }
         if (binding.adminDashboard.visibility==View.VISIBLE)
         {
             viewModel.getDashboardDataAdmin()
         }
        else if (binding.graphLlc.visibility==View.VISIBLE)
         {

        }*/
    }


    private fun hideGraphProgressBar() {
        findViewById<View>(R.id.select_period_layout).visibility = View.VISIBLE
        findViewById<ProgressBar>(R.id.barProgressBar).visibility = View.GONE
    }

    private fun showGraphProgressBar() {
        findViewById<View>(R.id.select_period_layout).visibility = View.GONE
        findViewById<ProgressBar>(R.id.barProgressBar).visibility = View.VISIBLE
    }

    private fun hideTableProgressBar() {
        findViewById<View>(R.id.table_first_item).visibility = View.VISIBLE
        findViewById<RecyclerView>(R.id.dashboard_table).visibility = View.VISIBLE
        findViewById<ProgressBar>(R.id.tableProgressBar).visibility = View.GONE
    }

    private fun showTableProgressBar() {
        findViewById<RecyclerView>(R.id.dashboard_table).visibility = View.GONE
        findViewById<View>(R.id.table_first_item).visibility = View.GONE
        findViewById<ProgressBar>(R.id.tableProgressBar).visibility = View.VISIBLE
    }

    private fun logout() {
        session.logoutUser()
        startActivity(Intent(this@MainActivity, LoginActivity::class.java))
        finish()
    }

    private fun showLogoutDialog() {
        val builder = AlertDialog.Builder(this)
        builder.setTitle("Logout")
        builder.setMessage("Are you sure you want to logout?")
        builder.setNegativeButton("Cancel") { dialog, _ ->
            dialog.dismiss()
        }
        builder.setPositiveButton("Yes") { dialog, _ ->
            logout()
            dialog.dismiss()
        }
        val dialog: AlertDialog = builder.create()
        dialog.show()
    }

    private fun setDashboardGraphList(resultResponse: List<DashboardGraphResponseItem>) {
        val barEntriesList = ArrayList<BarEntry>() //7, 13
        val xAxisLabel = ArrayList<String>()
        resultResponse.forEachIndexed { index, dashboardGraphResponseItem ->
            dashboardGraphResponseItem.count?.let {
                BarEntry(
                    index.toFloat(),
                    it.toFloat()
                )
            }?.let {
                barEntriesList.add(
                    it
                )
            }
            dashboardGraphResponseItem.day?.let {
                convertDateStringToDate(it)?.let {
                    xAxisLabel.add(
                        it
                    )
                }
            }
        }
        addLables(barEntriesList, xAxisLabel)
    }

    fun convertDateStringToDate(inputDate: String): String? {
        val inputFormat = SimpleDateFormat("dd-MM-yyyy", Locale.getDefault())
        val outputFormat = SimpleDateFormat("dd MMM", Locale.getDefault())
        try {
            val date = inputFormat.parse(inputDate)
            return outputFormat.format(date)
        } catch (e: Exception) {
            e.printStackTrace()
        }

        return ""
    }

    private fun addLables(entries: ArrayList<BarEntry>, xAxisLabel: ArrayList<String>) {
        val barChart: BarChart = findViewById(R.id.idBarChart)

        val dataSet = BarDataSet(entries, null)
        dataSet.color = Color.BLUE

        val data = BarData(dataSet)
        barChart.data = data

        // Customize the X-axis labels
        val xAxis = barChart.xAxis
        xAxis.position = XAxis.XAxisPosition.BOTH_SIDED
        xAxis.axisMinimum = 0f
        xAxis.granularity = 1f
        xAxis.valueFormatter = IndexAxisValueFormatter(xAxisLabel)
        xAxis.position = XAxis.XAxisPosition.BOTTOM
        // Refresh the chart
        barChart.invalidate()

    }


    class LabelFormatter : ValueFormatter() {
        private val sdf = SimpleDateFormat("dd.MM", Locale.getDefault())

        @Deprecated("Deprecated in Java")
        override fun getFormattedValue(value: Float, axis: AxisBase?): String {
            // Assuming value represents a timestamp in milliseconds
            val date = Date(value.toLong())
            return sdf.format(date)
        }
    }

    private fun setDashboardTableList(resultResponse: List<DashboardDataResponseItem>?,resultResponseAdmin:List<GetVehicleDashboardDataAdminResponse>?) {
        if(resultResponse!=null)
        {
            val list = mutableListOf<DashboardDataResponseItem>()
            list.addAll(resultResponse)
            val vehicleListRecycler = findViewById<RecyclerView>(R.id.dashboard_table)
            vehicleRecyclerAdapter?.let {
                vehicleRecyclerAdapter?.updateItems(list,null)
            } ?: kotlin.run {
                vehicleRecyclerAdapter = DashboardTableAdapter(this@MainActivity, list,null)
            }
            vehicleListRecycler!!.adapter = vehicleRecyclerAdapter
            vehicleListRecycler.layoutManager = LinearLayoutManager(this)
        }
        else if(resultResponseAdmin!=null)
        {
            val list = mutableListOf<GetVehicleDashboardDataAdminResponse>()
            list.addAll(resultResponseAdmin)
            val vehicleListRecycler = findViewById<RecyclerView>(R.id.dashboard_table)
            vehicleRecyclerAdapter?.let {
                vehicleRecyclerAdapter?.updateItems(null,list)
            } ?: kotlin.run {
                vehicleRecyclerAdapter = DashboardTableAdapter(this@MainActivity, null,list)
            }
            vehicleListRecycler!!.adapter = vehicleRecyclerAdapter
            vehicleListRecycler.layoutManager = LinearLayoutManager(this)
        }

    }

    private fun showRecyclerDialog(barChart: Boolean) {
        val dialogView = LayoutInflater.from(this).inflate(R.layout.pop_up_menu_layout, null)
        val recyclerView: RecyclerView = dialogView.findViewById(R.id.recyclerView)

        // Create and set up your RecyclerView adapter and layout manager here
        val layoutManager = LinearLayoutManager(this)
        val list = mutableListOf<Int>()
        list.add(7)
        list.add(14)
        list.add(21)

        val dialogBuilder = AlertDialog.Builder(this)
            .setView(dialogView)
            .setPositiveButton("Close") { dialog, _ ->
                dialog.dismiss()
            }

        val alertDialog = dialogBuilder.create()

        val adapter = MyDialogAdapter(list) { item ->
            if (barChart) {
                binding.tvSelectPeriodBarchart1.text = "Period $item"
                viewModel.getDashboardGraphData(
                    request = DashboardPostRequest(
                        UserId = userID,
                        period = item
                    )
                )
            } else {
                binding.tvSelectPeriod2.text = "Period $item"
                viewModel.getDashboardData(
                    request = DashboardPostRequest(
                        UserId = userID,
                        period = item
                    )
                )
            }
            alertDialog.dismiss()
        }
        recyclerView.layoutManager = layoutManager
        recyclerView.adapter = adapter
        alertDialog.show()
    }

}