package com.example.fiatkyms.activity


import android.content.Intent
import android.os.Bundle
import android.view.MenuItem
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.app.AppCompatDelegate
import androidx.core.content.ContextCompat
import androidx.databinding.DataBindingUtil
import com.example.fiatkyms.R
import com.example.fiatkyms.databinding.ActivityVehicleDetailsScreenBinding
import com.example.fiatkyms.helper.Constants
import com.example.fiatkyms.model.searchvehicle.VehicleListResponseItem

class VehicleDetailsScreen : AppCompatActivity() {
    lateinit var binding: ActivityVehicleDetailsScreenBinding
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_NO)
        binding = DataBindingUtil.setContentView(this, R.layout.activity_vehicle_details_screen)
        binding.searchVehicleToolbar.title = "Find/Search Vehicle"
        binding.searchVehicleToolbar.setTitleTextColor(ContextCompat.getColor(this, R.color.white))
        setSupportActionBar(binding.searchVehicleToolbar)
        supportActionBar?.setDisplayHomeAsUpEnabled(true)
        supportActionBar?.setDisplayShowHomeEnabled(true)

        val previousScreenDataModel = intent.getParcelableExtra<VehicleListResponseItem>("model")


        binding.tvColorValue.text = previousScreenDataModel?.vehicleResponse?.color
        binding.tvEngineValue.text = previousScreenDataModel?.vehicleResponse?.engineNo
        binding.tvModelValue.text = previousScreenDataModel?.vehicleResponse?.modelCode
        binding.tvVinValue.text = previousScreenDataModel?.vehicleResponse?.vin

        binding.ivNavigate.setOnClickListener {
            startActivity(
                Intent(
                    this@VehicleDetailsScreen,
                    UnloadingConfirmationActivity::class.java
                ).apply {
                    previousScreenDataModel.run {
                        /*   this?.coordinates?.let { it1 -> Utils.parseString(it1) }?.get(0)
                               ?.let { it1 -> putExtra(Constants.LATITUDE, it1.latitude) }
                           this?.coordinates?.let { it1 -> Utils.parseString(it1) }?.get(0)
                               ?.let { it1 -> putExtra(Constants.LONGITUDE, it1.longitude) }*/

                        val (latitude: Double?, longitude) = this?.coordinates?.split(",")!!
                            .map { it.toDoubleOrNull() }
                        if (latitude != null && longitude != null) {
                            val intent = Intent(
                                this@VehicleDetailsScreen,
                                UnloadingConfirmationActivity::class.java
                            )
                            intent.putExtra(Constants.LATITUDE, latitude)
                            intent.putExtra(Constants.LONGITUDE, longitude)
                            startActivity(intent)
                        } else {
                            // Handle invalid coordinates here if needed.
                        }
                    }
                }
            )
        }
    }
    override fun onBackPressed() {
        super.onBackPressed()
        var intent=Intent(this@VehicleDetailsScreen,MainActivity::class.java)
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
}