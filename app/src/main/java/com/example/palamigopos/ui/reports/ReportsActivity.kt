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
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

class ReportsActivity : AppCompatActivity() {

    private lateinit var binding: ActivityReportsBinding

    private val viewModel: ReportsViewModel by viewModels {
        ReportsViewModel.Factory(AppDatabase.getInstance(this))
    }

    private val adapter = DailyReportAdapter { report ->
        // Calculate start and end of the day for the report
        val startOfDay = report.dateMillis
        val endOfDay = report.dateMillis + 86400000 - 1 // End of that day
        
        startActivity(Intent(this, HistoryActivity::class.java).apply {
            putExtra(HistoryActivity.EXTRA_START_DATE, startOfDay)
            putExtra(HistoryActivity.EXTRA_END_DATE, endOfDay)
            putExtra(HistoryActivity.EXTRA_DATE_LABEL, formatReportDate(report.dateMillis))
        })
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

    override fun onResume() {
        super.onResume()
        if (com.example.palamigopos.PinActivity.isPinRequired(this)) {
            val intent = Intent(this, com.example.palamigopos.PinActivity::class.java)
            intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
            startActivity(intent)
            finish()
        }
    }

    private fun formatReportDate(dateMillis: Long): String {
        val dateFormat = SimpleDateFormat("MMM dd, yyyy", Locale.getDefault())
        return dateFormat.format(Date(dateMillis))
    }
}
