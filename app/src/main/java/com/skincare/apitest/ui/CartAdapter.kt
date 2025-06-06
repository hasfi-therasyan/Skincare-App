package com.skincare.apitest.ui

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.skincare.apitest.databinding.ItemProductBinding
import com.skincare.apitest.model.Product

class CartAdapter(
    private val onDeleteClick: (Product) -> Unit,
    private val onItemClick: (Product) -> Unit
) : ListAdapter<Product, CartAdapter.CartViewHolder>(CartDiffCallback()) {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): CartViewHolder {
        val binding = ItemProductBinding.inflate(
            LayoutInflater.from(parent.context),
            parent,
            false
        )
        return CartViewHolder(binding)
    }

    override fun onBindViewHolder(holder: CartViewHolder, position: Int) {
        holder.bind(getItem(position))
    }

    inner class CartViewHolder(
        private val binding: ItemProductBinding
    ) : RecyclerView.ViewHolder(binding.root) {

        init {
            binding.deleteButton.setOnClickListener {
                val position = adapterPosition
                if (position != RecyclerView.NO_POSITION) {
                    onDeleteClick(getItem(position))
                }
            }
            binding.productItemLayout.setOnClickListener {
                val position = adapterPosition
                if (position != RecyclerView.NO_POSITION) {
                    onItemClick(getItem(position))
                }
            }
        }

        fun bind(product: Product) {
            binding.apply {
                productNameTextView.text = product.productName
                productDescriptionTextView.text = product.description
                productPriceTextView.text = product.getFormattedPrice()

                // Load image if available
                product.imageData?.let { imageUrl ->
                    Glide.with(productImageView)
                        .load(imageUrl)
                        .centerCrop()
                        .into(productImageView)
                }


                // Show delete button and hide cart button in cart list
                deleteButton.visibility = android.view.View.VISIBLE
                cartButton.visibility = android.view.View.GONE
            }
        }
    }

    private class CartDiffCallback : DiffUtil.ItemCallback<Product>() {
        override fun areItemsTheSame(oldItem: Product, newItem: Product): Boolean {
            return oldItem.id == newItem.id
        }

        override fun areContentsTheSame(oldItem: Product, newItem: Product): Boolean {
            return oldItem == newItem
        }
    }
}
