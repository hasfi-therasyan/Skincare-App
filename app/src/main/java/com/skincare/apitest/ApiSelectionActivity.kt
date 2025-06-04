package com.skincare.apitest

import android.content.Intent
import android.os.Bundle
import android.view.View
import android.widget.AdapterView
import android.widget.ArrayAdapter
import androidx.appcompat.app.AppCompatActivity
import com.google.android.material.button.MaterialButton
import android.widget.Spinner
import android.widget.Toast
import com.skincare.apitest.model.ApiType

class ApiSelectionActivity : AppCompatActivity() {

    private lateinit var apiSelectionSpinner: Spinner
    private lateinit var fetchProductsButton: MaterialButton

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_api_selection)

        apiSelectionSpinner = findViewById(R.id.apiSelectionSpinner)
        fetchProductsButton = findViewById(R.id.fetchProductsButton)

        val apiOptions = listOf("Use Retrofit", "Use Apollo")
        val adapter = ArrayAdapter(this, android.R.layout.simple_spinner_item, apiOptions)
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
        apiSelectionSpinner.adapter = adapter

        fetchProductsButton.setOnClickListener {
            val selectedPosition = apiSelectionSpinner.selectedItemPosition
            val selectedApi = if (selectedPosition == 0) ApiType.RETROFIT else ApiType.GRAPHQL

            // Pass selected API type to MainActivity via Intent extras
            val intent = Intent(this, MainActivity::class.java)
            intent.putExtra("selectedApiType", selectedApi.name)
            startActivity(intent)
            finish()
        }
    }
}
