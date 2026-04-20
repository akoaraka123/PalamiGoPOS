package com.example.palamigopos.ui.history

import android.content.Intent
import android.os.Bundle
import android.view.View
import android.widget.EditText
import android.widget.TextView
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.activity.viewModels
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.palamigopos.R
import com.example.palamigopos.data.local.AppDatabase
import com.example.palamigopos.databinding.ActivityHistoryBinding
import com.example.palamigopos.ui.adapter.OrderHistoryAdapter
import com.example.palamigopos.utils.CurrencyUtils
import com.example.palamigopos.utils.collectIn

class HistoryActivity : AppCompatActivity() {

    private lateinit var binding: ActivityHistoryBinding

    private val viewModel: HistoryViewModel by viewModels {
        HistoryViewModel.Factory(AppDatabase.getInstance(this))
    }

    private var orderToDelete: com.example.palamigopos.data.model.OrderEntity? = null

    companion object {
        private const val DELETE_ORDER_REQUEST_CODE = 1001
        const val EXTRA_START_DATE = "start_date"
        const val EXTRA_END_DATE = "end_date"
        const val EXTRA_DATE_LABEL = "date_label"
    }

    private val isFiltered: Boolean by lazy {
        intent.hasExtra(EXTRA_START_DATE) && intent.hasExtra(EXTRA_END_DATE)
    }

    private val adapter = OrderHistoryAdapter(
        onClick = { order ->
            startActivity(
                Intent(this, OrderDetailsActivity::class.java).apply {
                    putExtra(OrderDetailsActivity.EXTRA_ORDER_ID, order.id)
                    putExtra(OrderDetailsActivity.EXTRA_ORDER_NUMBER, order.orderNumber)
                    putExtra(OrderDetailsActivity.EXTRA_TOTAL, order.totalAmount)
                    putExtra(OrderDetailsActivity.EXTRA_CASH, order.cashReceived)
                    putExtra(OrderDetailsActivity.EXTRA_CHANGE, order.changeAmount)
                    putExtra(OrderDetailsActivity.EXTRA_PAYMENT_METHOD, order.paymentMethod)
                }
            )
        },
        onDelete = { order ->
            // Show PIN verification dialog before deleting
            orderToDelete = order
            showPinVerificationDialog()
        }
    )

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        binding = ActivityHistoryBinding.inflate(layoutInflater)
        setContentView(binding.root)

        ViewCompat.setOnApplyWindowInsetsListener(binding.root) { v, insets ->
            val bars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(bars.left, bars.top, bars.right, bars.bottom)
            insets
        }

        binding.toolbar.setNavigationOnClickListener { finish() }
        binding.toolbar.setNavigationIcon(androidx.appcompat.R.drawable.abc_ic_ab_back_material)

        binding.rvHistory.layoutManager = LinearLayoutManager(this)
        binding.rvHistory.adapter = adapter

        // Set toolbar title based on filter
        if (isFiltered) {
            val dateLabel = intent.getStringExtra(EXTRA_DATE_LABEL)
            binding.toolbar.title = dateLabel ?: "Transactions"
        }

        if (isFiltered) {
            val startDate = intent.getLongExtra(EXTRA_START_DATE, 0)
            val endDate = intent.getLongExtra(EXTRA_END_DATE, 0)
            viewModel.filterByDateRange(startDate, endDate)
            
            viewModel.filteredOrders.collectIn(this) { orders ->
                adapter.submitList(orders)
                binding.rvHistory.visibility = if (orders.isEmpty()) android.view.View.GONE else android.view.View.VISIBLE
                binding.emptyState.root.visibility = if (orders.isEmpty()) android.view.View.VISIBLE else android.view.View.GONE
                if (orders.isEmpty()) {
                    binding.emptyState.tvTitle.text = "No transactions"
                    binding.emptyState.tvMessage.text = "No transactions found for this date."
                }
            }
        } else {
            viewModel.orders.collectIn(this) { orders ->
                adapter.submitList(orders)
                binding.rvHistory.visibility = if (orders.isEmpty()) android.view.View.GONE else android.view.View.VISIBLE
                binding.emptyState.root.visibility = if (orders.isEmpty()) android.view.View.VISIBLE else android.view.View.GONE
                if (orders.isEmpty()) {
                    binding.emptyState.tvTitle.text = "No transactions yet"
                    binding.emptyState.tvMessage.text = "Complete a checkout to see sales history here."
                }
            }

            viewModel.todaySummary.collectIn(this) { summary ->
                binding.tvTodaySales.text = CurrencyUtils.format(summary.totalSales)
                binding.tvTodayOrders.text = summary.orderCount.toString()
            }
        }
    }

    override fun onResume() {
        super.onResume()
        if (com.example.palamigopos.PinActivity.isPinRequired(this)) {
            val intent = Intent(this, com.example.palamigopos.PinActivity::class.java)
            intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
            startActivity(intent)
            finish()
        } else {
            // Refresh today's summary when activity resumes (handles new day)
            viewModel.refreshTodaySummary()
        }
    }

    private fun showPinVerificationDialog() {
        val dialogView = layoutInflater.inflate(R.layout.dialog_pin_verification, null)
        val dialog = AlertDialog.Builder(this)
            .setView(dialogView)
            .create()

        var enteredPin = ""
        val dots = listOf(
            dialogView.findViewById<View>(R.id.dot1),
            dialogView.findViewById<View>(R.id.dot2),
            dialogView.findViewById<View>(R.id.dot3),
            dialogView.findViewById<View>(R.id.dot4)
        )
        val tvError = dialogView.findViewById<TextView>(R.id.tvPinError)
        val btnCancel = dialogView.findViewById<com.google.android.material.button.MaterialButton>(R.id.btnCancel)

        fun updatePinDots() {
            val filledDrawable = R.drawable.pin_dot_filled
            val emptyDrawable = R.drawable.pin_dot_empty

            dots.forEachIndexed { index, dot ->
                dot.setBackgroundResource(
                    if (index < enteredPin.length) filledDrawable else emptyDrawable
                )
            }
        }

        fun showError() {
            tvError.visibility = View.VISIBLE
            tvError.text = "Incorrect PIN"
        }

        fun hideError() {
            tvError.visibility = View.INVISIBLE
        }

        fun checkPinComplete() {
            if (enteredPin.length == 4) {
                if (enteredPin == com.example.palamigopos.PinActivity.getStoredPin(this)) {
                    // PIN correct, delete the order
                    orderToDelete?.let { order ->
                        viewModel.deleteOrder(order.id)
                        Toast.makeText(this, "Transaction deleted", Toast.LENGTH_SHORT).show()
                    }
                    orderToDelete = null
                    dialog.dismiss()
                } else {
                    // Incorrect PIN
                    showError()
                    enteredPin = ""
                    updatePinDots()
                }
            }
        }

        // Number buttons
        val numberButtons = listOf(
            dialogView.findViewById<com.google.android.material.button.MaterialButton>(R.id.btn1),
            dialogView.findViewById<com.google.android.material.button.MaterialButton>(R.id.btn2),
            dialogView.findViewById<com.google.android.material.button.MaterialButton>(R.id.btn3),
            dialogView.findViewById<com.google.android.material.button.MaterialButton>(R.id.btn4),
            dialogView.findViewById<com.google.android.material.button.MaterialButton>(R.id.btn5),
            dialogView.findViewById<com.google.android.material.button.MaterialButton>(R.id.btn6),
            dialogView.findViewById<com.google.android.material.button.MaterialButton>(R.id.btn7),
            dialogView.findViewById<com.google.android.material.button.MaterialButton>(R.id.btn8),
            dialogView.findViewById<com.google.android.material.button.MaterialButton>(R.id.btn9),
            dialogView.findViewById<com.google.android.material.button.MaterialButton>(R.id.btn0)
        )

        numberButtons.forEach { button ->
            button.setOnClickListener {
                if (enteredPin.length < 4) {
                    enteredPin += button.text.toString()
                    updatePinDots()
                    hideError()
                    checkPinComplete()
                }
            }
        }

        // Backspace button
        val btnBackspace = dialogView.findViewById<com.google.android.material.button.MaterialButton>(R.id.btnBackspace)
        btnBackspace.setOnClickListener {
            if (enteredPin.isNotEmpty()) {
                enteredPin = enteredPin.dropLast(1)
                updatePinDots()
                hideError()
            }
        }

        // Cancel button
        btnCancel.setOnClickListener {
            orderToDelete = null
            dialog.dismiss()
        }

        dialog.show()
    }
}
