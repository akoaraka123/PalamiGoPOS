package com.example.palamigopos.ui.inventory

import android.os.Bundle
import android.widget.ArrayAdapter
import androidx.activity.enableEdgeToEdge
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.palamigopos.data.local.AppDatabase
import com.example.palamigopos.data.model.ProductEntity
import com.example.palamigopos.databinding.ActivityInventoryBinding
import com.example.palamigopos.databinding.DialogAddEditProductBinding
import com.example.palamigopos.ui.adapter.InventoryAdapter
import com.example.palamigopos.utils.collectIn
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import com.google.android.material.snackbar.Snackbar

class InventoryActivity : AppCompatActivity() {

    private lateinit var binding: ActivityInventoryBinding

    private val viewModel: InventoryViewModel by viewModels {
        InventoryViewModel.Factory(AppDatabase.getInstance(this))
    }

    private val adapter = InventoryAdapter(
        onEdit = { showAddEditDialog(it) },
        onDelete = { product ->
            MaterialAlertDialogBuilder(this)
                .setTitle("Delete product")
                .setMessage("Delete ${product.name}?")
                .setNegativeButton("Cancel", null)
                .setPositiveButton("Delete") { _, _ ->
                    viewModel.deleteProduct(product)
                    Snackbar.make(binding.root, "Deleted", Snackbar.LENGTH_SHORT).show()
                }
                .show()
        },
        onToggle = { product, active ->
            viewModel.setActive(product, active)
        }
    )

    private val categories = listOf("Coffee", "Milk Creation", "Soda Spark")

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        binding = ActivityInventoryBinding.inflate(layoutInflater)
        setContentView(binding.root)

        ViewCompat.setOnApplyWindowInsetsListener(binding.root) { v, insets ->
            val bars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(bars.left, bars.top, bars.right, bars.bottom)
            insets
        }

        binding.toolbar.setNavigationIcon(androidx.appcompat.R.drawable.abc_ic_ab_back_material)
        binding.toolbar.setNavigationOnClickListener { finish() }

        binding.rvInventory.layoutManager = LinearLayoutManager(this)
        binding.rvInventory.adapter = adapter

        viewModel.products.collectIn(this) { list ->
            adapter.submitList(list)
            binding.rvInventory.visibility = if (list.isEmpty()) android.view.View.GONE else android.view.View.VISIBLE
            binding.emptyState.root.visibility = if (list.isEmpty()) android.view.View.VISIBLE else android.view.View.GONE
            if (list.isEmpty()) {
                binding.emptyState.tvTitle.text = "No products"
                binding.emptyState.tvMessage.text = "Tap Add Product to create your menu."
            }
        }

        binding.btnAdd.setOnClickListener {
            showAddEditDialog(null)
        }
    }

    private fun showAddEditDialog(product: ProductEntity?) {
        val dialogBinding = DialogAddEditProductBinding.inflate(layoutInflater)
        dialogBinding.tvTitle.text = if (product == null) "Add Product" else "Edit Product"

        val categoryAdapter = ArrayAdapter(this, android.R.layout.simple_list_item_1, categories)
        dialogBinding.actCategory.setAdapter(categoryAdapter)

        if (product != null) {
            dialogBinding.etName.setText(product.name)
            dialogBinding.actCategory.setText(product.category, false)
            dialogBinding.etPrice.setText(product.price.toString())
            dialogBinding.switchActive.isChecked = product.isActive
        } else {
            dialogBinding.actCategory.setText(categories.first(), false)
            dialogBinding.switchActive.isChecked = true
        }

        val dialog = MaterialAlertDialogBuilder(this)
            .setView(dialogBinding.root)
            .setNegativeButton("Cancel", null)
            .setPositiveButton(if (product == null) "Add" else "Save", null)
            .create()

        dialog.setOnShowListener {
            val positive = dialog.getButton(androidx.appcompat.app.AlertDialog.BUTTON_POSITIVE)
            positive.setOnClickListener {
                val name = dialogBinding.etName.text?.toString().orEmpty()
                val category = dialogBinding.actCategory.text?.toString().orEmpty().ifBlank { categories.first() }
                val price = dialogBinding.etPrice.text?.toString()?.toDoubleOrNull() ?: 0.0
                val isActive = dialogBinding.switchActive.isChecked

                val onError: (String) -> Unit = { msg ->
                    Snackbar.make(binding.root, msg, Snackbar.LENGTH_SHORT).show()
                }

                if (product == null) {
                    viewModel.addProduct(name, category, price, isActive, onError)
                    dialog.dismiss()
                } else {
                    viewModel.updateProduct(product, name, category, price, isActive, onError)
                    dialog.dismiss()
                }
            }
        }

        dialog.show()
    }
}
