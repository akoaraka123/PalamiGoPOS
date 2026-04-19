package com.example.palamigopos.ui.adapter

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.example.palamigopos.data.model.ProductEntity
import com.example.palamigopos.databinding.ItemProductBinding
import com.example.palamigopos.utils.CurrencyUtils

class ProductAdapter(
    private val onClick: (ProductEntity) -> Unit
) : ListAdapter<ProductEntity, ProductAdapter.VH>(Diff) {

    object Diff : DiffUtil.ItemCallback<ProductEntity>() {
        override fun areItemsTheSame(oldItem: ProductEntity, newItem: ProductEntity): Boolean =
            oldItem.id == newItem.id

        override fun areContentsTheSame(oldItem: ProductEntity, newItem: ProductEntity): Boolean =
            oldItem == newItem
    }

    inner class VH(private val binding: ItemProductBinding) : RecyclerView.ViewHolder(binding.root) {
        fun bind(item: ProductEntity) {
            binding.tvName.text = item.name
            binding.tvPrice.text = CurrencyUtils.format(item.price)
            binding.root.setOnClickListener { onClick(item) }
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): VH {
        val binding = ItemProductBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return VH(binding)
    }

    override fun onBindViewHolder(holder: VH, position: Int) {
        holder.bind(getItem(position))
    }
}
