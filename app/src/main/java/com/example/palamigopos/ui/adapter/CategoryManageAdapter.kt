package com.example.palamigopos.ui.adapter

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.example.palamigopos.data.model.CategoryEntity
import com.example.palamigopos.databinding.ItemCategoryManageBinding

class CategoryManageAdapter(
    private val onDelete: (CategoryEntity) -> Unit
) : RecyclerView.Adapter<CategoryManageAdapter.CategoryViewHolder>() {

    private var categories = listOf<CategoryEntity>()

    fun submitList(newCategories: List<CategoryEntity>) {
        categories = newCategories
        notifyDataSetChanged()
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): CategoryViewHolder {
        val binding = ItemCategoryManageBinding.inflate(
            LayoutInflater.from(parent.context),
            parent,
            false
        )
        return CategoryViewHolder(binding)
    }

    override fun onBindViewHolder(holder: CategoryViewHolder, position: Int) {
        holder.bind(categories[position])
    }

    override fun getItemCount(): Int = categories.size

    inner class CategoryViewHolder(
        private val binding: ItemCategoryManageBinding
    ) : RecyclerView.ViewHolder(binding.root) {

        fun bind(category: CategoryEntity) {
            binding.tvCategoryName.text = category.name
            binding.btnDelete.setOnClickListener {
                onDelete(category)
            }
        }
    }
}
