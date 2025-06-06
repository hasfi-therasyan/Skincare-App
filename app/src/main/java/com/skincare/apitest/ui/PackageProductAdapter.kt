package com.skincare.apitest.ui

import android.view.LayoutInflater
import android.view.ViewGroup
import android.widget.ArrayAdapter
import android.widget.Spinner
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.skincare.apitest.R
import com.skincare.apitest.databinding.ItemPackageProductBinding
import com.skincare.apitest.model.PackageProduct

class PackageProductAdapter(
    private val onItemClick: (PackageProduct) -> Unit,
    private val onCartClick: (PackageProduct, List<String>) -> Unit
) : ListAdapter<PackageProduct, PackageProductAdapter.PackageProductViewHolder>(PackageProductDiffCallback()) {

    // Store selected items per package id
    private val selectedItemsMap = mutableMapOf<Int, MutableList<String>>()

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

        init {
            binding.root.setOnClickListener {
                val position = adapterPosition
                if (position != RecyclerView.NO_POSITION) {
                    onItemClick(getItem(position))
                }
            }
            binding.cartButton.setOnClickListener {
                val position = adapterPosition
                if (position != RecyclerView.NO_POSITION) {
                    val pkg = getItem(position)
                    val selectedItems = selectedItemsMap[pkg.id] ?: MutableList(3) { "" }
                    onCartClick(pkg, selectedItems)
                }
            }
        }

        fun bind(packageProduct: PackageProduct) {
            binding.apply {
                packageNameTextView.text = packageProduct.packageName
                packagePriceTextView.text = packageProduct.getFormattedPrice()

                // Load image if available
                packageProduct.imageData?.let { imageUrl ->
                    Glide.with(packageImageView)
                        .load(imageUrl) // Changed to load image URL directly
                        .centerCrop()
                        .into(packageImageView)
                }

                // Use static spinners defined in layout instead of dynamic addition
                val spinner1 = binding.root.findViewById<Spinner>(R.id.spinner1)
                val spinner2 = binding.root.findViewById<Spinner>(R.id.spinner2)
                val spinner3 = binding.root.findViewById<Spinner>(R.id.spinner3)

                val spinners = listOf(spinner1, spinner2, spinner3)

                // Initialize selected items list if not present
                val selectedItemsLocal = selectedItemsMap.getOrPut(packageProduct.id) {
                    // Initialize with first 3 items in order or fewer if not enough items
                    MutableList(spinners.size) { packageProduct.items.getOrNull(it) ?: "" }
                }

                // For each spinner, set adapter and selection
                spinners.forEachIndexed { index, spinner ->
                    // Filter items to exclude already selected items in other spinners except current index
                    val filteredItems = packageProduct.items.flatMap { it.split(",").map { it.trim() } }
                        .filter { item ->
                            val selectedIndex = selectedItemsLocal.indexOf(item)
                            selectedIndex == -1 || selectedIndex == index
                        }

                    val adapter = ArrayAdapter(
                        binding.root.context,
                        android.R.layout.simple_spinner_item,
                        filteredItems
                    )
                    adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
                    spinner.adapter = adapter

                    // Set initial selection to the item at the spinner's index or first item
                    val selectedItem = selectedItemsLocal.getOrNull(index) ?: filteredItems.getOrNull(0) ?: ""
                    val selectionIndex = filteredItems.indexOf(selectedItem).takeIf { it >= 0 } ?: 0
                    spinner.setSelection(selectionIndex)

                    spinner.onItemSelectedListener = object : android.widget.AdapterView.OnItemSelectedListener {
                        override fun onItemSelected(
                            parent: android.widget.AdapterView<*>,
                            view: android.view.View?,
                            position: Int,
                            id: Long
                        ) {
                            val newItem = adapter.getItem(position) ?: ""
                            if (selectedItemsLocal[index] != newItem) {
                                selectedItemsLocal[index] = newItem
                                notifyItemChanged(adapterPosition)
                            }
                        }

                        override fun onNothingSelected(parent: android.widget.AdapterView<*>) {
                            // Do nothing
                        }
                    }

                    spinner.isEnabled = true
                    spinner.isClickable = true
                }
            }
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