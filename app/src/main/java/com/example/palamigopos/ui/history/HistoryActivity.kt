package com.example.palamigopos.ui.history

import android.content.Intent
import android.os.Bundle
import androidx.activity.enableEdgeToEdge
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.recyclerview.widget.LinearLayoutManager
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

    private val adapter = OrderHistoryAdapter { order ->
        startActivity(
            Intent(this, OrderDetailsActivity::class.java).apply {
                putExtra(OrderDetailsActivity.EXTRA_ORDER_ID, order.id)
                putExtra(OrderDetailsActivity.EXTRA_ORDER_NUMBER, order.orderNumber)
                putExtra(OrderDetailsActivity.EXTRA_TOTAL, order.totalAmount)
                putExtra(OrderDetailsActivity.EXTRA_CASH, order.cashReceived)
                putExtra(OrderDetailsActivity.EXTRA_CHANGE, order.changeAmount)
            }
        )
    }

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

    override fun onResume() {
        super.onResume()
        if (com.example.palamigopos.PinActivity.isPinRequired(this)) {
            val intent = Intent(this, com.example.palamigopos.PinActivity::class.java)
            intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
            startActivity(intent)
            finish()
        }
    }
}
