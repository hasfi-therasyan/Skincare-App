package com.skincare.apitest.ui

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.skincare.apitest.databinding.ItemPackageProductBinding
import com.skincare.apitest.model.PackageProduct

class CartPackageAdapter(
    private val onDeleteClick: (PackageProduct) -> Unit,
    private val onItemClick: (PackageProduct) -> Unit
) : ListAdapter<PackageProduct, CartPackageAdapter.CartPackageViewHolder>(CartPackageDiffCallback()) {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): CartPackageViewHolder {
        val binding = ItemPackageProductBinding.inflate(
            LayoutInflater.from(parent.context),
            parent,
            false
        )
        return CartPackageViewHolder(binding)
    }

    override fun onBindViewHolder(holder: CartPackageViewHolder, position: Int) {
        holder.bind(getItem(position))
    }

    inner class CartPackageViewHolder(
        private val binding: ItemPackageProductBinding
    ) : RecyclerView.ViewHolder(binding.root) {

        init {
            binding.cartButton.visibility = android.view.View.GONE
            binding.cartButton.isEnabled = false

            binding.cartButton.setOnClickListener(null)

            binding.root.setOnClickListener {
                val position = adapterPosition
                if (position != RecyclerView.NO_POSITION) {
                    onItemClick(getItem(position))
                }
            }

            // Remove references to deleteButton since it no longer exists in the layout
        }

        fun bind(packageProduct: PackageProduct) {
            binding.apply {
                packageNameTextView.text = packageProduct.packageName
                packageItemsTextView.text = packageProduct.items.joinToString(", ")
                packagePriceTextView.text = packageProduct.getFormattedPrice()

                packageProduct.imageData?.let { imageData ->
                    val base64Image = "data:image/png;base64,$imageData"
                    Glide.with(packageImageView)
                        .load(base64Image)
                        .centerCrop()
                        .into(packageImageView)
                }
            }
        }
    }

    private class CartPackageDiffCallback : DiffUtil.ItemCallback<PackageProduct>() {
        override fun areItemsTheSame(oldItem: PackageProduct, newItem: PackageProduct): Boolean {
            return oldItem.id == newItem.id
        }

        override fun areContentsTheSame(oldItem: PackageProduct, newItem: PackageProduct): Boolean {
            return oldItem == newItem
        }
    }
}
