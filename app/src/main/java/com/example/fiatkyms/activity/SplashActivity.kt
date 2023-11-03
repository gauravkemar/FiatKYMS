package com.example.fiatkyms.activity


import android.content.Intent
import android.os.Bundle
import android.os.Handler
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.app.AppCompatDelegate
import com.example.fiatkyms.R
import com.example.fiatkyms.helper.Constants
import com.example.fiatkyms.helper.Utils


class SplashActivity : AppCompatActivity() {

    private val SPLASH_SCREEN_TIME_OUT = 1000L

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_splash)
        AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_NO)
        Handler().postDelayed({
            if (Utils.getSharedPrefsBoolean(this@SplashActivity, Constants.LOGGEDIN, false)) {
                Utils.setSharedPrefsBoolean(this@SplashActivity, Constants.LOGGEDIN, true)
                startActivity(Intent(this@SplashActivity, MainActivity::class.java))
                finish()
            } else {
                startActivity(Intent(this@SplashActivity, MainActivity::class.java))
                finish()
            }
        }, SPLASH_SCREEN_TIME_OUT)
    }
}