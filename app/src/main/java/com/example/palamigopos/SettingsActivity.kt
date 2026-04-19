package com.example.palamigopos

import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.widget.TextView
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import com.example.palamigopos.databinding.ActivitySettingsBinding
import com.example.palamigopos.databinding.DialogResetPinBinding
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import com.google.android.material.snackbar.Snackbar

class SettingsActivity : AppCompatActivity() {

    private lateinit var binding: ActivitySettingsBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivitySettingsBinding.inflate(layoutInflater)
        setContentView(binding.root)

        setupToolbar()
        setupClickListeners()
    }

    private fun setupToolbar() {
        binding.toolbar.setNavigationOnClickListener {
            finish()
        }
    }

    private fun setupClickListeners() {
        binding.btnResetPin.setOnClickListener {
            showResetPinDialog()
        }
    }

    private fun showResetPinDialog() {
        val dialogBinding = DialogResetPinBinding.inflate(LayoutInflater.from(this))

        val dialog = MaterialAlertDialogBuilder(this)
            .setView(dialogBinding.root)
            .setCancelable(true)
            .create()

        dialog.window?.setBackgroundDrawableResource(android.R.color.transparent)

        var step = 1 // 1 = Current PIN, 2 = New PIN, 3 = Confirm New PIN
        var newPin = ""

        fun updateStep() {
            when (step) {
                1 -> {
                    dialogBinding.tvDialogTitle.text = "Enter Current PIN"
                    dialogBinding.tvDialogSubtitle.text = "Enter your existing 4-digit PIN"
                }
                2 -> {
                    dialogBinding.tvDialogTitle.text = "Enter New PIN"
                    dialogBinding.tvDialogSubtitle.text = "Create a new 4-digit PIN"
                }
                3 -> {
                    dialogBinding.tvDialogTitle.text = "Confirm New PIN"
                    dialogBinding.tvDialogSubtitle.text = "Re-enter your new PIN to confirm"
                }
            }
            clearPinInput(dialogBinding)
        }

        // Setup keypad
        val numberButtons = listOf(
            dialogBinding.btn0, dialogBinding.btn1, dialogBinding.btn2,
            dialogBinding.btn3, dialogBinding.btn4, dialogBinding.btn5,
            dialogBinding.btn6, dialogBinding.btn7, dialogBinding.btn8, dialogBinding.btn9
        )

        numberButtons.forEach { button ->
            button.setOnClickListener {
                if (dialogBinding.pinInput.text.length < 4) {
                    dialogBinding.pinInput.append(button.text.toString())
                    updatePinDots(dialogBinding)
                }
            }
        }

        dialogBinding.btnBackspace.setOnClickListener {
            val currentText = dialogBinding.pinInput.text.toString()
            if (currentText.isNotEmpty()) {
                dialogBinding.pinInput.setText(currentText.dropLast(1))
                updatePinDots(dialogBinding)
                dialogBinding.tvError.visibility = TextView.INVISIBLE
            }
        }

        dialogBinding.btnCancel.setOnClickListener {
            dialog.dismiss()
        }

        dialogBinding.btnConfirm.setOnClickListener {
            val enteredPin = dialogBinding.pinInput.text.toString()

            if (enteredPin.length != 4) {
                dialogBinding.tvError.text = "PIN must be 4 digits"
                dialogBinding.tvError.visibility = TextView.VISIBLE
                return@setOnClickListener
            }

            when (step) {
                1 -> {
                    // Verify current PIN
                    val currentPin = PinActivity.getStoredPin(this)
                    if (enteredPin == currentPin) {
                        step = 2
                        updateStep()
                    } else {
                        dialogBinding.tvError.text = "Incorrect current PIN"
                        dialogBinding.tvError.visibility = TextView.VISIBLE
                    }
                }
                2 -> {
                    // Save new PIN and go to confirmation
                    newPin = enteredPin
                    step = 3
                    updateStep()
                }
                3 -> {
                    // Confirm new PIN
                    if (enteredPin == newPin) {
                        // Save the new PIN
                        PinActivity.savePin(this, newPin)
                        dialog.dismiss()
                        Snackbar.make(
                            binding.root,
                            "PIN successfully updated",
                            Snackbar.LENGTH_LONG
                        ).show()
                    } else {
                        dialogBinding.tvError.text = "PINs do not match. Try again."
                        dialogBinding.tvError.visibility = TextView.VISIBLE
                        step = 2
                        updateStep()
                    }
                }
            }
        }

        updateStep()
        dialog.show()
    }

    private fun updatePinDots(binding: DialogResetPinBinding) {
        val dots = listOf(binding.dot1, binding.dot2, binding.dot3, binding.dot4)
        val pinLength = binding.pinInput.text.length

        dots.forEachIndexed { index, dot ->
            dot.setBackgroundResource(
                if (index < pinLength) R.drawable.pin_dot_filled else R.drawable.pin_dot_empty
            )
        }
    }

    private fun clearPinInput(binding: DialogResetPinBinding) {
        binding.pinInput.text?.clear()
        updatePinDots(binding)
        binding.tvError.visibility = TextView.INVISIBLE
    }

    override fun onResume() {
        super.onResume()
        if (PinActivity.isPinRequired(this)) {
            val intent = Intent(this, PinActivity::class.java)
            intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
            startActivity(intent)
            finish()
        }
    }
}
