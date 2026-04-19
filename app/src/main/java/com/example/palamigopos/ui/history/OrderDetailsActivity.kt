package com.example.palamigopos.ui.history

import android.os.Bundle
import androidx.activity.enableEdgeToEdge
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.palamigopos.data.local.AppDatabase
import com.example.palamigopos.databinding.ActivityOrderDetailsBinding
import com.example.palamigopos.ui.adapter.OrderDetailsAdapter
import com.example.palamigopos.utils.CurrencyUtils
import com.example.palamigopos.utils.collectIn

class OrderDetailsActivity : AppCompatActivity() {

    private lateinit var binding: ActivityOrderDetailsBinding

    private val viewModel: HistoryViewModel by viewModels {
        HistoryViewModel.Factory(AppDatabase.getInstance(this))
    }

    private val adapter = OrderDetailsAdapter()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        binding = ActivityOrderDetailsBinding.inflate(layoutInflater)
        setContentView(binding.root)

        ViewCompat.setOnApplyWindowInsetsListener(binding.root) { v, insets ->
            val bars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(bars.left, bars.top, bars.right, bars.bottom)
            insets
        }

        val orderId = intent.getIntExtra(EXTRA_ORDER_ID, -1)
        val orderNumber = intent.getStringExtra(EXTRA_ORDER_NUMBER) ?: "Order Details"
        val total = intent.getDoubleExtra(EXTRA_TOTAL, 0.0)
        val cash = intent.getDoubleExtra(EXTRA_CASH, 0.0)
        val change = intent.getDoubleExtra(EXTRA_CHANGE, 0.0)

        binding.toolbar.title = orderNumber
        binding.toolbar.setNavigationIcon(androidx.appcompat.R.drawable.abc_ic_ab_back_material)
        binding.toolbar.setNavigationOnClickListener { finish() }

        binding.tvTotal.text = "Total: ${CurrencyUtils.format(total)}"
        binding.tvCash.text = "Cash: ${CurrencyUtils.format(cash)}"
        binding.tvChange.text = "Change: ${CurrencyUtils.format(change)}"

        binding.rvItems.layoutManager = LinearLayoutManager(this)
        binding.rvItems.adapter = adapter

        viewModel.orderItems(orderId).collectIn(this) { items ->
            adapter.submitList(items)
        }
    }

    companion object {
        const val EXTRA_ORDER_ID = "extra_order_id"
        const val EXTRA_ORDER_NUMBER = "extra_order_number"
        const val EXTRA_TOTAL = "extra_total"
        const val EXTRA_CASH = "extra_cash"
        const val EXTRA_CHANGE = "extra_change"
    }
}
