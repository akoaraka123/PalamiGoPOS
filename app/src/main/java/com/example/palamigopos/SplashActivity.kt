package com.example.palamigopos

import android.annotation.SuppressLint
import android.content.Intent
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

@SuppressLint("CustomSplashScreen")
class SplashActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_splash)

        // Delay then go to MainActivity
        lifecycleScope.launch {
            delay(2500) // Show splash for 2.5 seconds
            startActivity(Intent(this@SplashActivity, MainActivity::class.java))
            finish() // Close splash so back button won't return here
        }
    }
}
