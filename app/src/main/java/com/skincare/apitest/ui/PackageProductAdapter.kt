package com.skincare.apitest.ui

import android.view.LayoutInflater
import android.view.ViewGroup
import android.widget.ArrayAdapter
import android.widget.Spinner
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
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
                    val selectedItems = selectedItemsMap[pkg.id] ?: pkg.items.toMutableList()
                    onCartClick(pkg, selectedItems)
                }
            }
        }

        fun bind(packageProduct: PackageProduct) {
            binding.apply {
                packageNameTextView.text = packageProduct.packageName
                packagePriceTextView.text = packageProduct.getFormattedPrice()

                // Load image if available
                packageProduct.imageData?.let { imageData ->
                    val base64Image = "data:image/png;base64,$imageData"
                    Glide.with(packageImageView)
                        .load(base64Image)
                        .centerCrop()
                        .into(packageImageView)
                }

                // Clear previous views in itemsContainer
                itemsContainer.removeAllViews()

                // Initialize selected items list if not present
                val selectedItems = selectedItemsMap.getOrPut(packageProduct.id) {
                    packageProduct.items.toMutableList()
                }

                // For each item, add a Spinner dropdown
                packageProduct.items.forEachIndexed { index, item ->
                    val spinner = Spinner(binding.root.context)
                    val adapter = ArrayAdapter(
                        binding.root.context,
                        android.R.layout.simple_spinner_item,
                        packageProduct.items // You may replace with a full list of available items if needed
                    )
                    adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
                    spinner.adapter = adapter
                    spinner.setSelection(adapter.getPosition(selectedItems[index]))

                    spinner.setOnItemSelectedListener(object : android.widget.AdapterView.OnItemSelectedListener {
                        override fun onItemSelected(
                            parent: android.widget.AdapterView<*>,
                            view: android.view.View?,
                            position: Int,
                            id: Long
                        ) {
                            selectedItems[index] = adapter.getItem(position) ?: item
                        }

                        override fun onNothingSelected(parent: android.widget.AdapterView<*>) {
                            // Do nothing
                        }
                    })

                    itemsContainer.addView(spinner)
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
