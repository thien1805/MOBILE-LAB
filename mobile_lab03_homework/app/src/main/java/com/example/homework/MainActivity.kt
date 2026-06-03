package com.example.homework

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.core.widget.addTextChangedListener
import com.example.homework.adapter.ImageAdapter
import com.example.homework.ai.ImageAnalyzer
import com.example.homework.databinding.ActivityMainBinding
import com.example.homework.network.RetrofitInstance
import com.example.homework.BuildConfig
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

class MainActivity : AppCompatActivity() {

    private lateinit var binding: ActivityMainBinding
    private lateinit var adapter: ImageAdapter
    private lateinit var analyzer: ImageAnalyzer

    private var searchJob: Job? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        // Inflate layout
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        // RecyclerView
        binding.recyclerView.layoutManager = LinearLayoutManager(this)

        // AI analyzer
        analyzer = ImageAnalyzer(this)

        // 🔥 Load mặc định (tránh màn trắng)
        fetchImages("nature")

        // 🔍 Sự kiện nhấn nút Search
        binding.btnSearch.setOnClickListener {
            val query = binding.search.text.toString().trim()
            if (query.isNotEmpty()) {
                searchJob?.cancel() // Hủy bỏ bất kỳ tác vụ tìm kiếm tự động nào đang chờ
                fetchImages(query)
            }
        }

        // 🔍 Tự động tìm kiếm khi gõ (debounce)
        binding.search.addTextChangedListener { text ->
            val query = text.toString().trim()

            searchJob?.cancel()
            searchJob = lifecycleScope.launch {
                delay(500)

                if (query.length >= 2) {
                    fetchImages(query)
                }
            }
        }
    }

    private fun fetchImages(query: String) {

        val api = RetrofitInstance.api

        lifecycleScope.launch {
            try {
                val response = api.getImages(
                    key = BuildConfig.PIXABAY_KEY,
                    query = query
                )

                // Gán adapter (AI sẽ chạy)
                adapter = ImageAdapter(response.hits, analyzer)
                binding.recyclerView.adapter = adapter

            } catch (e: Exception) {
                e.printStackTrace()
            }
        }
    }
}