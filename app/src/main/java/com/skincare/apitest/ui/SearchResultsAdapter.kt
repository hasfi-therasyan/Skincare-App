package com.skincare.apitest.ui

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.skincare.apitest.R
import com.skincare.apitest.SearchItem

class SearchResultsAdapter(
    private val onItemClick: (SearchItem) -> Unit
) : ListAdapter<SearchItem, SearchResultsAdapter.SearchResultViewHolder>(SearchResultDiffCallback()) {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): SearchResultViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.item_search_result, parent, false)
        return SearchResultViewHolder(view)
    }

    override fun onBindViewHolder(holder: SearchResultViewHolder, position: Int) {
        val item = getItem(position)
        holder.bind(item)
    }

    inner class SearchResultViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        private val nameTextView: TextView = itemView.findViewById(R.id.searchResultNameTextView)
        private val descriptionTextView: TextView = itemView.findViewById(R.id.searchResultDescriptionTextView)

        fun bind(item: SearchItem) {
            when (item) {
                is SearchItem.IndividualSearchItem -> {
                    nameTextView.text = item.product.productName
                    descriptionTextView.text = item.product.description
                }
                is SearchItem.PackageSearchItem -> {
                    nameTextView.text = item.packageProduct.packageName
                    descriptionTextView.text = "Package with ${item.packageProduct.items.size} items"
                }
            }
            itemView.setOnClickListener {
                onItemClick(item)
            }
        }
    }

    class SearchResultDiffCallback : DiffUtil.ItemCallback<SearchItem>() {
        override fun areItemsTheSame(oldItem: SearchItem, newItem: SearchItem): Boolean {
            return when {
                oldItem is SearchItem.IndividualSearchItem && newItem is SearchItem.IndividualSearchItem ->
                    oldItem.product.id == newItem.product.id
                oldItem is SearchItem.PackageSearchItem && newItem is SearchItem.PackageSearchItem ->
                    oldItem.packageProduct.id == newItem.packageProduct.id
                else -> false
            }
        }

        override fun areContentsTheSame(oldItem: SearchItem, newItem: SearchItem): Boolean {
            return oldItem == newItem
        }
    }
}
