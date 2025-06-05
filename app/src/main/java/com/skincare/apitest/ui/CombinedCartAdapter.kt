package com.skincare.apitest.ui

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.skincare.apitest.databinding.ItemPackageProductBinding
import com.skincare.apitest.databinding.ItemProductBinding
import com.skincare.apitest.model.PackageProduct
import com.skincare.apitest.model.CustomPackageProduct
import com.skincare.apitest.model.Product
import com.skincare.apitest.repository.CartItem

private const val VIEW_TYPE_PRODUCT = 1
private const val VIEW_TYPE_PACKAGE_PRODUCT = 2

class CombinedCartAdapter(
    private val onDeleteProductClick: (Product) -> Unit,
    private val onItemProductClick: (Product) -> Unit,
    private val onDeletePackageClick: (PackageProduct) -> Unit,
    private val onItemPackageClick: (PackageProduct) -> Unit
) : ListAdapter<CartItem, RecyclerView.ViewHolder>(CartItemDiffCallback()) {

    override fun getItemViewType(position: Int): Int {
        return when (getItem(position)) {
            is CartItem.IndividualProduct -> VIEW_TYPE_PRODUCT
            is CartItem.PackageProductItem -> VIEW_TYPE_PACKAGE_PRODUCT
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder {
        return when (viewType) {
            VIEW_TYPE_PRODUCT -> {
                val binding = ItemProductBinding.inflate(LayoutInflater.from(parent.context), parent, false)
                ProductViewHolder(binding)
            }
            VIEW_TYPE_PACKAGE_PRODUCT -> {
                val binding = ItemPackageProductBinding.inflate(LayoutInflater.from(parent.context), parent, false)
                PackageProductViewHolder(binding)
            }
            else -> throw IllegalArgumentException("Invalid view type")
        }
    }

    override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
        when (val item = getItem(position)) {
            is CartItem.IndividualProduct -> (holder as ProductViewHolder).bind(item.product)
            is CartItem.PackageProductItem -> (holder as PackageProductViewHolder).bind(item.packageProduct)
        }
    }

    inner class ProductViewHolder(private val binding: ItemProductBinding) : RecyclerView.ViewHolder(binding.root) {
        init {
            binding.deleteButton.visibility = android.view.View.VISIBLE
            binding.deleteButton.setOnClickListener {
                val position = adapterPosition
                if (position != RecyclerView.NO_POSITION) {
                    val item = getItem(position)
                    if (item is CartItem.IndividualProduct) {
                        onDeleteProductClick(item.product)
                    }
                }
            }
            binding.productItemLayout.setOnClickListener {
                val position = adapterPosition
                if (position != RecyclerView.NO_POSITION) {
                    val item = getItem(position)
                    if (item is CartItem.IndividualProduct) {
                        onItemProductClick(item.product)
                    }
                }
            }
        }

        fun bind(product: Product) {
            binding.apply {
                productNameTextView.text = product.productName
                productDescriptionTextView.text = product.description
                productPriceTextView.text = product.getFormattedPrice()

                product.imageData?.let { imageData ->
                    val base64Image = "data:image/png;base64,$imageData"
                    Glide.with(productImageView)
                        .load(base64Image)
                        .centerCrop()
                        .into(productImageView)
                }

                // deleteButton.visibility = android.view.View.VISIBLE
                cartButton.visibility = android.view.View.GONE
            }
        }
    }

    inner class PackageProductViewHolder(private val binding: ItemPackageProductBinding) : RecyclerView.ViewHolder(binding.root) {
        init {
            binding.cartButton.visibility = android.view.View.GONE
            binding.cartButton.isEnabled = false
            binding.deleteButton.visibility = android.view.View.VISIBLE
            binding.deleteButton.setOnClickListener {
                val position = adapterPosition
                if (position != RecyclerView.NO_POSITION) {
                    val item = getItem(position)
                    if (item is CartItem.PackageProductItem) {
                        onDeletePackageClick(item.packageProduct.basePackage)
                    }
                }
            }

            binding.root.setOnClickListener {
                val position = adapterPosition
                if (position != RecyclerView.NO_POSITION) {
                    val item = getItem(position)
                    if (item is CartItem.PackageProductItem) {
                        onItemPackageClick(item.packageProduct.basePackage)
                    }
                }
            }
        }

        fun bind(customPackageProduct: CustomPackageProduct) {
            binding.apply {
                packageNameTextView.text = customPackageProduct.packageName

                // Setup spinners to show selected items and available items
                val itemsList = customPackageProduct.basePackage.items.flatMap { item -> item.split(",").map { it.trim() } }
                val spinners = listOf(spinner1, spinner2, spinner3)
                val selectedItemsLocal = customPackageProduct.selectedItems.toMutableList()

                spinners.forEachIndexed { index, spinner ->
                    val filteredItems = itemsList.filter { item ->
                        val selectedIndex = selectedItemsLocal.indexOf(item)
                        selectedIndex == -1 || selectedIndex == index
                    }

                    val adapter = android.widget.ArrayAdapter<String>(
                        binding.root.context,
                        android.R.layout.simple_spinner_item,
                        filteredItems
                    )
                    adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
                    spinner.adapter = adapter

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
                                // Update the selectedItems in the customPackageProduct if needed
                                // Notify adapter or update UI accordingly
                            }
                        }

                        override fun onNothingSelected(parent: android.widget.AdapterView<*>) {
                            // Do nothing
                        }
                    }

                    spinner.isEnabled = true
                    spinner.isClickable = true
                }

                packagePriceTextView.text = customPackageProduct.getFormattedPrice()

                customPackageProduct.imageData?.let { imageData ->
                    val base64Image = "data:image/png;base64,$imageData"
                    Glide.with(packageImageView)
                        .load(base64Image)
                        .centerCrop()
                        .into(packageImageView)
                }
            }
        }
    }

    private class CartItemDiffCallback : DiffUtil.ItemCallback<CartItem>() {
        override fun areItemsTheSame(oldItem: CartItem, newItem: CartItem): Boolean {
            return when {
                oldItem is CartItem.IndividualProduct && newItem is CartItem.IndividualProduct -> oldItem.product.id == newItem.product.id
                oldItem is CartItem.PackageProductItem && newItem is CartItem.PackageProductItem -> oldItem.packageProduct.id == newItem.packageProduct.id
                else -> false
            }
        }

        override fun areContentsTheSame(oldItem: CartItem, newItem: CartItem): Boolean {
            return oldItem == newItem
        }
    }
}