package com.skincare.apitest

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import com.skincare.apitest.databinding.ActivitySearchBinding

class SearchActivity : AppCompatActivity() {

    private lateinit var binding: ActivitySearchBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivitySearchBinding.inflate(layoutInflater)
        setContentView(binding.root)

        supportActionBar?.setDisplayHomeAsUpEnabled(true)
        supportActionBar?.title = "Search Results"

        val query = intent.getStringExtra("search_query") ?: ""

        // Clear previous fragments to avoid stacking multiple fragments
        val fragmentManager = supportFragmentManager
        val existingFragment = fragmentManager.findFragmentById(binding.fragmentContainer.id)
        if (existingFragment != null) {
            fragmentManager.beginTransaction().remove(existingFragment).commitNow()
        }

        val fragment = SearchResultsFragment.newInstance(query)
        supportFragmentManager.beginTransaction()
            .replace(binding.fragmentContainer.id, fragment)
            .commit()
    }

    override fun onSupportNavigateUp(): Boolean {
        finish()
        return true
    }
}
