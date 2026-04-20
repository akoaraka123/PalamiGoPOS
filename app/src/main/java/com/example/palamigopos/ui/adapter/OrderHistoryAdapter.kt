package com.example.palamigopos.ui.adapter

import android.content.Context
import android.graphics.Color
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.example.palamigopos.R
import com.example.palamigopos.data.model.OrderEntity
import com.example.palamigopos.databinding.ItemOrderHistoryBinding
import com.example.palamigopos.utils.CurrencyUtils
import com.example.palamigopos.utils.DateTimeUtils

class OrderHistoryAdapter(
    private val onClick: (OrderEntity) -> Unit,
    private val onDelete: (OrderEntity) -> Unit
) : ListAdapter<OrderEntity, OrderHistoryAdapter.VH>(Diff) {

    object Diff : DiffUtil.ItemCallback<OrderEntity>() {
        override fun areItemsTheSame(oldItem: OrderEntity, newItem: OrderEntity): Boolean =
            oldItem.id == newItem.id

        override fun areContentsTheSame(oldItem: OrderEntity, newItem: OrderEntity): Boolean =
            oldItem == newItem
    }

    inner class VH(private val binding: ItemOrderHistoryBinding) : RecyclerView.ViewHolder(binding.root) {
        fun bind(item: OrderEntity) {
            binding.tvOrderNumber.text = item.orderNumber
            binding.tvDateTime.text = DateTimeUtils.formatDateTime(item.createdAt)
            binding.tvTotal.text = CurrencyUtils.format(item.totalAmount)
            binding.chipPaymentMethod.text = item.paymentMethod

            // Set chip background color based on payment method
            val context = binding.root.context
            val colorRes = when (item.paymentMethod) {
                "GCash" -> R.color.pg_blue
                "Cash" -> R.color.pg_green
                else -> R.color.pg_gold
            }
            binding.chipPaymentMethod.chipBackgroundColor = ContextCompat.getColorStateList(context, colorRes)
            binding.chipPaymentMethod.setTextColor(ContextCompat.getColor(context, android.R.color.black))

            binding.root.setOnClickListener { onClick(item) }
            binding.btnDelete.setOnClickListener { onDelete(item) }
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): VH {
        val binding = ItemOrderHistoryBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return VH(binding)
    }

    override fun onBindViewHolder(holder: VH, position: Int) {
        holder.bind(getItem(position))
    }
}
