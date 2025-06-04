package com.skincare.apitest.ui

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.skincare.apitest.databinding.ItemPackageProductBinding
import com.skincare.apitest.model.PackageProduct

class PackageProductAdapter(
    private val onItemClick: (PackageProduct) -> Unit,
    private val onCartClick: (PackageProduct) -> Unit
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
                    onCartClick(getItem(position))
                }
            }
        }

        fun bind(packageProduct: PackageProduct) {
            binding.apply {
                packageNameTextView.text = packageProduct.packageName
                packageItemsTextView.text = packageProduct.items.joinToString(separator = ", ")
                packagePriceTextView.text = packageProduct.getFormattedPrice()

                // Load image if available
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

    private class PackageProductDiffCallback : DiffUtil.ItemCallback<PackageProduct>() {
        override fun areItemsTheSame(oldItem: PackageProduct, newItem: PackageProduct): Boolean {
            return oldItem.id == newItem.id
        }

        override fun areContentsTheSame(oldItem: PackageProduct, newItem: PackageProduct): Boolean {
            return oldItem == newItem
        }
    }
}
