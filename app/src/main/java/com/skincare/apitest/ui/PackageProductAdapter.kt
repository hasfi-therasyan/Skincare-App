package com.skincare.apitest.ui

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.AdapterView
import android.widget.ArrayAdapter
import android.widget.AutoCompleteTextView
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.skincare.apitest.databinding.ItemPackageProductBinding
import com.skincare.apitest.model.PackageProduct

class PackageProductAdapter(
    private val onCartClick: (PackageProduct, List<String>) -> Unit
) : ListAdapter<PackageProduct, PackageProductAdapter.PackageProductViewHolder>(PackageProductDiffCallback()) {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): PackageProductViewHolder {
        val binding = ItemPackageProductBinding.inflate(
            LayoutInflater.from(parent.context),
            parent,
            false
        )
        return PackageProductViewHolder(binding)
    }

    override fun onBindViewHolder(holder: PackageProductViewHolder, position: Int) {
        holder.bind(getItem(position))
    }

    inner class PackageProductViewHolder(
        private val binding: ItemPackageProductBinding
    ) : RecyclerView.ViewHolder(binding.root) {

        private val selectedItems = mutableListOf<String>()
        private val autoCompleteAdapters = mutableMapOf<AutoCompleteTextView, ArrayAdapter<String>>()
        private var allItems = listOf<String>()

        init {
            binding.cartButton.setOnClickListener {
                val position = adapterPosition
                if (position != RecyclerView.NO_POSITION) {
                    val pkg = getItem(position)
                    // Get the current selected items from the spinners
                    val currentSelectedItems = getCurrentSelectedItems()
                    onCartClick(pkg, currentSelectedItems)
                }
            }
        }

        fun bind(packageProduct: PackageProduct) {
            binding.apply {
                packageNameTextView.text = packageProduct.packageName
                packagePriceTextView.text = packageProduct.getFormattedPrice()

                // Load image if available
                packageProduct.imageData?.let { imageData ->
                    Glide.with(packageImageView)
                        .load(imageData)
                        .centerCrop()
                        .into(packageImageView)
                }

                // Setup auto complete text views
                setupAutoCompleteTextViews(packageProduct)
            }
        }

        private fun setupAutoCompleteTextViews(packageProduct: PackageProduct) {
            allItems = packageProduct.items.flatMap { it.split(",").map { it.trim() } }
            
            // Initialize selected items list
            selectedItems.clear()
            selectedItems.add("") // Empty selection for spinner1
            selectedItems.add("") // Empty selection for spinner2
            selectedItems.add("") // Empty selection for spinner3
            
            // Create and setup adapters for each auto complete text view
            setupAutoCompleteAdapter(binding.spinner1, 0)
            setupAutoCompleteAdapter(binding.spinner2, 1)
            setupAutoCompleteAdapter(binding.spinner3, 2)
        }

        private fun setupAutoCompleteAdapter(autoCompleteTextView: AutoCompleteTextView, spinnerIndex: Int) {
            val adapter = ArrayAdapter(binding.root.context, android.R.layout.simple_dropdown_item_1line, getOptionsForSpinner(spinnerIndex))
            
            autoCompleteAdapters[autoCompleteTextView] = adapter
            autoCompleteTextView.setAdapter(adapter)
            
            // Make the AutoCompleteTextView clickable to show dropdown
            autoCompleteTextView.setOnClickListener {
                autoCompleteTextView.showDropDown()
            }
            
            // Set up item selected listener
            autoCompleteTextView.setOnItemClickListener { _, _, position, _ ->
                val selectedItem = if (position > 0) getOptionsForSpinner(spinnerIndex)[position] else ""
                selectedItems[spinnerIndex] = selectedItem
                updateAllAutoCompleteTextViews()
            }
            
            // Set default text
            autoCompleteTextView.setText("", false)
        }

        private fun getOptionsForSpinner(spinnerIndex: Int): List<String> {
            val options = mutableListOf("Select an item")
            val currentSelection = selectedItems[spinnerIndex]
            
            // Get available items (excluding items selected in other spinners)
            val availableItems = allItems.filter { item ->
                !selectedItems.contains(item) || item == currentSelection
            }
            
            options.addAll(availableItems)
            return options
        }

        private fun updateAllAutoCompleteTextViews() {
            // Update each auto complete text view's options
            setupAutoCompleteAdapter(binding.spinner1, 0)
            setupAutoCompleteAdapter(binding.spinner2, 1)
            setupAutoCompleteAdapter(binding.spinner3, 2)
            
            // Restore selections
            restoreSelections()
        }

        private fun restoreSelections() {
            val selection1 = selectedItems[0]
            val selection2 = selectedItems[1]
            val selection3 = selectedItems[2]
            
            if (selection1.isNotEmpty()) binding.spinner1.setText(selection1, false)
            if (selection2.isNotEmpty()) binding.spinner2.setText(selection2, false)
            if (selection3.isNotEmpty()) binding.spinner3.setText(selection3, false)
        }

        private fun getCurrentSelectedItems(): List<String> {
            // Get the current text from each spinner and filter out empty selections
            val spinner1Text = binding.spinner1.text.toString()
            val spinner2Text = binding.spinner2.text.toString()
            val spinner3Text = binding.spinner3.text.toString()
            
            return listOf(spinner1Text, spinner2Text, spinner3Text).filter { it.isNotEmpty() }
        }
    }

    private class PackageProductDiffCallback : DiffUtil.ItemCallback<PackageProduct>() {
        override fun areItemsTheSame(oldItem: PackageProduct, newItem: PackageProduct): Boolean {
            return oldItem.id == newItem.id
        }

        override fun areContentsTheSame(oldItem: PackageProduct, newItem: PackageProduct): Boolean {
            return oldItem == newItem
        }
    }
}
