package com.example.palamigopos.ui.adapter

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.example.palamigopos.data.model.OrderItemEntity
import com.example.palamigopos.databinding.ItemOrderDetailBinding
import com.example.palamigopos.utils.CurrencyUtils

class OrderDetailsAdapter : ListAdapter<OrderItemEntity, OrderDetailsAdapter.VH>(Diff) {

    object Diff : DiffUtil.ItemCallback<OrderItemEntity>() {
        override fun areItemsTheSame(oldItem: OrderItemEntity, newItem: OrderItemEntity): Boolean =
            oldItem.id == newItem.id

        override fun areContentsTheSame(oldItem: OrderItemEntity, newItem: OrderItemEntity): Boolean =
            oldItem == newItem
    }

    inner class VH(private val binding: ItemOrderDetailBinding) : RecyclerView.ViewHolder(binding.root) {
        fun bind(item: OrderItemEntity) {
            binding.tvName.text = item.productName
            binding.tvQty.text = "x${item.quantity}"
            binding.tvPrice.text = CurrencyUtils.format(item.productPrice)
            binding.tvSubtotal.text = CurrencyUtils.format(item.subtotal)
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): VH {
        val binding = ItemOrderDetailBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return VH(binding)
    }

    override fun onBindViewHolder(holder: VH, position: Int) {
        holder.bind(getItem(position))
    }
}
