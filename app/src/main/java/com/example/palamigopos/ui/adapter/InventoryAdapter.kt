package com.example.palamigopos.ui.adapter

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.example.palamigopos.data.model.ProductEntity
import com.example.palamigopos.databinding.ItemInventoryProductBinding
import com.example.palamigopos.utils.CurrencyUtils

class InventoryAdapter(
    private val onEdit: (ProductEntity) -> Unit,
    private val onDelete: (ProductEntity) -> Unit,
    private val onToggle: (ProductEntity, Boolean) -> Unit
) : ListAdapter<ProductEntity, InventoryAdapter.VH>(Diff) {

    object Diff : DiffUtil.ItemCallback<ProductEntity>() {
        override fun areItemsTheSame(oldItem: ProductEntity, newItem: ProductEntity): Boolean =
            oldItem.id == newItem.id

        override fun areContentsTheSame(oldItem: ProductEntity, newItem: ProductEntity): Boolean =
            oldItem == newItem
    }

    inner class VH(private val binding: ItemInventoryProductBinding) : RecyclerView.ViewHolder(binding.root) {
        fun bind(item: ProductEntity) {
            binding.tvName.text = item.name
            binding.tvCategory.text = item.category
            binding.tvPrice.text = CurrencyUtils.format(item.price)
            binding.switchActive.isChecked = item.isActive
            binding.tvStatus.text = if (item.isActive) "Active" else "Inactive"

            binding.switchActive.setOnCheckedChangeListener(null)
            binding.switchActive.isChecked = item.isActive
            binding.switchActive.setOnCheckedChangeListener { _, isChecked ->
                onToggle(item, isChecked)
            }

            binding.btnEdit.setOnClickListener { onEdit(item) }
            binding.btnDelete.setOnClickListener { onDelete(item) }
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): VH {
        val binding = ItemInventoryProductBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return VH(binding)
    }

    override fun onBindViewHolder(holder: VH, position: Int) {
        holder.bind(getItem(position))
    }
}
