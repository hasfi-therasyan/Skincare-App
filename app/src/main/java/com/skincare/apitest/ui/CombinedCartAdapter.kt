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
            binding.root.setOnClickListener {
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
                productName.text = product.productName
                productDescription.text = product.description
                productPrice.text = product.getFormattedPrice()

                product.imageData?.let { imageUrl ->
                    Glide.with(productImage)
                        .load(imageUrl)
                        .centerCrop()
                        .into(productImage)
                }

                // Hide cart button in cart list
                addToCartButton.visibility = android.view.View.GONE
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
                packagePriceTextView.text = customPackageProduct.getFormattedPrice()

                // Hide the spinners in cart view
                spinner1.visibility = android.view.View.GONE
                spinner2.visibility = android.view.View.GONE
                spinner3.visibility = android.view.View.GONE
                
                // Hide the TextInputLayout containers
                val spinner1Container = spinner1.parent as? android.view.ViewGroup
                val spinner2Container = spinner2.parent as? android.view.ViewGroup
                val spinner3Container = spinner3.parent as? android.view.ViewGroup
                
                spinner1Container?.visibility = android.view.View.GONE
                spinner2Container?.visibility = android.view.View.GONE
                spinner3Container?.visibility = android.view.View.GONE
                
                // Show selected items in a clear format
                productsLabel.visibility = android.view.View.VISIBLE
                if (customPackageProduct.selectedItems.isNotEmpty()) {
                    val selectedItemsText = customPackageProduct.selectedItems.joinToString(" • ")
                    productsLabel.text = "Selected Items:\n$selectedItemsText"
                } else {
                    productsLabel.text = "No items selected"
                }

                customPackageProduct.imageData?.let { imageUrl ->
                    Glide.with(packageImageView)
                        .load(imageUrl)
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