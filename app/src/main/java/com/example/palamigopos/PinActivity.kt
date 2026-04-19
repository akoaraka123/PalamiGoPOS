package com.example.palamigopos

import android.annotation.SuppressLint
import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.view.View
import androidx.appcompat.app.AppCompatActivity
import com.example.palamigopos.databinding.ActivityPinBinding

class PinActivity : AppCompatActivity() {

    private lateinit var binding: ActivityPinBinding
    private var enteredPin = ""
    private val correctPin: String
        get() = getStoredPin()

    companion object {
        private const val PREFS_NAME = "PalamiGoPrefs"
        private const val KEY_PIN = "user_pin"
        private const val KEY_LAST_VERIFIED = "last_pin_verified"
        private const val DEFAULT_PIN = "1234"
        private const val PIN_TIMEOUT_MS = 30000 // 30 seconds for testing (change to 5 mins = 300000 for production)

        fun getStoredPin(context: Context): String {
            val prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
            return prefs.getString(KEY_PIN, DEFAULT_PIN) ?: DEFAULT_PIN
        }

        fun savePin(context: Context, newPin: String) {
            val prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
            prefs.edit().putString(KEY_PIN, newPin).apply()
        }

        fun recordPinVerified(context: Context) {
            val prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
            prefs.edit().putLong(KEY_LAST_VERIFIED, System.currentTimeMillis()).apply()
        }

        fun isPinRequired(context: Context): Boolean {
            val prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
            val lastVerified = prefs.getLong(KEY_LAST_VERIFIED, 0)
            val timeSinceLastVerified = System.currentTimeMillis() - lastVerified
            return timeSinceLastVerified > PIN_TIMEOUT_MS
        }

        fun clearPinVerification(context: Context) {
            val prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
            prefs.edit().remove(KEY_LAST_VERIFIED).apply()
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityPinBinding.inflate(layoutInflater)
        setContentView(binding.root)

        setupKeypad()
        updatePinDots()
    }

    private fun getStoredPin(): String {
        return getStoredPin(this)
    }

    private fun setupKeypad() {
        // Number buttons
        val numberButtons = listOf(
            binding.btn0, binding.btn1, binding.btn2,
            binding.btn3, binding.btn4, binding.btn5,
            binding.btn6, binding.btn7, binding.btn8, binding.btn9
        )

        numberButtons.forEach { button ->
            button.setOnClickListener {
                if (enteredPin.length < 4) {
                    enteredPin += button.text.toString()
                    updatePinDots()
                    checkPinComplete()
                }
            }
        }

        // Backspace button
        binding.btnBackspace.setOnClickListener {
            if (enteredPin.isNotEmpty()) {
                enteredPin = enteredPin.dropLast(1)
                updatePinDots()
                hideError()
            }
        }
    }

    private fun updatePinDots() {
        val dots = listOf(binding.dot1, binding.dot2, binding.dot3, binding.dot4)
        val filledDrawable = R.drawable.pin_dot_filled
        val emptyDrawable = R.drawable.pin_dot_empty

        dots.forEachIndexed { index, dot ->
            dot.setBackgroundResource(
                if (index < enteredPin.length) filledDrawable else emptyDrawable
            )
        }
    }

    private fun checkPinComplete() {
        if (enteredPin.length == 4) {
            if (enteredPin == correctPin) {
                // Correct PIN - record verification time and go to MainActivity
                recordPinVerified(this)
                val intent = Intent(this, MainActivity::class.java)
                intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
                startActivity(intent)
                finish()
            } else {
                // Incorrect PIN
                showError()
                enteredPin = ""
                updatePinDots()
            }
        }
    }

    private fun showError() {
        binding.tvPinError.visibility = View.VISIBLE
        binding.tvPinError.text = "Incorrect PIN"
    }

    private fun hideError() {
        binding.tvPinError.visibility = View.INVISIBLE
    }

    // Prevent back button from closing the app
    @SuppressLint("MissingSuperCall")
    override fun onBackPressed() {
        // Do nothing - user must enter correct PIN
    }
}
