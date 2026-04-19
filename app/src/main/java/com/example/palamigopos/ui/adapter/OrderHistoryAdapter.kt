package com.example.palamigopos.ui.adapter

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.example.palamigopos.data.model.OrderEntity
import com.example.palamigopos.databinding.ItemOrderHistoryBinding
import com.example.palamigopos.utils.CurrencyUtils
import com.example.palamigopos.utils.DateTimeUtils

class OrderHistoryAdapter(
    private val onClick: (OrderEntity) -> Unit
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
            binding.root.setOnClickListener { onClick(item) }
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
