package com.example.palamigopos.ui.inventory

import android.content.Intent
import android.os.Bundle
import android.view.Menu
import android.view.MenuItem
import android.widget.ArrayAdapter
import android.widget.EditText
import androidx.activity.enableEdgeToEdge
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.palamigopos.R
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

    private val categories = mutableListOf<String>()

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

        setSupportActionBar(binding.toolbar)
        supportActionBar?.setDisplayHomeAsUpEnabled(true)

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

        viewModel.categories.collectIn(this) { categoryList ->
            categories.clear()
            categories.addAll(categoryList.map { it.name })
        }

        binding.btnAdd.setOnClickListener {
            showAddEditDialog(null)
        }
    }

    override fun onCreateOptionsMenu(menu: Menu?): Boolean {
        menuInflater.inflate(R.menu.menu_inventory, menu)
        return true
    }

    override fun onSupportNavigateUp(): Boolean {
        finish()
        return true
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        return when (item.itemId) {
            R.id.action_add_category -> {
                showAddCategoryDialog()
                true
            }
            R.id.action_manage_categories -> {
                showManageCategoriesDialog()
                true
            }
            else -> super.onOptionsItemSelected(item)
        }
    }

    private fun showAddCategoryDialog() {
        val dialogBinding = com.example.palamigopos.databinding.DialogAddCategoryBinding.inflate(layoutInflater)

        val dialog = MaterialAlertDialogBuilder(this)
            .setView(dialogBinding.root)
            .setNegativeButton("Cancel", null)
            .setPositiveButton("Add", null)
            .create()

        // Set dialog background to match app dark theme
        dialog.window?.setBackgroundDrawableResource(R.color.pg_surface)
        
        // Set dialog window layout parameters to match Add Product dialog
        dialog.window?.setLayout(
            (resources.displayMetrics.widthPixels * 0.9).toInt(),
            android.view.WindowManager.LayoutParams.WRAP_CONTENT
        )

        dialog.setOnShowListener {
            val positive = dialog.getButton(androidx.appcompat.app.AlertDialog.BUTTON_POSITIVE)
            positive.setOnClickListener {
                val categoryName = dialogBinding.etCategoryName.text?.toString().orEmpty()
                viewModel.addCategory(categoryName) { error ->
                    Snackbar.make(binding.root, error, Snackbar.LENGTH_SHORT).show()
                }
                dialog.dismiss()
            }
        }

        dialog.show()
    }

    private fun showManageCategoriesDialog() {
        val dialogBinding = com.example.palamigopos.databinding.DialogManageCategoriesBinding.inflate(layoutInflater)
        val categories = viewModel.categories.value
        
        if (categories.isEmpty()) {
            Snackbar.make(binding.root, "No categories to manage", Snackbar.LENGTH_SHORT).show()
            return
        }

        val adapter = com.example.palamigopos.ui.adapter.CategoryManageAdapter { category ->
            // Show confirmation dialog for deletion
            MaterialAlertDialogBuilder(this)
                .setTitle("Delete Category")
                .setMessage("Delete '${category.name}'?")
                .setNegativeButton("Cancel", null)
                .setPositiveButton("Delete") { _, _ ->
                    viewModel.deleteCategory(category.id)
                    Snackbar.make(binding.root, "Category deleted", Snackbar.LENGTH_SHORT).show()
                    dialogBinding.rvCategories.adapter?.let { it as? com.example.palamigopos.ui.adapter.CategoryManageAdapter }?.submitList(viewModel.categories.value)
                }
                .show()
        }

        dialogBinding.rvCategories.layoutManager = LinearLayoutManager(this)
        dialogBinding.rvCategories.adapter = adapter
        adapter.submitList(categories)

        val dialog = MaterialAlertDialogBuilder(this)
            .setView(dialogBinding.root)
            .setNegativeButton("Close", null)
            .create()

        // Set dialog background to match app dark theme
        dialog.window?.setBackgroundDrawableResource(R.color.pg_surface)
        
        // Set dialog window layout parameters to match Add Product dialog
        dialog.window?.setLayout(
            (resources.displayMetrics.widthPixels * 0.9).toInt(),
            android.view.WindowManager.LayoutParams.WRAP_CONTENT
        )

        dialog.show()
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

        // Set dialog background to match app dark theme
        dialog.window?.setBackgroundDrawableResource(R.color.pg_surface)
        
        // Set dialog window layout parameters to match Checkout dialog
        dialog.window?.setLayout(
            (resources.displayMetrics.widthPixels * 0.9).toInt(),
            android.view.WindowManager.LayoutParams.WRAP_CONTENT
        )

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

    override fun onResume() {
        super.onResume()
        if (com.example.palamigopos.PinActivity.isPinRequired(this)) {
            val intent = Intent(this, com.example.palamigopos.PinActivity::class.java)
            intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
            startActivity(intent)
            finish()
        }
    }
}
