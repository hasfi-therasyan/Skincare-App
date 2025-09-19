package com.skincare.apitest

import android.content.Intent
import android.os.Bundle
import android.view.View
import android.widget.AdapterView
import android.widget.ArrayAdapter
import androidx.appcompat.app.AppCompatActivity
import com.google.android.material.button.MaterialButton
import android.widget.AutoCompleteTextView
import android.widget.Toast
import com.skincare.apitest.model.ApiType

class ApiSelectionActivity : AppCompatActivity() {

    private lateinit var apiSelectionSpinner: AutoCompleteTextView
    private lateinit var fetchProductsButton: MaterialButton

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_api_selection)

        apiSelectionSpinner = findViewById(R.id.apiSelectionSpinner)
        fetchProductsButton = findViewById(R.id.fetchProductsButton)

        val apiOptions = listOf("Use Retrofit", "Use Apollo")
        val adapter = ArrayAdapter(this, android.R.layout.simple_dropdown_item_1line, apiOptions)
        apiSelectionSpinner.setAdapter(adapter)
        apiSelectionSpinner.setText(apiOptions[0], false) // Set default selection

        // Make the AutoCompleteTextView clickable to show dropdown
        apiSelectionSpinner.setOnClickListener {
            apiSelectionSpinner.showDropDown()
        }

        // Handle item selection
        apiSelectionSpinner.setOnItemClickListener { _, _, position, _ ->
            val selectedApi = if (position == 0) ApiType.RETROFIT else ApiType.GRAPHQL
            Toast.makeText(this, "Selected: ${apiOptions[position]}", Toast.LENGTH_SHORT).show()
        }

        fetchProductsButton.setOnClickListener {
            val selectedText = apiSelectionSpinner.text.toString()
            val selectedApi = if (selectedText == "Use Retrofit") ApiType.RETROFIT else ApiType.GRAPHQL

            // Pass selected API type to MainActivity via Intent extras
            val intent = Intent(this, MainActivity::class.java)
            intent.putExtra("selectedApiType", selectedApi.name)
            startActivity(intent)
            finish()
        }
    }
}
