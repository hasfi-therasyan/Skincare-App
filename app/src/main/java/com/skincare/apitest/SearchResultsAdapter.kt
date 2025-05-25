package com.skincare.apitest

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.skincare.apitest.databinding.ItemSearchResultBinding

sealed class SearchItem {
    data class IndividualSearchItem(val product: com.skincare.apitest.model.Product) : SearchItem()
    data class PackageSearchItem(val packageProduct: com.skincare.apitest.model.PackageProduct) : SearchItem()
}

class SearchResultsAdapter(
    private val onItemClick: (SearchItem) -> Unit
) : ListAdapter<SearchItem, SearchResultsAdapter.SearchResultViewHolder>(object : DiffUtil.ItemCallback<SearchItem>() {
    override fun areItemsTheSame(oldItem: SearchItem, newItem: SearchItem): Boolean {
        return when {
            oldItem is SearchItem.IndividualSearchItem && newItem is SearchItem.IndividualSearchItem ->
                oldItem.product.id == newItem.product.id
            oldItem is SearchItem.PackageSearchItem && newItem is SearchItem.PackageSearchItem ->
                oldItem.packageProduct.id == newItem.packageProduct.id
            else -> false
        }
    }
    override fun areContentsTheSame(oldItem: SearchItem, newItem: SearchItem): Boolean = oldItem == newItem
}) {
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): SearchResultViewHolder {
        val binding = ItemSearchResultBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return SearchResultViewHolder(binding)
    }
    override fun onBindViewHolder(holder: SearchResultViewHolder, position: Int) {
        holder.bind(getItem(position))
    }
    inner class SearchResultViewHolder(private val binding: ItemSearchResultBinding) : RecyclerView.ViewHolder(binding.root) {
        fun bind(item: SearchItem) {
            when (item) {
                is SearchItem.IndividualSearchItem -> {
                    binding.productNameTextView.text = item.product.productName
                    binding.productPriceTextView.text = item.product.getFormattedPrice()
                    binding.productTypeTextView.text = "Individual"
                    item.product.imageData?.let { imageData ->
                        val base64Image = "data:image/png;base64,$imageData"
                        Glide.with(binding.productImageView)
                            .load(base64Image)
                            .centerCrop()
                            .into(binding.productImageView)
                    }
                }
                is SearchItem.PackageSearchItem -> {
                    binding.productNameTextView.text = item.packageProduct.packageName
                    binding.productPriceTextView.text = item.packageProduct.getFormattedPrice()
                    binding.productTypeTextView.text = "Package"
                    item.packageProduct.imageData?.let { imageData ->
                        val base64Image = "data:image/png;base64,$imageData"
                        Glide.with(binding.productImageView)
                            .load(base64Image)
                            .centerCrop()
                            .into(binding.productImageView)
                    }
                }
            }
            binding.root.setOnClickListener { onItemClick(item) }
        }
    }
}
