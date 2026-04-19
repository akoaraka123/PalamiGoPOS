package com.example.palamigopos.ui.adapter

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.example.palamigopos.data.model.DailySalesReport
import com.example.palamigopos.databinding.ItemDailyReportBinding
import com.example.palamigopos.utils.CurrencyUtils
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

class DailyReportAdapter(
    private val onClick: (DailySalesReport) -> Unit
) : ListAdapter<DailySalesReport, DailyReportAdapter.VH>(Diff) {

    object Diff : DiffUtil.ItemCallback<DailySalesReport>() {
        override fun areItemsTheSame(oldItem: DailySalesReport, newItem: DailySalesReport): Boolean =
            oldItem.dateMillis == newItem.dateMillis

        override fun areContentsTheSame(oldItem: DailySalesReport, newItem: DailySalesReport): Boolean =
            oldItem == newItem
    }

    private val dateFormat = SimpleDateFormat("MMM dd, yyyy", Locale.getDefault())

    inner class VH(private val binding: ItemDailyReportBinding) : RecyclerView.ViewHolder(binding.root) {
        fun bind(item: DailySalesReport) {
            binding.tvDate.text = dateFormat.format(Date(item.dateMillis))
            binding.tvTotalSales.text = CurrencyUtils.format(item.totalSales)
            binding.tvOrderCount.text = "${item.totalOrders} orders"
            binding.root.setOnClickListener { onClick(item) }
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): VH {
        val binding = ItemDailyReportBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return VH(binding)
    }

    override fun onBindViewHolder(holder: VH, position: Int) {
        holder.bind(getItem(position))
    }
}
