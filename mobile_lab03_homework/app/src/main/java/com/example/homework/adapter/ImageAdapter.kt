package com.example.homework.adapter

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import coil.load
import com.example.homework.ai.ImageAnalyzer
import com.example.homework.api.ImageItem
import com.example.homework.databinding.ItemImageBinding
import kotlinx.coroutines.*

class ImageAdapter(
    private val originalList: List<ImageItem>,
    private val analyzer: ImageAnalyzer
) : RecyclerView.Adapter<ImageAdapter.VH>() {

    private var filteredList = originalList.toMutableList()
    private val tagMap = mutableMapOf<Int, List<String>>() // lưu tags

    class VH(val binding: ItemImageBinding) :
        RecyclerView.ViewHolder(binding.root)

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): VH {
        val binding = ItemImageBinding.inflate(
            LayoutInflater.from(parent.context),
            parent,
            false
        )
        return VH(binding)
    }

    override fun getItemCount() = filteredList.size

    override fun onBindViewHolder(holder: VH, position: Int) {
        val item = filteredList[position]

        holder.binding.image.load(item.webformatURL)

        CoroutineScope(Dispatchers.Main).launch {
            val tags = analyzer.analyze(item.webformatURL)
            tagMap[item.id] = tags
            holder.binding.tags.text = tags.joinToString(", ")
        }
    }

    // 🔥 FILTER FUNCTION
    fun filter(query: String) {
        val lower = query.lowercase()

        filteredList = if (lower.isEmpty()) {
            originalList.toMutableList()
        } else {
            originalList.filter { item ->
                val tags = tagMap[item.id] ?: emptyList()
                tags.any { it.lowercase().contains(lower) }
            }.toMutableList()
        }

        notifyDataSetChanged()
    }
}