package com.example.palamigopos.ui.reports

import android.content.Intent
import android.os.Bundle
import androidx.activity.enableEdgeToEdge
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.palamigopos.data.local.AppDatabase
import com.example.palamigopos.databinding.ActivityReportsBinding
import com.example.palamigopos.ui.adapter.DailyReportAdapter
import com.example.palamigopos.ui.history.HistoryActivity
import com.example.palamigopos.utils.collectIn

class ReportsActivity : AppCompatActivity() {

    private lateinit var binding: ActivityReportsBinding

    private val viewModel: ReportsViewModel by viewModels {
        ReportsViewModel.Factory(AppDatabase.getInstance(this))
    }

    private val adapter = DailyReportAdapter { report ->
        // Optional: open history filtered by date (simplified: opens history)
        startActivity(Intent(this, HistoryActivity::class.java))
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        binding = ActivityReportsBinding.inflate(layoutInflater)
        setContentView(binding.root)

        ViewCompat.setOnApplyWindowInsetsListener(binding.root) { v, insets ->
            val bars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(bars.left, bars.top, bars.right, bars.bottom)
            insets
        }

        binding.toolbar.setNavigationIcon(androidx.appcompat.R.drawable.abc_ic_ab_back_material)
        binding.toolbar.setNavigationOnClickListener { finish() }

        binding.rvReports.layoutManager = LinearLayoutManager(this)
        binding.rvReports.adapter = adapter

        viewModel.dailyReports.collectIn(this) { reports ->
            adapter.submitList(reports)
            binding.rvReports.visibility = if (reports.isEmpty()) android.view.View.GONE else android.view.View.VISIBLE
            binding.emptyState.root.visibility = if (reports.isEmpty()) android.view.View.VISIBLE else android.view.View.GONE
            if (reports.isEmpty()) {
                binding.emptyState.tvTitle.text = "No reports yet"
                binding.emptyState.tvMessage.text = "Complete sales to generate daily reports."
            }
        }
    }
}
